package mas_project;

import static mas_project.Utils.doubles;
import static mas_project.Utils.ints;

import java.util.LinkedList;
import java.util.List;

import com.github.rinde.rinsim.core.Simulator;


/**
 * Example showing a fleet of taxis that have to pickup and transport customers
 * around the city of Leuven.
 * <p>
 * If this class is run on MacOS it might be necessary to use
 * -XstartOnFirstThread as a VM argument.
 * 
 * @author Jonas Kapitzke, Doruk Dündar
 */
public class Experiment {
	static List<Parameter> exps;
	static int nrOfexpsRunning;
	final static int trials = 1;
	static ExperimentSetup es;

	public static void main(String[] args) {
		exps = new LinkedList<>();

		// Change experiment number here:
		es = new ExperimentSetup(5);

		// set parameters
		for (int runTime : es.runTimeInterval) {
			for (int commRange : es.commRangeInterval) {
				for (double commReliability : es.commReliabilityInterval) {
					for (int numTaxis : es.numTaxisInterval) {
						for (int numCustomers : es.numCustomersInterval) {
							for (double newCustomerProb : es.newCustomerProbInterval) {
								for (int seeRange : es.seeRangeInterval) {
									for (double lazyProb : es.lazyProbInterval) {
										for (int trial = 0; trial < trials; trial++) {
											Parameter p = new Parameter(runTime, commRange, commReliability, numTaxis,
													numCustomers, newCustomerProb, seeRange, lazyProb, new Metric());
											p.withGui(false);
											p.withTesting(true);
											exps.add(p);
										}
									}
								}
							}
						}
					}
				}
			}
		}

		nrOfexpsRunning = exps.size();

		for (Parameter exp : exps) {
			Simulator sim = TaxiProblem.run(exp);
		}
	}

	/**
	 * Is called by simulator, if an experiment finishes
	 * 
	 * @param metric
	 */
	public static void experimentStoppedListener(Metric metric) {
		nrOfexpsRunning -= 1;

		System.out.println((exps.size() - nrOfexpsRunning) + "/" + exps.size() + " finished");

		if (nrOfexpsRunning == 0) {
			allExperimentsAreFinished();
		}
	}

	/**
	 * Is called, when all experiments have finished. Now start processing the data
	 */
	private static void allExperimentsAreFinished() {
		// save results to csv
		String content = Parameter.csvHeader() + "\n";
		for (Parameter exp : exps) {
			content += Parameter.listToCSV(exp.toCSV()) + "\n";
		}
		Utils.writeToCSV(content, "mas_taxi_delivery_data" + es.filename);

		System.out.println("DONE.");
	}
}
