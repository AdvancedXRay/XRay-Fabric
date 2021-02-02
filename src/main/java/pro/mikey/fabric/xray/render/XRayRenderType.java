package pro.mikey.fabric.xray.render;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import org.lwjgl.opengl.GL11;

import java.util.OptionalDouble;

public class XRayRenderType extends RenderLayer {
  private static final LineWidth THICK_LINES = new LineWidth(OptionalDouble.of(2D));
  private static final RenderPhase.DepthTest noDepth =
      new RenderPhase.DepthTest("always", GL11.GL_ALWAYS);

  static final RenderLayer OVERLAY_LINES =
      of(
          "overlay_lines",
          VertexFormats.POSITION_COLOR,
          GL11.GL_LINES,
          128,
          RenderLayer.MultiPhaseParameters.builder()
              .lineWidth(THICK_LINES)
              .layering(VIEW_OFFSET_Z_LAYERING)
              .transparency(TRANSLUCENT_TRANSPARENCY)
              .texture(NO_TEXTURE)
              .depthTest(XRayRenderType.noDepth)
              //              .depthTest(ALWAYS_DEPTH_TEST)
              //              .cull(DISABLE_CULLING)
              //              .lightmap(DISABLE_LIGHTMAP)
              //              .writeMaskState(COLOR_MASK)
              .build(false));

  public XRayRenderType(
      String name,
      VertexFormat vertexFormat,
      int drawMode,
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
