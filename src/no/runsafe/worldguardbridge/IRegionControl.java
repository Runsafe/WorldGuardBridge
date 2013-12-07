package no.runsafe.worldguardbridge;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import no.runsafe.framework.api.IWorld;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.minecraft.RunsafeLocation;

import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IRegionControl
{
	boolean isInPvPZone(IPlayer player);

	String getCurrentRegion(IPlayer player);

	ProtectedRegion getRegion(IWorld world, String name);

	List<IPlayer> getPlayersInRegion(IWorld world, String regionName);

	List<String> getRegionsAtLocation(RunsafeLocation location);

	List<String> getApplicableRegions(IPlayer player);

	Map<String, Set<String>> getAllRegionsWithOwnersInWorld(IWorld world);

	RunsafeLocation getRegionLocation(IWorld world, String name);

	Set<String> getOwners(IWorld world, String name);

	Set<String> getMembers(IWorld world, String name);

	List<String> getOwnedRegions(IPlayer player, IWorld world);

	List<String> getRegionsInWorld(IWorld world);

	Map<String, Rectangle2D> getRegionRectanglesInWorld(IWorld world);

	boolean deleteRegion(IWorld world, String name);

	boolean createRegion(IPlayer owner, IWorld world, String name, RunsafeLocation pos1, RunsafeLocation pos2);

	boolean redefineRegion(IWorld world, String name, RunsafeLocation pos1, RunsafeLocation pos2);

	boolean addMemberToRegion(IWorld world, String name, IPlayer player);

	boolean removeMemberFromRegion(IWorld world, String name, IPlayer player);

	Rectangle2D getRectangle(IWorld world, String name);
}
