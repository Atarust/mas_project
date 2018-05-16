package mas_project;

import org.apache.commons.math3.random.RandomGenerator;

import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.pdp.Vehicle;
import com.github.rinde.rinsim.core.model.pdp.VehicleDTO;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.base.Optional;

/**
 * This class is like your mommy. It hides all the bad and shitty details of the
 * world and explains it in simple terms, that even little Donald Trump
 * could understand it. If you say: "Mom, I wanna go there!" Then your mommy
 * goes there with you. (BTW: You are a shitty little prat, that only has beliefs,
 * desires and intentions)
 * 
 * 
 * @author jonas
 *
 */
public class TaxiImplDetails extends Vehicle {

	private static final double SPEED = 30000d;
	private static final double SEE_RANGE = 42000000;
	private static final double COMM_RANGE = 1337;
	private final RandomGenerator rng;
	private RoadModel rm;
	private PDPModel pm;
	private Optional<CommDevice> device;

	private final IBDIAgent agent;

	protected TaxiImplDetails(Point startPosition, int capacity, RandomGenerator rng) {
		super(getAgent(startPosition, capacity));
		this.rng = rng;
		agent = new BDIAgent(rng);
	}

	@Override
	public void afterTick(TimeLapse timeLapse) {
	}

	@Override
	protected void tickImpl(TimeLapse time) {
		rm = getRoadModel();
		pm = getPDPModel();
		final TaxiAction capabilities = new TaxiAction(this, rm, pm, device, rng, SPEED, SEE_RANGE, COMM_RANGE);

		agent.updateBelief(capabilities, time);
		agent.updateDesire(capabilities, time);
		agent.updateIntention(capabilities, time);
	}

	private static VehicleDTO getAgent(Point startPosition, int capacity) {
		return VehicleDTO.builder().capacity(capacity).startPosition(startPosition).speed(SPEED).build();
	}

}
