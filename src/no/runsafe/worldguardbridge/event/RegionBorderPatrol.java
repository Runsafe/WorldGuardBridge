package no.runsafe.worldguardbridge.event;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.IServer;
import no.runsafe.framework.api.IWorld;
import no.runsafe.framework.api.event.IAsyncEvent;
import no.runsafe.framework.api.event.IServerReady;
import no.runsafe.framework.api.event.player.IPlayerMove;
import no.runsafe.framework.api.event.player.IPlayerTeleport;
import no.runsafe.framework.api.event.world.IWorldLoad;
import no.runsafe.framework.api.event.world.IWorldUnload;
import no.runsafe.framework.api.log.IConsole;
import no.runsafe.framework.api.log.IDebug;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.internal.wrapper.ObjectUnwrapper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RegionBorderPatrol implements IPlayerMove, IAsyncEvent, IServerReady, IPlayerTeleport, IWorldLoad, IWorldUnload
{
	public RegionBorderPatrol(IDebug output, IConsole console, IServer server)
	{
		this.debugger = output;
		this.console = console;
		this.server = server;
	}

	@Override
	public boolean OnPlayerTeleport(IPlayer player, ILocation from, ILocation to)
	{
		return OnPlayerMove(player, from, to);
	}

	@Override
	public boolean OnPlayerMove(IPlayer player, ILocation from, ILocation to)
	{
		if (serverHasWorldGuard())
		{
			CheckIfLeavingRegion(player, from, to);
			CheckIfEnteringRegion(player, from, to);
		}
		return true;
	}

	@Override
	public void OnServerReady()
	{
		ready = true;
		flushRegions();
	}

	@Override
	public void OnWorldLoad(IWorld world)
	{
		if (!ready || !serverHasWorldGuard())
			return;
		loadWorldRegions(world);
	}

	@Override
	public void OnWorldUnload(IWorld world)
	{
		if (!ready || !serverHasWorldGuard())
			return;
		String worldName = world.getName();

		// Check if world isn't loaded. Can't unload a world if it's not loaded.
		if (!regions.containsKey(worldName))
		{
			console.logWarning("&eCould not unload regions for world &6%s&e. No regions to unload.", worldName);
			return;
		}

		regions.remove(worldName);
		console.logInformation("&2Unloaded all regions in world &a%s&2.", worldName);
	}

	private void flushRegions()
	{
		regions.clear();
		if (!serverHasWorldGuard())
			return;
		int regionAmount = 0;
		for (IWorld world : server.getWorlds())
			regionAmount += loadWorldRegions(world);
		console.logInformation("&2Loaded &a%d&2 regions across &a%d&2 worlds.&r", regionAmount, regions.size());
	}

	/**
	 * Loads all regions in a world.
	 * Outputs the number of regions loaded for the world to the console.
	 * If the world's regions are already loaded, it unloads then reloads them.
	 *
	 * @param world World to load regions for.
	 * @return Number of regions loaded.
	 */
	private int loadWorldRegions(IWorld world)
	{
		int regionAmount = 0;
		String worldName = world.getName();
		ConcurrentHashMap<String, ProtectedRegion> worldRegions = regions.get(worldName);

		if (worldRegions != null)
			worldRegions.clear();
		else
		{
			worldRegions = new ConcurrentHashMap<>();
			regions.putIfAbsent(worldName, worldRegions);
		}

		Map<String, ProtectedRegion> regions = worldGuard.getRegionManager(ObjectUnwrapper.convert(world)).getRegions();
		for (String region : regions.keySet())
		{
			regionAmount++;
			worldRegions.putIfAbsent(region, regions.get(region));
		}
		console.logInformation("&2Loaded &a%d&2 regions in world &a%s&2.&r", regionAmount, worldName);

		return regionAmount;
	}

	private boolean serverHasWorldGuard()
	{
		if (this.worldGuard == null)
			this.worldGuard = server.getPlugin("WorldGuard");

		return this.worldGuard != null;
	}

	private void CheckIfEnteringRegion(IPlayer player, ILocation from, ILocation to)
	{
		if (!regions.containsKey(to.getWorld().getName()))
			return;
		Map<String, ProtectedRegion> worldRegions = regions.get(to.getWorld().getName());
		for (String region : worldRegions.keySet())
		{
			ProtectedRegion area = worldRegions.get(region);
			if (isInside(area, to.getWorld(), to) && !isInside(area, to.getWorld(), from))
			{
				debugger.debugFine("Player is entering the region %s, sending notification!", region);
				new RegionEnterEvent(player, to.getWorld(), region).Fire();
			}
		}
	}

	private void CheckIfLeavingRegion(IPlayer player, ILocation from, ILocation to)
	{
		if (!regions.containsKey(from.getWorld().getName()))
			return;
		Map<String, ProtectedRegion> worldRegions = regions.get(from.getWorld().getName());
		for (String region : worldRegions.keySet())
		{
			ProtectedRegion area = worldRegions.get(region);
			if (!isInside(area, from.getWorld(), to) && isInside(area, from.getWorld(), from))
			{
				debugger.debugFine("Player is leaving the region %s, sending notification!", region);
				new RegionLeaveEvent(player, from.getWorld(), region).Fire();
			}
		}
	}

	private boolean isInside(ProtectedRegion area, IWorld world, ILocation location)
	{
		return world.equals(location.getWorld())
			&& area.contains(location.getBlockX(), location.getBlockY(), location.getBlockZ());
	}

	private boolean ready = false;
	private WorldGuardPlugin worldGuard;
	private final ConcurrentHashMap<String, ConcurrentHashMap<String, ProtectedRegion>> regions =
		new ConcurrentHashMap<>();
	private final IDebug debugger;
	private final IConsole console;
	private final IServer server;
}
