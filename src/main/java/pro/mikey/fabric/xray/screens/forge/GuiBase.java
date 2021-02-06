package pro.mikey.fabric.xray.screens.forge;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import pro.mikey.fabric.xray.XRay;

import java.util.List;

public abstract class GuiBase extends Screen {
  static final Identifier BG_LARGE = new Identifier(XRay.PREFIX_GUI + "bg-help.png");
  private static final Identifier BG_NORMAL = new Identifier(XRay.PREFIX_GUI + "bg.png");
  private static final LiteralText NON_FINAL_WARNING =
      new LiteralText("This Gui is NOT final, changes to come soon");
  private final boolean hasSide;
  private String sideTitle = "";
  private int backgroundWidth = 229;
  private int backgroundHeight = 235;

  GuiBase(boolean hasSide) {
    super(new LiteralText(""));
    this.hasSide = hasSide;
  }

  // this should be moved to some sort of utility package but fuck it :).
  //  static void drawTexturedQuadFit(
  //      double x, double y, double width, double height, int[] color, float alpha) {
  //    Tessellator tessellator = Tessellator.getInstance();
  //    BufferBuilder tessellate = tessellator.getBuffer();
  //
  //    RenderSystem.pushMatrix();
  //    tessellate.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
  //
  //    if (color != null) {
  //      RenderSystem.color4f(
  //          (float) color[0] / 255, (float) color[1] / 255, (float) color[2] / 255, alpha / 255);
  //    }
  //
  //    tessellate.pos(x + 0, y + height, (double) 0).tex(0, 1).endVertex();
  //    tessellate.pos(x + width, y + height, (double) 0).tex(1, 1).endVertex();
  //    tessellate.pos(x + width, y + 0, (double) 0).tex(1, 0).endVertex();
  //    tessellate.pos(x + 0, y + 0, (double) 0).tex(0, 0).endVertex();
  //    tessellator.draw();
  //
  //    RenderSystem.popMatrix();
  //  }

  //  static void drawTexturedQuadFit(
  //      double x, double y, double width, double height, int[] color) {
  //    drawTexturedQuadFit(x, y, width, height, color, 255f);
  //  }
  //
  //  private static void drawTexturedQuadFit(
  //      double x, double y, double width, double height, int color) {
  //    drawTexturedQuadFit(
  //        x, y, width, height, new int[] {color >> 16 & 0xff, color >> 8 & 0xff, color & 0xff},
  // 255f);
  //  }

  abstract void renderExtra(MatrixStack stack, int x, int y, float partialTicks);

  @Override
  public boolean charTyped(char keyTyped, int __unknown) {
    super.charTyped(keyTyped, __unknown);

    if (keyTyped == 1 && this.getMinecraft().player != null) {
      this.getMinecraft().player.closeScreen();
    }

    return false;
  }

  @Override
  public void render(MatrixStack stack, int x, int y, float partialTicks) {
    this.renderBackground(stack);

    this.textRenderer.drawWithShadow(
        stack,
        NON_FINAL_WARNING,
        this.width / 2f - this.textRenderer.getWidth(NON_FINAL_WARNING) / 2f,
        this.height / 2f - 128,
        0xFFaf00);

    RenderSystem.pushMatrix();

    int width = this.width;
    int height = this.height;
    this.getMinecraft().getTextureManager().bindTexture(this.getBackground());
    if (this.hasSide) {
      this.drawTexture(stack, width / 2 + 60, height / 2 - 180 / 2, 0, 0, 150, 180, 150, 180);
      drawTexture(
          stack,
          width / 2 - 150,
          height / 2 - 118,
          0,
          0,
          this.backgroundWidth,
          this.backgroundHeight,
          this.backgroundWidth,
          this.backgroundHeight);

      if (this.hasSideTitle()) {
        this.getFontRender()
            .drawWithShadow(
                stack, this.sideTitle, (float) width / 2 + 80, (float) height / 2 - 77, 0xffff00);
      }
    }

    if (!this.hasSide) {
      drawTexture(
          stack,
          width / 2 - this.backgroundWidth / 2 + 1,
          height / 2 - this.backgroundHeight / 2,
          0,
          0,
          this.backgroundWidth,
          this.backgroundHeight,
          this.backgroundWidth,
          this.backgroundHeight);
    }

    RenderSystem.enableTexture();
    if (this.hasTitle()) {
      if (this.hasSide) {
        this.getFontRender()
            .drawWithShadow(
                stack, this.title(), (float) width / 2 - 138, (float) height / 2 - 105, 0xffff00);
      } else {
        this.getFontRender()
            .drawWithShadow(
                stack,
                this.title(),
                (float) width / 2 - ((float) this.backgroundWidth / 2) + 14,
                (float) height / 2 - ((float) this.backgroundHeight / 2) + 13,
                0xffff00);
      }
    }

    RenderSystem.popMatrix();

    this.renderExtra(stack, x, y, partialTicks);

    List<AbstractButtonWidget> buttons = this.buttons;
    for (AbstractButtonWidget button : buttons) {
      button.render(stack, x, y, partialTicks);
    }

    for (AbstractButtonWidget button : buttons) {
      if (button instanceof SupportButton && button.isHovered()) {
        this.renderTooltip(stack, ((SupportButton) button).getSupport(), x, y);
      }
    }

    super.render(stack, x, y, partialTicks);
  }

  public Identifier getBackground() {
    return BG_NORMAL;
  }

  public boolean hasTitle() {
    return false;
  }

  public String title() {
    return "";
  }

  private boolean hasSideTitle() {
    return !this.sideTitle.isEmpty();
  }

  void setSideTitle(String title) {
    this.sideTitle = title;
  }

  void setSize(int width, int height) {
    this.backgroundWidth = width;
    this.backgroundHeight = height;
  }

  TextRenderer getFontRender() {
    return this.getMinecraft().textRenderer;
  }

  int getWidth() {
    return this.width;
  }

  int getHeight() {
    return this.height;
  }

  @Override
  public boolean isPauseScreen() {
    return false;
  }

  public MinecraftClient getMinecraft() {
    return this.client;
  }
}
