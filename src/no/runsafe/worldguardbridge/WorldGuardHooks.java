package no.runsafe.worldguardbridge;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.hook.IPlayerBuildPermission;
import no.runsafe.framework.api.hook.IPlayerPvPFlag;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.internal.wrapper.ObjectUnwrapper;
import org.bukkit.Location;

public class WorldGuardHooks implements IPlayerBuildPermission, IPlayerPvPFlag
{

	public WorldGuardHooks(WorldGuardInterface worldGuard)
	{
		this.worldGuard = worldGuard;
	}

	@Override
	public boolean blockPlayerBuilding(IPlayer player, ILocation location)
	{
		return worldGuard.serverHasWorldGuard()
			&& !worldGuard.getGlobalRegionManager()
			.canBuild(ObjectUnwrapper.convert(player), (Location) ObjectUnwrapper.convert(location));
	}

	@Override
	public boolean isPvPDisabled(IPlayer player)
	{
		if (!worldGuard.serverHasWorldGuard())
			return false;

		RegionManager manager = worldGuard.getGlobalRegionManager().get(ObjectUnwrapper.convert(player.getWorld()));
		ILocation playerLocation = player.getLocation();
		BlockVector location = new BlockVector(playerLocation.getX(), playerLocation.getY(), playerLocation.getZ());
		ApplicableRegionSet applicable = manager.getApplicableRegions(location);
		return !applicable.allows(DefaultFlag.PVP, worldGuard.wrapPlayer( ObjectUnwrapper.convert(player)));
	}

	private final WorldGuardInterface worldGuard;
}
