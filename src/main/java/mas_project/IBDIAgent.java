package mas_project;

public interface IBDIAgent {

	void updateBelief(TaxiCapabilities capabilities);

	void updateDesire(TaxiCapabilities capabilities);

	void updateIntention(TaxiCapabilities capabilities);

}