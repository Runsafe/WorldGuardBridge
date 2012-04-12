package no.runsafe.worldguardbridge;

import no.runsafe.framework.messaging.IMessageBusService;
import no.runsafe.framework.messaging.Message;
import no.runsafe.framework.messaging.MessageBusStatus;
import no.runsafe.framework.messaging.Response;
import no.runsafe.framework.output.IOutput;

public class MessageProcessor implements IMessageBusService
{
    private IOutput output;
    private WorldGuardInterface worldGuardInterface;
    private boolean hasWarned = false;

    public MessageProcessor(IOutput output, WorldGuardInterface worldGuardInterface)
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
        if (this.worldGuardInterface.serverHasWorldGuard())
        {
            this.hasWarned = false;
            BridgeResponse response = BridgeResponse.valueOf(message.getQuestion());
            Response returnResponse = new Response();
            returnResponse.setSourceService(this.getServiceName());

            switch (response)
            {
                case PLAYER_IN_PVP_ZONE:
                    if (this.worldGuardInterface.isInPvPZone(message.getPlayer()))
                        returnResponse.setStatus(MessageBusStatus.OK);
                    else
                        returnResponse.setStatus(MessageBusStatus.NOT_OK);
                break;

                default:
                    returnResponse = null;
                break;
            }

            return returnResponse;
        }
        else
        {
            if (!this.hasWarned)
            {
                this.output.outputToConsole(Constants.WORLD_GUARD_MISSING);
                this.hasWarned = true;
            }

            return null;
        }
    }
}
