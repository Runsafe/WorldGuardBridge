package no.runsafe.worldguardbridge;

import no.runsafe.framework.messaging.IMessageBusService;
import no.runsafe.framework.messaging.Message;
import no.runsafe.framework.messaging.MessageBusStatus;
import no.runsafe.framework.messaging.Response;
import no.runsafe.framework.output.IOutput;

public class WorldGuardService implements IMessageBusService
{
	private final IOutput output;
	private final WorldGuardInterface worldGuardInterface;
	private boolean hasWarned = false;

	public WorldGuardService(IOutput output, WorldGuardInterface worldGuardInterface)
	{
		this.output = output;
		this.worldGuardInterface = worldGuardInterface;
	}

	public String getServiceName()
	{
		return "WorldGuardBridge";
	}

	public Response processMessage(Message message)
	{
		if(this.worldGuardInterface.serverHasWorldGuard())
		{
			this.hasWarned = false;
			WorldGuardQuestions response = WorldGuardQuestions.valueOf(message.getQuestion());
			Response returnResponse = new Response();
			returnResponse.setSourceService(this.getServiceName());

			switch(response)
			{
				case PLAYER_IN_PVP_ZONE:
					if(this.worldGuardInterface.isInPvPZone(message.getPlayer()))
						returnResponse.setStatus(MessageBusStatus.OK);
					else
						returnResponse.setStatus(MessageBusStatus.NOT_OK);
					break;

				case PLAYER_IN_REGION:
					String regionName = this.worldGuardInterface.getCurrentRegion(message.getPlayer());
					if(regionName == null)
						returnResponse.setStatus(MessageBusStatus.NOT_OK);
					else
					{
						returnResponse.setStatus(MessageBusStatus.OK);
						returnResponse.setResponse(regionName);
					}

				default:
					returnResponse = null;
					break;
			}

			return returnResponse;
		} else
		{
			if(!this.hasWarned)
			{
				this.output.outputToConsole(Constants.WORLD_GUARD_MISSING);
				this.hasWarned = true;
			}

			return null;
		}
	}
}
