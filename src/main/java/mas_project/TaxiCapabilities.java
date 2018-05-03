package mas_project;

import java.util.Set;
import java.util.stream.Collectors;

import javax.measure.Measure;
import javax.measure.quantity.Length;

import com.github.rinde.rinsim.core.model.comm.CommDevice;
import com.github.rinde.rinsim.core.model.comm.CommDeviceBuilder;
import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.comm.MessageContents;
import com.github.rinde.rinsim.core.model.pdp.PDPModel;
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
public class TaxiCapabilities implements CommUser {

	private final Vehicle agent;
	private final RoadModel rm;
	private final PDPModel pm;
	private final double speed; // may be used to calculate shortest path accorcing to time
	private final double seeRange; // straight line distance
	private final double commRange;
	private final double commReliability;
	Optional<CommDevice> device;

	/**
	 * @param agent
	 * @param rm
	 * @param pm
	 * @param speed
	 * @param seeRange
	 */
	public TaxiCapabilities(Vehicle agent, RoadModel rm, PDPModel pm, double speed, double seeRange, double commRange) {
		this.agent = agent;
		this.rm = rm;
		this.pm = pm;
		this.speed = speed;
		this.seeRange = seeRange;
		device = Optional.absent();
		this.commRange = commRange;
		this.commReliability = 0;
	}

	/**
	 * @return objects, which the agent can see
	 */
	public Set<RoadUser> see() {
		return getObjInRange(rm.getPosition(agent), seeRange);
	}

	/**
	 * picks up a passenger. Attempt fails, if they are not at same position
	 * 
	 * @param parcel
	 *            passenger
	 * @param time
	 */
	public void pickUp(Parcel parcel, TimeLapse time) {
		pm.pickup(agent, parcel, time);
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
	 * delivers a passenger. Attempt fails, if agent is not at parcels desired
	 * destination.
	 * 
	 * @param parcel
	 *            passenger
	 * @param time
	 */
	public void deliver(Parcel parcel, TimeLapse time) {
		pm.deliver(agent, parcel, time);
	}

	/**
	 * broadcast position of a roadUser
	 * 
	 * @param obj
	 * @param point
	 *            position of obj
	 */
	public void broadcast(RoadUser obj, Point point) {
		device.get().broadcast(new ObjectPosition(obj, point), commRange);
	}

	/**
	 * @return object location information, which were broadcasted since last
	 *         function call.
	 */
	public Set<ObjectPosition> readMessages() {
		return device.get().getUnreadMessages().stream().map(message -> (ObjectPosition) message.getContents())
				.collect(Collectors.toSet());
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

	private Set<RoadUser> getObjInRange(Point point, double range) {
		return rm.getObjects(obj -> Point.distance(point, rm.getPosition(obj)) < range);
	}

	@Override
	public Optional<Point> getPosition() {
		if (rm.containsObject(agent)) {
			return Optional.of(rm.getPosition(agent));
		}
		return Optional.absent();
	}

	@Override
	public void setCommDevice(CommDeviceBuilder builder) {
		if (commRange >= 0) {
			builder.setMaxRange(commRange);
		}
		device = Optional.of(builder.setReliability(commReliability).build());
	}

	static class ObjectPosition implements MessageContents {
		public final RoadUser obj;
		public final Point point;

		ObjectPosition(RoadUser obj, Point point) {
			this.obj = obj;
			this.point = point;
		}

		String get() {
			return "" + obj + point;
		}
	}

}
