package org.BlueWallStudio.argest;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.BlueWallStudio.argest.packet.PacketManager;
import org.BlueWallStudio.argest.config.ModConfig;

public class WorldEventHandler {
    private static int tickCounter = 0;
    private static int INTERVAL = ModConfig.getInstance().packetProcessingDelay;

    public static void registerEvents() {
        // Register server world tick event to process packets every N ticks
        ServerTickEvents.END_WORLD_TICK.register((world) -> {
            tickCounter++;
            if (tickCounter >= INTERVAL) {
                PacketManager.tick(world);
                tickCounter = 0;
            }
        });
    }
}
