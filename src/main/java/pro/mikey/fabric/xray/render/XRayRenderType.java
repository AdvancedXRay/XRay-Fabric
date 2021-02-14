package pro.mikey.fabric.xray.render;

import net.minecraft.client.render.RenderLayer;
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
              .lineWidth(THICK_LINES)
              .layering(VIEW_OFFSET_Z_LAYERING)
              .transparency(TRANSLUCENT_TRANSPARENCY)
              .texture(NO_TEXTURE)
              .depthTest(ALWAYS_DEPTH_TEST)
              .cull(DISABLE_CULLING)
              .lightmap(DISABLE_LIGHTMAP)
              .writeMaskState(COLOR_MASK)
              .build(false));

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
        endAction);
  }
}
