package no.runsafe.worldguardbridge;

import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.IWorld;
import no.runsafe.framework.api.player.IPlayer;

import javax.annotation.Nonnull;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface IRegionControl
{
	boolean worldGuardIsMissing();

	boolean isInPvPZone(IPlayer player);

	List<IPlayer> getPlayersInRegion(IWorld world, String regionName);

	List<String> getRegionsAtLocation(ILocation location);

	List<String> getApplicableRegions(IPlayer player);

	Map<String, Set<IPlayer>> getAllRegionsWithOwnersInWorld(IWorld world);

	ILocation getRegionLocation(IWorld world, String name);

	Set<UUID> getOwnerUniqueIds(IWorld world, String name);

	Set<IPlayer> getOwnerPlayers(IWorld world, String name);

	Set<UUID> getMemberUniqueIds(IWorld world, String name);

	@Nonnull
	Set<IPlayer> getMemberPlayers(IWorld world, String name);

	List<String> getOwnedRegions(IPlayer player, IWorld world);

	List<String> getRegionsInWorld(IWorld world);

	Map<String, Rectangle2D> getRegionRectanglesInWorld(IWorld world);

	void deleteRegion(IWorld world, String name);

	boolean createRegion(IPlayer owner, IWorld world, String name, ILocation pos1, ILocation pos2);

	boolean redefineRegion(IWorld world, String name, ILocation pos1, ILocation pos2);

	void renameRegion(IWorld world, String currentName, String newName);

	boolean addMemberToRegion(IWorld world, String name, IPlayer player);

	boolean removeMemberFromRegion(IWorld world, String name, IPlayer player);

	Rectangle2D getRectangle(IWorld world, String name);

	boolean playerCannotBuildHere(IPlayer player, ILocation location);
}
