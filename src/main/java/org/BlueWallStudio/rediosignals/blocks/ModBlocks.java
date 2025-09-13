package org.BlueWallStudio.rediosignals.blocks;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import org.BlueWallStudio.rediosignals.RedioSignals;
import org.BlueWallStudio.rediosignals.blocks.entity.DecoderBlockEntity;
import org.BlueWallStudio.rediosignals.blocks.entity.EncoderBlockEntity;

public class ModBlocks {
    public static Block ENCODER_BLOCK;
    public static Block DECODER_BLOCK;

    public static BlockEntityType<EncoderBlockEntity> ENCODER_BLOCK_ENTITY;
    public static BlockEntityType<DecoderBlockEntity> DECODER_BLOCK_ENTITY;

    public static void init() {
        // Encoder
        Identifier encoderId = Identifier.of(RedioSignals.MOD_ID, "encoder");
        RegistryKey<Block> encoderKey = RegistryKey.of(RegistryKeys.BLOCK, encoderId);

        ENCODER_BLOCK = Blocks.register(
                encoderKey,
                EncoderBlock::new,
                AbstractBlock.Settings.create().strength(3.0f).requiresTool());
        Items.register(ENCODER_BLOCK); // automatically creates BlockItem

        // Decoder
        Identifier decoderId = Identifier.of(RedioSignals.MOD_ID, "decoder");
        RegistryKey<Block> decoderKey = RegistryKey.of(RegistryKeys.BLOCK, decoderId);

        DECODER_BLOCK = Blocks.register(
                decoderKey,
                DecoderBlock::new,
                AbstractBlock.Settings.create().strength(3.0f).requiresTool());
        Items.register(DECODER_BLOCK);

        // BlockEntities
        ENCODER_BLOCK_ENTITY = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                encoderId,
                FabricBlockEntityTypeBuilder.create(EncoderBlockEntity::new, ENCODER_BLOCK).build());

        DECODER_BLOCK_ENTITY = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                decoderId,
                FabricBlockEntityTypeBuilder.create(DecoderBlockEntity::new, DECODER_BLOCK).build());
    }
}
