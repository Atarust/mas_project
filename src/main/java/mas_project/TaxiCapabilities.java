package mas_project;

import java.util.Set;

import com.github.rinde.rinsim.core.model.comm.Message;
import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadUser;
import com.github.rinde.rinsim.core.model.time.TimeLapse;

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

	private final RoadModel rm;
	private final PDPModel pm;
	private final double SPEED;
	private final double SEE_RANGE;

	public TaxiCapabilities(RoadModel rm, PDPModel pm, double speed, double seeRange) {
		this.rm = rm;
		this.pm = pm;
		this.SPEED = speed;
		this.SEE_RANGE = seeRange;
	}

	public Set<RoadUser> see() {
		// TODO
		return null;
	}

	public void pickUp(RoadUser obj, TimeLapse time) {
		// TODO
	}

	public void goTo(RoadUser obj, TimeLapse time) {
		// TODO
	}

	public void broadcast(Message info) {
		// TODO
	}

	public double distance(RoadUser obj1, RoadUser obj2) {
		// TODO
		return -1;
	}

}
