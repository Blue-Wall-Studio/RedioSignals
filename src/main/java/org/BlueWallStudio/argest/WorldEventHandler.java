package org.BlueWallStudio.argest;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.server.world.ServerWorld;
import org.BlueWallStudio.argest.signal.SignalManager;

public class WorldEventHandler {

    public static void registerEvents() {
        // Регистрируем обработчик выгрузки мира
        ServerWorldEvents.UNLOAD.register((server, world) -> {
            // Очищаем кэш SignalManager при выгрузке мира
            SignalManager.onWorldUnload(world);
        });

        // Регистрируем тик сервера для обработки пакетов
        ServerTickEvents.END_WORLD_TICK.register((world) -> {
            SignalManager.getInstance(world).tick();
        });

        // Опционально: принудительное сохранение при остановке сервера
        ServerLifecycleEvents.SERVER_STOPPING.register((server) -> {
            // Помечаем все SignalManager как dirty для принудительного сохранения
            for (ServerWorld world : server.getWorlds()) {
                SignalManager manager = SignalManager.getInstance(world);
                manager.markDirty();
                // PersistentStateManager автоматически сохранит данные при остановке сервера
            }
        });
    }
}
