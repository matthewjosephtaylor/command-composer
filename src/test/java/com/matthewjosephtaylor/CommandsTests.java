package com.matthewjosephtaylor;

import static com.matthewjosephtaylor.CompositionGroup.DEFAULT_COMMAND_TYPE;
import static com.matthewjosephtaylor.CompositionGroup.DEFAULT_EXECUTABLES_DIR;
import static com.matthewjosephtaylor.CompositionGroup.DEFAULT_NAME;
import static com.matthewjosephtaylor.CompositionGroup.DEFAULT_PERSIST;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Collections;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.esotericsoftware.yamlbeans.YamlException;
import com.google.common.collect.ImmutableList;

public class CommandsTests {

	private static final Logger logger = Logger.getLogger(CommandsTests.class);

	@Test
	public void testYamlDeserialization() throws FileNotFoundException, YamlException {
		final java.net.URL url = this.getClass().getResource("/test-1.yml");
		final FileReader fileReader = new FileReader(url.getFile());
		final CompositionGroup commands = YamlParser.parse(CommandLine.parseArgv("test", new String[0]), fileReader);
		final CompositionGroup expected;
		{
			final CommandMapping commandMapping1 = new CommandMapping("java9", "java", "-2",
					ImmutableList.of("/var/data/pgdata", "/var/output/logs"), false, "java:9");
			final CommandMapping commandMapping2 = new CommandMapping("yarn", "yarn",
					CommandMapping.SpecialCanSee.HOME.name(),
					Collections.emptyList(), false, "my-special-yarn-image:latest");

			expected = new CompositionGroup(DEFAULT_NAME, DEFAULT_EXECUTABLES_DIR, DEFAULT_COMMAND_TYPE,
					DEFAULT_PERSIST, ImmutableList.of(commandMapping1, commandMapping2));
		}
		assertThat(commands, equalTo(expected));

	}

	@Test
	public void testDockerCommandGeneration() {
		final CommandMapping commandMapping1 = new CommandMapping("java9", "java", "home",
				ImmutableList.of("/var/data/pgdata", "/var/output/logs"), false, "java:9");
		logger.info(commandMapping1.shellAlias());

		final CommandMapping commandMapping2 = new CommandMapping("java9", "java", "-2",
				ImmutableList.of("/var/data/pgdata", "/var/output/logs"), false, "java:9");
		logger.info(commandMapping2.shellAlias());
	}
}
