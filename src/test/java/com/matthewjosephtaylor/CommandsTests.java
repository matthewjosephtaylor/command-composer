package com.matthewjosephtaylor;

import static com.matthewjosephtaylor.composer.values.CompositionGroup.DEFAULT_COMMAND_TYPE;
import static com.matthewjosephtaylor.composer.values.CompositionGroup.DEFAULT_EXECUTABLES_DIR;
import static com.matthewjosephtaylor.composer.values.CompositionGroup.DEFAULT_NAME;
import static com.matthewjosephtaylor.composer.values.CompositionGroup.DEFAULT_PERSIST;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Collections;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.esotericsoftware.yamlbeans.YamlException;
import com.google.common.collect.ImmutableList;
import com.matthewjosephtaylor.composer.CommandLine;
import com.matthewjosephtaylor.composer.parsers.YamlParser;
import com.matthewjosephtaylor.composer.values.CommandMapping;
import com.matthewjosephtaylor.composer.values.CompositionGroup;

public class CommandsTests {

	private static final Logger logger = Logger.getLogger(CommandsTests.class);

	@Test
	public void testYamlDeserialization() throws FileNotFoundException, YamlException {
		final java.net.URL url = this.getClass().getResource("/test-1.yml");
		final FileReader fileReader = new FileReader(url.getFile());
		final CompositionGroup yamlCompositionGroup = YamlParser.parse(CommandLine.parseArgv("test", new String[0]),
				fileReader);
		final CompositionGroup expected;
		{
			final CommandMapping commandMapping1 = new CommandMapping("java9", "java", "-2",
					ImmutableList.of("/var/data/pgdata", "/var/output/logs"), false, "java:9", Collections.emptyList());
			final CommandMapping commandMapping2 = new CommandMapping("yarn", "yarn",
					CommandMapping.SpecialCanSee.HOME.name(),
					Collections.emptyList(), false, "my-special-yarn-image:latest", ImmutableList.of("123", "456"));

			expected = new CompositionGroup(DEFAULT_NAME, DEFAULT_EXECUTABLES_DIR, DEFAULT_COMMAND_TYPE,
					DEFAULT_PERSIST, ImmutableList.of(commandMapping1, commandMapping2));
		}
		assertThat(yamlCompositionGroup.commands, equalTo(expected.commands));
		assertThat(yamlCompositionGroup.commandType, equalTo(expected.commandType));
		assertThat(yamlCompositionGroup.executablesDir, equalTo(expected.executablesDir));
		assertThat(yamlCompositionGroup.name, equalTo(expected.name));
		assertThat(yamlCompositionGroup.persist, equalTo(expected.persist));
		assertThat(yamlCompositionGroup, equalTo(expected));

	}

	@Test
	public void testDockerCommandGeneration() {
		final CommandMapping commandMapping1 = new CommandMapping("java9", "java", "home",
				ImmutableList.of("/var/data/pgdata", "/var/output/logs"), false, "java:9", Collections.emptyList());
		logger.info(commandMapping1.shellAlias());
		assertThat(commandMapping1.shellAlias(), equalTo(
				"alias java9='docker run -it --rm -w=\"${PWD}\" -e \"HOME=${HOME}\" -v \"${HOME}:${HOME}\" -v \"/var/data/pgdata:/var/data/pgdata\" -v \"/var/output/logs:/var/output/logs\"  \"java:9\" java'"));

		final CommandMapping commandMapping2 = new CommandMapping("java9", "java", "-2",
				ImmutableList.of("/var/data/pgdata", "/var/output/logs"), false, "java:9",
				ImmutableList.of("9090", "3001"));
		logger.info(commandMapping2.shellAlias());
		assertThat(commandMapping2.shellAlias(),
				equalTo("alias java9='docker run -it --rm -w=\"${PWD}\" -e \"HOME=${HOME}\" -v \"$(cd ../../;pwd):$(cd ../../;pwd)\" -v \"/var/data/pgdata:/var/data/pgdata\" -v \"/var/output/logs:/var/output/logs\" -p \"9090:9090\" -p \"3001:3001\" \"java:9\" java'"));
	}
}
