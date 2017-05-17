package no.runsafe.worldguardbridge;

import com.google.common.collect.Sets;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.GlobalRegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.IServer;
import no.runsafe.framework.api.IWorld;
import no.runsafe.framework.api.event.plugin.IPluginEnabled;
import no.runsafe.framework.api.log.IConsole;
import no.runsafe.framework.api.log.IDebug;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.internal.wrapper.ObjectUnwrapper;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.awt.geom.Rectangle2D;
import java.util.*;

@SuppressWarnings("WeakerAccess")
public class WorldGuardInterface implements IPluginEnabled, IRegionControl
{
	public WorldGuardInterface(IDebug console, IConsole console1, IServer server)
	{
		this.debugger = console;
		this.console = console1;
		this.server = server;
	}

	@Override
	public void OnPluginEnabled()
	{
		if (!serverHasWorldGuard())
			console.logError("Could not find WorldGuard on this server!");
	}

	@Override
	public boolean serverHasWorldGuard()
	{
		if (this.worldGuard == null)
			this.worldGuard = server.getPlugin("WorldGuard");

		return this.worldGuard != null;
	}

	@Override
	public boolean playerCanBuildHere(IPlayer player, ILocation location)
	{
		return worldGuard.canBuild((Player) ObjectUnwrapper.convert(player), (Location) ObjectUnwrapper.convert(location));
	}

	@Override
	public boolean isInPvPZone(IPlayer player)
	{
		if (player == null || !serverHasWorldGuard())
			return false;
		RegionManager regionManager = worldGuard.getRegionManager((World) ObjectUnwrapper.convert(player.getWorld()));
		ApplicableRegionSet set = regionManager.getApplicableRegions((Location) ObjectUnwrapper.convert(player.getLocation()));
		return set.size() != 0 && set.allows(DefaultFlag.PVP);
	}

