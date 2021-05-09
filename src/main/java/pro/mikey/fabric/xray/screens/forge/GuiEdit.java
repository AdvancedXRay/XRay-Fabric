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
        this.addButton(
            this.changeDefaultState =
                new ButtonWidget(
                    this.getWidth() / 2 - 138,
                    this.getHeight() / 2 + 60,
                    202,
                    20,
                    new LiteralText(
                        this.block.isDefault()
                            ? "Already scanning for all states"
                            : "Scan for all block states"),
                    button -> {
                        this.lastState = this.block.getState();
                        this.block.setState(this.block.getState().getBlock().getDefaultState());
                        button.active = false;
                    }
                ));

        if (this.block.isDefault()) {
            this.changeDefaultState.active = false;
        }

        this.addButton(
            new ButtonWidget(
                (this.getWidth() / 2) + 78,
                this.getHeight() / 2 - 60,
                120,
                20,
                new TranslatableText("xray.single.delete"),
                b -> {
                    try {
                        Stores.BLOCKS.get().get(0).getEntries().remove(this.block);
                        Stores.BLOCKS.write();
                        Stores.BLOCKS.updateCache();
                    } catch (Exception e) {
                    }
                    this.onClose();
                    this.getMinecraft().openScreen(new GuiSelectionScreen());
                }
            ));

        this.addButton(
            new ButtonWidget(
                (this.getWidth() / 2) + 78,
                this.getHeight() / 2 + 58,
                120,
                20,
                new TranslatableText("xray.single.cancel"),
                b -> {
                    this.onClose();
                    this.getMinecraft().openScreen(new GuiSelectionScreen());
                }
            ));
        this.addButton(
            new ButtonWidget(
                this.getWidth() / 2 - 138,
                this.getHeight() / 2 + 83,
                202,
                20,
                new TranslatableText("xray.single.save"),
                b -> {
                    try {
                        int index = Stores.BLOCKS.get().get(0).getEntries().indexOf(this.block);
                        BlockEntry entry = Stores.BLOCKS.get().get(0).getEntries().get(index);
                        entry.setName(this.oreName.getText());
                        entry.setColor(
                            new BasicColor(
                                (int) (this.redSlider.getValue() * 255),
                                (int) (this.greenSlider.getValue() * 255),
                                (int) (this.blueSlider.getValue() * 255)
                            ));
                        entry.setState(this.block.getState());
                        entry.setDefault(this.lastState != null);
                        Stores.BLOCKS.get().get(0).getEntries().set(index, entry);
                        Stores.BLOCKS.write();
                        Stores.BLOCKS.updateCache();
                    } catch (Exception ex) {
                    } // lazy catching for basic failures

                    this.onClose();
                    this.getMinecraft().openScreen(new GuiSelectionScreen());
                }
            ));

        this.addButton(
            this.redSlider =
                new RatioSliderWidget(
                    this.getWidth() / 2 - 138,
                    this.getHeight() / 2 - 40,
                    100,
                    20,
                    new TranslatableText("xray.color.red"),
                    0
                ));
        this.addButton(
            this.greenSlider =
                new RatioSliderWidget(
                    this.getWidth() / 2 - 138,
                    this.getHeight() / 2 - 18,
                    100,
                    20,
                    new TranslatableText("xray.color.green"),
                    0
                ));
        this.addButton(
            this.blueSlider =
                new RatioSliderWidget(
                    this.getWidth() / 2 - 138,
                    this.getHeight() / 2 + 4,
                    100,
                    20,
                    new TranslatableText("xray.color.blue"),
                    0
                ));

        this.oreName =
            new TextFieldWidget(
                this.getMinecraft().textRenderer,
                this.getWidth() / 2 - 138,
                this.getHeight() / 2 - 63,
                202,
                20,
                LiteralText.EMPTY
            );
        this.oreName.setText(this.block.getName());
        this.children.add(this.oreName);
        this.children.add(this.redSlider);
        this.children.add(this.greenSlider);
        this.children.add(this.blueSlider);

        this.redSlider.setValue(this.block.getHex().getRed() / 255f);
        this.greenSlider.setValue(this.block.getHex().getGreen() / 255f);
        this.blueSlider.setValue(this.block.getHex().getBlue() / 255f);
    }

    @Override
    public void tick() {
        super.tick();
        this.oreName.tick();
    }

    @Override
    public void renderExtra(MatrixStack stack, int x, int y, float partialTicks) {
        this.getFontRender()
            .drawWithShadow(
                stack,
                this.block.getName(),
                this.getWidth() / 2f - 138,
                this.getHeight() / 2f - 90,
                0xffffff
            );

        this.oreName.render(stack, x, y, partialTicks);

        GuiAddBlock.renderPreview(
            this.getWidth() / 2 - 35,
            this.getHeight() / 2 - 40,
            (float) this.redSlider.getValue() * 255,
            (float) this.greenSlider.getValue() * 255,
            (float) this.blueSlider.getValue() * 255
        );

        DiffuseLighting.enableGuiDepthLighting();
        this.itemRenderer.renderInGuiWithOverrides(
            this.block.getStack(), this.getWidth() / 2 + 50, this.getHeight() / 2 - 105);
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
