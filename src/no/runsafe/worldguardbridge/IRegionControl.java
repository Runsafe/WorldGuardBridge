package no.runsafe.worldguardbridge;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import no.runsafe.framework.minecraft.RunsafeLocation;
import no.runsafe.framework.minecraft.RunsafeWorld;
import no.runsafe.framework.minecraft.player.RunsafePlayer;

import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IRegionControl
{
	boolean isInPvPZone(RunsafePlayer player);

	String getCurrentRegion(RunsafePlayer player);

	ProtectedRegion getRegion(RunsafeWorld world, String name);

	List<RunsafePlayer> getPlayersInRegion(RunsafeWorld world, String regionName);

	List<String> getRegionsAtLocation(RunsafeLocation location);

	List<String> getApplicableRegions(RunsafePlayer player);

	Map<String, Set<String>> getAllRegionsWithOwnersInWorld(RunsafeWorld world);

	RunsafeLocation getRegionLocation(RunsafeWorld world, String name);

	Set<String> getOwners(RunsafeWorld world, String name);

	Set<String> getMembers(RunsafeWorld world, String name);

	List<String> getOwnedRegions(RunsafePlayer player, RunsafeWorld world);

	List<String> getRegionsInWorld(RunsafeWorld world);

	Map<String, Rectangle2D> getRegionRectanglesInWorld(RunsafeWorld world);

	boolean deleteRegion(RunsafeWorld world, String name);

	boolean createRegion(RunsafePlayer owner, RunsafeWorld world, String name, RunsafeLocation pos1, RunsafeLocation pos2);

	boolean redefineRegion(RunsafeWorld world, String name, RunsafeLocation pos1, RunsafeLocation pos2);

	boolean addMemberToRegion(RunsafeWorld world, String name, RunsafePlayer player);

	boolean removeMemberFromRegion(RunsafeWorld world, String name, RunsafePlayer player);

	Rectangle2D getRectangle(RunsafeWorld world, String name);
}
