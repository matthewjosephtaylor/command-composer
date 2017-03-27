package com.matthewjosephtaylor;

import java.util.List;
import java.util.function.Consumer;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

public class CommandLine {
	public static final String DEFAULT_YAML_FILE = "command-compose.yml";

	@Parameter(description = "command-name[:container-command-name] [image-name] [can-see] [add-dir ...]")
	public List<String> unparsedArguments;

	@Parameter(names = { "-h", "--help" }, description = "Print Help", help = true)
	public boolean help;

	@Parameter(names = { "--version" }, description = "Print version information")
	public boolean version;

	@Parameter(names = { "-v", "--verbose" }, description = "Verbose output")
	public boolean verbose;

	@Parameter(names = { "-f",
			"--file FILE" }, description = "Specify an alternate compose file (default: " + DEFAULT_YAML_FILE + ")")
	public String file;

	@Parameter(names = { "-e", "--executables" }, description = "Output exectutables (default: shell aliases)")
	public boolean executables;

	@Parameter(names = { "-o", "--out" }, description = "Directory to output executable commands (default: "
			+ CompositionGroup.DEFAULT_EXECUTABLES_DIR + "). Setting this will force --executables")
	public String outputDirectory;

	@Parameter(names = { "-p",
			"--persist" }, description = "Persist composition to default envornment in current working directory named "
					+ CompositionGroup.DEFAULT_NAME)
	public Boolean persist;

	@Parameter(names = { "-n",
			"--name" }, description = "Name of envionment (default: .command-commpose). Setting this will force --persist")
	public String name;

	private CommandLine() {
	}

	private Consumer<Integer> usageAndExit;

	private void setUsageAndExitOperation(final Consumer<Integer> usageAndExit) {
		this.usageAndExit = usageAndExit;
	}

	public void usageAndExit(final int statusCode) {
		usageAndExit.accept(statusCode);
	}

	public static CommandLine parseArgv(final String programName, final String[] args) {
		final CommandLine commandLine = new CommandLine();
		try {
			final JCommander jcommander = new JCommander(commandLine);
			commandLine.setUsageAndExitOperation(status -> {
				jcommander.usage();
				System.exit(status);
			});
			jcommander.setProgramName(programName);
			jcommander.setAcceptUnknownOptions(false);
			jcommander.setAllowAbbreviatedOptions(true);
			jcommander.setCaseSensitiveOptions(false);
			jcommander.setColumnSize(999);
			jcommander.parse(args);
			if (commandLine.help) {
				commandLine.usageAndExit(-1);
			}
		} catch (final ParameterException pe) {
			System.out.println(pe.getMessage());
			System.exit(-1);

		}
		return commandLine;
	}
}
