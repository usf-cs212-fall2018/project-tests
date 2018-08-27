import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;

import org.junit.jupiter.api.Assertions;

public class TestUtilities {

	private static final String ERROR_FORMAT = "%nActual File:%n    %s%nExpected File:%n    %s%nArguments:%n    %s%nMessage:%n    %s%n";

	public static final Path ACTUAL_PATH = Paths.get("out");
	public static final Path EXPECTED_PATH = Paths.get("expected");

	/**
	 * Produces debug-friendly output when a JUnit test fails.
	 *
	 * @param test     name of test case
	 * @param expected path to expected output
	 * @param args     original arguments sent to {@link Driver}
	 * @param message  error message with more details
	 * @return formatted error message
	 */
	public static String errorMessage(Path actual, Path expected, String[] args,
			String message) {
		return String.format(ERROR_FORMAT, actual.toString(), expected.toString(),
				String.join(" ", args), message);
	}

	/**
	 * Checks whether environment setup is correct, with a input and output
	 * directory located within the base directory.
	 *
	 * @return true if expected paths are found and readable/writable
	 */
	public static boolean isEnvironmentSetup() {
		try {
			Files.createDirectories(ACTUAL_PATH);
		}
		catch (IOException e) {
			System.err.println("Unable to create actual output directory.");
			return false;
		}

		return Files.isReadable(EXPECTED_PATH)
				&& Files.isWritable(ACTUAL_PATH);
	}

	/**
	 * Checks line-by-line if two files are equal. If one file contains extra
	 * blank lines at the end of the file, the two are still considered equal.
	 * Works even if the path separators in each file are different.
	 *
	 * @param path1 path to first file to compare with
	 * @param path2 path to second file to compare with
	 * @return positive value if two files are equal, negative value if not
	 *
	 * @throws IOException
	 */
	public static int checkFiles(Path path1, Path path2) throws IOException {
		Charset charset = StandardCharsets.UTF_8;
		String separator = Matcher.quoteReplacement(File.separator);

		// used to output line mismatch
		int count = 0;

		try (BufferedReader reader1 = Files.newBufferedReader(path1, charset);
				BufferedReader reader2 = Files.newBufferedReader(path2, charset);) {
			String line1 = reader1.readLine();
			String line2 = reader2.readLine();

			while (true) {
				count++;

				// compare lines until we hit a null (i.e. end of file)
				if ((line1 != null) && (line2 != null)) {
					// use consistent path separators
					line1 = line1.replaceAll(separator, "/");
					line2 = line2.replaceAll(separator, "/");

					// remove trailing spaces
					line1 = line1.trim();
					line2 = line2.trim();

					// check if lines are equal
					if (!line1.equals(line2)) {
						return -count;
					}

					// read next lines if we get this far
					line1 = reader1.readLine();
					line2 = reader2.readLine();
				}
				else {
					// discard extra blank lines at end of reader1
					while ((line1 != null) && line1.trim().isEmpty()) {
						line1 = reader1.readLine();
					}

					// discard extra blank lines at end of reader2
					while ((line2 != null) && line2.trim().isEmpty()) {
						line2 = reader2.readLine();
					}

					if (line1 == line2) {
						// only true if both are null, otherwise one file had
						// extra non-empty lines
						return count;
					}
					else {
						// extra blank lines found in one file
						return -count;
					}
				}
			}
		}
	}

	/**
	 * Checks whether {@link Driver} generates the expected output without any
	 * exceptions. Will print the stack trace if an exception occurs. Designed to
	 * be used within an unit test. If the test was successful, deletes the actual
	 * file. Otherwise, keeps the file for debugging purposes.
	 *
	 * @param test   name of tests for debugging
	 * @param actual path to actual output
	 * @param expect path to expected output
	 * @param args   arguments to pass to {@link Driver}
	 */
	public static void checkOutput(Path expected, Path actual, String[] args) {

		try {
			// Remove old actual file (if exists), setup directories if needed
			Files.deleteIfExists(actual);
			Files.createDirectories(actual.getParent());

			// Generate actual output file
			System.out.printf("%nRunning: %s...%n", actual.toString());
			Driver.main(args);

			// Double-check we can read the expected output file
			if (!Files.isReadable(expected)) {
				String message = "Unable to read expected output file.";
				Assertions.fail(errorMessage(actual, expected, args, message));
			}

			// Double-check we can read the actual output file
			if (!Files.isReadable(actual)) {
				String message = "Unable to read actual output file.";
				Assertions.fail(errorMessage(actual, expected, args, message));
			}

			// Compare the two files
			int count = checkFiles(actual, expected);

			if (count <= 0) {
				String message = "Difference detected on line: " + -count + ".";
				Assertions.fail(errorMessage(actual, expected, args, message));
			}

			// At this stage, the files were the same and we can delete actual.
			Files.deleteIfExists(actual);
		}
		catch (Exception e) {
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));

			String message = writer.toString();
			Assertions.fail(errorMessage(actual, expected, args, message));
		}
	}

	/**
	 * Checks whether {@link Driver} will run without generating any exceptions.
	 * Will print the stack trace if an exception occurs. Designed to be used
	 * within an unit test.
	 *
	 * @param name name of test for debugging
	 * @param args arguments to pass to {@link Driver}
	 */
	public static void checkExceptions(String[] args) {
		try {
			System.out.printf("%nRunning Driver %s...%n", String.join(" ", args));
			Driver.main(args);
		}
		catch (Exception e) {
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));

			String debug = String.format(
					"%nArguments:%n    [%s]%nException:%n    %s%n",
					String.join(" ", args), writer.toString());
			Assertions.fail(debug);
		}
	}

}
