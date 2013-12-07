package no.runsafe.worldguardbridge;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.minecraft.RunsafeLocation;
import no.runsafe.framework.minecraft.RunsafeWorld;

import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IRegionControl
{
	boolean isInPvPZone(IPlayer player);

	String getCurrentRegion(IPlayer player);

	ProtectedRegion getRegion(RunsafeWorld world, String name);

	List<IPlayer> getPlayersInRegion(RunsafeWorld world, String regionName);

	List<String> getRegionsAtLocation(RunsafeLocation location);

	List<String> getApplicableRegions(IPlayer player);

	Map<String, Set<String>> getAllRegionsWithOwnersInWorld(RunsafeWorld world);

	RunsafeLocation getRegionLocation(RunsafeWorld world, String name);

	Set<String> getOwners(RunsafeWorld world, String name);

	Set<String> getMembers(RunsafeWorld world, String name);

	List<String> getOwnedRegions(IPlayer player, RunsafeWorld world);

	List<String> getRegionsInWorld(RunsafeWorld world);

	Map<String, Rectangle2D> getRegionRectanglesInWorld(RunsafeWorld world);

	boolean deleteRegion(RunsafeWorld world, String name);

	boolean createRegion(IPlayer owner, RunsafeWorld world, String name, RunsafeLocation pos1, RunsafeLocation pos2);

	boolean redefineRegion(RunsafeWorld world, String name, RunsafeLocation pos1, RunsafeLocation pos2);

	boolean addMemberToRegion(RunsafeWorld world, String name, IPlayer player);

	boolean removeMemberFromRegion(RunsafeWorld world, String name, IPlayer player);

	Rectangle2D getRectangle(RunsafeWorld world, String name);
}
