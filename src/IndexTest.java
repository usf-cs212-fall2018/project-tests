import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

/*
 * I recommend you work on individual tests in the order they are provided in
 * this file. Do NOT try to run the entire file at once until you are confident
 * you are passing each group of tests first. To run a single test method or a
 * nested test class, click the name of the test method or class (this should
 * move your cursor). Then, right-click and select "Run As..." and "JUnit Test"
 * from the popup menus. To re-run the same set of tests, use the "Rerun Test"
 * button in the JUnit view.
 */

public class IndexTest {

	@Test
	public void testEnvironment() {
		assertTrue(TestUtilities.isEnvironmentSetup());
		assertTrue(Files.isReadable(Paths.get("text")));
	}

	@Nested
	public class ExceptionsTest {

		@Test
		public void testNoArguments() {
			String[] args = {};
			assertTimeout(Duration.ofSeconds(30), () -> {
				TestUtilities.checkExceptions(args);
			});
		}

		@Test
		public void testBadArguments() {
			String[] args = { "hello", "world" };
			assertTimeout(Duration.ofSeconds(30), () -> {
				TestUtilities.checkExceptions(args);
			});
		}

		@Test
		public void testMissingPath() {
			String[] args = { "-path" };
			assertTimeout(Duration.ofSeconds(30), () -> {
				TestUtilities.checkExceptions(args);
			});
		}

		@Test
		public void testInvalidPath() {
			// generates a random path name
			String path = Long.toHexString(Double.doubleToLongBits(Math.random()));
			String[] args = { "-path", path };
			assertTimeout(Duration.ofSeconds(30), () -> {
				TestUtilities.checkExceptions(args);
			});
		}

		@Test
		public void testNoOutput() throws IOException {
			String path = Paths.get("text", "simple", "hello.txt").toString();
			String[] args = { "-path", path };

			// make sure to delete old index.json if it exists
			Path output = Paths.get("index.json");
			Files.deleteIfExists(output);

			assertTimeout(Duration.ofSeconds(30), () -> {
				TestUtilities.checkExceptions(args);
			});

			// make sure a new index.json was not created
			assertFalse(Files.exists(output));
		}

		@Test
		public void testDefaultOutput() throws IOException {
			String path = Paths.get("text", "simple", "hello.txt").toString();
			String[] args = { "-path", path, "-index" };

			// make sure to delete old index.json if it exists
			Path output = Paths.get("index.json");
			Files.deleteIfExists(output);

			assertTimeout(Duration.ofSeconds(30), () -> {
				TestUtilities.checkExceptions(args);
			});

			// make sure a new index.json was created
			assertTrue(Files.exists(output));
		}

		@Test
		public void testEmptyOutput() throws IOException {
			String[] args = { "-index" };

			// make sure to delete old index.json if it exists
			Path output = Paths.get("index.json");
			Files.deleteIfExists(output);

			assertTimeout(Duration.ofSeconds(30), () -> {
				TestUtilities.checkExceptions(args);
			});

			// make sure a new index.json was created
			assertTrue(Files.exists(output));
		}

		@Test
		public void testSwitchedOrder() throws IOException {
			String path = Paths.get("text", "simple", "hello.txt").toString();
			String[] args = { "-index", "-path", path };

			// make sure to delete old index.json if it exists
			Path output = Paths.get("index.json");
			Files.deleteIfExists(output);

			assertTimeout(Duration.ofSeconds(30), () -> {
				TestUtilities.checkExceptions(args);
			});

			// make sure a new index.json was created
			assertTrue(Files.exists(output));
		}
	}

	// Static classes are provided to allow extending in other test suites.
	public static class IndexOutputTest {
		@TestFactory
		public Stream<DynamicTest> testSimple() {
			Path path = Paths.get("text", "simple");
			String[][] tests = {
					{ "simple-hello",    path.resolve("hello.txt").toString() },
					{ "simple-animals",  path.resolve("animals.text").toString() },
					{ "simple-capitals", path.resolve("capitals.txt").toString() },
					{ "simple-digits",   path.resolve("digits.txt").toString() },
					{ "simple-position", path.resolve("position.teXt").toString() },
					{ "simple-symbols",  path.resolve("symbols.txt").toString() },
					{ "simple-words",    path.resolve("words.tExT").toString() },
					{ "simple",          path.toString() },
			};

			return generateTests(tests);
		}

		@TestFactory
		public Stream<DynamicTest> testRFCs() {
			Path path = Paths.get("text", "rfcs");

			String[][] tests = {
					{ "rfc475",  path.resolve("rfc475.txt").toString() },
					{ "rfc6838", path.resolve("rfc6838.txt").toString() },
					{ "rfc6805", path.resolve("rfc6805.txt").toString() },
					{ "rfc6797", path.resolve("rfc6797.txt").toString() },
					{ "rfc5646", path.resolve("rfc5646.txt").toString() },
					{ "rfcs",    path.toString() },
			};

			return generateTests(tests);
		}

		// These tests take awhile. Only run them if you are passing the others!
		@TestFactory
		public Stream<DynamicTest> testGuten() {
			Path path = Paths.get("text", "gutenberg");

			String[][] tests = {
					{ "guten-pg37134", path.resolve("pg37134.txt").toString() },
					{ "guten-pg1661",  path.resolve("pg1661.txt").toString() },
					{ "guten-pg1322",  path.resolve("pg1322.txt").toString() },
					{ "guten-pg1228",  path.resolve("pg1228.txt").toString() },
					{ "guten-1400-0",  path.resolve("1400-0.txt").toString() },
			};

			return generateTests(tests);
		}

		// These tests take awhile. Only run them if you are passing the others!
		@TestFactory
		public Stream<DynamicTest> testLong() {
			String[][] tests = {
					{ "guten",  Paths.get("text", "gutenberg").toString() },
					{ "all",    Paths.get("text").toString() },
			};

			return generateTests(tests);
		}

		public Stream<DynamicTest> generateTests(String[][] tests) {
			return Stream.of(tests).map(params -> dynamicTest(params[0], () -> {
				String filename = String.format("index-text-%s.json", params[0]);
				Path actual = TestUtilities.ACTUAL_PATH.resolve(filename);
				Path expected = TestUtilities.EXPECTED_PATH.resolve("index-text").resolve(filename);

				String[] args = { "-path", params[1], "-index", actual.toString() };
				TestUtilities.checkOutput(expected, actual, args);
			}));
		}
	}

	@Nested
	public class NestedIndexOutputTest extends IndexOutputTest {
		// This placeholder class just makes sure the tests in StaticIndexOutputTest
		// are properly included when the entire file is run.
	}
}
