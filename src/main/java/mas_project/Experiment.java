package mas_project;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.github.rinde.rinsim.core.Simulator;

public class Experiment {
	static List<Metric> metrics = new LinkedList<>();; // is added in dirty static way by simulator
	static Set<Parameter> exps;
	static int nrOfexpsRunning;
	public static void main(String[] args) {
		// set parameters
		exps = new HashSet<>();
		exps.add(new Parameter(20000*1000,100, new Metric()));
		exps.add(new Parameter(20000*1000,100, new Metric()));
		

		nrOfexpsRunning = exps.size();
		
		
		for (Parameter exp : exps) {
			Simulator sim = TaxiProblem.run(exp);
		}

	}

	/**
	 * Is called by simulator, if an experiment finishes
	 * @param metric
	 */
	public static void experimentStoppedListener(Metric metric) {
		nrOfexpsRunning -= 1;
		
		if(nrOfexpsRunning == 0) {
			allExperimentsAreFinished();
		}
		// simulation has stopped
		System.out.println(metric.getResult());
	}

	/**
	 * Is called, when all experiments have finished. Now start processing the data
	 */
	private static void allExperimentsAreFinished() {
		// TODO Auto-generated method stub
		
	}

}
