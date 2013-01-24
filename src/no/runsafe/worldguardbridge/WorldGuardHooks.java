package no.runsafe.worldguardbridge;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import no.runsafe.framework.hook.IPlayerBuildPermission;
import no.runsafe.framework.hook.IPlayerPvPFlag;
import no.runsafe.framework.server.RunsafeLocation;
import no.runsafe.framework.server.player.RunsafePlayer;

public class WorldGuardHooks implements IPlayerBuildPermission, IPlayerPvPFlag
{

	public WorldGuardHooks(WorldGuardInterface worldGuard)
	{
		this.worldGuard = worldGuard;
	}

	@Override
	public boolean blockPlayerBuilding(RunsafePlayer player, RunsafeLocation location)
	{
		if (!worldGuard.serverHasWorldGuard())
			return false;

		return !worldGuard.getGlobalRegionManager().canBuild(player.getRawPlayer(), location.getRaw());
	}

	@Override
	public boolean isFlaggedForPvP(RunsafePlayer player)
	{
		if (!worldGuard.serverHasWorldGuard())
			return true;

		RegionManager manager = worldGuard.getGlobalRegionManager().get(player.getWorld().getRaw());
		RunsafeLocation playerLocation = player.getLocation();
		BlockVector location = new BlockVector(playerLocation.getX(), playerLocation.getY(), playerLocation.getZ());
		ApplicableRegionSet applicable = manager.getApplicableRegions(location);
		return applicable.allows(DefaultFlag.PVP, worldGuard.wrapPlayer(player.getRawPlayer()));
	}

	private final WorldGuardInterface worldGuard;
}
