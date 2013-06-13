package no.runsafe.worldguardbridge.event;

import no.runsafe.framework.minecraft.RunsafeWorld;
import no.runsafe.framework.minecraft.event.player.RunsafeCustomEvent;
import no.runsafe.framework.minecraft.player.RunsafePlayer;

public class CustomEvents
{
	public static void Enter(RunsafePlayer player, RunsafeWorld world, String region)
	{
		new RunsafeCustomEvent(player, "region.enter", new RegionData(world, region)).Fire();
	}

	public static void Leave(RunsafePlayer player, RunsafeWorld world, String region)
	{
		new RunsafeCustomEvent(player, "region.leave", new RegionData(world, region)).Fire();
	}
}
