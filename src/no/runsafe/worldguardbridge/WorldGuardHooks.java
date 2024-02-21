package no.runsafe.worldguardbridge;

import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.hook.IPlayerBuildPermission;
import no.runsafe.framework.api.hook.IPlayerPvPFlag;
import no.runsafe.framework.api.player.IPlayer;

public class WorldGuardHooks implements IPlayerBuildPermission, IPlayerPvPFlag
{

	public WorldGuardHooks(WorldGuardInterface worldGuard)
	{
		this.worldGuard = worldGuard;
	}

	@Override
	public boolean blockPlayerBuilding(IPlayer player, ILocation location)
	{
		return worldGuard.serverHasWorldGuard() && !worldGuard.playerCanBuildHere(player, location);
	}

	@Override
	public boolean isPvPDisabled(IPlayer player)
	{
		if (!worldGuard.serverHasWorldGuard())
			return false;

		ILocation playerLocation = player.getLocation();
		if (playerLocation == null)
		{
			return true;
		}
		return !worldGuard.isInPvPZone(player);
	}

	private final WorldGuardInterface worldGuard;
}
