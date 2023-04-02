package pro.mikey.fabric.xray.screens.forge;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import pro.mikey.fabric.xray.XRay;

public abstract class GuiBase extends Screen {
    static final ResourceLocation BG_LARGE = new ResourceLocation(XRay.PREFIX_GUI + "bg-help.png");
    private static final ResourceLocation BG_NORMAL = new ResourceLocation(XRay.PREFIX_GUI + "bg.png");
    private final boolean hasSide;
    private String sideTitle = "";
    private int backgroundWidth = 229;
    private int backgroundHeight = 235;

    GuiBase(boolean hasSide) {
        super(Component.empty());
        this.hasSide = hasSide;
    }

    abstract void renderExtra(PoseStack stack, int x, int y, float partialTicks);

    @Override
    public boolean charTyped(char keyTyped, int __unknown) {
        super.charTyped(keyTyped, __unknown);

        if (keyTyped == 1 && this.getMinecraft().player != null) {
            this.getMinecraft().player.clientSideCloseContainer();
        }

        return false;
    }

    @Override
    public void render(PoseStack stack, int x, int y, float partialTicks) {
        this.renderBackground(stack);

        int width = this.width;
        int height = this.height;
        RenderSystem.setShaderTexture(0, this.getBackground());
        if (this.hasSide) {
            blit(stack, width / 2 + 60, height / 2 - 180 / 2, 0, 0, 150, 180, 150, 180);
            blit(
                    stack,
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
                this.getFontRender()
                        .drawShadow(
                                stack, this.sideTitle, (float) width / 2 + 80, (float) height / 2 - 77, 0xffff00);
            }
        }

        if (!this.hasSide) {
            blit(
                    stack,
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
                this.getFontRender()
                        .drawShadow(
                                stack, this.title(), (float) width / 2 - 138, (float) height / 2 - 105, 0xffff00);
            } else {
                this.getFontRender()
                        .drawShadow(
                                stack,
                                this.title(),
                                (float) width / 2 - ((float) this.backgroundWidth / 2) + 14,
                                (float) height / 2 - ((float) this.backgroundHeight / 2) + 13,
                                0xffff00
                        );
            }
        }

        this.renderExtra(stack, x, y, partialTicks);
        super.render(stack, x, y, partialTicks);
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
