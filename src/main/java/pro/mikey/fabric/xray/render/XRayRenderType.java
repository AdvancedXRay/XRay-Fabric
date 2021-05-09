package pro.mikey.fabric.xray.render;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;

import java.util.OptionalDouble;

public class XRayRenderType extends RenderLayer {
    private static final LineWidth THICK_LINES = new LineWidth(OptionalDouble.of(3.0D));
    static final RenderLayer OVERLAY_LINES =
        of(
            "overlay_lines",
            VertexFormats.POSITION_COLOR,
            VertexFormat.DrawMode.LINES,
            256,
            RenderLayer.MultiPhaseParameters.builder()
                //                .shader(LINES_SHADER)
                .shader(RenderPhase.LINES_SHADER)
                .lineWidth(THICK_LINES)
                .layering(VIEW_OFFSET_Z_LAYERING)
                .transparency(TRANSLUCENT_TRANSPARENCY)
                //              .target(ITEM_TARGET)
                .texture(NO_TEXTURE)
                //                .writeMaskState(COLOR_MASK)
                .cull(DISABLE_CULLING)
                .depthTest(ALWAYS_DEPTH_TEST)
                .build(false)
        );

    public XRayRenderType(
        String name,
        VertexFormat vertexFormat,
        VertexFormat.DrawMode drawMode,
        int expectedBufferSize,
        boolean hasCrumbling,
        boolean translucent,
        Runnable startAction,
        Runnable endAction) {
        super(
            name,
            vertexFormat,
            drawMode,
            expectedBufferSize,
            hasCrumbling,
            translucent,
            startAction,
            endAction
        );
    }
}
