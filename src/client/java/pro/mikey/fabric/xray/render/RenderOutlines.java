package pro.mikey.fabric.xray.render;

import com.mojang.blaze3d.buffers.BufferType;
import com.mojang.blaze3d.buffers.BufferUsage;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.*;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4fStack;
import pro.mikey.fabric.xray.ScanController;
import pro.mikey.fabric.xray.XRay;
import pro.mikey.fabric.xray.records.BlockPosWithColor;
import pro.mikey.fabric.xray.storage.SettingsStore;

import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.concurrent.atomic.AtomicBoolean;

public class RenderOutlines {
    private static GpuBuffer vertexBuffer;
    private static int indexCount = 0;
    private static final RenderSystem.AutoStorageIndexBuffer indices = RenderSystem.getSequentialBuffer(VertexFormat.Mode.LINES);
    public static AtomicBoolean requestedRefresh = new AtomicBoolean(false);

    public static RenderPipeline LINES_NO_DEPTH = RenderPipeline.builder(RenderPipelines.MATRICES_COLOR_SNIPPET)
            .withLocation("pipeline/xray_lines")
            .withVertexShader("core/rendertype_lines")
            .withFragmentShader(ResourceLocation.fromNamespaceAndPath(XRay.MOD_ID, "frag/rendertype_lines_unaffected"))
            .withUniform("LineWidth", UniformType.FLOAT)
            .withUniform("ScreenSize", UniformType.VEC2)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(false)
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.LINES)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .build();

    public static synchronized void render(WorldRenderContext context) {

        if (ScanController.renderQueue.isEmpty() || !SettingsStore.getInstance().get().isActive()) {
            return;
        }

        RenderPipeline pipeline = LINES_NO_DEPTH;
        if (vertexBuffer == null || requestedRefresh.get()) {
            requestedRefresh.set(false);

            if (vertexBuffer != null) {
                vertexBuffer.close();
            }

            BufferBuilder bufferBuilder = Tesselator.getInstance().begin(
                    pipeline.getVertexFormatMode(), pipeline.getVertexFormat()
            );

            for (BlockPosWithColor blockProps : ScanController.renderQueue) {
                PoseStack poseStack = context.matrixStack();
                if (blockProps == null || poseStack == null) {
                    continue;
                }

                final float size = 1.0f;
                final int x = blockProps.pos().getX(), y = blockProps.pos().getY(), z = blockProps.pos().getZ();

                final float red = (blockProps.color().red()) / 255f;
                final float green = (blockProps.color().green()) / 255f;
                final float blue = (blockProps.color().blue()) / 255f;

                ShapeRenderer.renderLineBox(poseStack, bufferBuilder, x, y, z, x + size, y + size, z + size, red, green, blue, 1f);
            }

            try (MeshData meshData = bufferBuilder.buildOrThrow()) {
                vertexBuffer = RenderSystem.getDevice()
                        .createBuffer(() -> "Outline vertex buffer", BufferType.VERTICES, BufferUsage.STATIC_WRITE, meshData.vertexBuffer());
                indexCount = meshData.drawState().indexCount();
            }
        }

        if (indexCount != 0) {
            Vec3 playerPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().reverse();
            RenderTarget renderTarget = Minecraft.getInstance().getMainRenderTarget();

            if (renderTarget.getColorTexture() == null) {
                return;
            }

            GpuBuffer gpuBuffer = indices.getBuffer(indexCount);

            try (RenderPass renderPass =  RenderSystem.getDevice()
                    .createCommandEncoder()
                    .createRenderPass(renderTarget.getColorTexture(), OptionalInt.empty(), renderTarget.getDepthTexture(), OptionalDouble.empty())) {

                Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
                matrix4fStack.pushMatrix();
                matrix4fStack.translate((float) playerPos.x(), (float) playerPos.y(), (float) playerPos.z());
                renderPass.setPipeline(pipeline);
                renderPass.setIndexBuffer(gpuBuffer, indices.type());
                renderPass.setVertexBuffer(0, vertexBuffer);
                renderPass.drawIndexed(0, indexCount);

                matrix4fStack.popMatrix();
            }
        }
    }
}
