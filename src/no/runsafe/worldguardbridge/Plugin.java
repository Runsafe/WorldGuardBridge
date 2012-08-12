package no.runsafe.worldguardbridge;

import no.runsafe.framework.RunsafePlugin;

public class Plugin extends RunsafePlugin
{
	@Override
	protected void PluginSetup()
	{
		this.addComponent(WorldGuardInterface.class);
		this.addComponent(WorldGuardService.class);
	}
}
