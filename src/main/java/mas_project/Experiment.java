package mas_project;

import static mas_project.Utils.doubles;
import static mas_project.Utils.ints;

import java.util.LinkedList;
import java.util.List;

import com.github.rinde.rinsim.core.Simulator;


public class Experiment {
	static List<Parameter> exps;
	static int nrOfexpsRunning;

	public static void main(String[] args) {
		// set parameters
		exps = new LinkedList<>();

		List<Integer> runTimeInterval = ints(1000 * 10000);
		List<Integer> commRangeInterval = ints( 10 * 1000, 1000*1000);
		List<Double> commReliabilityInterval = doubles(1.0);
		List<Integer> numTaxisInterval = ints(5,10,20,50);
		List<Integer> numCustomersInterval = ints(10);
		List<Double> newCustomerProbInterval = doubles(0.01);
		List<Integer> seeRangeInterval = ints(1000);
		List<Double> lazyProbInterval = doubles(0.5);

		for (int runTime : runTimeInterval) {
			for (int commRange : commRangeInterval) {
				for (double commReliability : commReliabilityInterval) {
					for (int numTaxis : numTaxisInterval) {
						for (int numCustomers : numCustomersInterval) {
							for (double newCustomerProb : newCustomerProbInterval) {
								for (int seeRange : seeRangeInterval) {
									for (double lazyProb : lazyProbInterval) {
										Parameter p = new Parameter(runTime, commRange, commReliability, numTaxis,
												numCustomers, newCustomerProb, seeRange, lazyProb, new Metric());
										p.withGui(true);
										p.withTesting(false);
										exps.add(p);
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
		Utils.writeToCSV(content, "preliminary_exploration_searchImpact4");

		System.out.println("DONE.");
	}
}
