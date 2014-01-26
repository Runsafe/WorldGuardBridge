package no.runsafe.worldguardbridge.command;

import no.runsafe.framework.api.command.ExecutableCommand;
import no.runsafe.framework.api.command.ICommandExecutor;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.worldguardbridge.event.RegionBorderPatrol;

public class RescanRegions extends ExecutableCommand
{
	public RescanRegions(RegionBorderPatrol patroller)
	{
		super("rescanwgregions", "Rescans regions for region enter events", "runsafe.worldguard");
		this.patroller = patroller;
	}

	@Override
	public String OnExecute(ICommandExecutor executor, IArgumentList iArgumentList)
	{
		patroller.OnServerReady();
		return "Regions have been refreshed.";
	}

	private final RegionBorderPatrol patroller;
}
