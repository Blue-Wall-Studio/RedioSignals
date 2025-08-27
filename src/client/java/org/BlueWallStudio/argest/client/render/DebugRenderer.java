package org.BlueWallStudio.argest.client.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.BlueWallStudio.argest.debug.DebugManager;

public class DebugRenderer {
    private static final int DEBUG_COLOR = 0xFFFFFF;
    private static final int BACKGROUND_COLOR = 0x80000000;

    public void renderHud(DrawContext drawContext, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || !DebugManager.getInstance().isDebugging(client.player.getUuid())) {
            return;
        }

        MatrixStack matrices = drawContext.getMatrices();
        TextRenderer textRenderer = client.textRenderer;

        int y = 10;
        int x = 10;

        // Заголовок
        Text title = Text.literal("§6[RedstoneNet Debug]");
        drawContext.drawTextWithShadow(textRenderer, title, x, y, DEBUG_COLOR);
        y += 12;

        // Информация о позиции игрока
        if (client.player != null) {
            BlockPos pos = client.player.getBlockPos();
            Text posText = Text.literal(String.format("§7Position: %d, %d, %d", pos.getX(), pos.getY(), pos.getZ()));
            drawContext.drawTextWithShadow(textRenderer, posText, x, y, DEBUG_COLOR);
            y += 10;
        }

        // Активные пакеты (если есть доступ к ним)
        Text packetsText = Text.literal("§7Active Packets: Loading...");
        drawContext.drawTextWithShadow(textRenderer, packetsText, x, y, DEBUG_COLOR);
        y += 10;

        // Команды помощи
        Text helpText = Text.literal("§8/redstonenet debug - Toggle debug mode");
        drawContext.drawTextWithShadow(textRenderer, helpText, x, y, 0xAAAAAA);
    }
}
