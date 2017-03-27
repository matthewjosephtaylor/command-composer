package com.matthewjosephtaylor;

import java.io.FileReader;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import com.matthewjosephtaylor.CompositionGroup.CommandType;

public class YamlParser {

	public static CompositionGroup parse(final CommandLine commandLine, final FileReader fileReader) {
		try {
			final YamlReader yamlReader = new YamlReader(fileReader);
			final Object topObject = yamlReader.read();
			final String name = Optional.ofNullable(commandLine.name)
					.orElse(getOptionalValueFromMap(topObject, CompositionGroup.NAME,
							String.class).orElse(CompositionGroup.DEFAULT_NAME));

			final String executablesDir = Optional.ofNullable(commandLine.outputDirectory)
					.orElse(getOptionalValueFromMap(topObject, CompositionGroup.EXECUTABLES_DIR,
							String.class).orElse(CompositionGroup.DEFAULT_EXECUTABLES_DIR));

			final Boolean persist = Optional.ofNullable(commandLine.persist)
					.orElse(getOptionalValueFromMap(topObject, CompositionGroup.PERSIST,
							Boolean.class).orElse(CompositionGroup.DEFAULT_PERSIST));

			final CommandType commandType = Optional.ofNullable(commandLine.executables).orElse(false)
					? CommandType.EXECUTABLE : getOptionalValueFromMap(topObject, CompositionGroup.PERSIST,
							CommandType.class).orElse(CompositionGroup.DEFAULT_COMMAND_TYPE);

			final List<CommandMapping> commandMappings = parseCommands(
					getValueFromMap(topObject, CompositionGroup.COMMANDS, Map.class));

			return new CompositionGroup(name,
					executablesDir,
					commandType,
					persist, commandMappings);
		} catch (final YamlException e) {
			throw new RuntimeException(e);
		}
	}

	private static Object assertObjectNotNull(final Object object, final String errorSubject) {
		if (object == null) {
			throw new RuntimeException("No value found for: " + errorSubject);
		}
		return object;
	}

	private static <T> T assertObjectOfType(final Object object, final Class<T> clazz, final String errorSubject) {
		assertObjectNotNull(object, errorSubject);
		if (clazz.isAssignableFrom(object.getClass())) {
			return (T) object;
		}
		throw new RuntimeException(
				"Unexptected type for: " + errorSubject + " type: " + object.getClass().getTypeName());
	}

	private static <T> Optional<T> getOptionalValueFromMap(final Object object, final String key,
			final Class<T> clazz) {
		final Map<Object, Object> map = assertObjectOfType(object, Map.class, key);
		final Object value = map.get(key);
		if (value == null) {
			return Optional.empty();
		}
		if (clazz.isAssignableFrom(value.getClass())) {
			return (Optional<T>) Optional.of(value);
		} else if (clazz.isAssignableFrom(Boolean.class)) {
			return (Optional<T>) Optional.of(toBoolean(value));
		} else if (clazz.isAssignableFrom(CommandType.class)) {
			return (Optional<T>) Optional.of(CommandType.valueOf((String) value));
		} else {
			throw new RuntimeException("Unexpected type for: " + key + " type: " + value.getClass().getTypeName());
		}

	}

	private static <T> T getValueFromMap(final Object object, final String key, final Class<T> clazz) {
		final Map<Object, Object> map = assertObjectOfType(object, Map.class, key);
		final Object value = map.get(key);
		assertObjectNotNull(value, key);
		if (clazz.isAssignableFrom(object.getClass())) {
			return (T) value;
		} else {
			throw new RuntimeException("Unexpected type for: " + key + " type: " + value.getClass().getTypeName());
		}
	}

	private static CommandMapping parseCommandMapping(final Entry<String, Object> kvp) {
		final String hostCommandName = kvp.getKey();
		final Map<String, Object> mappingValues = assertObjectOfType(kvp.getValue(), Map.class, hostCommandName);
		return new CommandMapping(hostCommandName, mappingValues);

	}

	private static List<CommandMapping> parseCommands(final Map<String, Object> commandMap) {
		return commandMap.entrySet().stream().map(kvp -> parseCommandMapping(kvp))
				.collect(Collectors.toList());
	}

	public static boolean toBoolean(final Object object) {
		boolean result;
		if (object instanceof String) {
			final String string = (String) object;
			result = Boolean.parseBoolean(string);
		} else if (object instanceof Boolean) {
			result = (boolean) object;
		} else {
			throw new RuntimeException("Unable to determine boolean value for: " + object);
		}
		return result;
	}

}
