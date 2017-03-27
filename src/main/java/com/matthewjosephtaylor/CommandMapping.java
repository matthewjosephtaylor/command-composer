package com.matthewjosephtaylor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.google.common.collect.ImmutableList;

public class CommandMapping {
	public enum SpecialCanSee {
		CWD,
		HOME,
		NOTHING;

		public static Optional<SpecialCanSee> optionalValueOf(final String canSee) {
			return Arrays.stream(values()).filter(scc -> scc.name().equalsIgnoreCase(canSee)).findFirst();
		}
	}

	public final String hostCommandName;
	public static final String CONTAINER_COMMAND = "container-command";
	public final String containerCommandName;
	public static final String CAN_SEE = "can-see";
	public final String canSee;
	public static final String ALSO_SEE = "also-see";
	public final ImmutableList<String> alsoSee;
	public static final String STATEFUL = "stateful";
	public final boolean stateful;
	public static final String IMAGE = "image";
	public final String imageName;

	public CommandMapping(final String hostCommandName, final String containerCommandName, final String canSee,
			final Iterable<String> alsoSee, final boolean stateful, final String imageName) {
		this.hostCommandName = hostCommandName;
		this.containerCommandName = containerCommandName;
		this.canSee = canSee;
		this.alsoSee = ImmutableList.copyOf(alsoSee);
		this.stateful = stateful;
		this.imageName = imageName;
	}

	public CommandMapping(final String hostCommandName, final Map<String, ? super Object> mappingValues) {
		this.hostCommandName = hostCommandName;
		containerCommandName = (String) mappingValues.getOrDefault(CONTAINER_COMMAND, hostCommandName);
		canSee = (String) mappingValues.getOrDefault(CAN_SEE, SpecialCanSee.CWD.name());
		alsoSee = ImmutableList
				.copyOf((Iterable<String>) mappingValues.getOrDefault(ALSO_SEE, Collections.emptyList()));
		stateful = YamlParser.toBoolean(mappingValues.getOrDefault(STATEFUL, Boolean.FALSE));
		imageName = (String) mappingValues.getOrDefault(IMAGE, hostCommandName);
	}



	//docker run -i -w="${HOST_CWD}" -e "HOME=${HOME}" --mac-address="${MAC_ADDR}" -v "${HOME}:${HOME}" "${IMAGE_NAME}" $@
	private static String DOCKER_RUN = "docker run -it --rm -w=\"${PWD}\" -e \"HOME=${HOME}\" %s \"%s\" %s";
	private static String SHELL_ALIAS = "alias %s='%s'";
	private static String EXECUTABLE = "#!/usr/bin/env sh\n%s $@";

	public String shellAlias() {
		return String.format(SHELL_ALIAS, hostCommandName, dockerCommand());
	}

	public void writeExecutable(final File outputDirectory) {
		final File outputFile = new File(outputDirectory.getAbsolutePath() + "/" + hostCommandName);
		try (final PrintWriter printWriter = new PrintWriter(outputFile)) {
			printWriter.write(String.format(EXECUTABLE, dockerCommand()));
			outputFile.setExecutable(true);
		} catch (final FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private String dockerCommand() {
		final String result;
		if (stateful) {
			throw new UnsupportedOperationException("Stateful support not yet implmented.");
		} else {
			result = String.format(DOCKER_RUN, createDockerVolumeParameters(canSee, alsoSee), imageName,
					containerCommandName);
		}
		return result;
	}

	private static String createDockerVolumeParameters(final String canSee, final List<String> alsoSee) {
		return Stream.of(createCanSeeVolumeParameter(canSee), createAlsoSeeValueParameter(alsoSee))
				.collect(Collectors.joining(" "));
	}

	private static String createAlsoSeeValueParameter(final List<String> alsoSee) {
		return alsoSee.stream().map(as -> String.format("-v \"%s:%s\"", as, as)).collect(Collectors.joining(" "));
	}

	private static String createCanSeeVolumeParameter(final String canSee) {
		final String result;
		final Optional<SpecialCanSee> optionalSpecialCanSee = SpecialCanSee.optionalValueOf(canSee);
		if (optionalSpecialCanSee.isPresent()) {
			switch (optionalSpecialCanSee.get()) {
				case NOTHING:
					result = "";
					break;
				case CWD:
					result = "-v \"${PWD}:${PWD}\"";
					break;
				case HOME:
					result = "-v \"${HOME}:${HOME}\"";
					break;
				default:
					throw new RuntimeException(
							"No support for special can-see of type: " + optionalSpecialCanSee.get());
			}
		} else {
			try {
				final int directoryStepCount = Integer.valueOf(canSee) * -1;
				final String directorySteps = Stream.generate(() -> 1).limit(directoryStepCount).map(x -> "../")
						.collect(Collectors.joining());
				//(cd ../../; pwd)
				result = String.format("-v \"$(cd %s;pwd):$(cd %s;pwd)\"", directorySteps, directorySteps);

			} catch (final NumberFormatException e) {
				throw new RuntimeException(
						"Error parsing can-see. Expected number in range [0,-infinity], found: " + canSee);
			}
		}
		return result;
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
