package com.matthewjosephtaylor.composer.parsers;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.matthewjosephtaylor.composer.CommandLine;
import com.matthewjosephtaylor.composer.values.CommandMapping;
import com.matthewjosephtaylor.composer.values.CompositionGroup;
import com.matthewjosephtaylor.composer.values.CompositionGroup.CommandType;

public class CommandLineParser {

	//[j9:java, java:9, HOME, /var/tmp/stuff]
	public static CompositionGroup parse(final CommandLine commandLine) {

		final String name = Optional.ofNullable(commandLine.name).orElse(CompositionGroup.DEFAULT_NAME);

		final String executablesDir = Optional.ofNullable(commandLine.outputDirectory)
				.orElse(CompositionGroup.DEFAULT_EXECUTABLES_DIR);

		final boolean persist = Optional.ofNullable(commandLine.name).isPresent() ? true
				: Optional.of(commandLine.persist).orElse(CompositionGroup.DEFAULT_PERSIST);

		final CommandType commandType = commandLine.executables
				|| Optional.ofNullable(commandLine.outputDirectory).isPresent() ? CommandType.EXECUTABLE
						: CommandType.ALIAS;
		
		final Iterable<String> ports = Splitter.on(",").split(Optional.ofNullable(commandLine.ports).orElse(""));

		return new CompositionGroup(name, executablesDir, commandType, persist,
				Lists.newArrayList((parseUnparsedArguments(ports, commandLine.unparsedArguments))));
	}

	private static CommandMapping parseUnparsedArguments(final Iterable<String> ports, final List<String> unparsedArguments) {

		if ((unparsedArguments == null) || unparsedArguments.isEmpty()) {
			throw new RuntimeException("Unable to determine command name");
		}
		final Map<String, ? super Object> mappingValues = Maps.newHashMap();
		
		mappingValues.put(CommandMapping.PORTS, ports);

		final List<String> splitFirstArgument = Splitter.on(":").splitToList(unparsedArguments.get(0));
		final String hostCommandName = splitFirstArgument.get(0);

		if (splitFirstArgument.size() > 1) {
			mappingValues.put(CommandMapping.CONTAINER_COMMAND, splitFirstArgument.get(1));

		}

		if (unparsedArguments.size() > 1) {
			mappingValues.put(CommandMapping.IMAGE, unparsedArguments.get(1));
		}

		if (unparsedArguments.size() > 2) {
			mappingValues.put(CommandMapping.CAN_SEE, unparsedArguments.get(2));
		}

		if (unparsedArguments.size() > 3) {
			mappingValues.put(CommandMapping.ALSO_SEE, unparsedArguments.stream().skip(3).collect(Collectors.toList()));
		}

		return new CommandMapping(hostCommandName, mappingValues);
	}

}
