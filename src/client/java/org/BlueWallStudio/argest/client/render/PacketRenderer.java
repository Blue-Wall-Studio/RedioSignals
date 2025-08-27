package org.BlueWallStudio.argest.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class PacketRenderer {
    public void render(WorldRenderContext context, RenderTickCounter tickCounter) {
        float tickDelta = tickCounter.getTickDelta(false);
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        renderActivePackets(context);
    }

    private void renderActivePackets(WorldRenderContext context) {
        MatrixStack matrices = context.matrixStack();
        VertexConsumerProvider consumers = context.consumers(); // may be null or not Immediate

        // Безопасные проверки: если чего-то нет — просто не рисуем
        if (matrices == null) return;
        if (consumers == null) return;
        if (!(consumers instanceof VertexConsumerProvider.Immediate)) return;

        VertexConsumerProvider.Immediate immediate = (VertexConsumerProvider.Immediate) consumers;

        // Пример: рендер одного пакета в центре игрока (замените реальными данными)
        matrices.push();
        try {
            Vec3d packetPos = new Vec3d(0.0, 1.0, 0.0); // тестовая позиция
            int packetColor = 0x00FFAA; // тестовый цвет
            renderPacket(matrices, immediate, packetPos, packetColor);
        } finally {
            matrices.pop();
        }

        // После всех вызовов к getBuffer у Immediate нужно вызвать draw() чтобы слить данные
        immediate.draw();
    }

    private void renderPacket(MatrixStack matrices, VertexConsumerProvider.Immediate immediate,
                              Vec3d pos, int color) {
        if (matrices == null) return;

        matrices.push();
        try {
            matrices.translate(pos.x, pos.y, pos.z);

            Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
            VertexConsumer vertexConsumer = immediate.getBuffer(RenderLayer.getLines());

            float size = 0.1f;
            float r = ((color >> 16) & 0xFF) / 255.0f;
            float g = ((color >> 8) & 0xFF) / 255.0f;
            float b = (color & 0xFF) / 255.0f;
            float a = 1.0f;

            // Рисуем outline куба
            drawCubeOutline(vertexConsumer, positionMatrix,
                    -size, -size, -size, size, size, size,
                    r, g, b, a);
        } finally {
            matrices.pop();
        }
    }

    private void drawCubeOutline(VertexConsumer vertexConsumer, Matrix4f matrix,
                                 float minX, float minY, float minZ, float maxX, float maxY, float maxZ,
                                 float r, float g, float b, float a) {
        // Нижняя грань (4 ребра по 2 вершины каждое)
        emitVertex(vertexConsumer, matrix, minX, minY, minZ, r, g, b, a);
        emitVertex(vertexConsumer, matrix, maxX, minY, minZ, r, g, b, a);

        emitVertex(vertexConsumer, matrix, maxX, minY, minZ, r, g, b, a);
        emitVertex(vertexConsumer, matrix, maxX, minY, maxZ, r, g, b, a);

        emitVertex(vertexConsumer, matrix, maxX, minY, maxZ, r, g, b, a);
        emitVertex(vertexConsumer, matrix, minX, minY, maxZ, r, g, b, a);

        emitVertex(vertexConsumer, matrix, minX, minY, maxZ, r, g, b, a);
        emitVertex(vertexConsumer, matrix, minX, minY, minZ, r, g, b, a);

        // Верхняя грань
        emitVertex(vertexConsumer, matrix, minX, maxY, minZ, r, g, b, a);
        emitVertex(vertexConsumer, matrix, maxX, maxY, minZ, r, g, b, a);

        emitVertex(vertexConsumer, matrix, maxX, maxY, minZ, r, g, b, a);
        emitVertex(vertexConsumer, matrix, maxX, maxY, maxZ, r, g, b, a);

        emitVertex(vertexConsumer, matrix, maxX, maxY, maxZ, r, g, b, a);
        emitVertex(vertexConsumer, matrix, minX, maxY, maxZ, r, g, b, a);

        emitVertex(vertexConsumer, matrix, minX, maxY, maxZ, r, g, b, a);
        emitVertex(vertexConsumer, matrix, minX, maxY, minZ, r, g, b, a);

        // Вертикальные линии
        emitVertex(vertexConsumer, matrix, minX, minY, minZ, r, g, b, a);
        emitVertex(vertexConsumer, matrix, minX, maxY, minZ, r, g, b, a);

        emitVertex(vertexConsumer, matrix, maxX, minY, minZ, r, g, b, a);
        emitVertex(vertexConsumer, matrix, maxX, maxY, minZ, r, g, b, a);

        emitVertex(vertexConsumer, matrix, maxX, minY, maxZ, r, g, b, a);
        emitVertex(vertexConsumer, matrix, maxX, maxY, maxZ, r, g, b, a);

        emitVertex(vertexConsumer, matrix, minX, minY, maxZ, r, g, b, a);
        emitVertex(vertexConsumer, matrix, minX, maxY, maxZ, r, g, b, a);
    }

    /**
     * Утилита для вывода одной вершины линии.
     *
     * В разных mappings последовательность методов у VertexConsumer может немного отличаться:
     * - Общая последовательность в большинстве Yarn mappings: vertex(...).color(...).next();
     * - В других mappings может быть endVertex(), emit(), и т.д.
     *
     * Если у вас в проекте метод next() отсутствует — замените тело этой функции
     * на последовательность, которая соответствует вашим mappings (например, .next() -> .endVertex()).
     */
    private void emitVertex(VertexConsumer vc, Matrix4f mat,
                            float x, float y, float z,
                            float r, float g, float b, float a) {
        // Задаём позицию, цвет и базовые атрибуты вершины цепочкой вызовов.
        // Нет вызова next()/endVertex() — в текущих mappings вершина завершается
        // когда заданы все необходимые элементы (последний из них обычно normal/light/overlay/texture).
        vc.vertex(mat, x, y, z)
                .color(r, g, b, a)
                .texture(0f, 0f)    // если текстурные координаты не нужны — можно поставить 0,0
                .overlay(0, 0)      // overlay UV, 0,0 по умолчанию
                .light(0, 0)        // light (packed uv) — 0,0 если у вас нет освещения
                .normal(0f, 1f, 0f); // простой нормальный вектор; для линий обычно не важен
    }
}
