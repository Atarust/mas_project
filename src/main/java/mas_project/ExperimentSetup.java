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
		runTimeInterval = ints(1000 * 10000);
		commRangeInterval = ints(1, 1000 * 1000);
		commReliabilityInterval = doubles(1.0);
		numTaxisInterval = ints(5);
		numCustomersInterval = ints(10);
		newCustomerProbInterval = doubles(0.005);
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
		case 6: // I want  run from this with lots of trials
			numTaxisInterval = ints(2, 5, 10, 20);
			newCustomerProbInterval = doubles(0.001, 0.005, 0.02, 0.1);
			break;
		case 7:
			numTaxisInterval = ints(50);
			numCustomersInterval = ints(10);
			newCustomerProbInterval = doubles(0.2);
			seeRangeInterval = ints(1000);
			commRangeInterval = ints(10 * 1000);
			break;
		case 8:
			numTaxisInterval = ints(2, 5, 10);
			seeRangeInterval = ints(10, 100, 1000, 10 * 1000, 100 * 1000, 1000 * 1000);
			break;
		case 9:
			seeRangeInterval = ints(10, 31, 100, 310, 1000, 3100, 10 * 1000, 31 * 1000, 100 * 1000, 310 * 1000,
					1000 * 1000);
			break;
		case 10:
			// if seeRange is big, then impact of commRange is also big - as can be seen in
			// config 3.
			// This is to find parameters where impact of comm is big. We already saw
			// increasing seeRange brings good impact. Let's play around more:
			seeRangeInterval = ints(100 * 1000);
			numTaxisInterval = ints(2, 5, 10);
			newCustomerProbInterval = doubles(0.001, 0.005);
			break;
		case 11:
			// if seeRange is big, then impact of commRange is also big - as can be seen in
			// config 3.
			// This is to find parameters where impact of comm is big. We already saw
			// increasing seeRange brings good impact. Let's play around more:
			seeRangeInterval = ints(100 * 1000);
			numTaxisInterval = ints(10);
			newCustomerProbInterval = doubles(0.005);
			break;
		case 12: // DOES NOT WORK - stops after like 26 iterations
			// makes config 10 together with 11
			seeRangeInterval = ints(100 * 1000);
			numTaxisInterval = ints(2, 5, 10);
			newCustomerProbInterval = doubles(0.01);
			break;
		case 13:
			numTaxisInterval = ints(20,5);
			numCustomersInterval = ints(200,100,50,10,1);
			newCustomerProbInterval = doubles(0.0);
			break;
		case 14: // run with, but only at the end, with good default parameters
			lazyProbInterval = doubles(0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0);
			break;
		case 15:
			numTaxisInterval = ints(2,3,4, 5,6,7,8,9, 10,15, 20);
			break;
		case 16: // 9 with only important things to have more trials
			seeRangeInterval = ints(31, 31*10, 31 * 1000);
			break;
		case 17: // micro
			seeRangeInterval = ints(100);
			numTaxisInterval = ints(50);
			//numCustomersInterval = ints(10);
			newCustomerProbInterval = doubles(0.1);
			break;	
			
		case 18: // exp3
			numTaxisInterval = ints(5);
			newCustomerProbInterval = doubles(0.1);
			
			seeRangeInterval = ints(10, 100, 1000, 10 * 1000, 100 * 1000, 1000 * 1000);
			break;	
		case 20:
			commRangeInterval = ints(1, 3 * 1000, 10 * 1000, 30*1000, 1000 * 1000);
			break;
			
		default:
			throw new IllegalArgumentException("Experiment not defined. PROBABLY A break; is missing in ExperimentSetup.!");
		}

		filename = String.valueOf(exp) + "_runtime" + runTimeInterval.get(0);
	}
}
