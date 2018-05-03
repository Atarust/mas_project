package mas_project;

import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.pdp.Vehicle;
import com.github.rinde.rinsim.core.model.pdp.VehicleDTO;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;

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

	private static final double SPEED = 1000d;
	private static final double SEE_RANGE = 42;

	protected TaxiImplDetails(Point startPosition, int capacity) {
		super(getAgent(startPosition, capacity));
	}

	@Override
	public void afterTick(TimeLapse timeLapse) {
	}

	@Override
	protected void tickImpl(TimeLapse time) {
		final RoadModel rm = getRoadModel();
		final PDPModel pm = getPDPModel();
		final IBDIAgent agent = new BDIAgent();
		final TaxiCapabilities capabilities = new TaxiCapabilities(rm, pm, SPEED, SEE_RANGE);

		agent.updateBelief(capabilities);
		agent.updateDesire(capabilities);
		agent.updateIntention(capabilities);

	}

	private static VehicleDTO getAgent(Point startPosition, int capacity) {
		return VehicleDTO.builder().capacity(capacity).startPosition(startPosition).speed(SPEED).build();
	}

}
