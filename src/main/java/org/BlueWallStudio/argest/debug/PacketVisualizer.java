package org.BlueWallStudio.argest.debug;

import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import java.util.*;
import org.BlueWallStudio.argest.packet.Packet;
import org.BlueWallStudio.argest.config.ModConfig;

public class PacketVisualizer {
    private static ModConfig config = ModConfig.getInstance();

    public void showPacketCreation(ServerWorld world, Packet packet, Set<UUID> debugPlayers) {
        BlockPos pos = packet.getCurrentPos();

        // Show wind particles (or other matching ones)
        for (UUID playerId : debugPlayers) {
            ServerPlayerEntity player = world.getServer().getPlayerManager().getPlayer(playerId);
            if (player != null && player.getWorld() == world) {
                // Particles for packet creation visualisation - fixed call
                if (config.showParticles) {
                    world.spawnParticles(
                            new DustParticleEffect(0x00FF00, 1.0f),
                            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                            10, 0.3, 0.3, 0.3, 0.1);
                }

                // Display text information on screen
                player.sendMessage(Text.literal(String.format(
                        "§a[Packet Created] Pos: %s, Strength: %s, Type: %s",
                        pos.toShortString(),
                        Arrays.toString(packet.getPacketStrengths()),
                        packet.getPacketType())), true);
            }
        }
    }

    public void showPacketMovement(ServerWorld world, Packet oldPacket, Packet newPacket,
            Set<UUID> debugPlayers) {
        BlockPos newPos = newPacket.getCurrentPos();

        for (UUID playerId : debugPlayers) {
            ServerPlayerEntity player = world.getServer().getPlayerManager().getPlayer(playerId);
            if (player != null && player.getWorld() == world) {
                // Blue move particles (fixed call)
                if (config.showParticles) {
                    world.spawnParticles(
                            new DustParticleEffect(0x0066FF, 1.0f),
                            newPos.getX() + 0.5, newPos.getY() + 0.5, newPos.getZ() + 0.5,
                            5, 0.2, 0.2, 0.2, 0.05);
                }

                // Create text display for direction
                createDirectionDisplay(world, newPos, newPacket.getCurrentDirection());
            }
        }
    }

    public void showPacketDeath(ServerWorld world, Packet packet, String reason, Set<UUID> debugPlayers) {
        BlockPos pos = packet.getCurrentPos();

        for (UUID playerId : debugPlayers) {
            ServerPlayerEntity player = world.getServer().getPlayerManager().getPlayer(playerId);
            if (player != null && player.getWorld() == world) {
                // Red death particles (fixed call)
                if (config.showParticles) {
                    world.spawnParticles(
                            new DustParticleEffect(0xFF0000, 1.0f),
                            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                            15, 0.4, 0.4, 0.4, 0.2);
                }

                // Display text information on screen
                player.sendMessage(Text.literal(String.format(
                        "§c[Packet Died] Pos: %s, Reason: %s",
                        pos.toShortString(), reason)), true);
            }
        }
    }

    public void showWirelessTransmission(ServerWorld world, Packet packet, Set<UUID> debugPlayers) {
        BlockPos pos = packet.getCurrentPos();

        for (UUID playerId : debugPlayers) {
            ServerPlayerEntity player = world.getServer().getPlayerManager().getPlayer(playerId);
            if (player != null && player.getWorld() == world) {
                // Purple particles for wireless transmission
                if (config.showParticles) {
                    world.spawnParticles(
                            new DustParticleEffect(0x9966FF, 1.0f),
                            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                            8, 0.3, 0.3, 0.3, 0.1);
                }

                // Display wireless transmission message
                player.sendMessage(Text.literal(String.format(
                        "§d[Transmitted wirelessly] Pos: %s, Strength: %s, Type: %s",
                        pos.toShortString(),
                        Arrays.toString(packet.getPacketStrengths()),
                        packet.getPacketType())), true);
            }
        }
    }

    public void showWirelessReception(ServerWorld world, Packet packet, Set<UUID> debugPlayers) {
        BlockPos pos = packet.getCurrentPos();

        for (UUID playerId : debugPlayers) {
            ServerPlayerEntity player = world.getServer().getPlayerManager().getPlayer(playerId);
            if (player != null && player.getWorld() == world) {
                // Cyan particles for wireless reception
                if (config.showParticles) {
                    world.spawnParticles(
                            new DustParticleEffect(0x00FFFF, 1.0f),
                            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                            8, 0.3, 0.3, 0.3, 0.1);
                }

                // Display wireless reception message
                player.sendMessage(Text.literal(String.format(
                        "§b[Processed by wireless receiver] Pos: %s, Strength: %s, Type: %s",
                        pos.toShortString(),
                        Arrays.toString(packet.getPacketStrengths()),
                        packet.getPacketType())), true);
            }
        }
    }

    private void createDirectionDisplay(ServerWorld world, BlockPos pos, net.minecraft.util.math.Direction direction) {
        // Create temporary text display
        // NOTE: moving displays is hard, but they can be deleted and recreated
        Vec3d displayPos = Vec3d.ofCenter(pos).add(0, 1, 0);

        // Display only static text for simplicity
        // In final implementation we can use DisplayEntity.TextDisplayEntity
        // and manage its position via NBT or recreation
    }
}
