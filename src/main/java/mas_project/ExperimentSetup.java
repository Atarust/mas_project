package mas_project;

import static mas_project.Utils.doubles;
import static mas_project.Utils.ints;

import java.util.List;

public class ExperimentSetup {

	List<Integer> runTimeInterval;
	List<Integer> commRangeInterval;
	List<Double> commReliabilityInterval;
	List<Integer> numTaxisInterval;
	List<Integer> numCustomersInterval;
	List<Double> newCustomerProbInterval;
	List<Integer> seeRangeInterval;
	List<Double> lazyProbInterval;
	String filename;

	public ExperimentSetup(int exp) {
		// default values
		runTimeInterval = ints(1000 * 1000);
		commRangeInterval = ints(1, 1000 * 1000);
		commReliabilityInterval = doubles(1.0);
		numTaxisInterval = ints(5);
		numCustomersInterval = ints(10);
		newCustomerProbInterval = doubles(0.01);
		seeRangeInterval = ints(1000);
		lazyProbInterval = doubles(0.0);
		filename = String.valueOf(exp) + "_runtime" + runTimeInterval.get(0);

		switch (exp) {
		case 1:
			commRangeInterval = ints(1, 1 * 1000, 2 * 1000, 3 * 1000, 4 * 1000, 5 * 1000, 6 * 1000, 7 * 1000, 8 * 1000,
					9 * 1000, 10 * 1000, 1000 * 1000);
			break;
		case 2:
			newCustomerProbInterval = doubles(0.001, 0.002, 0.005, 0.01, 0.02, 0.05, 0.1);
			break;
		case 3:
			seeRangeInterval = ints(10, 100, 1000, 10 * 1000, 100 * 1000, 1000 * 1000);
			break;
		case 4:
			newCustomerProbInterval = doubles(0.001, 0.01, 0.1, 0.2);
			break;
		case 5:
			numTaxisInterval = ints(2, 5, 10, 20);
			break;
		default:
			throw new IllegalArgumentException("Experiment not defined.");
		}
	}
}
