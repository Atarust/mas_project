package mas_project;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class Utils {
	public static void writeToCSV(String content, String filename) {
		// Get the file reference
		Path path = Paths.get("results/" + filename + ".csv");

		// Use try-with-resource to get auto-closeable writer instance
		try (BufferedWriter writer = Files.newBufferedWriter(path)) {
			writer.write(content);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static List<Integer> ints(Integer... i) {
		return Arrays.asList(i);
	}

	public static List<Double> doubles(Double... d) {
		return Arrays.asList(d);
	}

}
