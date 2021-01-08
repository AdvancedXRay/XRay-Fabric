package pro.mikey.fabric.xray.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import pro.mikey.fabric.xray.ScanController;
import pro.mikey.fabric.xray.storage.Stores;

public class RenderOutlines {
    public static void render(MatrixStack matrices, Camera camera) {
        if (ScanController.renderQueue.isEmpty() || !Stores.SETTINGS.get().isActive()) {
            return;
        }

        Vec3d cameraPos = camera.getPos();
        VertexConsumerProvider.Immediate entityVertexConsumers = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
        VertexConsumer builder = entityVertexConsumers.getBuffer(XRayRenderType.OVERLAY_LINES);

        matrices.push();
        matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        ScanController.renderQueue.forEach(e -> renderBlockBounding(matrices, builder, e));

        RenderSystem.disableDepthTest();
        matrices.pop();
        entityVertexConsumers.draw(XRayRenderType.OVERLAY_LINES);
    }

    private static void renderBlockBounding(MatrixStack matrices, VertexConsumer builder, BlockPos b) {
        if( b == null )
            return;

        final float size = 1.0f;
        final float x = b.getX(), y = b.getY(), z = b.getZ(), opacity = .5f;

        final float red = 0;//(b.getColor() >> 16 & 0xff) / 255f;
        final float green = 0;//(b.getColor() >> 8 & 0xff) / 255f;
        final float blue = 255;//(b.getColor() & 0xff) / 255f;

        WorldRenderer.drawBox(matrices, builder, x, y, z, x + size, y + size, z + size, red, green, blue, opacity);
    }
}
