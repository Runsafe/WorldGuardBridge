package no.runsafe.worldguardbridge;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import no.runsafe.framework.player.RunsafePlayer;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

public class WorldGuardInterface
{
	private Server server;
	private WorldGuardPlugin worldGuard;

	public WorldGuardInterface(Server server)
	{
		this.server = server;
	}

	private WorldGuardPlugin getWorldGuard(Server server)
	{
		Plugin plugin = server.getPluginManager().getPlugin("WorldGuard");

		if (plugin == null || !(plugin instanceof WorldGuardPlugin))
			return null;

		return (WorldGuardPlugin) plugin;
	}

	public boolean serverHasWorldGuard()
	{
		if (this.worldGuard == null)
			this.worldGuard = this.getWorldGuard(this.server);

		if (this.worldGuard != null)
			return true;

		return false;
	}

	public boolean isInPvPZone(RunsafePlayer player)
	{
		RegionManager regionManager = worldGuard.getRegionManager(player.getWorld().getRaw());
		ApplicableRegionSet set = regionManager.getApplicableRegions(player.getRaw().getLocation());

		return set.allows(DefaultFlag.PVP);
	}

	public String getCurrentRegion(RunsafePlayer player)
	{
		RegionManager regionManager = worldGuard.getRegionManager(player.getWorld().getRaw());
		ApplicableRegionSet set = regionManager.getApplicableRegions(player.getRaw().getLocation());
		if (set.size() == 0)
			return null;
		StringBuilder sb = new StringBuilder();
		for (ProtectedRegion r : set)
		{
			if (sb.length() > 0)
				sb.append(";");
			sb.append(r.getId());
		}
		return sb.toString();
	}
}
