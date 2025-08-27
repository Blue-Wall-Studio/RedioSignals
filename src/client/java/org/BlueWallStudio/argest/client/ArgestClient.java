package org.BlueWallStudio.argest.client;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.util.Identifier;
import org.BlueWallStudio.argest.client.render.PacketRenderer;

public class ArgestClient implements ClientModInitializer {

    private static final Identifier LAYER_ID = Identifier.of("argest", "packet_renderer_layer");

    @Override
    public void onInitializeClient() {
        PacketRenderer packetRenderer = new PacketRenderer();
    }

}
