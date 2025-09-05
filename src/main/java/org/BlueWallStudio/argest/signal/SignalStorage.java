package org.BlueWallStudio.argest.signal;

import java.util.Set;
import java.util.HashSet;
import net.minecraft.world.PersistentState;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtElement;

public class SignalStorage extends PersistentState {
    private final Set<SignalPacket> packets = new HashSet<>();

    public static SignalStorage createFromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        SignalStorage storage = new SignalStorage();
        NbtList list = nbt.getList("packets", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < list.size(); i++) {
            storage.packets.add(SignalPacket.fromNbt(list.getCompound(i)));
        }
        return storage;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        NbtList list = new NbtList();
        for (SignalPacket packet : packets) {
            list.add(packet.toNbt());
        }
        nbt.put("packets", list);
        return nbt;
    }

    public Set<SignalPacket> getPackets() {
        return packets;
    }
}
