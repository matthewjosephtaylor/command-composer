package com.matthewjosephtaylor.composer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;

import org.apache.log4j.Logger;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.matthewjosephtaylor.composer.parsers.CommandLineParser;
import com.matthewjosephtaylor.composer.parsers.YamlParser;
import com.matthewjosephtaylor.composer.values.CompositionGroup;
import com.matthewjosephtaylor.composer.values.CompositionGroup.CommandType;

public class Main {
	private static final Logger logger = Logger.getLogger(Main.class);

	private static final String PATH_FORMAT = "PATH=\"%s:${PATH}\"";

	public static void main(final String[] args) throws Exception {
		SLF4JBridgeHandler.install();
		final CommandLine commandLine = CommandLine.parseArgv("command-composer", args);
		try {
			processCommandLine(commandLine);
		} catch (final Exception e) {
			stderr(commandLine.verbose, e.getMessage(), e);
		}
	}

	private static void processCommandLine(final CommandLine commandLine) {
		if (commandLine.version) {
			System.out.println("Command Composer v" + Main.class.getPackage().getImplementationVersion()
					+ "(" + ResourceBundle.getBundle("buildInfo").getString("buildNumber") + ")");
		}

		final CompositionGroup compositionGroup;
		if (commandLine.file != null) {
			compositionGroup = parseYmlCommandsInFile(commandLine, commandLine.file);
		} else if (new File(CommandLine.DEFAULT_YAML_FILE).isFile()) {
			compositionGroup = parseYmlCommandsInFile(commandLine, CommandLine.DEFAULT_YAML_FILE);
		} else if (commandLine.unparsedArguments != null) {
			compositionGroup = CommandLineParser.parse(commandLine);
		} else {
			commandLine.usageAndExit(-1);
			throw new RuntimeException("Expected program termination");
		}

		processCompositionGroup(commandLine.verbose, compositionGroup);

	}

	private static void processCompositionGroup(final boolean verbose, final CompositionGroup compositionGroup) {
		stdout(verbose, "processing compositionGroup: " + compositionGroup.toString());
		final StringBuilder env = new StringBuilder();
		final UnaryOperator<String> envbuilder = (s) -> {
			env.append(s);
			env.append("\n");
			return s;
		};
		if (compositionGroup.commandType.equals(CommandType.EXECUTABLE)) {
			final File outputDirectory = new File(compositionGroup.executablesDir);
			stdout(verbose, "writing out executables to directory: " + outputDirectory.getAbsolutePath());
			if (!outputDirectory.exists() && !outputDirectory.mkdirs()) {
				throw new RuntimeException("Problem creating output directory: " + outputDirectory.getAbsolutePath());
			}
			compositionGroup.commands.forEach(c -> c.writeExecutable(outputDirectory));
			envbuilder.apply(stdout(String.format(PATH_FORMAT, outputDirectory.getAbsolutePath())));

		} else {
			compositionGroup.commands.forEach(c -> envbuilder.apply(stdout(c.shellAlias())));
		}
		if (compositionGroup.persist) {
			final File persistenceFile = new File("." + compositionGroup.name);
			try (PrintWriter printWriter = new PrintWriter(persistenceFile)) {
				stdout(verbose, "persisting environtment to file: " + persistenceFile.getAbsolutePath());
				printWriter.write(env.toString());
			} catch (final FileNotFoundException e) {
				throw new RuntimeException("Error writing environment file: " + persistenceFile.getAbsolutePath(), e);
			}
		}
	}

	private static CompositionGroup parseYmlCommandsInFile(CommandLine commandLine, final String yamlFile) {
		stdout(commandLine.verbose, "Using yaml file: " + yamlFile);
		try {
			return YamlParser.parse(commandLine, new FileReader(new File(yamlFile)));
		} catch (final FileNotFoundException e) {
			throw new RuntimeException("Unable to find yaml file: " + yamlFile);
		}

	}

	private static String stdout(final String message) {
		System.out.println(message);
		return message;
	}

	private static String stdout(final boolean verbose, final String message) {
		if (verbose) {
			System.out.println(message);
			return message;
		}
		return "";
	}

	private static void stderr(final boolean verbose, final String message, final Exception e) {
		System.err.println(message);
		if (verbose) {
			e.printStackTrace(System.err);
		}
	}
}
