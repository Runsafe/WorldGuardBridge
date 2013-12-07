package no.runsafe.worldguardbridge;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import no.runsafe.framework.api.hook.IPlayerBuildPermission;
import no.runsafe.framework.api.hook.IPlayerPvPFlag;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.internal.wrapper.ObjectUnwrapper;
import no.runsafe.framework.minecraft.RunsafeLocation;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class WorldGuardHooks implements IPlayerBuildPermission, IPlayerPvPFlag
{

	public WorldGuardHooks(WorldGuardInterface worldGuard)
	{
		this.worldGuard = worldGuard;
	}

	@Override
	public boolean blockPlayerBuilding(IPlayer player, RunsafeLocation location)
	{
		return worldGuard.serverHasWorldGuard()
			&& !worldGuard.getGlobalRegionManager().canBuild((Player) ObjectUnwrapper.convert(player), location.getRaw());
	}

	@Override
	public boolean isPvPDisabled(IPlayer player)
	{
		if (!worldGuard.serverHasWorldGuard())
			return false;

		RegionManager manager = worldGuard.getGlobalRegionManager().get((World)ObjectUnwrapper.convert(player.getWorld()));
		RunsafeLocation playerLocation = player.getLocation();
		BlockVector location = new BlockVector(playerLocation.getX(), playerLocation.getY(), playerLocation.getZ());
		ApplicableRegionSet applicable = manager.getApplicableRegions(location);
		return !applicable.allows(DefaultFlag.PVP, worldGuard.wrapPlayer((Player) ObjectUnwrapper.convert(player)));
	}

	private final WorldGuardInterface worldGuard;
}
