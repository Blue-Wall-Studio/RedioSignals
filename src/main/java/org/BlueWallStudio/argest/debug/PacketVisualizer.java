package org.BlueWallStudio.argest.debug;

import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import java.util.*;
import org.BlueWallStudio.argest.signal.SignalPacket;

public class PacketVisualizer {
    public void showPacketCreation(SignalPacket packet, Set<UUID> debugPlayers) {
        ServerWorld world = getWorldFromPacket(packet);
        if (world == null) return;

        BlockPos pos = packet.getCurrentPos();

        // Показываем частицы ветра (или другие подходящие)
        for (UUID playerId : debugPlayers) {
            ServerPlayerEntity player = world.getServer().getPlayerManager().getPlayer(playerId);
            if (player != null && player.getWorld() == world) {
                // Частицы для визуализации создания пакета - исправленный вызов
                world.spawnParticles(
                        new DustParticleEffect(0x00FF00, 1.0f),
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        10, 0.3, 0.3, 0.3, 0.1
                );

                // Информация в чат
                player.sendMessage(Text.literal(String.format(
                        "§a[Packet Created] Pos: %s, Strength: %s, Type: %s",
                        pos.toShortString(),
                        Arrays.toString(packet.getSignalStrengths()),
                        packet.getSignalType()
                )), true);
            }
        }
    }

    public void showPacketMovement(SignalPacket oldPacket, SignalPacket newPacket, Set<UUID> debugPlayers) {
        ServerWorld world = getWorldFromPacket(newPacket);
        if (world == null) return;

        BlockPos oldPos = oldPacket.getCurrentPos();
        BlockPos newPos = newPacket.getCurrentPos();

        for (UUID playerId : debugPlayers) {
            ServerPlayerEntity player = world.getServer().getPlayerManager().getPlayer(playerId);
            if (player != null && player.getWorld() == world) {
                // Частицы движения - синие (исправленный вызов)
                world.spawnParticles(
                        new DustParticleEffect(0x0066FF, 1.0f),
                        newPos.getX() + 0.5, newPos.getY() + 0.5, newPos.getZ() + 0.5,
                        5, 0.2, 0.2, 0.2, 0.05
                );

                // Создаем текстовый дисплей для направления
                createDirectionDisplay(world, newPos, newPacket.getCurrentDirection());
            }
        }
    }

    public void showPacketDeath(SignalPacket packet, String reason, Set<UUID> debugPlayers) {
        ServerWorld world = getWorldFromPacket(packet);
        if (world == null) return;

        BlockPos pos = packet.getCurrentPos();

        for (UUID playerId : debugPlayers) {
            ServerPlayerEntity player = world.getServer().getPlayerManager().getPlayer(playerId);
            if (player != null && player.getWorld() == world) {
                // Красные частицы смерти (исправленный вызов)
                world.spawnParticles(
                        new DustParticleEffect(0xFF0000, 1.0f),
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        15, 0.4, 0.4, 0.4, 0.2
                );

                // Сообщение в чат
                player.sendMessage(Text.literal(String.format(
                        "§c[Packet Died] Pos: %s, Reason: %s",
                        pos.toShortString(), reason
                )), true);
            }
        }
    }

    private void createDirectionDisplay(ServerWorld world, BlockPos pos, net.minecraft.util.math.Direction direction) {
        // Создаем временный текстовый дисплей
        // Примечание: движущиеся дисплеи сложны, но можно создавать и удалять их
        Vec3d displayPos = Vec3d.ofCenter(pos).add(0, 1, 0);

        // Для упрощения показываем только статичный текст
        // В полной реализации можно использовать DisplayEntity.TextDisplayEntity
        // и управлять его позицией через NBT или пересоздание
    }

    // Простая реализация getWorldFromPacket - теперь мир хранится в пакете
    private ServerWorld getWorldFromPacket(SignalPacket packet) {
        return packet.getWorld();
    }
}
