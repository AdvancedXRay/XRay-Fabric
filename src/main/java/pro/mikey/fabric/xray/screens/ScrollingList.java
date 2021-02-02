package pro.mikey.fabric.xray.screens;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.opengl.GL11;

/**
 * A bare bones implementation of the scrolling list for mc without the background or borders. With
 * GL_SCISSOR to crop out the overflow
 *
 * <p>This is how an abstract implementation should look... :cry:
 */
public class ScrollingList<E extends EntryListWidget.Entry<E>> extends EntryListWidget<E> {
  ScrollingList(MinecraftClient client, int x, int y, int width, int height, int slotHeightIn) {
    super(client, width, height, y - (height / 2), (y - (height / 2)) + height, slotHeightIn);
    this.setLeftPos(x - (width / 2));
    this.method_31323(false); // removes gradient at top and bottom
    this.method_31322(false); // removes background
  }

  @Override
  public int getRowWidth() {
    return this.width;
  }

  @Override
  public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
    double scale = this.client.getWindow().getScaleFactor();

    GL11.glEnable(GL11.GL_SCISSOR_TEST);
    GL11.glScissor(
        (int) (this.left * scale),
        (int) (this.client.getWindow().getFramebufferHeight() - ((this.top + this.height) * scale)),
        (int) (this.width * scale),
        (int) (this.height * scale));

    super.render(stack, mouseX, mouseY, partialTicks);

    GL11.glDisable(GL11.GL_SCISSOR_TEST);
  }

  @Override
  public int getScrollbarPositionX() {
    return (this.left + this.width) - 6;
  }
}
