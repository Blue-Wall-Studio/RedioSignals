package org.BlueWallStudio.rediosignals;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.BlueWallStudio.rediosignals.debug.CommandRegister;
import org.BlueWallStudio.rediosignals.blocks.ModBlocks;
import org.BlueWallStudio.rediosignals.config.ModConfig;
import org.BlueWallStudio.rediosignals.network.NetworkHandler;
import org.BlueWallStudio.rediosignals.wire.WireRegistry;
import org.BlueWallStudio.rediosignals.wireless.receiver.WirelessReceiverRegistry;
import org.BlueWallStudio.rediosignals.wireless.transmitter.WirelessTransmitterRegistry;

public class RedioSignals implements ModInitializer {
    public static final String MOD_ID = "rediosignals";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        System.out.println("[Redio Signals] Initializing Argest mod...");

        // Config initialization
        ModConfig.getInstance();

        // Blocks and block entities registration
        ModBlocks.init();

        // Wire types registration
        WireRegistry.init();

        WirelessReceiverRegistry.initializeDefaults();

        WirelessTransmitterRegistry.initializeDefaults();

        WorldEventHandler.registerEvents();

        // Network packets configuration
        NetworkHandler.init();

        // Commands registration
        CommandRegistrationCallback.EVENT
                .register(
                        (dispatcher, registryAccess, environment) -> CommandRegister.register(dispatcher, environment));

        System.out.println("[Redio Signals] Mod initialized successfully!");
    }
}
