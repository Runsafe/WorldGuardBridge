package no.runsafe.worldguardbridge;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import no.runsafe.framework.api.hook.IPlayerBuildPermission;
import no.runsafe.framework.api.hook.IPlayerPvPFlag;
import no.runsafe.framework.minecraft.RunsafeLocation;
import no.runsafe.framework.minecraft.player.RunsafePlayer;

public class WorldGuardHooks implements IPlayerBuildPermission, IPlayerPvPFlag
{

	public WorldGuardHooks(WorldGuardInterface worldGuard)
	{
		this.worldGuard = worldGuard;
	}

	@Override
	public boolean blockPlayerBuilding(RunsafePlayer player, RunsafeLocation location)
	{
		return worldGuard.serverHasWorldGuard()
			&& !worldGuard.getGlobalRegionManager().canBuild(player.getRawPlayer(), location.getRaw());
	}

	@Override
	public boolean isPvPDisabled(RunsafePlayer player)
	{
		if (!worldGuard.serverHasWorldGuard())
			return false;

		RegionManager manager = worldGuard.getGlobalRegionManager().get(player.getWorld().getRaw());
		RunsafeLocation playerLocation = player.getLocation();
		BlockVector location = new BlockVector(playerLocation.getX(), playerLocation.getY(), playerLocation.getZ());
		ApplicableRegionSet applicable = manager.getApplicableRegions(location);
		return !applicable.allows(DefaultFlag.PVP, worldGuard.wrapPlayer(player.getRawPlayer()));
	}

	private final WorldGuardInterface worldGuard;
}
