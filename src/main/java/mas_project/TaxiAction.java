package mas_project;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.measure.Measure;
import javax.measure.quantity.Length;

import org.apache.commons.math3.random.RandomGenerator;

import com.github.rinde.rinsim.core.model.comm.CommDevice;
import com.github.rinde.rinsim.core.model.comm.MessageContents;
import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.pdp.PDPModel.ParcelState;
import com.github.rinde.rinsim.core.model.pdp.PDPModel.VehicleState;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.pdp.Vehicle;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadUser;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.base.Optional;

/**
 * This class is like your mummy. It tells you what you are allowed to do and
 * what you are not allowed to do. If your mummy allows you to pick up or deliver
 * an object, you are allowed to. If your mummy tells you that you can see the
 * person, than you can see the person.
 * 
 * This class abstracts from the implementation details of the framework and
 * gives simpler methods which define the capabilities in the given scenario.
 * 
 * @author jonas
 *
 */
public class TaxiAction {

	private final Vehicle agent;
	private final RoadModel rm;
	private final PDPModel pm;
	private final double speed; // may be used to calculate shortest path according to time
	private final double seeRange; // straight line distance
	private final double commRange;
	Optional<CommDevice> device;

	RandomGenerator rng;
	private Metric metric;

	/**
	 * @param agent
	 * @param rm
	 * @param pm
	 * @param rng
	 * @param metric 
	 * @param speed
	 * @param seeRange
	 */
	public TaxiAction(Vehicle agent, RoadModel rm, PDPModel pm, Optional<CommDevice> device, RandomGenerator rng,
			Metric metric, double speed, double seeRange, double commRange) {
		this.agent = agent;
		this.rm = rm;
		this.pm = pm;
		this.speed = speed;
		this.seeRange = seeRange;
		this.device = device;
		this.commRange = commRange;

		this.rng = rng;
		this.metric = metric;
	}

	/**
	 * @return objects, which the agent can see
	 */
	public Map<RoadUser, Point> see() {
		return rm.getObjectsAndPositions().entrySet().stream()
				.filter(e -> Point.distance(e.getValue(), rm.getPosition(agent)) < seeRange)
				.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
	}

	/**
	 * picks up a passenger. Attempt fails, if they are not at same position
	 * 
	 * @param parcel
	 *            passenger
	 * @param time
	 */
	public void pickUp(Parcel parcel, TimeLapse time) {
		// pickup needs to be called only once. Then just wait until it is picked up.
		if (isOnRoad(parcel)) {
			pm.pickup(agent, parcel, time);
		}
	}

	/**
	 * Moves the agent to a point.
	 * 
	 * @param point
	 * @param time
	 */
	public void goTo(Point point, TimeLapse time) {
		rm.moveTo(agent, point, time);
	}

	/**
	 * Moves the agent to where obj is
	 * 
	 * @param obj
	 *            agent moves to this object
	 * @param time
	 */
	public void goTo(RoadUser obj, TimeLapse time) {
		goTo(rm.getPosition(obj), time);
	}

	/**
	 * 
	 * @param obj1
	 * @param obj2
	 * @return distance when driving on roads - could be either length or time.
	 */
	public double distanceOnRoad(RoadUser obj1, RoadUser obj2) {
		// TODO: Which metric is used to measure "short"? Is it distance or time?
		Measure<Double, Length> dist = rm.getDistanceOfPath(rm.getShortestPathTo(obj1, obj2));
		return dist.doubleValue(dist.getUnit());
	}

	/**
	 * delivers a passenger. Attempt fails, if agent is not at parcels desired
	 * destination.
	 * 
	 * @param parcel
	 *            passenger
	 * @param time
	 */
	public void deliver(Parcel parcel, TimeLapse time) {
		if (!isAt(parcel.getDeliveryLocation())) {
			System.out.println("Error, not at delivery destination");
		}
		if (!isInCargo(parcel)) {
			System.out.println("Error, Lost cargo!!");
		}
		if (parcel.canBeDelivered(agent, time.getTime())
				&& !pm.getVehicleState(agent).equals(VehicleState.DELIVERING)) {
			pm.deliver(agent, parcel, time);
			metric.passengerDelivered();
		}
	}

	/**
	 * @return A random position on the road map
	 */
	public Point randomPosition() {
		return rm.getRandomPosition(rng);
	}

	/**
	 * @return if cargo of agent is empty
	 */
	public boolean hasEmptyCargo() {
		return pm.getContents(agent).isEmpty();
	}

	/**
	 * @param point
	 * @return is agent at point
	 */
	public boolean isAt(Point point) {
		return rm.getPosition(agent).equals(point);
	}

	/**
	 * @param parcel
	 * @return whether parcel is on the road
	 */
	public boolean isOnRoad(Parcel parcel) {
		return rm.containsObject(parcel);
	}

	/**
	 * 
	 * @param parcel
	 * @return whether parcel is loaded in the cargo of the agent
	 */
	public boolean isInCargo(Parcel parcel) {
		return pm.containerContains(agent, parcel);
	}

	/**
	 * broadcast position of a roadUser
	 * 
	 * @param obj
	 * @param point
	 *            position of obj
	 */
	public void broadcastNewObject(RoadUser obj, Point point) {
		// TODO make sure to broadcast not to oneself!
		device.get().broadcast(new ObjectPosition(obj, point), commRange);
	}

	/**
	 * broadcast that agent has an intention to pick up passenger
	 * 
	 * @param passenger
	 */
	public void broadcastReservation(Parcel passenger) {
		// TODO make sure to broadcast not to oneself!
		if (device.isPresent()) {
			device.get().broadcast(new Reservation(passenger), commRange);
		}
	}

	/**
	 * broadcast that agent has no intention to pick up passenger
	 * 
	 * @param passenger
	 */
	public void broadcastUnreservation(Parcel passenger) {
		if (device.isPresent()) {
			device.get().broadcast(new Unreservation(passenger), commRange);
		}
	}

	/**
	 * @return object location information, which were broadcasted since last
	 *         function call.
	 */
	public Set<MessageContents> readMessages() {
		if (device.isPresent()) {
			return device.get().getUnreadMessages().stream().map(message -> message.getContents())
					.collect(Collectors.toSet());
		} else {
			return new HashSet<>();
		}
	}
	
	public ParcelState getParcelState(Parcel p) {
		return pm.getParcelState(p);
	}

	public static <T> T getRandomElement(Collection<T> passengers, RandomGenerator rng) {
		List<T> passengerList = passengers.stream().collect(Collectors.toList());
		return passengerList.get(rng.nextInt(passengerList.size()));
	}

	static class ObjectPosition implements MessageContents {
		public final RoadUser obj;
		public final Point point;

		ObjectPosition(RoadUser obj, Point point) {
			this.obj = obj;
			this.point = point;
		}
	}

	static class Reservation implements MessageContents {
		public final Parcel parcel;

		Reservation(Parcel passenger) {
			this.parcel = passenger;
		}
	}

	static class Unreservation implements MessageContents {
		public final Parcel parcel;

		Unreservation(Parcel passenger) {
			this.parcel = passenger;
		}
	}

}
