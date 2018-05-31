package mas_project;

import java.util.LinkedList;
import java.util.List;

import com.github.rinde.rinsim.core.Simulator;

public class Experiment {
	static List<Parameter> exps;
	static int nrOfexpsRunning;

	public static void main(String[] args) {
		// set parameters
		exps = new LinkedList<>();

		for (int commR = 0; commR < 10; commR++) {
			exps.add(new Parameter(1000 * 1000, 1 + 100*commR, new Metric()));
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

		if (nrOfexpsRunning == 0) {
			allExperimentsAreFinished();
		}
		// simulation has stopped
		System.out.println(metric.getResult());
	}

	/**
	 * Is called, when all experiments have finished. Now start processing the data
	 */
	private static void allExperimentsAreFinished() {
		for(Parameter exp : exps) {
			System.out.println(exp.toString() + exp.metric);
		}
	}

}
