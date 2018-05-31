package mas_project;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import com.github.rinde.rinsim.core.Simulator;

public class Experiment {
	static List<Parameter> exps;
	static int nrOfexpsRunning;

	public static void main(String[] args) {
		// set parameters
		exps = new LinkedList<>();

		int runTime = 1000 * 1000;
		int commRange = 1000;
		double commReliability = 1;
		int numTaxis = 10;
		int numCustomers = 20;
		double newCustomerProb = 0.04;
		int seeRange = 13370;
		
		for (int commR = 0; commR < 10; commR++) {
			commRange = 1 + 100 * commR;
			exps.add(new Parameter(runTime, commRange, commReliability, numTaxis, numCustomers, newCustomerProb, seeRange, new Metric()));
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
		// save results to csv
		String content = Parameter.csvHeader() + "\n";
		for (Parameter exp : exps) {
			content += Parameter.listToCSV(exp.toCSV()) + "\n";
		}
		writeToCSV(content, "filename");
		
		System.out.println("DONE.");
	}
	
	private static void writeToCSV(String content, String filename) {
		// Get the file reference
		Path path = Paths.get("results/" + filename + ".csv");

		// Use try-with-resource to get auto-closeable writer instance
		try (BufferedWriter writer = Files.newBufferedWriter(path)) {
			writer.write(content);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
