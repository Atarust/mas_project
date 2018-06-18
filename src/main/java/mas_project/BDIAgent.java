package mas_project;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.math3.random.RandomGenerator;

import com.github.rinde.rinsim.core.model.comm.MessageContents;
import com.github.rinde.rinsim.core.model.pdp.PDPModel.ParcelState;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.road.RoadUser;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.base.Optional;

import mas_project.TaxiAction.ForgetInformation;
import mas_project.TaxiAction.ObjectPosition;
import mas_project.TaxiAction.Reservation;
import mas_project.TaxiAction.Unreservation;

/**
 * This class is like a puppet play for little kiddies. Everything is simple and
 * shiny. No dead corpses laying around, no piss smelling in the corner. No,
 * everything looks like morning sunshine. This is the perfect place to have to
 * actual story written, what the taxis are doing.
 * 
 * @author jonas
 *
 */
public class BDIAgent implements IBDIAgent {
	/** All objects, the agent knows of */
	Map<RoadUser, Point> knownObjects;
	/** Passengers, where other agents claimed to pick them up */
	Map<Parcel, Double> claimedParcels;
	/** Passengers, which do not need to be picked up anymore. (TODO) */
	Set<Parcel> oldParcels;

	Optional<Parcel> passenger;
	Optional<Point> randomPosition;
	RandomGenerator rng;
	Metric metric;
	boolean lazyTaxi;

	State state;
	private static final boolean LOG_STATE = false;

	public BDIAgent(RandomGenerator rng, Metric metric, double lazyProb) {
		this.rng = rng;
		this.metric = metric;

		this.lazyTaxi = rng.nextDouble() < lazyProb;
		if(lazyTaxi) {
			metric.isLazy();
		}
		// Beliefs/Desires
		knownObjects = new HashMap<>();
		claimedParcels = new HashMap<>();
		oldParcels = new HashSet<>();
		// Intention
		passenger = Optional.absent();
		randomPosition = Optional.absent();
		state = State.idle;
		state.log();
	}

	@Override
	public void updateBelief(TaxiAction action, TimeLapse time) {
		knownObjects.putAll(action.see());

		action.see().keySet().stream().forEach(seenP -> {
			if (oldParcels.contains(seenP)) {
				System.out.println(seenP + " is seen, but was declared old. Maybe I should remove it from oldParcels.");
			}
		});

		action.see().keySet().stream().filter(p -> p instanceof Parcel)
				.forEach(p -> metric.newParcelsSeen(((Parcel) p), time.getTime()));

		// If parcel can be seen, it is not an old parcel.
		oldParcels.removeAll(action.see().keySet());

		// I need to remove the old parcels twice, because of the metric measurement.
		oldParcels.stream().forEach(oldPassenger -> knownObjects.remove(oldPassenger));
		oldParcels.stream().forEach(oldPassenger -> claimedParcels.remove(oldPassenger));

		int nrParcelsBeforeComm = knownObjects.size();
		// broadcast all knowledge
		knownObjects.entrySet().stream().forEach(entry -> action.broadcastNewObject(entry.getKey(), entry.getValue()));

		// TODO communicate old passengers
		oldParcels.stream().forEach(parcel -> action.broadcastForgetInformation(parcel));
		
		processMessages(action.readMessages());
		metric.newParcelCommunicated(nrParcelsBeforeComm - knownObjects.size());
		oldParcels.stream().forEach(oldPassenger -> knownObjects.remove(oldPassenger));
		oldParcels.stream().forEach(oldPassenger -> claimedParcels.remove(oldPassenger));
		metric.newParcelsKnown(knownObjects.keySet().stream().filter(ru -> ru instanceof Parcel).count());
	}

	@Override
	public void updateDesire(TaxiAction action, TimeLapse time) {
		// System.out.println("I am " + state + (!passenger.isPresent()));
		if (state == State.idle && !passenger.isPresent()) {
			Set<RoadUser> passengers = knownObjects.keySet().stream().filter(obj -> obj instanceof Parcel)
					.filter(obj -> !isClaimedBySomeoneCloser((Parcel) obj, action)).collect(Collectors.toSet());
			if (!passengers.isEmpty() && !this.lazyTaxi) {
				// passenger = Optional.of((Parcel) TaxiAction.getRandomElement(passengers,
				// rng));
				passenger = action.getNearestElement(passengers);
				if (passenger.isPresent()) {
					action.broadcastReservation(passenger.get());
				}
				state = State.goto_parcel;
				state.log();
			}
		}

		if (passenger.isPresent()) {
			// if passenger got claimed by another taxi, think about changing your intention
			if (claimedParcels.containsKey(passenger.get())) {
				// TODO make sure the passenger is not claimed by yourself!
				double ourDistance = action.distanceTo(passenger.get());
				boolean contains = claimedParcels.entrySet().stream().filter(entry -> entry.getValue() < ourDistance)
						.filter(entry -> entry.getKey().equals(passenger.get())).findAny().isPresent();
				if (contains && (state == State.goto_parcel || state == State.idle) && action.hasEmptyCargo()) {
					// If passenger is being claimed by someone who is closer, and we are not
					// already picking that guy up, then forget the guy.
					passenger = Optional.absent();
				}
			}
		}
	}

