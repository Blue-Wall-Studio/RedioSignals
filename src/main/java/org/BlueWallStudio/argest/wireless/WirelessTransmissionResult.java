package org.BlueWallStudio.argest.wireless;


import net.minecraft.util.math.BlockPos;
import org.BlueWallStudio.argest.wireless.receiver.WirelessReceiver;

// Отдельный класс для результата беспроводной передачи
public class WirelessTransmissionResult {
    private final BlockPos receiverPos;
    private final WirelessReceiver receiver;
    private final int distance;
    private final boolean hasReceiver;

    private WirelessTransmissionResult(BlockPos pos, WirelessReceiver receiver, int distance) {
        this.receiverPos = pos;
        this.receiver = receiver;
        this.distance = distance;
        this.hasReceiver = true;
    }

    private WirelessTransmissionResult() {
        this.receiverPos = null;
        this.receiver = null;
        this.distance = -1;
        this.hasReceiver = false;
    }

    public static WirelessTransmissionResult found(BlockPos pos, WirelessReceiver receiver, int distance) {
        return new WirelessTransmissionResult(pos, receiver, distance);
    }

    public static WirelessTransmissionResult noReceiver() {
        return new WirelessTransmissionResult();
    }

    public boolean hasReceiver() { return hasReceiver; }
    public BlockPos getReceiverPos() { return receiverPos; }
    public WirelessReceiver getReceiver() { return receiver; }
    public int getDistance() { return distance; }
}
