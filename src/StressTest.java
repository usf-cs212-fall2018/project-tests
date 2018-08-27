import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

public class StressTest {

	private static final int WARM_RUNS = 5;
	private static final int TIME_RUNS = 5;

	private static final int THREADS = 5;

	// timeout PER RUN (not for all runs)
	private static final Duration TIMEOUT = Duration.ofMinutes(1);

	// used to prevent deadcode elimination
	public int result;

	@RepeatedTest(5)
	public void testIndexConsistency() {
		String filename = "index-text-all.json";
		Path actual = TestUtilities.ACTUAL_PATH.resolve(filename);
		Path expected = TestUtilities.EXPECTED_PATH.resolve("index-text").resolve(filename);

		String[] args = {
				"-path", Paths.get("text").toString(),
				"-index", actual.toString(),
				"-threads", Integer.toString(THREADS)
		};

		assertTimeout(TIMEOUT, () -> {
			TestUtilities.checkOutput(expected, actual, args);
		});
	}

	@RepeatedTest(5)
	public void testSearchConsistency() {
		String filename = "results-text-letters-all.json";
		Path actual = TestUtilities.ACTUAL_PATH.resolve(filename);
		Path expected = TestUtilities.EXPECTED_PATH.resolve("results-text-partial").resolve(filename);

		String[] args = {
				"-path", Paths.get("text").toString(),
				"-search", Paths.get("query", "letters.txt").toString(),
				"-results", actual.toString(),
				"-threads", Integer.toString(THREADS)
		};

		assertTimeout(TIMEOUT, () -> {
			TestUtilities.checkOutput(expected, actual, args);
		});
	}

	@Test
	public void testIndexRuntime() {
		String path = Paths.get("text").toString();

		String[] args1 = { "-path", path, "-threads", String.valueOf(1) };
		String[] args2 = { "-path", path, "-threads", String.valueOf(THREADS) };

		double singleAverage = benchmark(args1) / 1000000000.0;
		double threadAverage = benchmark(args2) / 1000000000.0;

		System.out.println();
		System.out.println("Indexing Benchmark:");
		System.out.printf("%d Threads: %.4f s%n", 1, singleAverage);
		System.out.printf("%d Threads: %.4f s%n", THREADS, threadAverage);
		System.out.printf("  Speedup: %.4f %n%n", singleAverage / threadAverage);

		assertTrue((singleAverage - threadAverage) > 0);
	}

	@Test
	public void testSearchRuntime() {
		String path = Paths.get("text").toString();
		String query = Paths.get("query", "letters.txt").toString();

		String[] args1 = { "-path", path, "-search", query, "-threads", String.valueOf(1) };
		String[] args2 = { "-path", path, "-search", query, "-threads", String.valueOf(THREADS) };

		double singleAverage = benchmark(args1) / 1000000000.0;
		double threadAverage = benchmark(args2) / 1000000000.0;

		System.out.println();
		System.out.println("Searching Benchmark:");
		System.out.printf("%d Threads: %.4f s%n", 1, singleAverage);
		System.out.printf("%d Threads: %.4f s%n", THREADS, threadAverage);
		System.out.printf("  Speedup: %.4f %n%n", singleAverage / threadAverage);

		assertTrue((singleAverage - threadAverage) > 0);
	}

	private double benchmark(String[] args) {
		long total = 0;
		long start = 0;

		try {
			for (int i = 0; i < WARM_RUNS; i++) {
				Driver.main(args);
			}

			for (int i = 0; i < TIME_RUNS; i++) {
				start = System.nanoTime();
				result = Driver.main(args);
				total += System.nanoTime() - start;
			}
		}
		catch (Exception e) {
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));

			String debug = String.format("%nArguments:%n    [%s]%nException:%n    %s%n", String.join(" ", args),
					writer.toString());
			fail(debug);
		}

		return (double) total / TIME_RUNS;
	}

}
