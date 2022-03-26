package pro.mikey.fabric.xray.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import org.lwjgl.opengl.GL11;
import pro.mikey.fabric.xray.ScanController;
import pro.mikey.fabric.xray.records.BlockPosWithColor;
import pro.mikey.fabric.xray.storage.Stores;

import java.util.concurrent.atomic.AtomicBoolean;

public class RenderOutlines {
    private static VertexBuffer vertexBuffer;
    public static AtomicBoolean requestedRefresh = new AtomicBoolean(false);

    private static int canvasLoaded = -1;

    public static synchronized void render(WorldRenderContext context) {
        if (canvasLoaded == -1) {
            canvasLoaded = FabricLoader.getInstance().isModLoaded("canvas") ? 1 : 0;
        }

        if (ScanController.renderQueue.isEmpty() || !Stores.SETTINGS.get().isActive()) {
            return;
        }

        if (vertexBuffer == null || requestedRefresh.get()) {
            requestedRefresh.set(false);
            vertexBuffer = new VertexBuffer();

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();

            buffer.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
            ScanController.renderQueue.forEach(blockProps -> {
                if (blockProps == null) {
                    return;
                }

                renderBlock(buffer, blockProps, 1);
            });

            buffer.end();

            vertexBuffer.upload(buffer);
        }

        if (vertexBuffer != null) {
            Camera camera = context.camera();
            Vec3d cameraPos = camera.getPos();

            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableTexture();

            MatrixStack poseStack = RenderSystem.getModelViewStack();
            poseStack.push();
            poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

            if (canvasLoaded == 1) { // canvas compat
                poseStack.multiply(new Quaternion(Vec3f.POSITIVE_X, camera.getPitch(), true));
                poseStack.multiply(new Quaternion(Vec3f.POSITIVE_Y, camera.getYaw() + 180f, true));
            }

            RenderSystem.applyModelViewMatrix();

            RenderSystem.depthFunc(GL11.GL_ALWAYS);
            vertexBuffer.setShader(poseStack.peek().getPositionMatrix(), context.projectionMatrix().copy(), GameRenderer.getPositionColorShader());
            RenderSystem.depthFunc(GL11.GL_LEQUAL);

            poseStack.pop();
            RenderSystem.applyModelViewMatrix();
        }
    }

    private static void renderBlock(BufferBuilder buffer, BlockPosWithColor blockProps, float opacity) {
        final float size = 1.0f;
        final double x = blockProps.pos().getX(), y = blockProps.pos().getY(), z = blockProps.pos().getZ();

        final float red = blockProps.color().red() / 255f;
        final float green = blockProps.color().green() / 255f;
        final float blue = blockProps.color().blue() / 255f;

        buffer.vertex(x, y + size, z).color(red, green, blue, opacity).next();
        buffer.vertex(x + size, y + size, z).color(red, green, blue, opacity).next();
        buffer.vertex(x + size, y + size, z).color(red, green, blue, opacity).next();
        buffer.vertex(x + size, y + size, z + size).color(red, green, blue, opacity).next();
        buffer.vertex(x + size, y + size, z + size).color(red, green, blue, opacity).next();
        buffer.vertex(x, y + size, z + size).color(red, green, blue, opacity).next();
        buffer.vertex(x, y + size, z + size).color(red, green, blue, opacity).next();
        buffer.vertex(x, y + size, z).color(red, green, blue, opacity).next();

        // BOTTOM
        buffer.vertex(x + size, y, z).color(red, green, blue, opacity).next();
        buffer.vertex(x + size, y, z + size).color(red, green, blue, opacity).next();
        buffer.vertex(x + size, y, z + size).color(red, green, blue, opacity).next();
        buffer.vertex(x, y, z + size).color(red, green, blue, opacity).next();
        buffer.vertex(x, y, z + size).color(red, green, blue, opacity).next();
        buffer.vertex(x, y, z).color(red, green, blue, opacity).next();
        buffer.vertex(x, y, z).color(red, green, blue, opacity).next();
        buffer.vertex(x + size, y, z).color(red, green, blue, opacity).next();

        // Edge 1
        buffer.vertex(x + size, y, z + size).color(red, green, blue, opacity).next();
        buffer.vertex(x + size, y + size, z + size).color(red, green, blue, opacity).next();

        // Edge 2
        buffer.vertex(x + size, y, z).color(red, green, blue, opacity).next();
        buffer.vertex(x + size, y + size, z).color(red, green, blue, opacity).next();

        // Edge 3
        buffer.vertex(x, y, z + size).color(red, green, blue, opacity).next();
        buffer.vertex(x, y + size, z + size).color(red, green, blue, opacity).next();

        // Edge 4
        buffer.vertex(x, y, z).color(red, green, blue, opacity).next();
        buffer.vertex(x, y + size, z).color(red, green, blue, opacity).next();
    }
}
