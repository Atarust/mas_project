package mas_project;

import java.util.Set;

import javax.measure.Measure;
import javax.measure.quantity.Length;

import com.github.rinde.rinsim.core.model.comm.MessageContents;
import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.pdp.Vehicle;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadUser;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;

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
public class TaxiCapabilities {

	private final Vehicle agent;
	private final RoadModel rm;
	private final PDPModel pm;
	private final double speed; // may be used to calculate shortest path accorcing to time
	private final double seeRange; // straight line distance
	private final double commRange;

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
		this.commRange = commRange;
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

	public void broadcast(Message info) {
		// TODO
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

}
