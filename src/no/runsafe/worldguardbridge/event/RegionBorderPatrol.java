package no.runsafe.worldguardbridge.event;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import no.runsafe.framework.api.IConfiguration;
import no.runsafe.framework.api.event.IAsyncEvent;
import no.runsafe.framework.api.event.player.IPlayerMove;
import no.runsafe.framework.api.event.player.IPlayerTeleport;
import no.runsafe.framework.api.event.plugin.IConfigurationChanged;
import no.runsafe.framework.minecraft.RunsafeLocation;
import no.runsafe.framework.minecraft.RunsafeServer;
import no.runsafe.framework.minecraft.RunsafeWorld;
import no.runsafe.framework.minecraft.player.RunsafePlayer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RegionBorderPatrol implements IPlayerMove, IAsyncEvent, IConfigurationChanged, IPlayerTeleport
{
	@Override
	public boolean OnPlayerTeleport(RunsafePlayer player, RunsafeLocation from, RunsafeLocation to)
	{
		return OnPlayerMove(player, from, to);
	}

	@Override
	public boolean OnPlayerMove(RunsafePlayer player, RunsafeLocation from, RunsafeLocation to)
	{
		if (serverHasWorldGuard())
		{
			CheckIfEnteringRegion(player, from, to);
			CheckIfLeavingRegion(player, from, to);
		}
		return true;
	}

	@Override
	public void OnConfigurationChanged(IConfiguration iConfiguration)
	{
		regions.clear();
		if (!serverHasWorldGuard())
			return;
		for (RunsafeWorld world : RunsafeServer.Instance.getWorlds())
		{
			if (!regions.containsKey(world.getName()))
				regions.putIfAbsent(world.getName(), new ConcurrentHashMap<String, ProtectedRegion>());
			ConcurrentHashMap<String, ProtectedRegion> worldRegions = regions.get(world.getName());
			RegionManager manager = worldGuard.getRegionManager(world.getRaw());
			Map<String, ProtectedRegion> regions = manager.getRegions();
			for (String region : regions.keySet())
				worldRegions.putIfAbsent(region, regions.get(region));
		}
	}

	private boolean serverHasWorldGuard()
	{
		if (this.worldGuard == null)
			this.worldGuard = RunsafeServer.Instance.getPlugin("WorldGuard");

		return this.worldGuard != null;
	}

	private void CheckIfEnteringRegion(RunsafePlayer player, RunsafeLocation from, RunsafeLocation to)
	{
		if (!regions.containsKey(player.getWorld().getName()))
			return;
		Map<String, ProtectedRegion> worldRegions = regions.get(player.getWorld().getName());
		for (String region : worldRegions.keySet())
		{
			ProtectedRegion area = worldRegions.get(region);
			if (isInside(area, to) && !isInside(area, from))
				CustomEvents.Enter(player, region);
		}
	}

	private void CheckIfLeavingRegion(RunsafePlayer player, RunsafeLocation from, RunsafeLocation to)
	{
		if (!regions.containsKey(player.getWorld().getName()))
			return;
		Map<String, ProtectedRegion> worldRegions = regions.get(player.getWorld().getName());
		for (String region : worldRegions.keySet())
		{
			ProtectedRegion area = worldRegions.get(region);
			if (!isInside(area, to) && isInside(area, from))
				CustomEvents.Leave(player, region);
		}
	}

	private boolean isInside(ProtectedRegion area, RunsafeLocation location)
	{
		return area.contains(location.getBlockX(), location.getBlockY(), location.getBlockZ());
	}

	private WorldGuardPlugin worldGuard;
	private final ConcurrentHashMap<String, ConcurrentHashMap<String, ProtectedRegion>> regions =
		new ConcurrentHashMap<String, ConcurrentHashMap<String, ProtectedRegion>>();
}
