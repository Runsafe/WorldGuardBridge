package no.runsafe.worldguardbridge.event;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import no.runsafe.framework.api.IConfiguration;
import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.IServer;
import no.runsafe.framework.api.IWorld;
import no.runsafe.framework.api.event.IServerReady;
import no.runsafe.framework.api.event.player.IPlayerMove;
import no.runsafe.framework.api.event.player.IPlayerTeleport;
import no.runsafe.framework.api.event.plugin.IConfigurationChanged;
import no.runsafe.framework.api.event.world.IWorldLoad;
import no.runsafe.framework.api.event.world.IWorldUnload;
import no.runsafe.framework.api.log.IConsole;
import no.runsafe.framework.api.log.IDebug;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.internal.wrapper.ObjectUnwrapper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RegionBorderPatrol implements IPlayerMove, IServerReady, IPlayerTeleport, IWorldLoad, IWorldUnload, IConfigurationChanged
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
		if (serverHasWorldGuard() && shouldCheckRegion(player, to)) CheckIfLeavingEnteringRegion(player, from, to);
		return true;
	}

	@Override
	public void OnConfigurationChanged(IConfiguration configuration)
	{
		minMoveThreshold = configuration.getConfigValueAsDouble("borderPatrol.threshold");
		if (Math.abs(minMoveThreshold) < 0.01D)
		{
			minMoveThreshold = 2;
		}
		console.logInformation("BorderPatrol threshold configured to be %.2f", minMoveThreshold);
	}

	@Override
	public void OnServerReady()
	{
		flushRegions();
		ready = true;
	}

	@Override
	public void OnWorldLoad(IWorld world)
	{
		if (ready && serverHasWorldGuard()) loadWorldRegions(world);
	}

	@Override
	public void OnWorldUnload(IWorld world)
	{
		if (!ready || !serverHasWorldGuard()) return;

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
		if (!serverHasWorldGuard()) return;
		for (IWorld world : server.getWorlds())
			loadWorldRegions(world);
	}

	/**
	 * Loads all regions in a world.
	 * Outputs the number of regions loaded for the world to the console.
	 * If the world's regions are already loaded, it unloads then reloads them.
	 *
	 * @param world World to load regions for.
	 */
	private void loadWorldRegions(IWorld world)
	{
		if (!serverHasWorldGuard())
		{
			return;
		}
		String worldName = world.getName();
		RegionManager regionManager = WorldGuardPlugin.inst().getRegionManager(ObjectUnwrapper.convert(world));
		if (regionManager == null)
		{
			console.logWarning("&eNo region manager found for world &a%s&e.", worldName);
			return;
		}
		Map<String, ProtectedRegion> regionsInWorld = regionManager.getRegions();

		ConcurrentHashMap<String, ProtectedRegion> worldRegions = new ConcurrentHashMap<>(regionsInWorld);
		regions.put(worldName, worldRegions);

		console.logInformation("&2Loaded &a%d&2 regions in world &a%s&2.", regionsInWorld.size(), worldName);
	}

	private void CheckIfLeavingEnteringRegion(IPlayer player, ILocation from, ILocation to)
	{
		IWorld fromWorld = from.getWorld();
		IWorld toWorld = to.getWorld();

		// Check if player is leaving
		if (regions.containsKey(fromWorld.getName()))
		{
			Map<String, ProtectedRegion> fromWorldRegions = regions.get(fromWorld.getName());
			for (String region : fromWorldRegions.keySet())
			{
				debugger.debugFiner("Checking if player %s is leaving the region %s", player.getName(), region);
				ProtectedRegion area = fromWorldRegions.get(region);
				if (isInside(area, from, fromWorld) && !isInside(area, to, toWorld))
				{
					debugger.debugFine("Player is leaving the region %s, sending notification!", region);
					new RegionLeaveEvent(player, fromWorld, region).Fire();
				}
			}
		}

		// Check if player is entering
		if (!regions.containsKey(toWorld.getName()))
		{
			debugger.debugFine(
				"World %s does not contain any known regions, skipping checks for entering regions", toWorld.getName());
			return;
		}
		Map<String, ProtectedRegion> toWorldRegions = regions.get(toWorld.getName());
		for (String region : toWorldRegions.keySet())
		{
			debugger.debugFiner("Checking if player %s is entering the region %s", player.getName(), region);
			ProtectedRegion area = toWorldRegions.get(region);
			if (!isInside(area, from, fromWorld) && isInside(area, to, toWorld))
			{
				debugger.debugFine("Player is entering the region %s, sending notification!", region);
				new RegionEnterEvent(player, toWorld, region).Fire();
			}
		}
	}

	private boolean isInside(ProtectedRegion area, ILocation location, IWorld world)
	{
		return world.equals(location.getWorld()) && area.contains(
			location.getBlockX(), location.getBlockY(), location.getBlockZ());
	}

	private boolean shouldCheckRegion(IPlayer player, ILocation to)
	{
		if (!lastPlayerLocations.containsKey(player.getName()))
		{
			debugger.debugFiner(
				"Player %s is moving for the first time to %.2f,%.2f,%.2f@%s, scanning..", player.getName(), to.getX(),
				to.getY(),
				to.getZ(), to.getWorld().getName()
			);
			lastPlayerLocations.put(player.getName(), to);
			return true;
		}
		ILocation lastLocation = lastPlayerLocations.get(player.getName());
		if (!lastLocation.getWorld().equals(to.getWorld()))
		{
			debugger.debugFiner(
				"Player %s is moving from world %s to %.2f,%.2f,%.2f@%s, scanning..", player.getName(),
				lastLocation.getWorld().getName(), to.getX(), to.getY(), to.getZ(), to.getWorld().getName()
			);
			lastPlayerLocations.put(player.getName(), to);
			return true;
		}
		double delta = lastLocation.distance(to);
		if (delta >= minMoveThreshold)
		{
			debugger.debugFiner(
				"Player %s has moved %.2f blocks from %.2f,%.2f,%.2f to %.2f,%.2f,%.2f in world %s, scanning..",
				player.getName(), delta,
				lastLocation.getX(), lastLocation.getY(), lastLocation.getZ(),
				to.getX(), to.getY(), to.getZ(),
				to.getWorld().getName()
			);
			lastPlayerLocations.put(player.getName(), to);
			return true;
		}
		debugger.debugFinest(
			"Player %s has moved %.2f blocks from %.2f,%.2f,%.2f to %.2f,%.2f,%.2f@%s, not scanning.",
			player.getName(), delta,
			lastLocation.getX(), lastLocation.getY(), lastLocation.getZ(),
			to.getX(), to.getY(), to.getZ(),
			to.getWorld().getName()
		);
		return false;
	}

	private boolean serverHasWorldGuard()
	{
		debugger.debugFinest("Server WorldGuard is %s", WorldGuardPlugin.inst() == null ? "missing" : "available");
		return WorldGuardPlugin.inst() != null;
	}

	private final IServer server;
	private final IDebug debugger;
	private final IConsole console;
	private final Map<String, ConcurrentHashMap<String, ProtectedRegion>> regions = new ConcurrentHashMap<>();
	private final Map<String, ILocation> lastPlayerLocations = new ConcurrentHashMap<>();
	private boolean ready = false;
	// Minimum move distance to trigger a check
	private double minMoveThreshold = 2.0;
}