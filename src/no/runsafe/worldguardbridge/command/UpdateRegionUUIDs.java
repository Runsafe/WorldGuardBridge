package no.runsafe.worldguardbridge.command;

import no.runsafe.framework.api.command.ExecutableCommand;
import no.runsafe.framework.api.command.ICommandExecutor;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.worldguardbridge.IRegionControl;

public class UpdateRegionUUIDs extends ExecutableCommand
{
	public UpdateRegionUUIDs(IRegionControl regionControl)
	{
		super(
			"updateregionuuids",
			"Updates regions to store playerIDs instead of usernames.",
			"runsafe.worldguard"
		);
		this.regionControl = regionControl;
	}

	@Override
	public String OnExecute(ICommandExecutor executor, IArgumentList iArgumentList)
	{
		regionControl.updateUUIDs();
		return "&eRegions updated. Check the console for details.";
	}

	private final IRegionControl regionControl;
}
