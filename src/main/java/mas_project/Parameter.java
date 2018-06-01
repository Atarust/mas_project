package mas_project;

import static com.google.common.collect.Maps.newHashMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Monitor;

import com.github.rinde.rinsim.event.Listener;
import com.github.rinde.rinsim.geom.Graph;
import com.github.rinde.rinsim.geom.MultiAttributeData;

public class Parameter {
	public final long runTime;
	public final int commRange;
	public final double commReliability;
	public final int numTaxis;
	public final int numCustomers;
	public final double newCustomerProb;
	public final int seeRange;
	public final double lazyProb;

	public final Metric metric;

	public boolean testing = true;
	public boolean gui = false;
	
	// Useless parameters
	// time in ms

	public static final int NUM_DEPOTS = 1;
	public static final Display display = null;
	public static final Monitor m = null;
	public static final Listener list = null;
	public static final String graphFile = "/data/maps/leuven-simple.dot";

	public static final long SERVICE_DURATION = 60000;
	public static final int TAXI_CAPACITY = 1;
	public static final int DEPOT_CAPACITY = 100;

	public static final int SPEED_UP = 20;
	public static final int MAX_CAPACITY = 1;

	public static final String MAP_FILE = "/data/maps/leuven-simple.dot";
	public static final Map<String, Graph<MultiAttributeData>> GRAPH_CACHE = newHashMap();

	public static final long TEST_STOP_TIME = 20 * 60 * 1000;
	public static final int TEST_SPEED_UP = 64;

	public final long RANDOM_SEED = 42L + System.currentTimeMillis();

	public Parameter(long runTime, int commRange, double commReliability, int numTaxis, int numCustomers,
			double newCustomerProb, int seeRange, double lazyProb, Metric metric) {
		this.runTime = runTime;
		this.commRange = commRange;
		this.commReliability = commReliability;
		this.numTaxis = numTaxis;
		this.numCustomers = numCustomers;
		this.newCustomerProb = newCustomerProb;
		this.seeRange = seeRange;
		this.lazyProb = lazyProb;
		
		this.metric = metric;

	}

	public static Parameter defaultParameter() {
		int runTime = 1000*1000;
		int commRange = 100;
		double commReliability = 1;
		int numTaxis = 20;
		int numCustomers = 20;
		double newCustomerProb = 0.05;
		int seeRange = 2000;
		double lazyProb = 0.1;
		Metric metric = new Metric();
		return new Parameter(runTime, commRange, commReliability, numTaxis, numCustomers, newCustomerProb, seeRange, lazyProb, metric);
	}

	public static String csvHeader() {
		return "runTime,commRange,commReliability,numTaxis,numCustomers,newCustomerProb,seeRange,lazyProb," + Metric.csvHeader();
	}

	public List<Object> toCSV() {
		// Needs Arraylist to be able to addAll
		List<Object> objects = new ArrayList<>(Arrays.asList(new Object[] { runTime, commRange, commReliability,
				numTaxis, numCustomers, newCustomerProb, seeRange, lazyProb,}));
		objects.addAll(metric.toCSV());
		return objects;
	}

	public static String listToCSV(List<Object> objects) {
		String csv = "";
		for (Object el : objects) {
			csv += el + ",";
		}
		return csv;
	}
	
	public void withGui(boolean guiOn) {
		gui = guiOn;
	}
	
	public void withTesting(boolean testingOn) {
		testing = testingOn;
	}
	
	

}
