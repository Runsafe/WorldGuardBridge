package no.runsafe.worldguardbridge.event;

import no.runsafe.framework.minecraft.RunsafeWorld;
import no.runsafe.framework.minecraft.event.player.RunsafeCustomEvent;
import no.runsafe.framework.minecraft.player.RunsafePlayer;

import java.util.HashMap;
import java.util.Map;

public class RegionEnterEvent extends RunsafeCustomEvent
{
	public RegionEnterEvent(RunsafePlayer player, RunsafeWorld world, String region)
	{
		super(player, "region.enter", null);
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
	public Map<String, String> getData()
	{
		Map<String, String> data = new HashMap<String, String>();
		data.put("world", world.getName());
		data.put("region", region);
		return data;
	}

	private final RunsafeWorld world;
	private final String region;
}