	@Override
	public String getCurrentRegion(IPlayer player)
	{
		RegionManager regionManager = worldGuard.getRegionManager((World) ObjectUnwrapper.convert(player.getWorld()));
		ApplicableRegionSet set = regionManager.getApplicableRegions((Location) ObjectUnwrapper.convert(player.getLocation()));
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
	public ProtectedRegion getRegion(IWorld world, String name)
	{
		if (!serverHasWorldGuard())
			return null;
		if (world == null)
			return null;
		RegionManager regionManager = worldGuard.getRegionManager((World) ObjectUnwrapper.convert(world));
		if (regionManager == null)
			return null;
		return regionManager.getRegion(name);
	}

	@Override
	public List<IPlayer> getPlayersInRegion(IWorld world, String regionName)
	{
		List<IPlayer> worldPlayers = world.getPlayers();
		List<IPlayer> regionPlayers = new ArrayList<IPlayer>();

		for (IPlayer player : worldPlayers)
		{
			List<String> playerRegions = this.getApplicableRegions(player);
			if (playerRegions != null && playerRegions.contains(regionName))
				regionPlayers.add(player);
		}

		return regionPlayers;
	}

	@Override
	public List<String> getRegionsAtLocation(ILocation location)
	{
		if (!serverHasWorldGuard())
			return null;

		RegionManager regionManager = worldGuard.getRegionManager((World) ObjectUnwrapper.convert(location.getWorld()));
		ApplicableRegionSet set = regionManager.getApplicableRegions((Location) ObjectUnwrapper.convert(location));

		if (set.size() == 0)
			return null;

		ArrayList<String> regions = new ArrayList<String>();
		for (ProtectedRegion region : set)
			regions.add(region.getId());

		return regions;
	}

	@Override
	public List<String> getApplicableRegions(IPlayer player)
	{
		RegionManager regionManager = worldGuard.getRegionManager((World) ObjectUnwrapper.convert(player.getWorld()));
		ApplicableRegionSet set = regionManager.getApplicableRegions((Location) ObjectUnwrapper.convert(player.getLocation()));

		ArrayList<String> regions = new ArrayList<String>();
		for (ProtectedRegion r : set)
			regions.add(r.getId());

		return regions;
	}

	@Override
	public Map<String, Set<IPlayer>> getAllRegionsWithOwnersInWorld(IWorld world)
	{
		HashMap<String, Set<IPlayer>> result = new HashMap<String, Set<IPlayer>>();
		RegionManager regionManager = worldGuard.getRegionManager((World) ObjectUnwrapper.convert(world));
		Map<String, ProtectedRegion> regions = regionManager.getRegions();
		for (String region : regions.keySet())
			result.put(region, getOwnerPlayers(world, region));
		return result;
	}

	@Override
	public ILocation getRegionLocation(IWorld world, String name)
	{
		if (!serverHasWorldGuard())
			return null;

		ProtectedRegion region = worldGuard.getRegionManager((World) ObjectUnwrapper.convert(world)).getRegion(name);
		if (region == null)
			return null;
		BlockVector point = region.getMaximumPoint();
		return world.getLocation(point.getX(), point.getY(), point.getZ());
	}

	@Override
	public Set<String> getOwners(IWorld world, String name)
	{
		if (!serverHasWorldGuard())
			return null;

		return worldGuard.getRegionManager((World) ObjectUnwrapper.convert(world)).getRegion(name).getOwners().getPlayers();
	}

	@Override
	public Set<UUID> getOwnerUniqueIds(IWorld world, String name)
	{
		if (!serverHasWorldGuard())
			return null;

		return worldGuard.getRegionManager((World) ObjectUnwrapper.convert(world)).getRegion(name).getOwners().getUniqueIds();
	}

	@Override
	public Set<IPlayer> getOwnerPlayers(IWorld world, String name)
	{
		if (!serverHasWorldGuard())
			return null;

		Set<UUID> owners = getOwnerUniqueIds(world, name);
		Set<IPlayer> ownerPlayers = new HashSet<IPlayer>();
		for (UUID playerUUID : owners)
			ownerPlayers.add(server.getPlayer(playerUUID));

		return ownerPlayers;
	}

	@Override
	public Set<String> getMembers(IWorld world, String name)
	{
		if (!serverHasWorldGuard())
			return null;

		return Sets.newHashSet(worldGuard.getRegionManager((World) ObjectUnwrapper.convert(world)).getRegion(name).getMembers().getPlayers());
	}

	@Override
	public Set<UUID> getMemberUniqueIds(IWorld world, String name)
	{
		if (!serverHasWorldGuard())
			return null;

		return Sets.newHashSet(worldGuard.getRegionManager((World) ObjectUnwrapper.convert(world)).getRegion(name).getMembers().getUniqueIds());
	}

	@Override
	public Set<IPlayer> getMemberPlayers(IWorld world, String name)
	{
		if (!serverHasWorldGuard())
			return null;

		Set<UUID> owners = getMemberUniqueIds(world, name);
		Set<IPlayer> memberPlayers = new HashSet<IPlayer>();
		for (UUID playerUUID : owners)
			memberPlayers.add(server.getPlayer(playerUUID));

		return memberPlayers;
	}

	@Override
	public List<String> getOwnedRegions(IPlayer player, IWorld world)
	{
		if (world == null || player == null)
			return null;
		RegionManager regionManager = worldGuard.getRegionManager((World) ObjectUnwrapper.convert(world));
		ArrayList<String> regions = new ArrayList<String>();
		Map<String, ProtectedRegion> regionSet = regionManager.getRegions();
		for (String region : regionSet.keySet())
			if (regionSet.get(region).getOwners().contains(player.getUniqueId()))
				regions.add(region);
		return regions;
	}

	@Override
	public List<String> getMemberRegions(IPlayer player, IWorld world)
	{
		if (world == null || player == null)
			return null;
		RegionManager regionManager = worldGuard.getRegionManager((World) ObjectUnwrapper.convert(world));
		ArrayList<String> regions = new ArrayList<String>();
		Map<String, ProtectedRegion> regionSet = regionManager.getRegions();
		for (String region : regionSet.keySet())
			if (regionSet.get(region).getMembers().contains(player.getUniqueId()))
				regions.add(region);
		return regions;
	}

	@Override
	public List<String> getRegionsInWorld(IWorld world)
	{
		if (world == null)
			return new ArrayList<String>(0);

		RegionManager regionManager = worldGuard.getRegionManager((World) ObjectUnwrapper.convert(world));
		return new ArrayList<String>(regionManager.getRegions().keySet());
	}

	@Override
	public Map<String, Rectangle2D> getRegionRectanglesInWorld(IWorld world)
	{
		if (!serverHasWorldGuard() || world == null || worldGuard == null)
			return null;
		RegionManager regionManager = worldGuard.getRegionManager((World) ObjectUnwrapper.convert(world));
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
	public boolean deleteRegion(IWorld world, String name)
	{
		RegionManager regionManager = worldGuard.getRegionManager((World) ObjectUnwrapper.convert(world));
		if (regionManager.getRegion(name) == null)
			return false;
		regionManager.removeRegion(name);
		try
		{
			regionManager.save();
		}
		catch (StorageException e)
		{
			console.logException(e);
		}
		return true;
	}

	@Override
	public boolean createRegion(IPlayer owner, IWorld world, String name, ILocation pos1, ILocation pos2)
	{
		if (world == null || worldGuard == null)
			return false;

		RegionManager regionManager = worldGuard.getRegionManager((World) ObjectUnwrapper.convert(world));
		if (regionManager.hasRegion(name))
			return false;

		CuboidSelection selection = new CuboidSelection(
			(World) ObjectUnwrapper.convert(world),
			(Location) ObjectUnwrapper.convert(pos1),
			(Location) ObjectUnwrapper.convert(pos2)
		);
		BlockVector min = selection.getNativeMinimumPoint().toBlockVector();
		BlockVector max = selection.getNativeMaximumPoint().toBlockVector();
		ProtectedRegion region = new ProtectedCuboidRegion(name, min, max);
		region.getOwners().addPlayer(owner.getUniqueId());
		regionManager.addRegion(region);
		try
		{
			regionManager.save();
		}
		catch (StorageException e)
		{
			console.logException(e);
		}
		return regionManager.hasRegion(name);
	}

	@Override
	public boolean redefineRegion(IWorld world, String name, ILocation pos1, ILocation pos2)
	{
		if (world == null || worldGuard == null)
			return false;

		RegionManager regionManager = worldGuard.getRegionManager((World) ObjectUnwrapper.convert(world));
		ProtectedRegion existing = regionManager.getRegion(name);
		if (existing == null)
		{
			debugger.debugFine("Region manager does not know anything about the region %s in world %s!", name, world.getName());
			return false;
		}
		CuboidSelection selection = new CuboidSelection(
			(World) ObjectUnwrapper.convert(world),
			(Location) ObjectUnwrapper.convert(pos1),
			(Location) ObjectUnwrapper.convert(pos2)
		);
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
		catch (StorageException e)
		{
			console.logException(e);
		}
		return false;
	}

	@Override
	public boolean addMemberToRegion(IWorld world, String name, IPlayer player)
	{
		if (!serverHasWorldGuard())
			return false;

		RegionManager regionManager = worldGuard.getRegionManager((World) ObjectUnwrapper.convert(world));
		DefaultDomain members = regionManager.getRegion(name).getMembers();
		if (!members.contains(player.getUniqueId()))
		{
			members.addPlayer(player.getUniqueId());
			try
			{
				regionManager.save();
			}
			catch (StorageException e)
			{
				console.logException(e);
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean removeMemberFromRegion(IWorld world, String name, IPlayer player)
	{
		if (!serverHasWorldGuard())
			return false;

		RegionManager regionManager = worldGuard.getRegionManager((World) ObjectUnwrapper.convert(world));
		DefaultDomain members = regionManager.getRegion(name).getMembers();
		if (members.contains(player.getUniqueId()))
		{
			members.removePlayer(player.getUniqueId());
			try
			{
				regionManager.save();
			}
			catch (StorageException e)
			{
				console.logException(e);
			}
			return true;
		}
		return false;
	}

	@Override
	public Rectangle2D getRectangle(IWorld world, String name)
	{
		if (!serverHasWorldGuard())
			return null;
		ProtectedRegion region = worldGuard.getRegionManager((World) ObjectUnwrapper.convert(world)).getRegion(name);
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
	private final IConsole console;
	private final IServer server;
}
