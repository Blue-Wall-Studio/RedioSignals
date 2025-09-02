package org.BlueWallStudio.argest;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.BlueWallStudio.argest.signal.SignalManager;
import org.BlueWallStudio.argest.wire.WireRegistry;
import org.BlueWallStudio.argest.debug.DebugCommand;
import org.BlueWallStudio.argest.blocks.ModBlocks;
import org.BlueWallStudio.argest.network.NetworkHandler;
import org.BlueWallStudio.argest.config.ModConfig;
import org.BlueWallStudio.argest.wireless.receiver.WirelessReceiverRegistry;
import org.BlueWallStudio.argest.wireless.transmitter.WirelessTransmitterRegistry;

public class Argest implements ModInitializer {
    public static final String MOD_ID = "argest";

    @Override
    public void onInitialize() {
        System.out.println("[Argest] Initializing Argest mod...");

        // Инициализируем конфигурацию
        ModConfig.getInstance();

        // Регистрируем блоки и блок-энтити
        ModBlocks.init();

        // Регистрируем типы проводов
        WireRegistry.init();

        WirelessReceiverRegistry.initializeDefaults();

        WirelessTransmitterRegistry.initializeDefaults();

        WorldEventHandler.registerEvents();

        // Настраиваем сетевые пакеты
        NetworkHandler.init();

        // Регистрируем команды
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> DebugCommand.register(dispatcher, environment));

        // Регистрируем тик-обработчики
        ServerTickEvents.END_WORLD_TICK.register(world -> SignalManager.getInstance(world).tick());

        System.out.println("[Argest] Mod initialized successfully!");
    }
}
