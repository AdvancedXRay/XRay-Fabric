package pro.mikey.fabric.xray.screens.forge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import pro.mikey.fabric.xray.Utils;
import pro.mikey.fabric.xray.XRay;

public abstract class GuiBase extends Screen {
    static final ResourceLocation BG_LARGE = Utils.rlFull(XRay.PREFIX_GUI + "bg-help.png");
    private static final ResourceLocation BG_NORMAL = Utils.rlFull(XRay.PREFIX_GUI + "bg.png");
    private final boolean hasSide;
    private String sideTitle = "";
    private int backgroundWidth = 229;
    private int backgroundHeight = 235;

    GuiBase(boolean hasSide) {
        super(Component.empty());
        this.hasSide = hasSide;
    }

    abstract void renderExtra(GuiGraphics guiGraphics, int x, int y, float partialTicks);

    @Override
    public boolean charTyped(char keyTyped, int __unknown) {
        super.charTyped(keyTyped, __unknown);

        if (keyTyped == 1 && this.getMinecraft().player != null) {
            this.getMinecraft().player.clientSideCloseContainer();
        }

        return false;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int x, int y, float partialTicks) {
        this.renderBackground(guiGraphics, x, y, partialTicks);
        int width = this.width;
        int height = this.height;
        if (this.hasSide) {
            guiGraphics.blit(RenderType::guiTextured, this.getBackground(), width / 2 + 60, height / 2 - 180 / 2, 0, 0, 150, 180, 150, 180);
            guiGraphics.blit(
                    RenderType::guiOpaqueTexturedBackground,
                    this.getBackground(),
                    width / 2 - 150,
                    height / 2 - 118,
                    0,
                    0,
                    this.backgroundWidth,
                    this.backgroundHeight,
                    this.backgroundWidth,
                    this.backgroundHeight
            );

            if (this.hasSideTitle()) {
                guiGraphics.drawString(this.font, this.sideTitle, width / 2 + 80, height / 2 - 77, 0xffff00);
            }
        }

        if (!this.hasSide) {
            guiGraphics.blit(
                    RenderType::guiTextured,
                    this.getBackground(),
                    width / 2 - this.backgroundWidth / 2 + 1,
                    height / 2 - this.backgroundHeight / 2,
                    0,
                    0,
                    this.backgroundWidth,
                    this.backgroundHeight,
                    this.backgroundWidth,
                    this.backgroundHeight
            );
        }

//        RenderSystem.enableTexture();
        if (this.hasTitle()) {
            if (this.hasSide) {
                guiGraphics.drawString(
                                this.font, this.title(), width / 2 - 138,  height / 2 - 105, 0xffff00);
            } else {
                guiGraphics.drawString(
                        this.font,
                                this.title(),
                                width / 2 - ( this.backgroundWidth / 2) + 14,
                                 height / 2 - ( this.backgroundHeight / 2) + 13,
                                0xffff00
                        );
            }
        }

        for (Renderable renderable : this.renderables) {
            renderable.render(guiGraphics, x, y, partialTicks);
        }

        this.renderExtra(guiGraphics, x, y, partialTicks);
    }

    public ResourceLocation getBackground() {
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

    Font getFontRender() {
        return this.getMinecraft().font;
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

    public Minecraft getMinecraft() {
        return this.minecraft;
    }
}
