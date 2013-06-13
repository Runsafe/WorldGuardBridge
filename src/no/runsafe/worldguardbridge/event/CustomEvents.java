package no.runsafe.worldguardbridge.event;

import no.runsafe.framework.minecraft.event.player.RunsafeCustomEvent;
import no.runsafe.framework.minecraft.player.RunsafePlayer;

public class CustomEvents
{
	public static void Enter(RunsafePlayer player, String region)
	{
		new RunsafeCustomEvent(player, "region.enter", region).Fire();
	}

	public static void Leave(RunsafePlayer player, String region)
	{
		new RunsafeCustomEvent(player, "region.leave", region);
	}
}
