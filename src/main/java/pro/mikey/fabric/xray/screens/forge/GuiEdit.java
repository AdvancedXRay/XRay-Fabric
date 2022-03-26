package pro.mikey.fabric.xray.screens.forge;

import net.minecraft.block.BlockState;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import pro.mikey.fabric.xray.records.BasicColor;
import pro.mikey.fabric.xray.records.BlockEntry;
import pro.mikey.fabric.xray.storage.Stores;

public class GuiEdit extends GuiBase {
    private final BlockEntry block;
    private TextFieldWidget oreName;
    private RatioSliderWidget redSlider;
    private RatioSliderWidget greenSlider;
    private RatioSliderWidget blueSlider;
    private ButtonWidget changeDefaultState;
    private BlockState lastState;

    GuiEdit(BlockEntry block) {
        super(true); // Has a sidebar
        this.setSideTitle(I18n.translate("xray.single.tools"));

        this.block = block;
    }

    @Override
    public void init() {
        this.addDrawableChild(this.changeDefaultState = new ButtonWidget(this.getWidth() / 2 - 138, this.getHeight() / 2 + 60, 202, 20, new LiteralText(this.block.isDefault() ? "Already scanning for all states" : "Scan for all block states"), button -> {
            this.lastState = this.block.getState();
            this.block.setState(this.block.getState().getBlock().getDefaultState());
            button.active = false;
        }));

        if (this.block.isDefault()) {
            this.changeDefaultState.active = false;
        }

        this.addDrawableChild(new ButtonWidget((this.getWidth() / 2) + 78, this.getHeight() / 2 - 60, 120, 20, new TranslatableText("xray.single.delete"), b -> {
            try {
                Stores.BLOCKS.get().get(0).entries().remove(this.block);
                Stores.BLOCKS.write();
                Stores.BLOCKS.updateCache();
            } catch (Exception e) {
            }
            this.getMinecraft().setScreen(new GuiSelectionScreen());
        }));

        this.addDrawableChild(new ButtonWidget((this.getWidth() / 2) + 78, this.getHeight() / 2 + 58, 120, 20, new TranslatableText("xray.single.cancel"), b -> {
            this.getMinecraft().setScreen(new GuiSelectionScreen());
        }));
        this.addDrawableChild(new ButtonWidget(this.getWidth() / 2 - 138, this.getHeight() / 2 + 83, 202, 20, new TranslatableText("xray.single.save"), b -> {
            try {
                int index = Stores.BLOCKS.get().get(0).entries().indexOf(this.block);
                BlockEntry entry = Stores.BLOCKS.get().get(0).entries().get(index);
                entry.setName(this.oreName.getText());
                entry.setColor(new BasicColor((int) (this.redSlider.getValue() * 255), (int) (this.greenSlider.getValue() * 255), (int) (this.blueSlider.getValue() * 255)));
                entry.setState(this.block.getState());
                entry.setDefault(this.lastState != null);
                Stores.BLOCKS.get().get(0).entries().set(index, entry);
                Stores.BLOCKS.write();
                Stores.BLOCKS.updateCache();
            } catch (Exception ignored) {
            } // lazy catching for basic failures

            this.getMinecraft().setScreen(new GuiSelectionScreen());
        }));

        this.addDrawableChild(this.redSlider = new RatioSliderWidget(this.getWidth() / 2 - 138, this.getHeight() / 2 - 40, 100, 20, new TranslatableText("xray.color.red"), 0));
        this.addDrawableChild(this.greenSlider = new RatioSliderWidget(this.getWidth() / 2 - 138, this.getHeight() / 2 - 18, 100, 20, new TranslatableText("xray.color.green"), 0));
        this.addDrawableChild(this.blueSlider = new RatioSliderWidget(this.getWidth() / 2 - 138, this.getHeight() / 2 + 4, 100, 20, new TranslatableText("xray.color.blue"), 0));

        this.oreName = new TextFieldWidget(this.getMinecraft().textRenderer, this.getWidth() / 2 - 138, this.getHeight() / 2 - 63, 202, 20, LiteralText.EMPTY);
        this.oreName.setText(this.block.getName());
        this.addDrawableChild(this.oreName);
        this.addDrawableChild(this.redSlider);
        this.addDrawableChild(this.greenSlider);
        this.addDrawableChild(this.blueSlider);

        this.redSlider.setValue(this.block.getHex().red() / 255f);
        this.greenSlider.setValue(this.block.getHex().green() / 255f);
        this.blueSlider.setValue(this.block.getHex().blue() / 255f);
    }

    @Override
    public void tick() {
        super.tick();
        this.oreName.tick();
    }

    @Override
    public void renderExtra(MatrixStack stack, int x, int y, float partialTicks) {
        this.getFontRender().drawWithShadow(stack, this.block.getName(), this.getWidth() / 2f - 138, this.getHeight() / 2f - 90, 0xffffff);

        this.oreName.render(stack, x, y, partialTicks);

        int color = (255 << 24) | ((int) (this.redSlider.getValue() * 255) << 16) | ((int) (this.greenSlider.getValue() * 255) << 8) | (int) (this.blueSlider.getValue() * 255);

        fill(stack, this.getWidth() / 2 - 35, this.getHeight() / 2 - 40, (this.getWidth() / 2 - 35) + 100, (this.getHeight() / 2 - 40) + 64, color);

        this.getFontRender().drawWithShadow(stack, "Color", this.getWidth() / 2f - 30, this.getHeight() / 2f - 35, 0xffffff);
        DiffuseLighting.enableGuiDepthLighting();
        this.itemRenderer.renderInGuiWithOverrides(this.block.getStack(), this.getWidth() / 2 + 50, this.getHeight() / 2 - 105);
        DiffuseLighting.disableGuiDepthLighting();
    }

    @Override
    public boolean mouseClicked(double x, double y, int mouse) {
        if (this.oreName.mouseClicked(x, y, mouse)) {
            this.setFocused(this.oreName);
        }

        return super.mouseClicked(x, y, mouse);
    }

    @Override
    public boolean mouseReleased(double x, double y, int mouse) {
        return super.mouseReleased(x, y, mouse);
    }

    @Override
    public boolean hasTitle() {
        return true;
    }

    @Override
    public String title() {
        return I18n.translate("xray.title.edit");
    }
}
