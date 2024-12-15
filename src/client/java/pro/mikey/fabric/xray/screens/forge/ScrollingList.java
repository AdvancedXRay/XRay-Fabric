package pro.mikey.fabric.xray.screens.forge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.narration.NarrationElementOutput;

public class ScrollingList<E extends AbstractSelectionList.Entry<E>> extends AbstractSelectionList<E> {
    ScrollingList(int x, int y, int width, int height, int slotHeightIn) {
        super(
                Minecraft.getInstance(),
                width,
                height,
                y - (height / 2),
                slotHeightIn);
        this.setX(x - (width / 2));
    }

    @Override
    public void renderWidget(GuiGraphics stack, int mouseX, int mouseY, float partialTicks) {
        super.renderWidget(stack, mouseX, mouseY, partialTicks);
    }

    @Override
    protected int getScrollbarPosition() {
        return (this.getX() + this.width) - 6;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    @Override
    public int getRowWidth() {
        return this.width - 10;
    }

    @Override
    public int getRowLeft() {
        return this.getX();
    }
}
