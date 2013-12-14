package no.runsafe.worldguardbridge;

import no.runsafe.framework.RunsafeConfigurablePlugin;
import no.runsafe.worldguardbridge.event.RegionBorderPatrol;

public class Plugin extends RunsafeConfigurablePlugin
{
	@Override
	protected void PluginSetup()
	{
		exportAPI(getInstance(WorldGuardInterface.class));
		this.addComponent(WorldGuardHooks.class);
		this.addComponent(RegionBorderPatrol.class);
	}
}
