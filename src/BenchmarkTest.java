import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.time.Duration;

import org.junit.jupiter.api.Test;

/*
 * This is provided to help with local benchmarking. It can help you
 * identify runtime issues if you are not passing the benchmark script on
 * the lab computers. However, it does not replace running the benchmark
 * script on the lab computers, which compares two different releases of
 * your code.
 *
 * This test is not part of the project 3 test group. You have to run it
 * separately.
 *
 * TURN OFF LOGGING BEFORE RUNNING THIS BENCHMARK!
 */
public class BenchmarkTest {
	private static final int WARM_RUNS = 5;
	private static final int TIME_RUNS = 10;

	private static final int THREADS = 5;

	// timeout PER RUN (not for all runs)
	private static final Duration TIMEOUT = Duration.ofMinutes(1);

	// not used
	public int blackhole;

	public static int runDriver(String[] args) {
		int result = 0;

		try {
			assertTimeout(TIMEOUT, () -> {
				Driver.main(args);
			});
		}
		catch (Exception e) {
			result = -1;
		}

		return result;
	}

	@Test
	public void testSingleVersusMulti() {

		String path = Paths.get("text").toString();
		String query = Paths.get("query", "letters.txt").toString();

		String[] args1 = { "-path", path, "-search", query };
		String[] args2 = { "-path", path, "-search", query, "-threads", String.valueOf(THREADS) };

		long[] threadRuns = benchmark(args2);
		long[] singleRuns = benchmark(args1);

		long singleTotal = 0;
		long threadTotal = 0;

		// print report of runs
		System.out.println("\nSingle vs Multi Benchmark:\n");
		System.out.printf("%-6s    %10s    %10s%n", "Warmup", "Single", "Multi");
		for (int i = 0; i < WARM_RUNS; i++) {
			System.out.printf("%-6d    %10.6f    %10.6f%n",
					i + 1,
					singleRuns[i] / 1000000000.0,
					threadRuns[i] / 1000000000.0);
		}

		System.out.println();

		System.out.printf("%-6s    %10s    %10s%n", "Timed", "Single", "Multi");
		for (int i = WARM_RUNS; i < (WARM_RUNS + TIME_RUNS); i++) {
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
		System.out.printf(" Single: %10.6f s%n",  singleAverage / 1000000000.0);
		System.out.printf("  Multi: %10.6f s%n",  threadAverage / 1000000000.0);
		System.out.printf("Speedup: %10.6f %n%n", singleAverage / threadAverage);

		assertTrue((singleAverage - threadAverage) > 0);
	}

	public long[] benchmark(String[] args) {
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
