package no.runsafe.worldguardbridge.event;

import no.runsafe.framework.minecraft.RunsafeWorld;

public class RegionData
{
	public RegionData(RunsafeWorld world, String region)
	{
		this.world = world;
		this.region = region;
	}

	public RunsafeWorld getWorld()
	{
		return world;
	}

	public String getRegion()
	{
		return region;
	}

	@Override
	public String toString()
	{
		return String.format("%s-%s", world.getName(), region);
	}

	private final RunsafeWorld world;
	private final String region;
}
