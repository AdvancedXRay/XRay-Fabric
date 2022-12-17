package pro.mikey.fabric.xray.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import pro.mikey.fabric.xray.ScanController;
import pro.mikey.fabric.xray.records.BlockPosWithColor;
import pro.mikey.fabric.xray.storage.SettingsStore;

import java.util.concurrent.atomic.AtomicBoolean;

import static net.minecraft.util.Mth.cos;
import static net.minecraft.util.Mth.sin;

public class RenderOutlines {
    private static VertexBuffer vertexBuffer;
    public static AtomicBoolean requestedRefresh = new AtomicBoolean(false);

    private static int canvasLoaded = -1;

    public static synchronized void render(WorldRenderContext context) {
        if (canvasLoaded == -1) {
            canvasLoaded = FabricLoader.getInstance().isModLoaded("canvas") ? 1 : 0;
        }

        if (ScanController.renderQueue.isEmpty() || !SettingsStore.getInstance().get().isActive()) {
            return;
        }

        if (vertexBuffer == null || requestedRefresh.get()) {
            requestedRefresh.set(false);
            vertexBuffer = new VertexBuffer();

            Tesselator tessellator = Tesselator.getInstance();
            BufferBuilder buffer = tessellator.getBuilder();

            buffer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
            ScanController.renderQueue.forEach(blockProps -> {
                if (blockProps == null) {
                    return;
                }

                renderBlock(buffer, blockProps, 1);
            });

            vertexBuffer.bind();
            vertexBuffer.upload(buffer.end());
            VertexBuffer.unbind();
        }

        if (vertexBuffer != null) {
            Camera camera = context.camera();
            Vec3 cameraPos = camera.getPosition();

            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableTexture();

            PoseStack poseStack = RenderSystem.getModelViewStack();
            poseStack.pushPose();
            poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

            if (canvasLoaded == 1) { // canvas compat
                float f = camera.getXRot() * 0.017453292F;
                poseStack.mulPose(new Quaternionf(1.0 * sin(f/2.0f), 0.0, 0.0, cos(f/2.0f)));
                f = (camera.getYRot() + 180f) * 0.017453292F;
                poseStack.mulPose(new Quaternionf(0.0, 1.0 * sin(f/2.0f), 0.0, cos(f/2.0f)));
            }

            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            RenderSystem.applyModelViewMatrix();
            RenderSystem.depthFunc(GL11.GL_ALWAYS);
            vertexBuffer.bind();
            vertexBuffer.drawWithShader(poseStack.last().pose(), new Matrix4f(context.projectionMatrix()), RenderSystem.getShader());
            VertexBuffer.unbind();
            RenderSystem.depthFunc(GL11.GL_LEQUAL);

            poseStack.popPose();
            RenderSystem.applyModelViewMatrix();
        }
    }

    private static void renderBlock(BufferBuilder buffer, BlockPosWithColor blockProps, float opacity) {
        final float size = 1.0f;
        final double x = blockProps.pos().getX(), y = blockProps.pos().getY(), z = blockProps.pos().getZ();

        final float red = blockProps.color().red() / 255f;
        final float green = blockProps.color().green() / 255f;
        final float blue = blockProps.color().blue() / 255f;

        buffer.vertex(x, y + size, z).color(red, green, blue, opacity).endVertex();
        buffer.vertex(x + size, y + size, z).color(red, green, blue, opacity).endVertex();
        buffer.vertex(x + size, y + size, z).color(red, green, blue, opacity).endVertex();
        buffer.vertex(x + size, y + size, z + size).color(red, green, blue, opacity).endVertex();
        buffer.vertex(x + size, y + size, z + size).color(red, green, blue, opacity).endVertex();
        buffer.vertex(x, y + size, z + size).color(red, green, blue, opacity).endVertex();
        buffer.vertex(x, y + size, z + size).color(red, green, blue, opacity).endVertex();
        buffer.vertex(x, y + size, z).color(red, green, blue, opacity).endVertex();

        // BOTTOM
        buffer.vertex(x + size, y, z).color(red, green, blue, opacity).endVertex();
        buffer.vertex(x + size, y, z + size).color(red, green, blue, opacity).endVertex();
        buffer.vertex(x + size, y, z + size).color(red, green, blue, opacity).endVertex();
        buffer.vertex(x, y, z + size).color(red, green, blue, opacity).endVertex();
        buffer.vertex(x, y, z + size).color(red, green, blue, opacity).endVertex();
        buffer.vertex(x, y, z).color(red, green, blue, opacity).endVertex();
        buffer.vertex(x, y, z).color(red, green, blue, opacity).endVertex();
        buffer.vertex(x + size, y, z).color(red, green, blue, opacity).endVertex();

        // Edge 1
        buffer.vertex(x + size, y, z + size).color(red, green, blue, opacity).endVertex();
        buffer.vertex(x + size, y + size, z + size).color(red, green, blue, opacity).endVertex();

        // Edge 2
        buffer.vertex(x + size, y, z).color(red, green, blue, opacity).endVertex();
        buffer.vertex(x + size, y + size, z).color(red, green, blue, opacity).endVertex();

        // Edge 3
        buffer.vertex(x, y, z + size).color(red, green, blue, opacity).endVertex();
        buffer.vertex(x, y + size, z + size).color(red, green, blue, opacity).endVertex();

        // Edge 4
        buffer.vertex(x, y, z).color(red, green, blue, opacity).endVertex();
        buffer.vertex(x, y + size, z).color(red, green, blue, opacity).endVertex();
    }
}
