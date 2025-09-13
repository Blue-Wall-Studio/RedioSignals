package org.BlueWallStudio.rediosignals;

import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class ModTags {
    public static final TagKey<Block> COPPER_WIRES = TagKey.of(RegistryKeys.BLOCK,
            Identifier.of(RedioSignals.MOD_ID, "copper_wires"));
    public static final TagKey<Block> GOLD_WIRES = TagKey.of(RegistryKeys.BLOCK,
            Identifier.of(RedioSignals.MOD_ID, "gold_wires"));
    public static final TagKey<Block> LAPIS_WIRES = TagKey.of(RegistryKeys.BLOCK,
            Identifier.of(RedioSignals.MOD_ID, "lapis_wires"));
    public static final TagKey<Block> IRON_WIRES = TagKey.of(RegistryKeys.BLOCK,
            Identifier.of(RedioSignals.MOD_ID, "iron_wires"));
    public static final TagKey<Block> QUARTZ_WIRES = TagKey.of(RegistryKeys.BLOCK,
            Identifier.of(RedioSignals.MOD_ID, "quartz_wires"));
    public static final TagKey<Block> COPPER_BULB_WIRES = TagKey.of(RegistryKeys.BLOCK,
            Identifier.of(RedioSignals.MOD_ID, "copper_bulb_wires"));

    // Tags for wireless transmission
    public static final TagKey<Block> WIRELESS_TRANSMITTERS = TagKey.of(RegistryKeys.BLOCK,
            Identifier.of(RedioSignals.MOD_ID, "wireless_transmitters"));
    public static final TagKey<Block> WIRELESS_RECEIVERS = TagKey.of(RegistryKeys.BLOCK,
            Identifier.of(RedioSignals.MOD_ID, "wireless_receivers"));
    public static final TagKey<Block> IRON_TRANSMITTERS = TagKey.of(RegistryKeys.BLOCK,
            Identifier.of(RedioSignals.MOD_ID, "iron_transmitters"));
    public static final TagKey<Block> COPPER_GRATE_RECEIVERS = TagKey.of(RegistryKeys.BLOCK,
            Identifier.of(RedioSignals.MOD_ID, "copper_grate_receivers"));
    public static final TagKey<Block> WIRELESS_BLOCKING_BLOCKS = TagKey.of(RegistryKeys.BLOCK,
            Identifier.of(RedioSignals.MOD_ID, "wireless_blocking"));

    // Common tag for all wires
    public static final TagKey<Block> ALL_WIRES = TagKey.of(RegistryKeys.BLOCK,
            Identifier.of(RedioSignals.MOD_ID, "wires"));
}
