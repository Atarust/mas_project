package mas_project;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.math3.random.RandomGenerator;

import com.github.rinde.rinsim.core.model.comm.MessageContents;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
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
	}

	@Override
	public void updateDesire(TaxiAction action, TimeLapse time) {
		if (state == State.idle && !passenger.isPresent()) {
			Set<RoadUser> passengers = knownObjects.keySet().stream().filter(obj -> obj instanceof Parcel)
					.filter(obj -> !claimedParcels.contains(obj)).collect(Collectors.toSet());
			if (!passengers.isEmpty()) {
				passenger = Optional.of((Parcel) TaxiAction.getRandomElement(passengers, rng));
				action.broadcastReservation(passenger.get());
				state = State.goto_parcel;
				state.log();
			}
			Parcel parcel = passenger.get();
			if (passenger.isPresent() && !action.isInCargo(parcel) && !action.isOnRoad(parcel)) {
				passenger = Optional.absent();
				System.out.println(
						"warning: Passenger is neither in roadmap nor in cargo. Ain't nobody got time to search him!");
			}
		}
	}

	@Override
	public void updateIntention(TaxiAction action, TimeLapse time) {
		while (time.hasTimeLeft()) {
			switch (state) {
			case idle:
				action.goTo(action.randomPosition(), time);
				break;
			case goto_parcel:
				if (passenger.isPresent()) {
					action.goTo(passenger.get().getPickupLocation(), time);
					if (action.isAt(passenger.get().getPickupLocation())) {
						state = State.pickup;
						state.log();
					}
				} else {
					// that shitty passenger seems to have disappeared.
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
					} // TODO: What happens if two taxis want to pickup the same passenger. Will stay stall?
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
					action.broadcastUnreservation(passenger.get());
					oldParcels.add(passenger.get());
					passenger = Optional.absent();
					state = State.idle;
					state.log();
				}
				break;
			default:
				throw new RuntimeException("Error, Illegal state!");
			}
		}
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
