import static org.junit.jupiter.api.Assertions.assertTimeout;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class CrawlTest {

	public static final Duration TIMEOUT = Duration.ofSeconds(30);

	@Nested
	public class NestedLocationTest extends LocationTest {
		// Placeholder for nesting tests
	}

	public static class LocationTest {

		public void test(String name, String url, int limit) {
			String filename = String.format("location-url-%s.json", name);
			Path actual = TestUtilities.ACTUAL_PATH.resolve(filename);
			Path expected = TestUtilities.EXPECTED_PATH.resolve("location-url").resolve(filename);

			String[] args = {
				"-url", url,
				"-limit", Integer.toString(limit),
				"-locations", actual.toString()
			};

			assertTimeout(TIMEOUT, () -> {
				TestUtilities.checkOutput(expected, actual, args);
			});
		}

		@Test
		public void testSimple() {
			test("simple", "https://www.cs.usfca.edu/~cs212/simple/index.html", 10);
		}

		@Test
		public void testBirds() {
			test("birds", "https://www.cs.usfca.edu/~cs212/birds/birds.html", 50);
		}

		@Test
		public void testRecurse() {
			test("recurse", "https://www.cs.usfca.edu/~cs212/recurse/link01.html", 100);
		}

		@Test
		public void testRedirect() {
			test("redirect", "https://www.cs.usfca.edu/~cs212/redirect/", 10);
		}

		@Test
		public void testGutenberg() {
			test("gutenberg", "https://www.cs.usfca.edu/~cs212/gutenberg/", 5);
		}

		@Test
		public void testCSS() {
			test("wdgcss", "https://www.cs.usfca.edu/~cs212/wdgcss/properties.html", 60);
		}

		@Test
		public void testNumpy() {
			test("numpy", "https://www.cs.usfca.edu/~cs212/numpy/user/index.html", 10);
		}
	}

	@Nested
	public class NestedIndexTest extends IndexTest {
		// Placeholder for nesting tests
	}

	public static class IndexTest {

		public void test(String name, String url, int limit) {
			String filename = String.format("index-url-%s.json", name);
			Path actual = TestUtilities.ACTUAL_PATH.resolve(filename);
			Path expected = TestUtilities.EXPECTED_PATH.resolve("index-url").resolve(filename);

			String[] args = {
				"-url", url,
				"-limit", Integer.toString(limit),
				"-index", actual.toString()
			};

			assertTimeout(TIMEOUT, () -> {
				TestUtilities.checkOutput(expected, actual, args);
			});
		}

		@Test
		public void testHello() {
			test("hello", "https://www.cs.usfca.edu/~cs212/simple/hello.html", 1);
		}

		@Test
		public void testSimple() {
			test("simple", "https://www.cs.usfca.edu/~cs212/simple/index.html", 10);
		}

		@Test
		public void testBirds() {
			test("birds", "https://www.cs.usfca.edu/~cs212/birds/birds.html", 50);
		}

		@Test
		public void testRecurse() {
			test("recurse", "https://www.cs.usfca.edu/~cs212/recurse/link01.html", 100);
		}

		@Test
		public void testRedirect() {
			test("redirect", "https://www.cs.usfca.edu/~cs212/redirect/", 10);
		}

		@Test
		public void testSecondVariety() {
			test("gutenberg-32032", "https://www.cs.usfca.edu/~cs212/gutenberg/32032-h/32032-h.htm", 1);
		}

		@Test
		public void testGutenberg() {
			test("gutenberg", "https://www.cs.usfca.edu/~cs212/gutenberg/", 5);
		}

		@Test
		public void testCSSProperties() {
			test("wdgcss-properties", "https://www.cs.usfca.edu/~cs212/wdgcss/properties.html", 1);
		}

		@Test
		public void testCSS() {
			test("wdgcss", "https://www.cs.usfca.edu/~cs212/wdgcss/properties.html", 60);
		}

		@Test
		public void testNumpyQuick() {
			test("numpy-quick", "https://www.cs.usfca.edu/~cs212/numpy/user/quickstart.html", 1);
		}

		@Test
		public void testNumpy() {
			test("numpy", "https://www.cs.usfca.edu/~cs212/numpy/user/index.html", 10);
		}
	}

	@Nested
	public class NestedSearchTest extends PartialSearchTest {
		// Placeholder for nesting tests
	}

	public static class PartialSearchTest {

		public void test(String name, String url, int limit, String queries) {
			String filename = String.format("results-url-%s.json", name);

			Path query = Paths.get("query", queries);
			Path actual = TestUtilities.ACTUAL_PATH.resolve(filename);
			Path expected = TestUtilities.EXPECTED_PATH.resolve("results-url-partial").resolve(filename);

			String[] args = {
				"-url", url,
				"-limit", Integer.toString(limit),
				"-search", query.toString(),
				"-results", actual.toString()
			};

			assertTimeout(TIMEOUT, () -> {
				TestUtilities.checkOutput(expected, actual, args);
			});
		}

		@Test
		public void testSimple() {
			test("simple", "https://www.cs.usfca.edu/~cs212/simple/index.html", 10, "simple.txt");
		}

		@Test
		public void testBirds() {
			test("birds", "https://www.cs.usfca.edu/~cs212/birds/birds.html", 50, "letters.txt");
		}

		@Test
		public void testGutenberg() {
			test("gutenberg", "https://www.cs.usfca.edu/~cs212/gutenberg/", 5, "gutenberg.txt");
		}

		@Test
		public void testCSS() {
			test("wdgcss", "https://www.cs.usfca.edu/~cs212/wdgcss/properties.html", 60, "letters.txt");
		}

		@Test
		public void testNumpy() {
			test("numpy", "https://www.cs.usfca.edu/~cs212/numpy/user/index.html", 10, "letters.txt");
		}
	}

	@Nested
	public class ExceptionTest {

		@Test
		public void testNegativeLimit() {
			String[] args = {
				"-url", "https://www.cs.usfca.edu/~cs212/birds/birds.html",
				"-limit", "-10"
			};

			assertTimeout(Duration.ofSeconds(30), () -> {
				TestUtilities.checkExceptions(args);
			});
		}

		@Test
		public void testZeroLimit() {
			String[] args = {
				"-url", "https://www.cs.usfca.edu/~cs212/birds/birds.html",
				"-limit", "0"
			};

			assertTimeout(Duration.ofSeconds(30), () -> {
				TestUtilities.checkExceptions(args);
			});
		}

		@Test
		public void testInvalidLimit() {
			String[] args = {
				"-url", "https://www.cs.usfca.edu/~cs212/birds/birds.html",
				"-limit", "pizza"
			};

			assertTimeout(Duration.ofSeconds(30), () -> {
				TestUtilities.checkExceptions(args);
			});
		}

		@Test
		public void testDefaultLimit() {
			String[] args = {
				"-url", "https://www.cs.usfca.edu/~cs212/birds/birds.html",
				"-limit"
			};

			assertTimeout(Duration.ofSeconds(30), () -> {
				TestUtilities.checkExceptions(args);
			});
		}

		@Test
		public void testGoneURL() {
			String[] args = {
				"-url", "https://www.cs.usfca.edu/~cs212/redirect/gone",
				"-limit", "1"
			};

			assertTimeout(Duration.ofSeconds(30), () -> {
				TestUtilities.checkExceptions(args);
			});
		}

		@Test
		public void testMissingURL() {
			String[] args = {
				"-url",
				"-limit", "1"
			};

			assertTimeout(Duration.ofSeconds(30), () -> {
				TestUtilities.checkExceptions(args);
			});
		}

		@Test
		public void testInvalidURL() {
			String[] args = {
				"-url", "https://www.cs.usfca.edu/~cs212/\0null",
				"-limit", "1"
			};

			assertTimeout(Duration.ofSeconds(30), () -> {
				TestUtilities.checkExceptions(args);
			});
		}
	}
}
