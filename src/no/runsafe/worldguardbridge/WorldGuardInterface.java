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
		return worldGuard.canBuild(ObjectUnwrapper.convert(player), (Location) ObjectUnwrapper.convert(location));
	}

	@Override
	public boolean isInPvPZone(IPlayer player)
	{
		if (player == null || !serverHasWorldGuard())
			return false;
		RegionManager regionManager = worldGuard.getRegionManager(ObjectUnwrapper.convert(player.getWorld()));
		ApplicableRegionSet set = regionManager.getApplicableRegions((Location) ObjectUnwrapper.convert(player.getLocation()));
		return set.size() != 0 && set.allows(DefaultFlag.PVP);
	}

	@Override
	public String getCurrentRegion(IPlayer player)
	{
		RegionManager regionManager = worldGuard.getRegionManager(ObjectUnwrapper.convert(player.getWorld()));
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
		RegionManager regionManager = worldGuard.getRegionManager(ObjectUnwrapper.convert(world));
		if (regionManager == null)
			return null;
		return regionManager.getRegion(name);
	}

	@Override
	public List<IPlayer> getPlayersInRegion(IWorld world, String regionName)
	{
		List<IPlayer> worldPlayers = world.getPlayers();
		List<IPlayer> regionPlayers = new ArrayList<>();

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

		RegionManager regionManager = worldGuard.getRegionManager(ObjectUnwrapper.convert(location.getWorld()));
		ApplicableRegionSet set = regionManager.getApplicableRegions((Location) ObjectUnwrapper.convert(location));

		if (set.size() == 0)
			return null;

		ArrayList<String> regions = new ArrayList<>();
		for (ProtectedRegion region : set)
			regions.add(region.getId());

		return regions;
	}

	@Override
	public List<String> getApplicableRegions(IPlayer player)
	{
		RegionManager regionManager = worldGuard.getRegionManager(ObjectUnwrapper.convert(player.getWorld()));
		ApplicableRegionSet set = regionManager.getApplicableRegions((Location) ObjectUnwrapper.convert(player.getLocation()));
		if (set.size() == 0)
			return Collections.emptyList();

		ArrayList<String> regions = new ArrayList<>();
		for (ProtectedRegion r : set)
			regions.add(r.getId());

		return regions;
	}

	/**
	 * Gets a list of all regions in a world with owners.
	 * Will not return owners that haven't been converted from being stored as usernames to UUIDs.
	 * @param world The world.
	 * @return Every region name with a set of its owners.
	 */
	@Override
	public Map<String, Set<IPlayer>> getAllRegionsWithOwnersInWorld(IWorld world)
	{
		HashMap<String, Set<IPlayer>> result = new HashMap<>();
		RegionManager regionManager = worldGuard.getRegionManager(ObjectUnwrapper.convert(world));
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

		ProtectedRegion region = worldGuard.getRegionManager(ObjectUnwrapper.convert(world)).getRegion(name);
		if (region == null)
			return null;
		BlockVector point = region.getMaximumPoint();
		return world.getLocation(point.getX(), point.getY(), point.getZ());
	}

	/**
	 * Gets a list of plot owner names world guard has stored.
	 * If the player is stored by UUID then their username will not be stored.
	 * Players who have been converted from being stored as user names to UUIDs will not be returned.
	 * @param world The world the region is in.
	 * @param name Textual identifier of what the region is called.
	 * @return List of owner names.
	 */
	@Override
	public Set<String> getOwners(IWorld world, String name)
	{
		if (!serverHasWorldGuard())
			return null;

		return worldGuard.getRegionManager(ObjectUnwrapper.convert(world)).getRegion(name).getOwners().getPlayers();
	}

	/**
	 * Gets a list of plot owner unique IDs world guard has stored.
	 * If the player is stored by their username they will not have a UUID stored.
	 * Players who have not been converted from being stored by their user names to UUIDs will not be returned.
	 * @param world The world the region is in.
	 * @param name Textual identifier of what the region is called.
	 * @return List of owner unique IDs.
	 */
	@Override
	public Set<UUID> getOwnerUniqueIds(IWorld world, String name)
	{
		if (!serverHasWorldGuard())
			return null;

		return worldGuard.getRegionManager(ObjectUnwrapper.convert(world)).getRegion(name).getOwners().getUniqueIds();
	}

	/**
	 * Gets a list of plot owners in the form of IPlayers.
	 * The player objects are created from the UUIDs from the getOwnerUniqueIds method.
	 * @param world The world the region is in.
	 * @param name Textual identifier of what the region is called.
	 * @return List of plot owners.
	 */
	@Override
	public Set<IPlayer> getOwnerPlayers(IWorld world, String name)
	{
		if (!serverHasWorldGuard())
			return null;

		Set<UUID> owners = getOwnerUniqueIds(world, name);
		Set<IPlayer> ownerPlayers = new HashSet<>();
		for (UUID playerUUID : owners)
			ownerPlayers.add(server.getPlayer(playerUUID));

		return ownerPlayers;
	}

	/**
	 * Gets a list of plot member names world guard has stored.
	 * If the player is stored by UUID then their username will not be stored.
	 * Players who have been converted from being stored as user names to UUIDs will not be returned.
	 * @param world The world the region is in.
	 * @param name Textual identifier of what the region is called.
	 * @return List of member names.
	 */
	@Override
	public Set<String> getMembers(IWorld world, String name)
	{
		if (!serverHasWorldGuard())
			return null;

		return Sets.newHashSet(worldGuard.getRegionManager(ObjectUnwrapper.convert(world)).getRegion(name).getMembers().getPlayers());
	}

	/**
	 * Gets a list of plot member unique IDs world guard has stored.
	 * If the player is stored by their username they will not have a UUID stored.
	 * Players who have not been converted from being stored by their user names to UUIDs will not be returned.
	 * @param world The world the region is in.
	 * @param name Textual identifier of what the region is called.
	 * @return List of member unique IDs.
	 */
	@Override
	public Set<UUID> getMemberUniqueIds(IWorld world, String name)
	{
		if (!serverHasWorldGuard())
			return null;

		return Sets.newHashSet(worldGuard.getRegionManager(ObjectUnwrapper.convert(world)).getRegion(name).getMembers().getUniqueIds());
	}

	/**
	 * Gets a list of plot members in the form of IPlayers.
	 * The player objects are created from the UUIDs from the getMemberUniqueIds method.
	 * @param world The world the region is in.
	 * @param name Textual identifier of what the region is called.
	 * @return List of plot members.
	 */
	@Override
	public Set<IPlayer> getMemberPlayers(IWorld world, String name)
	{
		if (!serverHasWorldGuard())
			return null;

		Set<UUID> owners = getMemberUniqueIds(world, name);
		Set<IPlayer> memberPlayers = new HashSet<>();
		for (UUID playerUUID : owners)
			memberPlayers.add(server.getPlayer(playerUUID));

		return memberPlayers;
	}

	@Override
	public List<String> getOwnedRegions(IPlayer player, IWorld world)
	{
		if (world == null || player == null)
			return null;
		RegionManager regionManager = worldGuard.getRegionManager(ObjectUnwrapper.convert(world));
		ArrayList<String> regions = new ArrayList<>();
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
		RegionManager regionManager = worldGuard.getRegionManager(ObjectUnwrapper.convert(world));
		ArrayList<String> regions = new ArrayList<>();
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
			return new ArrayList<>(0);

		RegionManager regionManager = worldGuard.getRegionManager(ObjectUnwrapper.convert(world));
		return new ArrayList<>(regionManager.getRegions().keySet());
	}

	@Override
	public Map<String, Rectangle2D> getRegionRectanglesInWorld(IWorld world)
	{
		if (!serverHasWorldGuard() || world == null || worldGuard == null)
			return null;
		RegionManager regionManager = worldGuard.getRegionManager(ObjectUnwrapper.convert(world));
		Map<String, ProtectedRegion> regionSet = regionManager.getRegions();
		HashMap<String, Rectangle2D> result = new HashMap<>();
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
		RegionManager regionManager = worldGuard.getRegionManager(ObjectUnwrapper.convert(world));
		if (regionManager.getRegion(name) == null)
			return false;
		regionManager.removeRegion(name);
		return this.saveRegionManager(regionManager);
	}

	@Override
	public boolean createRegion(IPlayer owner, IWorld world, String name, ILocation pos1, ILocation pos2)
	{
		if (world == null || worldGuard == null)
			return false;

		RegionManager regionManager = worldGuard.getRegionManager(ObjectUnwrapper.convert(world));
		if (regionManager.hasRegion(name))
			return false;

		CuboidSelection selection = new CuboidSelection(
			ObjectUnwrapper.convert(world),
			ObjectUnwrapper.convert(pos1),
			(Location) ObjectUnwrapper.convert(pos2)
		);
		BlockVector min = selection.getNativeMinimumPoint().toBlockVector();
		BlockVector max = selection.getNativeMaximumPoint().toBlockVector();
		ProtectedRegion region = new ProtectedCuboidRegion(name, min, max);
		region.getOwners().addPlayer(owner.getUniqueId());
		regionManager.addRegion(region);
		this.saveRegionManager(regionManager);
		return regionManager.hasRegion(name);
	}

	@Override
	public boolean redefineRegion(IWorld world, String name, ILocation pos1, ILocation pos2)
	{
		if (world == null || worldGuard == null)
			return false;

		RegionManager regionManager = worldGuard.getRegionManager(ObjectUnwrapper.convert(world));
		ProtectedRegion existing = regionManager.getRegion(name);
		if (existing == null)
		{
			debugger.debugFine("Region manager does not know anything about the region %s in world %s!", name, world.getName());
			return false;
		}
		CuboidSelection selection = new CuboidSelection(
			ObjectUnwrapper.convert(world),
			ObjectUnwrapper.convert(pos1),
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
		return this.saveRegionManager(regionManager);
	}

	@Override
	public boolean addMemberToRegion(IWorld world, String name, IPlayer player)
	{
		if (!serverHasWorldGuard())
			return false;

		RegionManager regionManager = worldGuard.getRegionManager(ObjectUnwrapper.convert(world));
		DefaultDomain members = regionManager.getRegion(name).getMembers();
		if (!members.contains(player.getUniqueId()))
		{
			members.addPlayer(player.getUniqueId());
			return this.saveRegionManager(regionManager);
		}
		return false;
	}

	@Override
	public boolean removeMemberFromRegion(IWorld world, String name, IPlayer player)
	{
		if (!serverHasWorldGuard())
			return false;

		RegionManager regionManager = worldGuard.getRegionManager(ObjectUnwrapper.convert(world));
		DefaultDomain members = regionManager.getRegion(name).getMembers();
		if (members.contains(player.getUniqueId()))
		{
			members.removePlayer(player.getUniqueId());
			return this.saveRegionManager(regionManager);
		}
		return false;
	}

	@Override
	public boolean addOwnerToRegion(IWorld world, String name, IPlayer player)
	{
		if (!serverHasWorldGuard())
			return false;

		RegionManager regionManager = worldGuard.getRegionManager(ObjectUnwrapper.convert(world));
		DefaultDomain owners = regionManager.getRegion(name).getOwners();
		if (!owners.contains(player.getUniqueId()))
		{
			owners.addPlayer(player.getUniqueId());
			return this.saveRegionManager(regionManager);
		}
		return false;
	}

	@Override
	public boolean removeOwnerFromRegion(IWorld world, String name, IPlayer player)
	{
		if (!serverHasWorldGuard())
			return false;

		RegionManager regionManager = worldGuard.getRegionManager(ObjectUnwrapper.convert(world));
		DefaultDomain owners = regionManager.getRegion(name).getOwners();
		if (owners.contains(player.getUniqueId()))
		{
			owners.removePlayer(player.getUniqueId());
			return this.saveRegionManager(regionManager);
		}
		return false;
	}

	@Override
	public Rectangle2D getRectangle(IWorld world, String name)
	{
		if (!serverHasWorldGuard())
			return null;
		ProtectedRegion region = worldGuard.getRegionManager(ObjectUnwrapper.convert(world)).getRegion(name);
		if (region == null)
			return null;
		Rectangle2D.Double area = new Rectangle2D.Double();
		BlockVector min = region.getMinimumPoint();
		BlockVector max = region.getMaximumPoint();
		area.setRect(min.getX(), min.getZ(), max.getX() - min.getX(), max.getZ() - min.getZ());
		return area;
	}

	/**
	 * Converts usernames to UUIDs in every loaded region.
	 * Outputs results to the console.
	 *
	 * Should be removed once all UUIDs are updated.
	 */
	public void updateUUIDs()
	{
		if (!serverHasWorldGuard())
			return;

		console.logInformation("Updating player Unique IDs for regions in all loaded worlds.");

		// Loop through every loaded world.
		for (IWorld world : server.getWorlds())
		{
			String worldName = world.getName();
			console.logInformation("Updating world: " + worldName);
			RegionManager regionManager = worldGuard.getRegionManager(ObjectUnwrapper.convert(world));

			// Make sure there are regions in this world before proceeding.
			Map<String, ProtectedRegion> regions = regionManager.getRegions();
			if (regions.isEmpty())
				continue;

			// Loop through regions in a world
			for (String regionName : regions.keySet())
			{
				// Get region and make sure it's valid
				ProtectedRegion regionObject = regionManager.getRegion(regionName);
				if (regionObject == null)
				{
					console.logWarning("Invalid region %s in world %s. ", regionName, worldName);
					continue;
				}

				// Convert Owners
				DefaultDomain regionOwners = regionObject.getOwners();
				if (regionOwners.size() > 0)
					updateRegionMembers(regionOwners, regionName);

				// Convert Members
				DefaultDomain regionMembers = regionObject.getMembers();
				if (regionMembers.size() > 0)
					updateRegionMembers(regionMembers, regionName);
			}

			// Save changes for the world.
			if (this.saveRegionManager(regionManager))
				console.logInformation("Saving region changes for world: " + world);
			else
				console.logError("Changes for world %s could not be saved.", world);
		}

		console.logInformation("Updating player Unique IDs complete.");
	}

	/**
	 * Converts member/owner names to UUID.
	 * Intended to be used with the updateUUIDs method.
	 * WARNING: Does not save changes, save regionManager after using this method.
	 * @param regionPlayers Domain of members or owners to convert.
	 * @param regionName Name of the region the region players are from.
	 *                   Only used to output information to the console.
	 *
	 * Should be removed once all UUIDs are updated.
	 */
	private void updateRegionMembers(DefaultDomain regionPlayers, String regionName)
	{
		for (String playerName : regionPlayers.getPlayers())
		{
			UUID playerID = server.getUniqueId(playerName);
			if (playerID != null)
			{
				// Remove player name
				regionPlayers.removePlayer(playerName);
				// Set player ID
				regionPlayers.addPlayer(playerID);
				// Output information
				console.logInformation(
					"Region player %s updated with UUID %s for region %s.",
					playerName, playerID, regionName
				);
			}
			else
				console.logWarning(
					"Player %s for region %s could not be updated.",
					playerName, regionName
				);
		}
	}

	/**
	 * Saves the region manager.
	 * Catches any StorageExceptions it might throw and outputs them to the console.
	 * @param regionManager Thing to save.
	 * @return True if successful, false if unsuccessful.
	 */
	private boolean saveRegionManager(RegionManager regionManager)
	{
		try
		{
			regionManager.save();
			return true;
		}
		catch (StorageException e)
		{
			console.logException(e);
			return false;
		}
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
