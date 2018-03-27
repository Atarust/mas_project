package warehouse;

import java.util.Queue;

import com.google.common.base.Optional;

public class BDIAgent<Belief,Desire,Intention extends Queue<?>> implements IBDIAgent<Belief, Desire, Intention> {
	private Optional<Belief> roadModel;
	private Optional<Desire> destination;
	private Optional<Intention> path;

	public BDIAgent() {
		destination = Optional.absent();
		path = Optional.absent();
		roadModel = Optional.absent();
	}

	@Override
	public void setIntention(Intention path) {
		this.path = Optional.of(path);
	}

	@Override
	public void setDesire(Desire point) {
		this.destination = Optional.of(point);
	}

	@Override
	public void setBelief(Belief model) {
		this.roadModel = Optional.of(model);
	}

	@Override
	public Desire getDesire() {
		return destination.get();
	}

	@Override
	public Intention getIntention() {
		return path.get();
	}

	@Override
	public Belief getBelief() {
		return roadModel.get();
	}
	
	@Override
	public boolean hasIntentions() {
		return roadModel.isPresent() && !path.get().isEmpty();
	}

	@Override
	public boolean hasDesires() {
		return destination.isPresent();
	}

	@Override
	public boolean hasBeliefs() {
		return roadModel.isPresent();
	}
}
