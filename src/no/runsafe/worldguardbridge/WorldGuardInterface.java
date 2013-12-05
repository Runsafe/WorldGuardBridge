package no.runsafe.worldguardbridge;

import com.google.common.collect.Sets;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.GlobalRegionManager;
import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import no.runsafe.framework.api.IDebug;
import no.runsafe.framework.api.IOutput;
import no.runsafe.framework.api.event.plugin.IPluginEnabled;
import no.runsafe.framework.minecraft.RunsafeLocation;
import no.runsafe.framework.minecraft.RunsafeServer;
import no.runsafe.framework.minecraft.RunsafeWorld;
import no.runsafe.framework.minecraft.player.RunsafePlayer;
import org.bukkit.entity.Player;

import java.awt.geom.Rectangle2D;
import java.util.*;

@SuppressWarnings("WeakerAccess")
public class WorldGuardInterface implements IPluginEnabled, IRegionControl
{

	public WorldGuardInterface(IDebug console)
	{
		this.debugger = console;
	}

	@Override
	public void OnPluginEnabled()
	{
		if (!serverHasWorldGuard())
			debugger.logError("Could not find WorldGuard on this server!");
	}

	public boolean serverHasWorldGuard()
	{
		if (this.worldGuard == null)
			this.worldGuard = RunsafeServer.Instance.getPlugin("WorldGuard");

		return this.worldGuard != null;
	}

	@Override
	public boolean isInPvPZone(RunsafePlayer player)
	{
		if (player == null || !serverHasWorldGuard())
			return false;
		RegionManager regionManager = worldGuard.getRegionManager(player.getWorld().getRaw());
		ApplicableRegionSet set = regionManager.getApplicableRegions(player.getLocation().getRaw());
		return set.size() != 0 && set.allows(DefaultFlag.PVP);
	}

	@Override
	public String getCurrentRegion(RunsafePlayer player)
	{
		RegionManager regionManager = worldGuard.getRegionManager(player.getWorld().getRaw());
		ApplicableRegionSet set = regionManager.getApplicableRegions(player.getLocation().getRaw());
		if (set.size() == 0)
			return null;
		StringBuilder sb = new StringBuilder();
		for (ProtectedRegion r : set)
		{
			if (sb.length() > 0)
				sb.append(";");
			sb.append(r.getId());
		}
		return sb.toString();
	}

	@Override
	public ProtectedRegion getRegion(RunsafeWorld world, String name)
	{
		RegionManager regionManager = worldGuard.getRegionManager(world.getRaw());
		return regionManager.getRegion(name);
	}

	@Override
	public List<RunsafePlayer> getPlayersInRegion(RunsafeWorld world, String regionName)
	{
		List<RunsafePlayer> worldPlayers = world.getPlayers();
		List<RunsafePlayer> regionPlayers = new ArrayList<RunsafePlayer>();

		for (RunsafePlayer player : worldPlayers)
		{
			List<String> playerRegions = this.getApplicableRegions(player);
			if (playerRegions != null && playerRegions.contains(regionName))
				regionPlayers.add(player);
		}

		return regionPlayers;
	}

	@Override
	public List<String> getRegionsAtLocation(RunsafeLocation location)
	{
		RegionManager regionManager = worldGuard.getRegionManager(location.getWorld().getRaw());
		ApplicableRegionSet set = regionManager.getApplicableRegions(location.getRaw());

		if (set.size() == 0)
			return null;

		ArrayList<String> regions = new ArrayList<String>();
		for (ProtectedRegion region : set)
			regions.add(region.getId());

		return regions;
	}

	@Override
	public List<String> getApplicableRegions(RunsafePlayer player)
	{
		RegionManager regionManager = worldGuard.getRegionManager(player.getWorld().getRaw());
		ApplicableRegionSet set = regionManager.getApplicableRegions(player.getLocation().getRaw());
		if (set.size() == 0)
			return null;

		ArrayList<String> regions = new ArrayList<String>();
		for (ProtectedRegion r : set)
			regions.add(r.getId());

		return regions;
	}

