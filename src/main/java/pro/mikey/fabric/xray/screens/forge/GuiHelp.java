package pro.mikey.fabric.xray.screens.forge;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GuiHelp extends GuiBase {
    private final List<LinedText> areas = new ArrayList<>();

    public GuiHelp() {
        super(false);
        this.setSize(380, 210);
    }

    @Override
    public void init() {
        super.init();

        this.areas.clear();
        this.areas.add(new LinedText("xray.message.help.gui"));
        this.areas.add(new LinedText("xray.message.help.warning"));

        this.addRenderableWidget(
                new Button.Builder(
                        Component.translatable("xray.single.close"),
                        b -> {
                            this.getMinecraft().setScreen(new GuiSelectionScreen());
                        }).pos((this.getWidth() / 2) - 100,
                        (this.getHeight() / 2) + 80).size(200,
                        20).build());
    }

    @Override
    public void renderExtra(PoseStack stack, int x, int y, float partialTicks) {
        float lineY = (this.getHeight() / 2f) - 85;
        for (LinedText linedText : this.areas) {
            for (String line : linedText.getLines()) {
                lineY += 12;
                this.getFontRender()
                        .drawShadow(stack, line, (this.getWidth() / 2f) - 176, lineY, Color.WHITE.getRGB());
            }
            lineY += 10;
        }
    }

    @Override
    public boolean hasTitle() {
        return true;
    }

    @Override
    public ResourceLocation getBackground() {
        return BG_LARGE;
    }

    @Override
    public String title() {
        return I18n.get("xray.single.help");
    }

    private static class LinedText {
        private final String[] lines;

        LinedText(String key) {
            this.lines = I18n.get(key).split("\\R");
        }

        String[] getLines() {
            return this.lines;
        }
    }
}
