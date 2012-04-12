package no.runsafe.worldguardbridge;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import no.runsafe.framework.RunsafePlugin;
import no.runsafe.framework.messaging.IMessageBusService;
import no.runsafe.framework.messaging.Message;
import no.runsafe.framework.messaging.Response;
import org.bukkit.plugin.Plugin;

public class Bridge extends RunsafePlugin
{

    @Override
    protected void PluginSetup()
    {
        this.addComponent(WorldGuardInterface.class);
        this.addComponent(MessageProcessor.class);
    }

}
