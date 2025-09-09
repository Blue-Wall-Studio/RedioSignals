package org.BlueWallStudio.argest;

import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class ModTags {
    public static final TagKey<Block> COPPER_WIRES = TagKey.of(RegistryKeys.BLOCK,
            Identifier.of("argest", "copper_wires"));
    public static final TagKey<Block> GOLD_WIRES = TagKey.of(RegistryKeys.BLOCK,
            Identifier.of("argest", "gold_wires"));
    public static final TagKey<Block> LAPIS_WIRES = TagKey.of(RegistryKeys.BLOCK,
            Identifier.of("argest", "lapis_wires"));
    public static final TagKey<Block> IRON_WIRES = TagKey.of(RegistryKeys.BLOCK,
            Identifier.of("argest", "iron_wires"));
    public static final TagKey<Block> QUARTZ_WIRES = TagKey.of(RegistryKeys.BLOCK,
            Identifier.of("argest", "quartz_wires"));
    public static final TagKey<Block> COPPER_BULB_WIRES = TagKey.of(RegistryKeys.BLOCK,
            Identifier.of("argest", "copper_bulb_wires"));

    // Tags for wireless transmission
    public static final TagKey<Block> WIRELESS_TRANSMITTERS = TagKey.of(RegistryKeys.BLOCK,
            Identifier.of("argest", "wireless_transmitters"));
    public static final TagKey<Block> WIRELESS_RECEIVERS = TagKey.of(RegistryKeys.BLOCK,
            Identifier.of("argest", "wireless_receivers"));
    public static final TagKey<Block> IRON_TRANSMITTERS = TagKey.of(RegistryKeys.BLOCK,
            Identifier.of("argest", "iron_transmitters"));
    public static final TagKey<Block> COPPER_GRATE_RECEIVERS = TagKey.of(RegistryKeys.BLOCK,
            Identifier.of("argest", "copper_grate_receivers"));
    public static final TagKey<Block> WIRELESS_BLOCKING_BLOCKS = TagKey.of(RegistryKeys.BLOCK,
            Identifier.of("argest", "wireless_blocking"));

    // Common tag for all wires
    public static final TagKey<Block> ALL_WIRES = TagKey.of(RegistryKeys.BLOCK,
            Identifier.of("argest", "wires"));
}
