package io.github.gravitation1;


import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


public class SemanticVersionTest
{
	@Test
	public void constructorsTest()
	{
		final int majorVersion = 1;

		{
			final SemanticVersion semanticVersion = new SemanticVersion(majorVersion);
			Assert.assertEquals(majorVersion, semanticVersion.getMajor());
			Assert.assertEquals(0, semanticVersion.getMinor());
			Assert.assertEquals(0, semanticVersion.getPatch());
		}

		final int minorVersion = 2;

		{
			final SemanticVersion semanticVersion = new SemanticVersion(majorVersion, minorVersion);
			Assert.assertEquals(majorVersion, semanticVersion.getMajor());
			Assert.assertEquals(minorVersion, semanticVersion.getMinor());
			Assert.assertEquals(0, semanticVersion.getPatch());
		}

		final int patchVersion = 3;

		{
			final SemanticVersion semanticVersion = new SemanticVersion(majorVersion, minorVersion, patchVersion);
			Assert.assertEquals(majorVersion, semanticVersion.getMajor());
			Assert.assertEquals(minorVersion, semanticVersion.getMinor());
			Assert.assertEquals(patchVersion, semanticVersion.getPatch());
		}

		final List<String> preReleaseData = Arrays.asList("identifier1", "identifier2");
		final List<String> buildMetadata = Arrays.asList("identifier3", "identifier4");

		{
			final SemanticVersion semanticVersion =
				new SemanticVersion(majorVersion, minorVersion, patchVersion, preReleaseData, buildMetadata);
			Assert.assertEquals(majorVersion, semanticVersion.getMajor());
			Assert.assertEquals(minorVersion, semanticVersion.getMinor());
			Assert.assertEquals(patchVersion, semanticVersion.getPatch());
			Assert.assertEquals(preReleaseData, semanticVersion.getPreReleaseData());
			Assert.assertNotSame(preReleaseData, semanticVersion.getPreReleaseData());
			Assert.assertEquals(buildMetadata, semanticVersion.getBuildMetadata());
			Assert.assertNotSame(buildMetadata, semanticVersion.getBuildMetadata());
		}
	}


	@Test
	public void semanticVersionVersionTest()
	{
		Assert.assertNotNull(SemanticVersion.SEMANTIC_VERSION_VERSION);
		Assert.assertEquals(2, SemanticVersion.SEMANTIC_VERSION_VERSION.getMajor());
		Assert.assertEquals(0, SemanticVersion.SEMANTIC_VERSION_VERSION.getMinor());
		Assert.assertEquals(0, SemanticVersion.SEMANTIC_VERSION_VERSION.getPatch());
		Assert.assertTrue(SemanticVersion.SEMANTIC_VERSION_VERSION.getPreReleaseData().isEmpty());
		Assert.assertTrue(SemanticVersion.SEMANTIC_VERSION_VERSION.getBuildMetadata().isEmpty());
	}


	@Test
	public void toStringTest()
	{
		Assert.assertEquals(
			"1.2.3-identifier1.identifier2+identifier3.identifier4",
			new SemanticVersion(
				1,
				2,
				3,
				Arrays.asList("identifier1", "identifier2"),
				Arrays.asList("identifier3", "identifier4"))
			.toString());
	}


	@Test(expected = SemanticVersion.IllegalMajorVersion.class)
	public void illegalMajorVersionTest()
	{
		new SemanticVersion(-1, 1, 1, new LinkedList<>(), new LinkedList<>());
	}


	@Test(expected = SemanticVersion.IllegalMinorVersion.class)
	public void illegalMinorVersionTest()
	{
		new SemanticVersion(1, -1, 1, new LinkedList<>(), new LinkedList<>());
	}


	@Test(expected = SemanticVersion.IllegalPatchVersion.class)
	public void illegalPatchVersionTest()
	{
		new SemanticVersion(1, 1, -1, new LinkedList<>(), new LinkedList<>());
	}


	@Test(expected = SemanticVersion.IllegalPreReleaseIdentifier.class)
	public void illegalPreReleaseIdentifierTest()
	{
		new SemanticVersion(1, 1, 1, Arrays.asList("!"), new LinkedList<>());
	}


