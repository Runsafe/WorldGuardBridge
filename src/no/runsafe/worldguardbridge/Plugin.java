package no.runsafe.worldguardbridge;

import no.runsafe.framework.RunsafeConfigurablePlugin;
import no.runsafe.framework.features.Events;
import no.runsafe.framework.features.FrameworkHooks;
import no.runsafe.worldguardbridge.event.RegionBorderPatrol;

public class Plugin extends RunsafeConfigurablePlugin
{
	@Override
	protected void PluginSetup()
	{
		addComponent(Events.class);
		addComponent(FrameworkHooks.class);

		exportAPI(getInstance(WorldGuardInterface.class));
		this.addComponent(WorldGuardHooks.class);
		this.addComponent(RegionBorderPatrol.class);
	}
}