	@Override
	public Map<String, Set<String>> getAllRegionsWithOwnersInWorld(RunsafeWorld world)
	{
		HashMap<String, Set<String>> result = new HashMap<String, Set<String>>();
		RegionManager regionManager = worldGuard.getRegionManager(world.getRaw());
		Map<String, ProtectedRegion> regions = regionManager.getRegions();
		for (String region : regions.keySet())
			result.put(region, regions.get(region).getOwners().getPlayers());
		return result;
	}

	@Override
	public RunsafeLocation getRegionLocation(RunsafeWorld world, String name)
	{
		if (!serverHasWorldGuard())
			return null;

		ProtectedRegion region = worldGuard.getRegionManager(world.getRaw()).getRegion(name);
		if (region == null)
			return null;
		BlockVector point = region.getMaximumPoint();
		return new RunsafeLocation(world, point.getX(), point.getY(), point.getZ());
	}

	@Override
	public Set<String> getOwners(RunsafeWorld world, String name)
	{
		if (!serverHasWorldGuard())
			return null;

		return worldGuard.getRegionManager(world.getRaw()).getRegion(name).getOwners().getPlayers();
	}

	@Override
	public Set<String> getMembers(RunsafeWorld world, String name)
	{
		if (!serverHasWorldGuard())
			return null;

		return Sets.newHashSet(worldGuard.getRegionManager(world.getRaw()).getRegion(name).getMembers().getPlayers());
	}

	@Override
	public List<String> getOwnedRegions(RunsafePlayer player, RunsafeWorld world)
	{
		if (world == null || player == null)
			return null;
		RegionManager regionManager = worldGuard.getRegionManager(world.getRaw());
		ArrayList<String> regions = new ArrayList<String>();
		Map<String, ProtectedRegion> regionSet = regionManager.getRegions();
		for (String region : regionSet.keySet())
			if (regionSet.get(region).getOwners().contains(player.getName()))
				regions.add(region);
		return regions;
	}

	@Override
	public List<String> getRegionsInWorld(RunsafeWorld world)
	{
		RegionManager regionManager = worldGuard.getRegionManager(world.getRaw());
		return new ArrayList<String>(regionManager.getRegions().keySet());
	}

	@Override
	public Map<String, Rectangle2D> getRegionRectanglesInWorld(RunsafeWorld world)
	{
		if (world == null || worldGuard == null)
			return null;
		RegionManager regionManager = worldGuard.getRegionManager(world.getRaw());
		Map<String, ProtectedRegion> regionSet = regionManager.getRegions();
		HashMap<String, Rectangle2D> result = new HashMap<String, Rectangle2D>();
		for (String regionName : regionSet.keySet())
		{
			Rectangle2D.Double area = new Rectangle2D.Double();
			ProtectedRegion region = regionSet.get(regionName);
			BlockVector min = region.getMinimumPoint();
			BlockVector max = region.getMaximumPoint();
			area.setRect(min.getX(), min.getZ(), max.getX() - min.getX(), max.getZ() - min.getZ());
			result.put(regionName, area);
		}
		return result;
	}

	@Override
	public boolean deleteRegion(RunsafeWorld world, String name)
	{
		RegionManager regionManager = worldGuard.getRegionManager(world.getRaw());
		if (regionManager.getRegion(name) == null)
			return false;
		regionManager.removeRegion(name);
		try
		{
			regionManager.save();
		}
		catch (ProtectionDatabaseException e)
		{
			debugger.logException(e);
		}
		return true;
	}

