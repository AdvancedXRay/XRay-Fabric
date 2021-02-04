package pro.mikey.fabric.xray.render;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import pro.mikey.fabric.xray.scan.ScanController;

import java.io.Closeable;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class XRayRenderer {
  private static final VertexConsumerProvider.Immediate LINE_BUFFERS =
      VertexConsumerProvider.immediate(
          Util.make(
              () -> {
                Map<RenderLayer, BufferBuilder> ret = new IdentityHashMap<>();
                ret.put(
                    XRayRenderType.OVERLAY_LINES,
                    new BufferBuilder(XRayRenderType.OVERLAY_LINES.getExpectedBufferSize()));
                return ret;
              }),
          Tessellator.getInstance().getBuffer());

  private final VertexFormat blockVertexFormat = VertexFormats.POSITION;
  private boolean firstRender = true;
  private MultiVBORenderer renderBuffer;

  private static void renderBlockBounding(
      MatrixStack matrices, VertexConsumer builder, BlockPos b) {
    if (b == null) {
      return;
    }

    final float size = 1.0f;
    final float x = b.getX(), y = b.getY(), z = b.getZ(), opacity = .5f;

    final float red = (float) Math.random(); // (b.getColor() >> 16 & 0xff) / 255f;
    final float green = (float) Math.random(); // (b.getColor() >> 8 & 0xff) / 255f;
    final float blue = (float) Math.random(); // (b.getColor() & 0xff) / 255f;

    WorldRenderer.drawBox(
        matrices, builder, x, y, z, x + size, y + size, z + size, red, green, blue, opacity);
  }

  public void render(MatrixStack matrices, Camera camera) {
    //    if (ScanController.renderQueue.isEmpty() || !Stores.SETTINGS.get().isActive()) {
    //      if (!this.firstRender) {
    //        this.firstRender = true;
    //      }
    //      return;
    //    }

    final Vec3d cameraPos = camera.getPos();
    if (!ScanController.activeChunks.isEmpty()) {
      VertexConsumerProvider.Immediate entityVertexConsumers =
          MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
      VertexConsumer builder = entityVertexConsumers.getBuffer(XRayRenderType.OVERLAY_LINES);

      ScanController.activeChunks.forEach(
          (chunk) -> {
            matrices.push();
            matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
            RenderSystem.disableDepthTest();

            WorldRenderer.drawBox(
                matrices,
                builder,
                chunk.getStartX(),
                69,
                chunk.getStartZ(),
                chunk.getEndX() + 1,
                70,
                chunk.getEndZ() + 1,
                (float) Math.random(),
                (float) Math.random(),
                (float) Math.random(),
                1);

            RenderSystem.enableDepthTest();
            matrices.pop();
          });
    }

    if (this.firstRender) {
      if (this.renderBuffer != null) {
        this.renderBuffer.close();
      }

      System.out.println("rendering buffer");
      this.renderBuffer =
          MultiVBORenderer.of(
              (buffer) -> {
                MatrixStack stack = new MatrixStack();
                stack.push();

                VertexConsumer buffer1 = buffer.getBuffer(XRayRenderType.OVERLAY_LINES);
                synchronized (ScanController.renderQueue) {
                  Iterator<BlockPos> iterator = ScanController.renderQueue.iterator();
                  while (iterator.hasNext()) {
                    renderBlockBounding(stack, buffer1, iterator.next());
                  }
                }
                //                ScanController.renderQueue.forEach(e -> renderBlockBounding(stack,
                // buffer1, e));

                stack.pop();
              });

      this.firstRender = false;
    }

    if (this.renderBuffer == null) {
      return;
    }

    matrices.push();
    RenderSystem.disableDepthTest();
    matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

    this.renderBuffer.render(matrices.peek().getModel());

    RenderSystem.enableDepthTest();
    matrices.pop();
  }

  /**
   * Borrowed from Building Gadgets, Promise I'll give it back when I have time to fix this up :P
   */
  public static class MultiVBORenderer implements Closeable {
    private static final int BUFFER_SIZE = 2 * 1024 * 1024 * 3;
    private final ImmutableMap<RenderLayer, VertexBuffer> buffers;

    MultiVBORenderer(Map<RenderLayer, VertexBuffer> buffers) {
      this.buffers = ImmutableMap.copyOf(buffers);
    }

    static MultiVBORenderer of(Consumer<VertexConsumerProvider> vertexProducer) {
      final Map<RenderLayer, BufferBuilder> builders = Maps.newHashMap();

      vertexProducer.accept(
          rt ->
              builders.computeIfAbsent(
                  rt,
                  (_rt) -> {
                    BufferBuilder builder = new BufferBuilder(BUFFER_SIZE);
                    builder.begin(_rt.getDrawMode(), _rt.getVertexFormat());

                    return builder;
                  }));

      Map<RenderLayer, VertexBuffer> buffers =
          Maps.transformEntries(
              builders,
              (rt, builder) -> {
                Objects.requireNonNull(rt);
                Objects.requireNonNull(builder);

                builder.end();
                VertexFormat fmt = rt.getVertexFormat();
                VertexBuffer vbo = new VertexBuffer(fmt);

                vbo.upload(builder);
                return vbo;
              });

      return new MultiVBORenderer(buffers);
    }

    void render(Matrix4f matrix) {
      this.buffers.forEach(
          (rt, vbo) -> {
            VertexFormat fmt = rt.getVertexFormat();

            rt.startDrawing();
            vbo.bind();
            fmt.startDrawing(0L);
            vbo.draw(matrix, rt.getDrawMode());
            VertexBuffer.unbind();
            fmt.endDrawing();
            rt.endDrawing();
          });
    }

    @Override
    public void close() {
      this.buffers.values().forEach(VertexBuffer::close);
    }
  }
}