	private boolean isClaimedBySomeoneCloser(Parcel obj, TaxiAction action) {
		double ownDist = action.distanceTo(obj);
		return claimedParcels.containsKey(obj) && claimedParcels.get(obj) < ownDist;
	}

	@Override
	public void updateIntention(TaxiAction action, TimeLapse time) {
		int loop = 0;
		
		boolean isIdleAtBeginning = state == State.idle;
		if (this.lazyTaxi) {
			state = State.idle;
		}
		while (time.hasTimeLeft()) {
			if(loop++ > 1000) {
				System.out.println("endless loop. exiting." + loop + state);
				break;
			}
			switch (state) {
			case idle:
				if (randomPosition.isPresent()) {
					if (action.isAt(randomPosition.get())) {
						randomPosition = Optional.of(action.randomPosition());
					}
					action.goTo(randomPosition.get(), time);
				} else {
					randomPosition = Optional.of(action.randomPosition());
				}
				break;
			case goto_parcel:
				
				if(!action.hasEmptyCargo()) {
					System.out.println("I don't have an empty cargo I should deliver first!");
					state = State.deliver;
				}

				if (passenger.isPresent()) {
					action.goTo(passenger.get().getPickupLocation(), time);
					if (action.isAt(passenger.get().getPickupLocation()) && action.isOnRoad(passenger.get())
							&& action.getParcelState(passenger.get()) == ParcelState.AVAILABLE) {
						state = State.pickup;
						state.log();
					} else if (action.isAt(passenger.get().getPickupLocation()) && !action.isOnRoad(passenger.get())){
						// passenger left
						state = State.idle;
						state.log();
						oldParcels.add(passenger.get());
						passenger = Optional.absent();
					}
				} else {
					// Passenger not available anymore. Pickup by someone else maybe.
					state = State.idle;
					state.log();
				}
				break;
			case pickup:
				if (passenger.isPresent() && action.isAt(passenger.get().getPickupLocation())) {
					metric.parcelPicked(passenger.get(), time.getTime());
					action.pickUp(passenger.get(), time);
					if (action.isInCargo(passenger.get())) {
						state = State.goto_dest;
						state.log();
					} else if (action.hasEmptyCargo()
							&& action.getParcelState(passenger.get()) != ParcelState.PICKING_UP) {
						// Parcel is neither on roadmap, nor in cargo and it also doesn't pick up atm.
						// Strange.
						// Only during Pickup, cargo is empty and parcel is off the road.
						forgetPassenger(action);
					}
				} else {
					System.out.println("wtf - passenger disappeared.");

					state = State.idle;
					state.log();
				}
				break;
			case goto_dest:
				action.goTo(passenger.get().getDeliveryLocation(), time);
				if (action.isAt(passenger.get().getDeliveryLocation())) {
					state = State.deliver;
					state.log();
				}
				break;
			case deliver:
				if (action.isInCargo(passenger.get())) {
					action.deliver(passenger.get(), time);
				} else {
					// System.out.println("Warning: Parcel was not in cargo anymore. idc lol");
				}
				if (action.hasEmptyCargo()) {
					forgetPassenger(action);
					state = State.idle;
					state.log();
				}
				break;
			default:
				throw new RuntimeException("Error, Illegal state!");
			}
		}

		metric.countTick();
		if (isIdleAtBeginning && state == State.idle) {
			metric.spentIdle();
		}
	}

	private void forgetPassenger(TaxiAction action) {
		action.broadcastUnreservation(passenger.get());
		oldParcels.add(passenger.get());
		passenger = Optional.absent();
	}

	private void processMessages(Set<MessageContents> messages) {
		// save all objects seen by other agents into knownObjects
		messages.stream().filter(m -> m instanceof TaxiAction.ObjectPosition).forEach(objPos -> {
			ObjectPosition objPoint = (TaxiAction.ObjectPosition) objPos;
			knownObjects.put(objPoint.obj, objPoint.point);
		});

		// note which objects are going to be transported by other agents
		messages.stream().filter(m -> m instanceof TaxiAction.Reservation).forEach(reservation -> {
			// TODO igore reservations which have a longer distance to passenger
			claimedParcels.put(((Reservation) reservation).parcel, ((Reservation) reservation).distance);
		});

		// note, if objects are available to be picked up again.
		messages.stream().filter(m -> m instanceof TaxiAction.Unreservation).forEach(unreservation -> {
			Parcel parcelToForget = ((Unreservation) unreservation).parcel;
			claimedParcels.remove(parcelToForget);
		});

		messages.stream().filter(m -> m instanceof TaxiAction.ForgetInformation).forEach(message -> {
			ForgetInformation forgetMessage = (TaxiAction.ForgetInformation) message;
			oldParcels.add(forgetMessage.parcel);
		});

	}

	@Override
	public String toString() {
		if(this.lazyTaxi) {
			return "" + this.state + ", know="
					+ knownObjects.keySet().stream().filter(ru -> ru instanceof Parcel).count() + " lazy";
		} else {
			return "" + this.state + ", know="
					+ knownObjects.keySet().stream().filter(ru -> ru instanceof Parcel).count();
		}
	}

	static enum State {
		idle, goto_parcel, pickup, goto_dest, deliver;

		public void log() {
			if (LOG_STATE) {
				System.out.println("STATE: " + this);
			}

		}

	}
}
