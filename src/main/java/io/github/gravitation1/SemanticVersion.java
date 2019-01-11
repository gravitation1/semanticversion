package io.github.gravitation1;


import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class SemanticVersion implements Comparable<SemanticVersion>
{
	public static final SemanticVersion SEMANTIC_VERSION_VERSION;

	private static final String SEPARATOR;
	private static final String SEPARATOR_REGEX;
	private static final String PRE_RELEASE_DELIMITER;
	private static final String BUILD_METADATA_DELIMITER;
	private static final Pattern COMMON_IDENTIFIER_FORMAT;
	private static final Pattern NUMERICAL_IDENTIFIER_FORMAT;
	private static final Pattern TEXTUAL_IDENTIFIER_CHECK;

	private final String fullVersion;
	private final int major;
	private final int minor;
	private final int patch;
	private final List<String> preReleaseData;
	private final List<String> buildMetadata;


	static
	{
		SEMANTIC_VERSION_VERSION = new SemanticVersion(2, 0, 0);
		SEPARATOR = ".";
		SEPARATOR_REGEX = "\\.";
		PRE_RELEASE_DELIMITER = "-";
		BUILD_METADATA_DELIMITER = "+";
		COMMON_IDENTIFIER_FORMAT = Pattern.compile("^[0-9A-Za-z-]+$");
		NUMERICAL_IDENTIFIER_FORMAT = Pattern.compile("^([0-9]|[1-9][0-9]+)$");
		TEXTUAL_IDENTIFIER_CHECK = Pattern.compile("^.*[A-Za-z-]+.*$");
	}


	public SemanticVersion(
		final int major)
	{
		this(major, 0);
	}


	public SemanticVersion(
		final int major,
		final int minor)
	{
		this(major, minor, 0);
	}


	public SemanticVersion(
		final int major,
		final int minor,
		final int patch)
	{
		this(major, minor, patch, new LinkedList<>(), new LinkedList<>());
	}


	public SemanticVersion(
		final int major,
		final int minor,
		final int patch,
		/* @Nonnull */ final List<String> preReleaseData,
		/* @Nonnull */ final List<String> buildMetadata)
	{
		this.major = checkMajorVersionNumber(major);
		this.minor = checkMinorVersionNumber(minor);
		this.patch = checkPatchVersionNumber(patch);
		this.preReleaseData =
			Collections
				.unmodifiableList(
					preReleaseData
						.stream()
						.map(SemanticVersion::checkPreReleaseIdentifier)
						.collect(Collectors.toList()));
		this.buildMetadata =
			Collections
				.unmodifiableList(
					buildMetadata
						.stream()
						.map(SemanticVersion::checkBuildMetadataIdentifier)
						.collect(Collectors.toList()));
		final StringBuilder stringBuilder = new StringBuilder();
			stringBuilder
				.append(this.getMajor())
				.append(SEPARATOR)
				.append(this.getMinor())
				.append(SEPARATOR)
				.append(this.getPatch());

		if (!this.getPreReleaseData().isEmpty())
		{
			stringBuilder
				.append(PRE_RELEASE_DELIMITER)
				.append(String.join(".", this.getPreReleaseData()));
		}

		if (!this.getBuildMetadata().isEmpty())
		{
			stringBuilder
				.append(BUILD_METADATA_DELIMITER)
				.append(String.join(".", this.getBuildMetadata()));
		}

		this.fullVersion = stringBuilder.toString();
	}


	/**
	 * @return Returns true if this version is unstable as per https://semver.org/#spec-item-4
	 */
	boolean isUnstable()
	{
		return 0 == this.major;
	}


	public int getMajor()
	{
		return this.major;
	}


	public int getMinor()
	{
		return this.minor;
	}


	public int getPatch()
	{
		return this.patch;
	}


	/* @Nonnull */
	public List<String> getPreReleaseData()
	{
		return this.preReleaseData;
	}


	/* @Nonnull */
	public List<String> getBuildMetadata()
	{
		return this.buildMetadata;
	}


	/**
	 * Implements ordering as per https://semver.org/#spec-item-11
	 *
	 * Note: This class has a natural ordering that is inconsistent with
	 *       equals. This is due to build metadata not participating in
	 *       version precedence, as per https://semver.org/#spec-item-10.
	 *       If equals was implemented in terms of compareTo, then when a
	 *       SemanticVersion would be used in a collection such as a hash map,
	 *       we may see unusual behavior for versions that had different
	 *       build metadata, but were otherwise identical.
	 *
	 * @param other The other semantic version to compare against.
	 * @return Returns an arbitrary negative number if this semantic version
	 *         has a lower precedence than the other semantic version. Returns
	 *         an arbitrary positive number if this semantic version has a
	 *         higher precedence than the other semantic version. Returns 0
	 *         if both semantic versions have the same precedence.
	 */
	@Override
	public int compareTo(
		/* @Nonnull */ final SemanticVersion other)
	{
		final int majorDiff = this.major - other.major;

		if (0 != majorDiff)
		{
			return majorDiff;
		}

		final int minorDiff = this.minor - other.minor;

		if (0 != minorDiff)
		{
			return minorDiff;
		}

		final int patchDiff = this.patch - other.patch;

		if (0 != patchDiff)
		{
			return patchDiff;
		}

		if (this.getPreReleaseData().isEmpty() && !other.getPreReleaseData().isEmpty())
		{
			return 1;
		}
		else if (!this.getPreReleaseData().isEmpty() && other.getPreReleaseData().isEmpty())
		{
			return -1;
		}

		int longerSize =
			this.preReleaseData.size() > other.preReleaseData.size()
				? this.preReleaseData.size()
				: other.preReleaseData.size();

		for (int i = 0; i < longerSize; ++i)
		{
			final String thisIdentifier = this.preReleaseData.size() > i ? this.preReleaseData.get(i) : null;
			final String otherIdentifier = other.preReleaseData.size() > i ? other.preReleaseData.get(i) : null;

			if (null != thisIdentifier && null == otherIdentifier)
			{
				return 1;
			}
			else if (null == thisIdentifier && null != otherIdentifier)
			{
				return -1;
			}
			// Both identifiers will never be both null, since that would have
			// terminated the for loop.
			final boolean thisIdentifierIsNumber = NUMERICAL_IDENTIFIER_FORMAT.matcher(thisIdentifier).matches();
			final boolean otherIdentifierIsNumber = NUMERICAL_IDENTIFIER_FORMAT.matcher(otherIdentifier).matches();

			if (thisIdentifierIsNumber && otherIdentifierIsNumber)
			{
				final int numericalDiff = Integer.parseInt(thisIdentifier) - Integer.parseInt(otherIdentifier);

				if (0 != numericalDiff)
				{
					return numericalDiff;
				}
			}
			else if (otherIdentifierIsNumber)
			{
				return 1;
			}
			else if (thisIdentifierIsNumber)
			{
				return -1;
			}
			else
			{
				final int textualDiff = thisIdentifier.compareTo(otherIdentifier);

				if (0 != textualDiff)
				{
					return textualDiff;
				}
			}
		}

		return 0;
	}


	@Override
	/* @Nonnull */
	public String toString()
	{
		return this.fullVersion;
	}


	@Override
	public int hashCode()
	{
		return this.fullVersion.hashCode();
	}


	/**
	 * Note: This class has a natural ordering that is inconsistent with
	 *       equals. This is due to build metadata not participating in
	 *       version precedence, as per https://semver.org/#spec-item-10.
	 *       If equals was implemented in terms of compareTo, then when a
	 *       SemanticVersion would be used in a collection such as a hash map,
	 *       we may see unusual behavior for versions that had different
	 *       build metadata, but were otherwise identical.
	 *
	 * @param other The version to compare to.
	 * @return Returns true IFF both versions have identical string
	 *         representations.
	 */
	@Override
	public boolean equals(
		final Object other)
	{
		if (this == other)
		{
			return true;
		}
		else if (other instanceof SemanticVersion)
		{
			return this.fullVersion.equals(((SemanticVersion) other).fullVersion);
		}
		else
		{
			return false;
		}
	}


	/* @Nonnull */
	public static SemanticVersion from(
		/* @Nonnull */ final String semanticVersionString)
	{
		final List<String> buildMetadata;
		final String withoutBuildMetadata;

		{
			final int buildMetadataStart = semanticVersionString.indexOf(BUILD_METADATA_DELIMITER);

			if (-1 != buildMetadataStart)
			{
				buildMetadata =
					Arrays.asList(
						semanticVersionString
							.substring(buildMetadataStart + 1)
							.split(SEPARATOR_REGEX));
				withoutBuildMetadata = semanticVersionString.substring(0, buildMetadataStart);
			}
			else
			{
				buildMetadata = new LinkedList<>();
				withoutBuildMetadata = semanticVersionString;
			}
		}

		final List<String> preReleaseData;
		final String baseVersion;

		{
			final int prereleaseStart = withoutBuildMetadata.indexOf(PRE_RELEASE_DELIMITER);

			if (-1 != prereleaseStart)
			{
				preReleaseData =
					Arrays.asList(
						withoutBuildMetadata
							.substring(prereleaseStart + 1)
							.split(SEPARATOR_REGEX));
				baseVersion = withoutBuildMetadata.substring(0, prereleaseStart);
			}
			else
			{
				preReleaseData = new LinkedList<>();
				baseVersion = withoutBuildMetadata;
			}
		}

		final List<String> baseVersionIdentifiers = Arrays.asList(baseVersion.split(SEPARATOR_REGEX));

		if (3 != baseVersionIdentifiers.size())
		{
			throw new InvalidBaseFormatException();
		}

		return new SemanticVersion(
			parseMajorVersionNumber(baseVersionIdentifiers.get(0)),
			parseMinorVersionNumber(baseVersionIdentifiers.get(1)),
			parsePatchVersionNumber(baseVersionIdentifiers.get(2)),
			preReleaseData,
			buildMetadata);
	}


	private static int parseMajorVersionNumber(
		/* @Nonnull */ final String majorVersion)
	throws
		IllegalMajorVersion
	{
		if (!NUMERICAL_IDENTIFIER_FORMAT.matcher(majorVersion).matches())
		{
			throw new IllegalMajorVersion();
		}

		return Integer.parseInt(majorVersion);
	}


	private static int parseMinorVersionNumber(
		/* @Nonnull */ final String minorVersion)
	throws
		IllegalMinorVersion
	{
		if (!NUMERICAL_IDENTIFIER_FORMAT.matcher(minorVersion).matches())
		{
			throw new IllegalMinorVersion();
		}

		return Integer.parseInt(minorVersion);
	}


	private static int parsePatchVersionNumber(
		/* @Nonnull */ final String patchVersion)
	throws
		IllegalPatchVersion
	{
		if (!NUMERICAL_IDENTIFIER_FORMAT.matcher(patchVersion).matches())
		{
			throw new IllegalPatchVersion();
		}

		return Integer.parseInt(patchVersion);
	}


	private static int checkMajorVersionNumber(
		final int majorVersionNumber)
	throws
		IllegalMajorVersion
	{
		if (0 > majorVersionNumber)
		{
			throw new IllegalMajorVersion();
		}

		return majorVersionNumber;
	}


	private static int checkMinorVersionNumber(
		final int minorVersionNumber)
	throws
		IllegalMinorVersion
	{
		if (0 > minorVersionNumber)
		{
			throw new IllegalMinorVersion();
		}

		return minorVersionNumber;
	}


	private static int checkPatchVersionNumber(
		final int patchVersionNumber)
	throws
		IllegalPatchVersion
	{
		if (0 > patchVersionNumber)
		{
			throw new IllegalPatchVersion();
		}

		return patchVersionNumber;
	}


	/* @Nonnull */
	private static String checkPreReleaseIdentifier(
		/* @Nonnull */ final String identifier)
	throws
		IllegalPreReleaseIdentifier
	{
		if (!COMMON_IDENTIFIER_FORMAT.matcher(identifier).matches())
		{
			throw new IllegalPreReleaseIdentifier();
		}
		else if (TEXTUAL_IDENTIFIER_CHECK.matcher(identifier).matches())
		{
			return identifier;
		}
		else if (!NUMERICAL_IDENTIFIER_FORMAT.matcher(identifier).matches())
		{
			throw new IllegalPreReleaseIdentifier();
		}
		else
		{
			return identifier;
		}
	}


	/* @Nonnull */
	private static String checkBuildMetadataIdentifier(
		/* @Nonnull */ final String identifier)
	throws
		IllegalBuildMetadataIdentifier
	{
		if (!COMMON_IDENTIFIER_FORMAT.matcher(identifier).matches())
		{
			throw new IllegalBuildMetadataIdentifier();
		}
		else
		{
			return identifier;
		}
	}


	public static class SemanticVersionException extends RuntimeException
	{
	}


	public static class InvalidBaseFormatException extends SemanticVersionException
	{
	}


	public static class IllegalMajorVersion extends SemanticVersionException
	{
	}


	public static class IllegalMinorVersion extends SemanticVersionException
	{
	}


	public static class IllegalPatchVersion extends SemanticVersionException
	{
	}


	public static class IllegalPreReleaseIdentifier extends SemanticVersionException
	{
	}


	public static class IllegalBuildMetadataIdentifier extends SemanticVersionException
	{
	}
}
