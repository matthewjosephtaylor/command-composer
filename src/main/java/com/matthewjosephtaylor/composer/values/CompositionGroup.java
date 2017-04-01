package com.matthewjosephtaylor.composer.values;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.google.common.collect.ImmutableList;

public class CompositionGroup {

	public enum CommandType {
		ALIAS,
		EXECUTABLE
	}

	public static final String NAME = "name";
	public static final String DEFAULT_NAME = "composed-commands";
	public final String name;

	public static final String EXECUTABLES_DIR = "executables-dir";
	public static final String DEFAULT_EXECUTABLES_DIR = "./";
	public final String executablesDir;

	public static final String COMMAND_TYPE = "command-type";
	public static final CommandType DEFAULT_COMMAND_TYPE = CommandType.ALIAS;
	public final CommandType commandType;

	public static final String PERSIST = "persist";
	public static final boolean DEFAULT_PERSIST = false;
	public final boolean persist;

	public static final String COMMANDS = "commands";
	public final ImmutableList<CommandMapping> commands;

	public CompositionGroup(final String name, final String executablesDir, final CommandType commandType,
			final boolean persist, final Iterable<CommandMapping> commands) {
		this.name = name;
		this.executablesDir = executablesDir;
		this.commandType = commandType;
		this.persist = persist;
		this.commands = ImmutableList.copyOf(commands);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, false);
	}

	@Override
	public boolean equals(final Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj, false);
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