	@Test(expected = SemanticVersion.IllegalBuildMetadataIdentifier.class)
	public void illegalBuildMetadataIdentifierTest()
	{
		new SemanticVersion(1, 1, 1, new LinkedList<>(), Arrays.asList("!"));
	}


	@Test
	public void isUnstableTest()
	{
		Assert.assertTrue(new SemanticVersion(0).isUnstable());
		Assert.assertFalse(new SemanticVersion(1).isUnstable());
	}


	@Test
	public void fromTestFull()
	{
		final SemanticVersion semanticVersion =
			SemanticVersion.from("1.2.3-identifier1.identifier2+identifier3.identifier4");
		Assert.assertEquals(1, semanticVersion.getMajor());
		Assert.assertEquals(2, semanticVersion.getMinor());
		Assert.assertEquals(3, semanticVersion.getPatch());

		final List<String> preReleaseData = semanticVersion.getPreReleaseData();
		Assert.assertEquals(2, preReleaseData.size());
		Assert.assertEquals("identifier1", preReleaseData.get(0));
		Assert.assertEquals("identifier2", preReleaseData.get(1));

		final List<String> buildMetadata = semanticVersion.getBuildMetadata();
		Assert.assertEquals(2, buildMetadata.size());
		Assert.assertEquals("identifier3", buildMetadata.get(0));
		Assert.assertEquals("identifier4", buildMetadata.get(1));
	}



	@Test
	public void fromTestNoPreReleaseData()
	{
		final SemanticVersion semanticVersion = SemanticVersion.from("1.2.3+identifier3.identifier4");
		Assert.assertEquals(1, semanticVersion.getMajor());
		Assert.assertEquals(2, semanticVersion.getMinor());
		Assert.assertEquals(3, semanticVersion.getPatch());

		Assert.assertTrue(semanticVersion.getPreReleaseData().isEmpty());

		final List<String> buildMetadata = semanticVersion.getBuildMetadata();
		Assert.assertEquals(2, buildMetadata.size());
		Assert.assertEquals("identifier3", buildMetadata.get(0));
		Assert.assertEquals("identifier4", buildMetadata.get(1));
	}


	@Test
	public void fromTestNoBuildMetadata()
	{
		final SemanticVersion semanticVersion = SemanticVersion.from("1.2.3-identifier1.identifier2");
		Assert.assertEquals(1, semanticVersion.getMajor());
		Assert.assertEquals(2, semanticVersion.getMinor());
		Assert.assertEquals(3, semanticVersion.getPatch());

		final List<String> preReleaseData = semanticVersion.getPreReleaseData();
		Assert.assertEquals(2, preReleaseData.size());
		Assert.assertEquals("identifier1", preReleaseData.get(0));
		Assert.assertEquals("identifier2", preReleaseData.get(1));

		Assert.assertTrue(semanticVersion.getBuildMetadata().isEmpty());
	}


	@Test
	public void fromTestNoPreReleaseDataAndNoBuildMetadata()
	{
		final SemanticVersion semanticVersion = SemanticVersion.from("1.2.3");
		Assert.assertEquals(1, semanticVersion.getMajor());
		Assert.assertEquals(2, semanticVersion.getMinor());
		Assert.assertEquals(3, semanticVersion.getPatch());

		Assert.assertTrue(semanticVersion.getPreReleaseData().isEmpty());

		Assert.assertTrue(semanticVersion.getBuildMetadata().isEmpty());
	}


	@Test(expected = SemanticVersion.IllegalMajorVersion.class)
	public void fromTestMajorVersionLeadingZeros()
	{
		SemanticVersion.from("01.2.3-4.5+6");
	}


	@Test(expected = SemanticVersion.IllegalMinorVersion.class)
	public void fromTestMinorVersionLeadingZeros()
	{
		SemanticVersion.from("1.02.3-4.5+6");
	}


	@Test(expected = SemanticVersion.IllegalPatchVersion.class)
	public void fromTestPatchVersionLeadingZeros()
	{
		SemanticVersion.from("1.2.03-4.5+6");
	}


