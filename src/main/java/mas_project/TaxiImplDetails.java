package mas_project;

import org.apache.commons.math3.random.RandomGenerator;

import com.github.rinde.rinsim.core.model.comm.CommDevice;
import com.github.rinde.rinsim.core.model.comm.CommDeviceBuilder;
import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.pdp.Vehicle;
import com.github.rinde.rinsim.core.model.pdp.VehicleDTO;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.base.Optional;

/**
 * This class is like your mommy. It hides all the bad and shitty details of the
 * world and explains it in simple terms, that even little Donald Trump could
 * understand it. If you say: "Mom, I wanna go there!" Then your mommy goes
 * there with you. (BTW: You are a shitty little prat, that only has beliefs,
 * desires and intentions)
 * 
 * 
 * @author jonas
 *
 */
public class TaxiImplDetails extends Vehicle implements CommUser {

	private static final double SPEED = 30000d;
	private static final double SEE_RANGE = 13370;
	private static final double COMM_RANGE = 13370;
	private final double commReliability = 1;
	private final RandomGenerator rng;
	private RoadModel rm;
	private PDPModel pm;
	private Optional<CommDevice> device;
	private Metric metric;

	private final IBDIAgent agent;

	protected TaxiImplDetails(Point startPosition, int capacity, RandomGenerator rng, Metric metric) {
		super(getAgent(startPosition, capacity));
		this.rng = rng;
		this.metric = metric;
		agent = new BDIAgent(rng);
	}

	@Override
	public void afterTick(TimeLapse timeLapse) {
	}

	@Override
	protected void tickImpl(TimeLapse time) {
		rm = getRoadModel();
		pm = getPDPModel();
		final TaxiAction capabilities = new TaxiAction(this, rm, pm, device, rng, metric, SPEED, SEE_RANGE, COMM_RANGE);

		agent.updateBelief(capabilities, time);
		agent.updateDesire(capabilities, time);
		agent.updateIntention(capabilities, time);
	}

	private static VehicleDTO getAgent(Point startPosition, int capacity) {
		return VehicleDTO.builder().capacity(capacity).startPosition(startPosition).speed(SPEED).build();
	}

	@Override
	public Optional<Point> getPosition() {
		if (rm != null && rm.containsObject(this)) {
			return Optional.of(rm.getPosition(this));
		}
		return Optional.absent();
	}

	@Override
	public void setCommDevice(CommDeviceBuilder builder) {
		if (COMM_RANGE >= 0) {
			builder.setMaxRange(COMM_RANGE);
		}
		device = Optional.of(builder.setReliability(commReliability).build());
	}

}
