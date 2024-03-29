package no.runsafe.worldguardbridge;

import no.runsafe.framework.RunsafeConfigurablePlugin;
import no.runsafe.framework.features.Commands;
import no.runsafe.framework.features.Events;
import no.runsafe.framework.features.FrameworkHooks;
import no.runsafe.worldguardbridge.command.*;
import no.runsafe.worldguardbridge.event.RegionBorderPatrol;

public class WorldGuardBridge extends RunsafeConfigurablePlugin
{
	@Override
	protected void pluginSetup()
	{
		addComponent(Events.class);
		addComponent(FrameworkHooks.class);
		addComponent(Commands.class);

		exportAPI(getInstance(WorldGuardInterface.class));
		addComponent(WorldGuardHooks.class);
		addComponent(RegionBorderPatrol.class);
		addComponent(RescanRegions.class);
	}
}
