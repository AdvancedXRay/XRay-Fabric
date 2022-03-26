package pro.mikey.fabric.xray.screens.forge;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import pro.mikey.fabric.xray.XRay;

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
                0xFFaf00
        );

        int width = this.width;
        int height = this.height;
        RenderSystem.setShaderTexture(0, this.getBackground());
        if (this.hasSide) {
            drawTexture(stack, width / 2 + 60, height / 2 - 180 / 2, 0, 0, 150, 180, 150, 180);
            drawTexture(
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
                    this.backgroundHeight
            );
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
                                0xffff00
                        );
            }
        }

        this.renderExtra(stack, x, y, partialTicks);

        for (Element button : this.children()) {
            if (button instanceof SupportButton && ((SupportButton) button).isHovered()) {
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
    public boolean shouldPause() {
        return false;
    }

    public MinecraftClient getMinecraft() {
        return this.client;
    }
}
