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
	public final Metric metric;

	public static final int NUM_TAXIS = 10;
	public static final int NUM_CUSTUMORS = 20;
	public static final int NUM_DEPOTS = 1;

	// Useless parameters
	// time in ms

	public static final Display display = null;
	public static final Monitor m = null;
	public static final Listener list = null;
	public static final String graphFile = "/data/maps/leuven-simple.dot";
	public static final boolean testing = true;

	public static final long SERVICE_DURATION = 60000;
	public static final int TAXI_CAPACITY = 1;
	public static final int DEPOT_CAPACITY = 100;

	public static final int SPEED_UP = 20;
	public static final int MAX_CAPACITY = 1;
	public static final double NEW_CUSTOMER_PROB = .04;

	public static final String MAP_FILE = "/data/maps/leuven-simple.dot";
	public static final Map<String, Graph<MultiAttributeData>> GRAPH_CACHE = newHashMap();

	public static final long TEST_STOP_TIME = 20 * 60 * 1000;
	public static final int TEST_SPEED_UP = 64;

	public static final long RANDOM_SEED = 42L;
	public static final double seeRange = 13370;
	public static final double commReliability = 1;
	public static final boolean gui = false;

	public Parameter(long runTime, int commRange, Metric metric) {
		this.runTime = runTime;
		this.commRange = commRange;
		this.metric = metric;
	}

	@Override
	public String toString() {
		return "[runTime:" + runTime + "], " + "[commRange:" + commRange + "]";
	}

	public static String csvHeader() {
		return "runTime,commRange," + Metric.csvHeader();
	}

	public List<Object> toCSV() {
		// Needs Arraylist to be able to addAll
		List<Object> objects = new ArrayList<>(Arrays.asList(new Object[] {runTime, commRange}));
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

}
