package no.runsafe.worldguardbridge.event;

import no.runsafe.framework.api.IWorld;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.minecraft.event.player.RunsafeCustomEvent;

import java.util.HashMap;
import java.util.Map;

public class RegionLeaveEvent extends RunsafeCustomEvent
{
	public RegionLeaveEvent(IPlayer player, IWorld world, String region)
	{
		super(player, "region.leave");
		this.world = world;
		this.region = region;
	}

	public IWorld getWorld()
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
		Map<String, String> data = new HashMap<>();
		data.put("world", world.getName());
		data.put("region", region);
		return data;
	}

	private final IWorld world;
	private final String region;
}
