package org.BlueWallStudio.argest.debug;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.BlueWallStudio.argest.signal.SignalPacket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DebugManager {
    private static DebugManager instance;
    private final Set<UUID> debugPlayers = ConcurrentHashMap.newKeySet();
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

    public void onPacketCreated(SignalPacket packet) {
        if (debugPlayers.isEmpty()) return;
        visualizer.showPacketCreation(packet, debugPlayers);
    }

    public void onPacketMoved(SignalPacket oldPacket, SignalPacket newPacket) {
        if (debugPlayers.isEmpty()) return;
        visualizer.showPacketMovement(oldPacket, newPacket, debugPlayers);
    }

    public void onPacketDied(SignalPacket packet, String reason) {
        if (debugPlayers.isEmpty()) return;
        visualizer.showPacketDeath(packet, reason, debugPlayers);
    }
}
