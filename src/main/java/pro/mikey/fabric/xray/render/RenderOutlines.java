package pro.mikey.fabric.xray.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import org.lwjgl.opengl.GL11;
import pro.mikey.fabric.xray.ScanController;
import pro.mikey.fabric.xray.records.BlockPosWithColor;
import pro.mikey.fabric.xray.storage.Stores;

// Thanks to JackFred2 (WhereIsIt) for some of the code here
public class RenderOutlines {
    public static synchronized void render(WorldRenderContext context) {
        if (ScanController.renderQueue.isEmpty() || !Stores.SETTINGS.get().isActive()) {
            return;
        }

        Camera camera = context.camera();
        Vec3d cameraPos = camera.getPos();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();

        MatrixStack matrices = RenderSystem.getModelViewStack();
        matrices.push();
//        matrices.scale(0.998f, 0.998f, 0.998f);

        if (FabricLoader.getInstance().isModLoaded("canvas")) { // canvas compat
            matrices.multiply(new Quaternion(Vec3f.POSITIVE_X, camera.getPitch(), true));
            matrices.multiply(new Quaternion(Vec3f.POSITIVE_Y, camera.getYaw() + 180f, true));
        }

        RenderSystem.applyModelViewMatrix();

        ScanController.renderQueue.forEach(blockProps -> {
            if (blockProps == null) {
                return;
            }

            Vec3d finalPos = cameraPos.subtract(blockProps.getPos().getX(), blockProps.getPos().getY(), blockProps.getPos().getZ()).negate();
            RenderSystem.disableDepthTest();
            RenderSystem.depthFunc(GL11.GL_ALWAYS);

            buffer.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
            renderBlock(buffer, blockProps, finalPos, 1);
            tessellator.draw();

            RenderSystem.depthFunc(GL11.GL_LEQUAL);
        });

        matrices.pop();
        RenderSystem.applyModelViewMatrix();
    }

    private static void renderBlock(BufferBuilder buffer, BlockPosWithColor b, Vec3d pos, float opacity) {
        if (b == null)
            return;

        final float size = 1.0f;
        final double x = pos.getX(), y = pos.getY(), z = pos.getZ();

        final float red = b.getColor().getRed() / 255f;
        final float green = b.getColor().getGreen() / 255f;
        final float blue = b.getColor().getBlue() / 255f;

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
