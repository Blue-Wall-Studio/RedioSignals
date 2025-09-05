package org.BlueWallStudio.argest.debug;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.server.world.ServerWorld;
import org.BlueWallStudio.argest.signal.SignalPacket;

public class DebugManager {
    private static DebugManager instance;
    private final Set<UUID> debugPlayers = new HashSet<>();
    private final PacketVisualizer visualizer;

    private DebugManager() {
        this.visualizer = new PacketVisualizer();
    }

    public static DebugManager getInstance() {
        if (instance == null) {
            instance = new DebugManager();
        }
        return instance;
    }

    public void toggleDebug(ServerPlayerEntity player) {
        UUID playerId = player.getUuid();
        if (debugPlayers.contains(playerId)) {
            debugPlayers.remove(playerId);
            player.sendMessage(Text.literal("§6[Argest] §cДебаг отключен"), false);
        } else {
            debugPlayers.add(playerId);
            player.sendMessage(Text.literal("§6[Argest] §aДебаг включен"), false);
        }
    }

    public boolean isDebugging(UUID playerId) {
        return debugPlayers.contains(playerId);
    }

    public void onPacketCreated(ServerWorld world, SignalPacket packet) {
        if (debugPlayers.isEmpty())
            return;
        visualizer.showPacketCreation(world, packet, debugPlayers);
    }

    public void onPacketMoved(ServerWorld world, SignalPacket oldPacket, SignalPacket newPacket) {
        if (debugPlayers.isEmpty())
            return;
        visualizer.showPacketMovement(world, oldPacket, newPacket, debugPlayers);
    }

    public void onPacketDied(ServerWorld world, SignalPacket packet, String reason) {
        if (debugPlayers.isEmpty())
            return;
        visualizer.showPacketDeath(world, packet, reason, debugPlayers);
    }
}
