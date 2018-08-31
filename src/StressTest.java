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

	// not used
	public int blackhole;

	public int runDriver(String[] args) {
		int result = 0;

		try {
			Driver.main(args);
		}
		catch (Exception e) {
			result = -1;
		}

		return result;
	}

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

		long[] singleRuns = benchmark(args1);
		long[] threadRuns = benchmark(args2);

		long singleTotal = 0;
		long threadTotal = 0;

		// print report of runs
		System.out.println("Indexing Benchmark:");
		System.out.printf("%-6s    %10s    %10s%n", "Warmup", "Single", "Multi");
		for (int i = 0; i < WARM_RUNS; i++) {
			System.out.printf("%-6d    %10.6f    %10.6f%n",
					i + 1,
					singleRuns[i] / 1000000000.0,
					threadRuns[i] / 1000000000.0);
		}

		System.out.printf("%-6s    %10s    %10s%n", "Timed", "Single", "Multi");
		for (int i = WARM_RUNS; i < WARM_RUNS + TIME_RUNS; i++) {
			singleTotal += singleRuns[i];
			threadTotal += threadRuns[i];
			System.out.printf("%-6d    %10.6f    %10.6f%n",
					i + 1,
					singleRuns[i] / 1000000000.0,
					threadRuns[i] / 1000000000.0);
		}

		double singleAverage = (double) singleTotal / TIME_RUNS;
		double threadAverage = (double) threadTotal / TIME_RUNS;

		System.out.println();
		System.out.printf("%d Threads: %10.6f s%n", 1, singleAverage / 1000000000.0);
		System.out.printf("%d Threads: %10.6f s%n", THREADS, threadAverage / 1000000000.0);
		System.out.printf("  Speedup: %10.6f %n%n", singleAverage / threadAverage);
	}

	@Test
	public void testSearchRuntime() {
		String path = Paths.get("text").toString();
		String query = Paths.get("query", "letters.txt").toString();

		String[] args1 = { "-path", path, "-search", query, "-threads", String.valueOf(1) };
		String[] args2 = { "-path", path, "-search", query, "-threads", String.valueOf(THREADS) };

		long[] singleRuns = benchmark(args1);
		long[] threadRuns = benchmark(args2);

		long singleTotal = 0;
		long threadTotal = 0;

		// print report of runs
		System.out.println("Searching Benchmark:");
		System.out.printf("%-6s    %10s    %10s%n", "Warmup", "Single", "Multi");
		for (int i = 0; i < WARM_RUNS; i++) {
			System.out.printf("%-6d    %10.6f    %10.6f%n",
					i + 1,
					singleRuns[i] / 1000000000.0,
					threadRuns[i] / 1000000000.0);
		}

		System.out.printf("%-6s    %10s    %10s%n", "Timed", "Single", "Multi");
		for (int i = WARM_RUNS; i < WARM_RUNS + TIME_RUNS; i++) {
			singleTotal += singleRuns[i];
			threadTotal += threadRuns[i];
			System.out.printf("%-6d    %10.6f    %10.6f%n",
					i + 1,
					singleRuns[i] / 1000000000.0,
					threadRuns[i] / 1000000000.0);
		}

		double singleAverage = (double) singleTotal / TIME_RUNS;
		double threadAverage = (double) threadTotal / TIME_RUNS;

		System.out.println();
		System.out.printf("%d Threads: %10.6f s%n", 1, singleAverage / 1000000000.0);
		System.out.printf("%d Threads: %10.6f s%n", THREADS, threadAverage / 1000000000.0);
		System.out.printf("  Speedup: %10.6f %n%n", singleAverage / threadAverage);

		assertTrue(singleAverage - threadAverage > 0);
	}

	private long[] benchmark(String[] args) {
		long start = 0;
		long[] runs = new long[WARM_RUNS + TIME_RUNS];

		blackhole = 0;

		try {
			for (int i = 0; i < WARM_RUNS; i++) {
				start = System.nanoTime();
				blackhole += runDriver(args);
				runs[i] = System.nanoTime() - start;
			}

			for (int i = 0; i < TIME_RUNS; i++) {
				start = System.nanoTime();
				blackhole += runDriver(args);
				runs[i + WARM_RUNS] = System.nanoTime() - start;
			}
		}
		catch (Exception e) {
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));

			String debug = String.format("%nArguments:%n    [%s]%nException:%n    %s%n", String.join(" ", args),
					writer.toString());
			fail(debug);
		}

		return runs;
	}

}
