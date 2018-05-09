package mas_project;

import com.github.rinde.rinsim.core.model.time.TimeLapse;

public interface IBDIAgent {

	void updateBelief(TaxiAction capabilities, TimeLapse time);

	void updateDesire(TaxiAction capabilities, TimeLapse time);

	void updateIntention(TaxiAction capabilities, TimeLapse time);

}