import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

public class SearchTest {

	@Test
	public void testEnvironment() {
		assertTrue(TestUtilities.isEnvironmentSetup());
		assertTrue(Files.isReadable(Paths.get("text")));
		assertTrue(Files.isReadable(Paths.get("query")));
	}

	@Nested
	public class ExceptionsTest {

		@Test
		public void testMissingQueryPath() {
			String path = Paths.get("text", "simple", "hello.txt").toString();
			String[] args = { "-path", path, "-query" };
			assertTimeout(Duration.ofSeconds(30), () -> {
				TestUtilities.checkExceptions(args);
			});
		}

		@Test
		public void testInvalidQueryPath() {
			String path = Paths.get("text", "simple", "hello.txt").toString();
			String query = Long.toHexString(Double.doubleToLongBits(Math.random()));
			String[] args = { "-path", path, "-search", query };
			assertTimeout(Duration.ofSeconds(30), () -> {
				TestUtilities.checkExceptions(args);
			});
		}

		@Test
		public void testInvalidExactPath() {
			String path = Paths.get("text", "simple", "hello.txt").toString();
			String query = Long.toHexString(Double.doubleToLongBits(Math.random()));
			String[] args = { "-path", path, "-search", query, "-exact" };
			assertTimeout(Duration.ofSeconds(30), () -> {
				TestUtilities.checkExceptions(args);
			});
		}

		@Test
		public void testNoOutput() throws IOException {
			String path = Paths.get("text", "simple", "hello.txt").toString();
			String query = Paths.get("query", "simple.txt").toString();
			String[] args = { "-path", path, "-search", query };

			// make sure to delete old index.json and results.json if it exists
			Path index = Paths.get("index.json");
			Path results = Paths.get("results.json");
			Files.deleteIfExists(index);
			Files.deleteIfExists(results);

			assertTimeout(Duration.ofSeconds(30), () -> {
				TestUtilities.checkExceptions(args);
			});

			// make sure a new index.json and results.json were not created
			Assert.assertFalse(Files.exists(index) || Files.exists(results));
		}

		@Test
		public void testDefaultOutput() throws IOException {
			String path = Paths.get("text", "simple", "hello.txt").toString();
			String query = Paths.get("query", "simple.txt").toString();
			String[] args = { "-path", path, "-search", query, "-results" };

			// make sure to delete old index.json and results.json if it exists
			Path index = Paths.get("index.json");
			Path results = Paths.get("results.json");
			Files.deleteIfExists(index);
			Files.deleteIfExists(results);

			assertTimeout(Duration.ofSeconds(30), () -> {
				TestUtilities.checkExceptions(args);
			});

			// make sure a new results.json was not created (but index.json was not)
			Assert.assertTrue(Files.exists(results) && !Files.exists(index));
		}

		@Test
		public void testEmptyIndex() throws IOException {
			String query = Paths.get("query", "simple.txt").toString();
			String[] args = { "-search", query, "-results" };

			// make sure to delete old index.json and results.json if it exists
			Path index = Paths.get("index.json");
			Path results = Paths.get("results.json");
			Files.deleteIfExists(index);
			Files.deleteIfExists(results);

			assertTimeout(Duration.ofSeconds(30), () -> {
				TestUtilities.checkExceptions(args);
			});

			// make sure a new results.json was not created (but index.json was not)
			Assert.assertTrue(Files.exists(results) && !Files.exists(index));
		}

		@Test
		public void testEmptyQuery() throws IOException {
			String[] args = { "-results" };

			// make sure to delete old index.json and results.json if it exists
			Path index = Paths.get("index.json");
			Path results = Paths.get("results.json");
			Files.deleteIfExists(index);
			Files.deleteIfExists(results);

			assertTimeout(Duration.ofSeconds(30), () -> {
				TestUtilities.checkExceptions(args);
			});

			// make sure a new results.json was not created (but index.json was not)
			Assert.assertTrue(Files.exists(results) && !Files.exists(index));
		}

		@Test
		public void testSwitchedOrder() throws IOException {
			String path = Paths.get("text", "simple", "hello.txt").toString();
			String query = Paths.get("query", "simple.txt").toString();
			String[] args = { "-search", query, "-results", "-path", path, "-exact" };

			// make sure to delete old index.json and results.json if it exists
			Path index = Paths.get("index.json");
			Path results = Paths.get("results.json");
			Files.deleteIfExists(index);
			Files.deleteIfExists(results);

			assertTimeout(Duration.ofSeconds(30), () -> {
				TestUtilities.checkExceptions(args);
			});

			// make sure a new results.json was not created (but index.json was not)
			Assert.assertTrue(Files.exists(results) && !Files.exists(index));
		}
	}

