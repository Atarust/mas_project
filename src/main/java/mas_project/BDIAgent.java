package mas_project;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.math3.random.RandomGenerator;

import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.road.RoadUser;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.base.Optional;

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
	Map<RoadUser, Point> objects;
	Optional<Parcel> passenger;
	RandomGenerator rng;

	State state;
	private static final boolean LOG_STATE = false;

	public BDIAgent(RandomGenerator rng) {
		this.rng = rng;
		passenger = Optional.absent();
		state = State.idle;
		state.log();
	}

	@Override
	public void updateBelief(TaxiAction action, TimeLapse time) {
		objects = action.see();
	}

	@Override
	public void updateDesire(TaxiAction action, TimeLapse time) {
		if (state == State.idle && !passenger.isPresent()) {
			Set<RoadUser> passengers = objects.keySet().stream().filter(obj -> obj instanceof Parcel)
					.collect(Collectors.toSet());
			if (!passengers.isEmpty()) {
				passenger = Optional.of((Parcel) TaxiAction.getRandomElement(passengers, rng));
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
				action.goTo(passenger.get().getPickupLocation(), time);
				if (action.isAt(passenger.get().getPickupLocation())) {
					state = State.pickup;
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
	
	static enum State {
		idle, goto_parcel, pickup, goto_dest, deliver;

		public void log() {
			if (LOG_STATE) {
				System.out.println("STATE: " + this);
			}

		}

	}
}
