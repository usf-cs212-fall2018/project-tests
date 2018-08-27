import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;

public class ThreadTest {

	@Nested
	public class ExceptionsTest {
		@Test
		public void testNegativeThreads() {
			String path = Paths.get("html", "simple", "hello.html").toString();
			String query = Paths.get("query", "simple.txt").toString();
			String threads = "-1";
			String[] args = { "-path", path, "-search", query, "-threads", threads };
			assertTimeout(Duration.ofSeconds(30), () -> {
				TestUtilities.checkExceptions(args);
			});
		}

		@Test
		public void testZeroThreads() {
			String path = Paths.get("html", "simple", "hello.html").toString();
			String query = Paths.get("query", "simple.txt").toString();
			String threads = "0";
			String[] args = { "-path", path, "-search", query, "-threads", threads };
			assertTimeout(Duration.ofSeconds(30), () -> {
				TestUtilities.checkExceptions(args);
			});
		}

		@Test
		public void testFractionThreads() {
			String path = Paths.get("html", "simple", "hello.html").toString();
			String query = Paths.get("query", "simple.txt").toString();
			String threads = "3.14";
			String[] args = { "-path", path, "-search", query, "-threads", threads };
			assertTimeout(Duration.ofSeconds(30), () -> {
				TestUtilities.checkExceptions(args);
			});
		}

		@Test
		public void testWordThreads() {
			String path = Paths.get("html", "simple", "hello.html").toString();
			String query = Paths.get("query", "simple.txt").toString();
			String threads = "fox";
			String[] args = { "-path", path, "-search", query, "-threads", threads };
			assertTimeout(Duration.ofSeconds(30), () -> {
				TestUtilities.checkExceptions(args);
			});
		}

		@Test
		public void testDefaultThreads() {
			String path = Paths.get("html", "simple", "hello.html").toString();
			String query = Paths.get("query", "simple.txt").toString();
			String[] args = { "-path", path, "-search", query, "-threads" };
			assertTimeout(Duration.ofSeconds(30), () -> {
				TestUtilities.checkExceptions(args);
			});
		}
	}

	@Nested
	public class IndexOutputThreads extends IndexTest.IndexOutputTest {

		public Stream<DynamicTest> generateTests(String[][] tests, int threads) {
			return Stream.of(tests).map(params -> dynamicTest(
					params[0] + " (" + Integer.toString(threads) + " threads)", () -> {
				String filename = String.format("index-text-%s.json", params[0]);
				Path actual = TestUtilities.ACTUAL_PATH.resolve(filename);
				Path expected = TestUtilities.EXPECTED_PATH.resolve("index-text").resolve(filename);

				String[] args = {
						"-path", params[1],
						"-index", actual.toString(),
						"-threads", Integer.toString(threads)
				};

				assertTimeout(Duration.ofMinutes(3), () -> {
					TestUtilities.checkOutput(expected, actual, args);
				});
			}));
		}

		@Override
		public Stream<DynamicTest> generateTests(String[][] tests) {
			Stream<DynamicTest> one = generateTests(tests, 1);
			Stream<DynamicTest> two = generateTests(tests, 2);
			Stream<DynamicTest> five = generateTests(tests, 5);

			return Stream.of(one, two, five).flatMap(s -> s);
		}
	}


	@Nested
	public class SearchExactThreads extends SearchTest.SearchExactTest {

		public Stream<DynamicTest> generateTests(String[][] tests, int threads) {
			return Stream.of(tests).map(params -> dynamicTest(
					params[0] + " (" + Integer.toString(threads) + " threads)", () -> {
				String filename = String.format("results-text-%s.json", params[0]);
				Path actual = TestUtilities.ACTUAL_PATH.resolve(filename);
				Path expected = TestUtilities.EXPECTED_PATH.resolve("results-text-exact").resolve(filename);

				String[] args = {
						"-path", params[2],
						"-search", Paths.get("query", params[1]).toString(),
						"-results", actual.toString(),
						"-exact",
						"-threads", Integer.toString(threads)
				};

				assertTimeout(Duration.ofMinutes(3), () -> {
					TestUtilities.checkOutput(expected, actual, args);
				});
			}));
		}

		@Override
		public Stream<DynamicTest> generateTests(String[][] tests) {
			// don't do extensive thread testing for this case
			return generateTests(tests, 3);
		}
	}

	@Nested
	public class SearchPartialThreads extends SearchTest.SearchExactTest {

		public Stream<DynamicTest> generateTests(String[][] tests, int threads) {
			return Stream.of(tests).map(params -> dynamicTest(
					params[0] + " (" + Integer.toString(threads) + " threads)", () -> {
				String filename = String.format("results-text-%s.json", params[0]);
				Path actual = TestUtilities.ACTUAL_PATH.resolve(filename);
				Path expected = TestUtilities.EXPECTED_PATH.resolve("results-text-partial").resolve(filename);

				String[] args = {
						"-path", params[2],
						"-search", Paths.get("query", params[1]).toString(),
						"-results", actual.toString(),
						"-threads", Integer.toString(threads)
				};

				assertTimeout(Duration.ofMinutes(3), () -> {
					TestUtilities.checkOutput(expected, actual, args);
				});
			}));
		}

		@Override
		public Stream<DynamicTest> generateTests(String[][] tests) {
			Stream<DynamicTest> one = generateTests(tests, 1);
			Stream<DynamicTest> two = generateTests(tests, 2);
			Stream<DynamicTest> five = generateTests(tests, 5);

			return Stream.of(one, two, five).flatMap(s -> s);
		}
	}
}