	public static class LocationsTest {
		@Test
		public void test() {
			String filename = "index-text-locations.json";
			Path actual = TestUtilities.ACTUAL_PATH.resolve(filename);
			Path expected = TestUtilities.EXPECTED_PATH.resolve(filename);

			String[] args = { "-path", "text", "-locations", actual.toString() };
			TestUtilities.checkOutput(expected, actual, args);
		}
	}

	@Nested
	public class NestedLocationsTest extends LocationsTest {
		// Placeholder for nesting tests
	}

	public static class SearchExactTest {

		@TestFactory
		public Stream<DynamicTest> testSimple() {
			Path path = Paths.get("text", "simple");
			String[][] tests = {
					{ "words-words", "words.txt", path.resolve("words.tExT").toString() },
					{ "animals-animals", "animals.txt", path.resolve("animals.text").toString() },
					{ "animals-simple", "animals.txt", path.toString() },
					{ "simple-simple", "simple.txt", path.toString() },
					{ "letters-simple", "letters.txt", path.toString() }
			};

			return generateTests(tests);
		}

		@TestFactory
		public Stream<DynamicTest> testRFCs() {
			Path path = Paths.get("text", "rfcs");
			String[][] tests = {
					{ "letters-rfc475",  "letters.txt", path.resolve("rfc475.txt").toString() },
					{ "letters-rfc5646", "letters.txt", path.resolve("rfc5646.txt").toString() },
					{ "letters-rfc6797", "letters.txt", path.resolve("rfc6797.txt").toString() },
					{ "letters-rfc6805", "letters.txt", path.resolve("rfc6805.txt").toString() },
					{ "letters-rfc6838", "letters.txt", path.resolve("rfc6838.txt").toString() },
					{ "letters-rfcs", "letters.txt", path.toString() }
			};

			return generateTests(tests);
		}

		// These tests take awhile. Only run them if you are passing the others!
		@TestFactory
		public Stream<DynamicTest> testGutenberg() {
			Path path = Paths.get("text", "gutenberg");
			String[][] tests = {
					{ "guten-1400-0",  "gutenberg.txt", path.resolve("1400-0.txt").toString() },
					{ "guten-pg1228",  "gutenberg.txt", path.resolve("pg1228.txt").toString() },
					{ "guten-pg1322",  "gutenberg.txt", path.resolve("pg1322.txt").toString() },
					{ "guten-pg1661",  "gutenberg.txt", path.resolve("pg1661.txt").toString() },
					{ "guten-pg37134", "gutenberg.txt", path.resolve("pg37134.txt").toString() }
			};

			return generateTests(tests);
		}

		// These tests take awhile. Only run them if you are passing the others!
		@TestFactory
		public Stream<DynamicTest> testLong() {
			String[][] tests = {
					{ "guten-guten", "gutenberg.txt", Paths.get("text", "gutenberg").toString() },
					{ "letters-guten", "letters.txt", Paths.get("text", "gutenberg").toString() },
					{ "letters-all", "letters.txt", Paths.get("text").toString() }
			};

			return generateTests(tests);
		}

		public Stream<DynamicTest> generateTests(String[][] tests) {
			return Stream.of(tests).map(params -> dynamicTest(params[0], () -> {
				String filename = String.format("results-text-%s.json", params[0]);
				Path actual = TestUtilities.ACTUAL_PATH.resolve(filename);
				Path expected = TestUtilities.EXPECTED_PATH.resolve("results-text-exact").resolve(filename);

				String[] args = {
						"-path", params[2],
						"-search", Paths.get("query", params[1]).toString(),
						"-results", actual.toString(),
						"-exact"
				};
				TestUtilities.checkOutput(expected, actual, args);
			}));
		}
	}

	@Nested
	public class NestedSearchExactTest extends SearchExactTest {
		// Placeholder for nesting tests
	}

	@Nested
	public class NestedSearchPartialTest extends SearchExactTest {
		// All of the same tests as SearchExactTest, but this time WITHOUT the
		// -exact search flag.

		@Override
		public Stream<DynamicTest> generateTests(String[][] tests) {
			return Stream.of(tests).map(params -> dynamicTest(params[0], () -> {
				String filename = String.format("results-text-%s.json", params[0]);
				Path actual = TestUtilities.ACTUAL_PATH.resolve(filename);
				Path expected = TestUtilities.EXPECTED_PATH.resolve("results-text-partial").resolve(filename);

				String[] args = {
						"-path", params[2],
						"-search", Paths.get("query", params[1]).toString(),
						"-results", actual.toString()
				};
				TestUtilities.checkOutput(expected, actual, args);
			}));
		}
	}
}
