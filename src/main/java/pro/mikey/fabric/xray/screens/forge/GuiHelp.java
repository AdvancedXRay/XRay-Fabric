package pro.mikey.fabric.xray.screens.forge;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

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

        this.addDrawableChild(
                new ButtonWidget(
                        (this.getWidth() / 2) - 100,
                        (this.getHeight() / 2) + 80,
                        200,
                        20,
                        new TranslatableText("xray.single.close"),
                        b -> {
                            this.getMinecraft().setScreen(new GuiSelectionScreen());
                        }));
    }

    @Override
    public void renderExtra(MatrixStack stack, int x, int y, float partialTicks) {
        float lineY = (this.getHeight() / 2f) - 85;
        for (LinedText linedText : this.areas) {
            for (String line : linedText.getLines()) {
                lineY += 12;
                this.getFontRender()
                        .drawWithShadow(stack, line, (this.getWidth() / 2f) - 176, lineY, Color.WHITE.getRGB());
            }
            lineY += 10;
        }
    }

    @Override
    public boolean hasTitle() {
        return true;
    }

    @Override
    public Identifier getBackground() {
        return BG_LARGE;
    }

    @Override
    public String title() {
        return I18n.translate("xray.single.help");
    }

    private static class LinedText {
        private final String[] lines;

        LinedText(String key) {
            this.lines = I18n.translate(key).split("\\R");
        }

        String[] getLines() {
            return this.lines;
        }
    }
}