	@Test(expected = SemanticVersion.IllegalPreReleaseIdentifier.class)
	public void fromTestPreReleaseIdentifierPart1LeadingZeros()
	{
		SemanticVersion.from("1.2.3-04.5+6");
	}


	@Test(expected = SemanticVersion.IllegalPreReleaseIdentifier.class)
	public void fromTestPreReleaseIdentifierPart2LeadingZeros()
	{
		SemanticVersion.from("1.2.3-4.05+6");
	}


	@Test(expected = SemanticVersion.InvalidBaseFormatException.class)
	public void fromTestInvalidBaseFormatTooManyGroupings()
	{
		SemanticVersion.from("1.2.3.7-4.05+6");
	}


	@Test(expected = SemanticVersion.InvalidBaseFormatException.class)
	public void fromTestInvalidBaseFormatTooFewGroupings()
	{
		SemanticVersion.from("1.2-4.05+6");
	}


	@Test
	public void fromTestBuildMetadataIdentifierLeadingZerosOkay()
	{
		// Leading zeros are okay for the build metadata, as shown in the
		// example "1.0.0-alpha+001" at https://semver.org/#spec-item-10
		Assert.assertNotNull(SemanticVersion.from("1.2.3-4.5+06"));
	}


	@Test
	public void fromTestUsingSemVerExamples()
	{
		// Testing all of the examples given at https://semver.org
		Assert.assertNotNull(SemanticVersion.from("1.0.0"));
		Assert.assertNotNull(SemanticVersion.from("1.0.0-alpha"));
		Assert.assertNotNull(SemanticVersion.from("1.0.0-alpha.1"));
		Assert.assertNotNull(SemanticVersion.from("1.0.0-0.3.7"));
		Assert.assertNotNull(SemanticVersion.from("1.0.0-x.7.z.92"));
		Assert.assertNotNull(SemanticVersion.from("1.0.0-alpha+001"));
		Assert.assertNotNull(SemanticVersion.from("1.0.0+20130313144700"));
		Assert.assertNotNull(SemanticVersion.from("1.0.0-beta+exp.sha.5114f85"));
	}


	@Test
	public void majorVersionPrecedenceTest()
	{
		Assert.assertEquals(
			0,
			SemanticVersion.from("1.0.0-alpha").compareTo(SemanticVersion.from("1.0.0-alpha")));
		Assert.assertTrue(
			0 > SemanticVersion.from("1.0.0-alpha").compareTo(SemanticVersion.from("2.0.0-alpha")));
		Assert.assertTrue(
			0 < SemanticVersion.from("2.0.0-alpha").compareTo(SemanticVersion.from("1.0.0-alpha")));
	}


	@Test
	public void minorVersionPrecedenceTest()
	{
		Assert.assertEquals(
			0,
			SemanticVersion.from("1.1.0-alpha").compareTo(SemanticVersion.from("1.1.0-alpha")));
		Assert.assertTrue(
			0 > SemanticVersion.from("1.1.0-alpha").compareTo(SemanticVersion.from("1.2.0-alpha")));
		Assert.assertTrue(
			0 < SemanticVersion.from("1.2.0-alpha").compareTo(SemanticVersion.from("1.1.0-alpha")));
	}


	@Test
	public void patchVersionPrecedenceTest()
	{
		Assert.assertEquals(
			0,
			SemanticVersion.from("1.0.1-alpha").compareTo(SemanticVersion.from("1.0.1-alpha")));
		Assert.assertTrue(
			0 > SemanticVersion.from("1.0.1-alpha").compareTo(SemanticVersion.from("1.0.2-alpha")));
		Assert.assertTrue(
			0 < SemanticVersion.from("1.0.2-alpha").compareTo(SemanticVersion.from("1.0.1-alpha")));
	}


