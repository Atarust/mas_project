package mas_project;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.math3.random.RandomGenerator;

import com.github.rinde.rinsim.core.model.comm.MessageContents;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.pdp.PDPModel.ParcelState;
import com.github.rinde.rinsim.core.model.road.RoadUser;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.base.Optional;

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
	Set<Parcel> claimedParcels;
	/** Passengers, which do not need to be picked up anymore. (TODO) */
	Set<Parcel> oldParcels;

	Optional<Parcel> passenger;
	Optional<Point> randomPosition;
	RandomGenerator rng;

	State state;
	private static final boolean LOG_STATE = false;

	public BDIAgent(RandomGenerator rng) {
		this.rng = rng;

		// Beliefs/Desires
		knownObjects = new HashMap<>();
		claimedParcels = new HashSet<>();
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

		// broadcast all knowledge
		knownObjects.entrySet().stream().forEach(entry -> action.broadcastNewObject(entry.getKey(), entry.getValue()));
		processMessages(action.readMessages());
		oldParcels.stream().forEach(oldPassenger -> knownObjects.remove(oldPassenger));
		oldParcels.stream().forEach(oldPassenger -> claimedParcels.remove(oldPassenger));
	}

	@Override
	public void updateDesire(TaxiAction action, TimeLapse time) {
		//System.out.println("I am " + state + (!passenger.isPresent()));
		if (state == State.idle && !passenger.isPresent()) {
			Set<RoadUser> passengers = knownObjects.keySet().stream().filter(obj -> obj instanceof Parcel)
					.filter(obj -> !claimedParcels.contains(obj)).collect(Collectors.toSet());
			if (!passengers.isEmpty()) {
				passenger = Optional.of((Parcel) TaxiAction.getRandomElement(passengers, rng));
				action.broadcastReservation(passenger.get());
				state = State.goto_parcel;
				state.log();
			}
		}
	}

	@Override
	public void updateIntention(TaxiAction action, TimeLapse time) {
		while (time.hasTimeLeft()) {
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
				if (passenger.isPresent() && action.isOnRoad(passenger.get()) && action.getParcelState(passenger.get()) == ParcelState.AVAILABLE) {
					action.goTo(passenger.get().getPickupLocation(), time);
					if (action.isAt(passenger.get().getPickupLocation())) {
						state = State.pickup;
						state.log();
					}
				} else {
					// Passenger not available anymore. Pickup by someone else maybe.
					passenger = Optional.absent();
					state = State.idle;
					state.log();
				}
				break;
			case pickup:
				if (action.isAt(passenger.get().getPickupLocation())) {
					action.pickUp(passenger.get(), time);
					if (action.isInCargo(passenger.get())) {
						state = State.goto_dest;
						state.log();
					} else if(action.hasEmptyCargo() && action.getParcelState(passenger.get()) != ParcelState.PICKING_UP) {
						// Parcel is neither on roadmap, nor in cargo and it also doesn't pick up atm. Strange.
						// Only during Pickup, cargo is empty and parcel is off the road.
						forgetPassenger(action);
					}
				} else {
					throw new RuntimeException(
							"Can not deliver, because not at right position. This should NEVER happen");
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
					System.out.println("Warning: Parcel was not in cargo anymore. idc lol");
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
			claimedParcels.add(((Reservation) reservation).parcel);
		});

		// note, if objects are available to be picked up again.
		messages.stream().filter(m -> m instanceof TaxiAction.Unreservation).forEach(unreservation -> {
			Parcel parcelToForget = ((Unreservation) unreservation).parcel;
			claimedParcels.remove(parcelToForget);
		});
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
