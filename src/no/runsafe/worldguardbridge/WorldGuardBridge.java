package no.runsafe.worldguardbridge;

import no.runsafe.framework.RunsafePlugin;
import no.runsafe.framework.features.Events;
import no.runsafe.framework.features.FrameworkHooks;
import no.runsafe.worldguardbridge.event.RegionBorderPatrol;

public class WorldGuardBridge extends RunsafePlugin
{
	@Override
	protected void pluginSetup()
	{
		addComponent(Events.class);
		addComponent(FrameworkHooks.class);

		exportAPI(getInstance(WorldGuardInterface.class));
		addComponent(WorldGuardHooks.class);
		addComponent(RegionBorderPatrol.class);
	}
}