	@Override
	public boolean createRegion(RunsafePlayer owner, RunsafeWorld world, String name, RunsafeLocation pos1, RunsafeLocation pos2)
	{
		if (world == null || worldGuard == null)
			return false;

		RegionManager regionManager = worldGuard.getRegionManager(world.getRaw());
		if (regionManager.hasRegion(name))
			return false;

		CuboidSelection selection = new CuboidSelection(world.getRaw(), pos1.getRaw(), pos2.getRaw());
		BlockVector min = selection.getNativeMinimumPoint().toBlockVector();
		BlockVector max = selection.getNativeMaximumPoint().toBlockVector();
		ProtectedRegion region = new ProtectedCuboidRegion(name, min, max);
		region.getOwners().addPlayer(owner.getName());
		regionManager.addRegion(region);
		try
		{
			regionManager.save();
		}
		catch (ProtectionDatabaseException e)
		{
			debugger.logException(e);
		}
		return regionManager.hasRegion(name);
	}

	@Override
	public boolean redefineRegion(RunsafeWorld world, String name, RunsafeLocation pos1, RunsafeLocation pos2)
	{
		if (world == null || worldGuard == null)
			return false;

		RegionManager regionManager = worldGuard.getRegionManager(world.getRaw());
		ProtectedRegion existing = regionManager.getRegion(name);
		if (existing == null)
		{
			debugger.debugFine("Region manager does not know anything about the region %s in world %s!", name, world.getName());
			return false;
		}
		CuboidSelection selection = new CuboidSelection(world.getRaw(), pos1.getRaw(), pos2.getRaw());
		BlockVector min = selection.getNativeMinimumPoint().toBlockVector();
		BlockVector max = selection.getNativeMaximumPoint().toBlockVector();
		ProtectedRegion region = new ProtectedCuboidRegion(name, min, max);


		// Copy details from the old region to the new one
		region.setMembers(existing.getMembers());
		region.setOwners(existing.getOwners());
		region.setFlags(existing.getFlags());
		region.setPriority(existing.getPriority());
		try
		{
			region.setParent(existing.getParent());
		}
		catch (ProtectedRegion.CircularInheritanceException ignore)
		{
			// This should not be thrown
		}

		regionManager.addRegion(region); // Replace region
		try
		{
			regionManager.save();
			return true;
		}
		catch (ProtectionDatabaseException e)
		{
			debugger.logException(e);
		}
		return false;
	}

	@Override
	public boolean addMemberToRegion(RunsafeWorld world, String name, RunsafePlayer player)
	{
		if (!serverHasWorldGuard())
			return false;

		RegionManager regionManager = worldGuard.getRegionManager(world.getRaw());
		DefaultDomain members = regionManager.getRegion(name).getMembers();
		if (!members.contains(player.getName()))
		{
			members.addPlayer(player.getName());
			try
			{
				regionManager.save();
			}
			catch (ProtectionDatabaseException e)
			{
				debugger.logException(e);
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean removeMemberFromRegion(RunsafeWorld world, String name, RunsafePlayer player)
	{
		if (!serverHasWorldGuard())
			return false;

		RegionManager regionManager = worldGuard.getRegionManager(world.getRaw());
		DefaultDomain members = regionManager.getRegion(name).getMembers();
		if (members.contains(player.getName()))
		{
			members.removePlayer(player.getName());
			try
			{
				regionManager.save();
			}
			catch (ProtectionDatabaseException e)
			{
				debugger.logException(e);
			}
			return true;
		}
		return false;
	}

	@Override
	public Rectangle2D getRectangle(RunsafeWorld world, String name)
	{
		if (!serverHasWorldGuard())
			return null;
		ProtectedRegion region = worldGuard.getRegionManager(world.getRaw()).getRegion(name);
		if (region == null)
			return null;
		Rectangle2D.Double area = new Rectangle2D.Double();
		BlockVector min = region.getMinimumPoint();
		BlockVector max = region.getMaximumPoint();
		area.setRect(min.getX(), min.getZ(), max.getX() - min.getX(), max.getZ() - min.getZ());
		return area;
	}

	GlobalRegionManager getGlobalRegionManager()
	{
		return worldGuard.getGlobalRegionManager();
	}

	LocalPlayer wrapPlayer(Player rawPlayer)
	{
		return worldGuard.wrapPlayer(rawPlayer);
	}

	private WorldGuardPlugin worldGuard;
	private final IDebug debugger;
}
