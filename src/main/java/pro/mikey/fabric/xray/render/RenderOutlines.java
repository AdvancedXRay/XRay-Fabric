package pro.mikey.fabric.xray.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import pro.mikey.fabric.xray.ScanController;
import pro.mikey.fabric.xray.records.BlockPosWithColor;
import pro.mikey.fabric.xray.storage.Stores;

public class RenderOutlines {
  public static synchronized void render(MatrixStack matrices, Camera camera) {
    if (ScanController.renderQueue.isEmpty() || !Stores.SETTINGS.get().isActive()) {
      return;
    }

    Vec3d cameraPos = camera.getPos();
    VertexConsumerProvider.Immediate entityVertexConsumers =
        MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
    VertexConsumer builder = entityVertexConsumers.getBuffer(XRayRenderType.OVERLAY_LINES);

    matrices.push();
    matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

    ScanController.renderQueue.forEach(e -> renderBlockBounding(matrices, builder, e));

    RenderSystem.disableDepthTest();
    matrices.pop();
    entityVertexConsumers.draw(XRayRenderType.OVERLAY_LINES);
  }

  private static void renderBlockBounding(
      MatrixStack matrices, VertexConsumer builder, BlockPosWithColor b) {
    if (b == null) {
      return;
    }

    final float size = 1.0f;
    final float x = b.getPos().getX(), y = b.getPos().getY(), z = b.getPos().getZ(), opacity = .5f;

    WorldRenderer.drawBox(
        matrices,
        builder,
        x,
        y,
        z,
        x + size,
        y + size,
        z + size,
        b.getColor().getRed() / 255f,
        b.getColor().getGreen() / 255f,
        b.getColor().getBlue() / 255f,
        opacity);
  }
}
