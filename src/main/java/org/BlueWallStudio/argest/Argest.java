package org.BlueWallStudio.argest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.BlueWallStudio.argest.debug.CommandRegister;
import org.BlueWallStudio.argest.blocks.ModBlocks;
import org.BlueWallStudio.argest.config.ModConfig;
import org.BlueWallStudio.argest.network.NetworkHandler;
import org.BlueWallStudio.argest.wire.WireRegistry;
import org.BlueWallStudio.argest.wireless.receiver.WirelessReceiverRegistry;
import org.BlueWallStudio.argest.wireless.transmitter.WirelessTransmitterRegistry;

public class Argest implements ModInitializer {
    public static final String MOD_ID = "argest";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        System.out.println("[Argest] Initializing Argest mod...");

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

        System.out.println("[Argest] Mod initialized successfully!");
    }
}