	@Test
	public void preReleaseDataPrecedenceTest()
	{
		Assert.assertEquals(
			0,
			SemanticVersion.from("1.0.0-alpha").compareTo(SemanticVersion.from("1.0.0-alpha")));
		Assert.assertEquals(
			0,
			SemanticVersion.from("1.0.0-alpha.1").compareTo(SemanticVersion.from("1.0.0-alpha.1")));
		Assert.assertTrue(
			0 < SemanticVersion.from("1.0.0-alpha.1").compareTo(SemanticVersion.from("1.0.0-alpha.0")));
		Assert.assertTrue(
			0 > SemanticVersion.from("1.0.0-alpha.0").compareTo(SemanticVersion.from("1.0.0-alpha.1")));
		Assert.assertTrue(
			0 < SemanticVersion.from("1.0.0").compareTo(SemanticVersion.from("1.0.0-alpha")));
		Assert.assertTrue(
			0 > SemanticVersion.from("1.0.0-alpha").compareTo(SemanticVersion.from("1.0.0")));
		Assert.assertTrue(
			0 > SemanticVersion.from("1.0.0-alpha").compareTo(SemanticVersion.from("1.0.0-beta")));
		Assert.assertTrue(
			0 < SemanticVersion.from("1.0.0-beta").compareTo(SemanticVersion.from("1.0.0-alpha")));
		Assert.assertTrue(
			0 < SemanticVersion.from("1.0.0-alpha.beta").compareTo(SemanticVersion.from("1.0.0-alpha")));
		Assert.assertTrue(
			0 > SemanticVersion.from("1.0.0-alpha").compareTo(SemanticVersion.from("1.0.0-alpha.beta")));
		Assert.assertTrue(
			0 < SemanticVersion.from("1.0.0-alpha.beta").compareTo(SemanticVersion.from("1.0.0-1.beta")));
		Assert.assertTrue(
			0 > SemanticVersion.from("1.0.0-1.beta").compareTo(SemanticVersion.from("1.0.0-alpha.beta")));
		Assert.assertTrue(
			0 > SemanticVersion.from("1.0.0-alpha.1").compareTo(SemanticVersion.from("1.0.0-alpha.beta")));
		Assert.assertTrue(
			0 < SemanticVersion.from("1.0.0-alpha.beta").compareTo(SemanticVersion.from("1.0.0-alpha.1")));
	}


	@Test
	public void buildMetadataIsIrrelevantForPrecedenceTest()
	{
		Assert.assertEquals(
			0,
			SemanticVersion.from("1.0.0-alpha").compareTo(SemanticVersion.from("1.0.0-alpha")));
		Assert.assertEquals(
			0,
			SemanticVersion.from("1.0.0-alpha+build").compareTo(SemanticVersion.from("1.0.0-alpha")));
		Assert.assertEquals(
			0,
			SemanticVersion.from("1.0.0-alpha").compareTo(SemanticVersion.from("1.0.0-alpha+build")));
		Assert.assertEquals(
			0,
			SemanticVersion.from("1.0.0-alpha+build").compareTo(SemanticVersion.from("1.0.0-alpha+build")));
	}


	@Test
	public void equalsTest()
	{
		Assert.assertEquals(SemanticVersion.from("1.0.0-alpha"), SemanticVersion.from("1.0.0-alpha"));
		Assert.assertNotEquals(SemanticVersion.from("1.0.0-alpha"), new Object());
		Assert.assertNotEquals(new Object(), SemanticVersion.from("1.0.0-alpha"));
		final SemanticVersion semanticVersion = new SemanticVersion(1, 2, 3);
		Assert.assertEquals(semanticVersion, semanticVersion);
	}


	@Test
	public void setTest()
	{
		final SemanticVersion version1 = SemanticVersion.from("1.0.0");
		final SemanticVersion version2 = SemanticVersion.from("1.0.0-alpha");
		final SemanticVersion version3 = SemanticVersion.from("1.0.0-alpha+beta");
		final Set<SemanticVersion> versions = new HashSet<>();
		versions.add(version1);
		versions.add(version2);
		versions.add(version3);
		Assert.assertEquals(3, versions.size());
		Assert.assertTrue(versions.contains(version1));
		Assert.assertTrue(versions.contains(version2));
		Assert.assertTrue(versions.contains(version3));
	}
}
