package warehouse;

import java.util.Queue;

public interface IBDIAgent<Belief, Desire, Intention extends Queue<?>> {

	void setIntention(Intention path);

	void setDesire(Desire point);

	void setBelief(Belief model);

	Desire getDesire();

	Intention getIntention();

	Belief getBelief();

	boolean hasIntentions();

	boolean hasDesires();

	boolean hasBeliefs();

}