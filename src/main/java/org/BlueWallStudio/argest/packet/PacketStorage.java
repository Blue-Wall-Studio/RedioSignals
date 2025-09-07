package org.BlueWallStudio.argest.packet;

import java.util.Set;
import java.util.HashSet;
import net.minecraft.world.PersistentState;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtElement;

public class PacketStorage extends PersistentState {
    private final Set<Packet> packets = new HashSet<>();

    public static PacketStorage createFromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        PacketStorage storage = new PacketStorage();
        NbtList list = nbt.getList("packets", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < list.size(); i++) {
            storage.packets.add(Packet.fromNbt(list.getCompound(i)));
        }
        return storage;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        NbtList list = new NbtList();
        for (Packet packet : packets) {
            list.add(packet.toNbt());
        }
        nbt.put("packets", list);
        return nbt;
    }

    public Set<Packet> getPackets() {
        return packets;
    }
}
