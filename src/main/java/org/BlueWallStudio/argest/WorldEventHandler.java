package org.BlueWallStudio.argest;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.BlueWallStudio.argest.signal.SignalManager;
import org.BlueWallStudio.argest.config.ModConfig;

public class WorldEventHandler {
    private static int tickCounter = 0;
    private static int INTERVAL = ModConfig.getInstance().signalProcessingDelay;

    public static void registerEvents() {
        // Register server world tick event to process packets every N ticks
        ServerTickEvents.END_WORLD_TICK.register((world) -> {
            tickCounter++;
            if (tickCounter >= INTERVAL) {
                SignalManager.tick(world);
                tickCounter = 0;
            }
        });
    }
}
