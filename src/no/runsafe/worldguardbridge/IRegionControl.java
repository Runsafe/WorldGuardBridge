package no.runsafe.worldguardbridge;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.IWorld;
import no.runsafe.framework.api.player.IPlayer;

import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface IRegionControl
{
	boolean serverHasWorldGuard();

	boolean isInPvPZone(IPlayer player);

	String getCurrentRegion(IPlayer player);

	ProtectedRegion getRegion(IWorld world, String name);

	List<IPlayer> getPlayersInRegion(IWorld world, String regionName);

	List<String> getRegionsAtLocation(ILocation location);

	List<String> getApplicableRegions(IPlayer player);

	Map<String, Set<IPlayer>> getAllRegionsWithOwnersInWorld(IWorld world);

	ILocation getRegionLocation(IWorld world, String name);

	Set<String> getOwners(IWorld world, String name);

	Set<UUID> getOwnerUniqueIds(IWorld world, String name);

	Set<IPlayer> getOwnerPlayers(IWorld world, String name);

	Set<String> getMembers(IWorld world, String name);

	Set<UUID> getMemberUniqueIds(IWorld world, String name);

	Set<IPlayer> getMemberPlayers(IWorld world, String name);

	List<String> getOwnedRegions(IPlayer player, IWorld world);

	List<String> getRegionsInWorld(IWorld world);

	Map<String, Rectangle2D> getRegionRectanglesInWorld(IWorld world);

	boolean deleteRegion(IWorld world, String name);

	boolean createRegion(IPlayer owner, IWorld world, String name, ILocation pos1, ILocation pos2);

	boolean redefineRegion(IWorld world, String name, ILocation pos1, ILocation pos2);

	boolean addMemberToRegion(IWorld world, String name, IPlayer player);

	boolean removeMemberFromRegion(IWorld world, String name, IPlayer player);

	Rectangle2D getRectangle(IWorld world, String name);

	boolean playerCanBuildHere(IPlayer player, ILocation location);
}
