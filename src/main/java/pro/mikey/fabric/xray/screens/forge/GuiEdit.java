package pro.mikey.fabric.xray.screens.forge;

import com.mojang.blaze3d.platform.Lighting;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import pro.mikey.fabric.xray.records.BasicColor;
import pro.mikey.fabric.xray.records.BlockEntry;
import pro.mikey.fabric.xray.storage.BlockStore;

public class GuiEdit extends GuiBase {
    private final BlockEntry block;
    private EditBox oreName;
    private RatioSliderWidget redSlider;
    private RatioSliderWidget greenSlider;
    private RatioSliderWidget blueSlider;
    private RatioSliderWidget alphaSlider;
    private Button changeDefaultState;
    private BlockState lastState;

    GuiEdit(BlockEntry block) {
        super(true); // Has a sidebar
        this.setSideTitle(I18n.get("xray.single.tools"));

        this.block = block;
    }

    @Override
    public void init() {
        this.addRenderableWidget(this.changeDefaultState = new Button.Builder( Component.literal(this.block.isDefault() ? "Already scanning for all states" : "Scan for all block states"), button -> {
            this.lastState = this.block.getState();
            this.block.setState(this.block.getState().getBlock().defaultBlockState());
            button.active = false;
        }).pos(this.getWidth() / 2 - 138, this.getHeight() / 2 + 60).size(202, 20).build());

        if (this.block.isDefault()) {
            this.changeDefaultState.active = false;
        }

        this.addRenderableWidget(new Button.Builder(Component.translatable("xray.single.delete"), b -> {
            try {
                BlockStore.getInstance().get().get(0).entries().remove(this.block);
                BlockStore.getInstance().write();
                BlockStore.getInstance().updateCache();
            } catch (Exception e) {
            }
            this.getMinecraft().setScreen(new GuiSelectionScreen());
        }).pos((this.getWidth() / 2) + 78, this.getHeight() / 2 - 60).size(120, 20).build());

        this.addRenderableWidget(new Button.Builder( Component.translatable("xray.single.cancel"), b -> {
            this.getMinecraft().setScreen(new GuiSelectionScreen());
        }).pos((this.getWidth() / 2) + 78, this.getHeight() / 2 + 58).size(120, 20).build());
        this.addRenderableWidget(new Button.Builder(Component.translatable("xray.single.save"), b -> {
            try {
                int index = BlockStore.getInstance().get().get(0).entries().indexOf(this.block);
                BlockEntry entry = BlockStore.getInstance().get().get(0).entries().get(index);
                entry.setName(this.oreName.getValue());
                entry.setColor(new BasicColor((int) (this.redSlider.getValue() * 255), (int) (this.greenSlider.getValue() * 255), (int) (this.blueSlider.getValue() * 255)));
                entry.setState(this.block.getState());
                entry.setDefault(this.lastState != null);
                BlockStore.getInstance().get().get(0).entries().set(index, entry);
                BlockStore.getInstance().write();
                BlockStore.getInstance().updateCache();
            } catch (Exception ignored) {
            } // lazy catching for basic failures

            this.getMinecraft().setScreen(new GuiSelectionScreen());
        }).pos(this.getWidth() / 2 - 138, this.getHeight() / 2 + 83).size(202, 20).build());

        this.addRenderableWidget(this.redSlider   = new RatioSliderWidget(this.getWidth() / 2 - 138, this.getHeight() / 2 - 40, 100, 20, Component.translatable("xray.color.red"),   0));
        this.addRenderableWidget(this.greenSlider = new RatioSliderWidget(this.getWidth() / 2 - 138, this.getHeight() / 2 - 18, 100, 20, Component.translatable("xray.color.green"), 0));
        this.addRenderableWidget(this.blueSlider  = new RatioSliderWidget(this.getWidth() / 2 - 138, this.getHeight() / 2 + 4,  100, 20, Component.translatable("xray.color.blue"),  0));
        this.addRenderableWidget(this.alphaSlider = new RatioSliderWidget(this.getWidth() / 2 - 138, this.getHeight() / 2 + 26, 100, 20, Component.translatable("xray.color.alpha"), 1));

        this.oreName = new EditBox(this.getMinecraft().font, this.getWidth() / 2 - 138, this.getHeight() / 2 - 63, 202, 20, Component.empty());
        this.oreName.setValue(this.block.getName());
        this.addRenderableWidget(this.oreName);
        this.addRenderableWidget(this.redSlider);
        this.addRenderableWidget(this.greenSlider);
        this.addRenderableWidget(this.blueSlider);
        this.addRenderableWidget(this.alphaSlider);

        this.redSlider.setValue(this.block.getHex().red() / 255f);
        this.greenSlider.setValue(this.block.getHex().green() / 255f);
        this.blueSlider.setValue(this.block.getHex().blue() / 255f);
        this.alphaSlider.setValue(this.block.getHex().alpha() / 255f);
    }

    @Override
    public void tick() {
        super.tick();
        this.oreName.tick();
    }

    @Override
    public void renderExtra(GuiGraphics guiGraphics, int x, int y, float partialTicks) {
        guiGraphics.drawString(this.font, this.block.getName(), this.getWidth() / 2 - 138, this.getHeight() / 2 - 90, 0xffffff);

        this.oreName.render(guiGraphics, x, y, partialTicks);

        int color = (255 << 24) | ((int) (this.redSlider.getValue() * 255) << 16) | ((int) (this.greenSlider.getValue() * 255) << 8) | (int) (this.blueSlider.getValue() * 255);

        guiGraphics.fill(this.getWidth() / 2 - 35, this.getHeight() / 2 - 40, (this.getWidth() / 2 - 35) + 100, (this.getHeight() / 2 - 40) + 64, color);

        guiGraphics.drawString(this.font, "Color", this.getWidth() / 2 - 30, this.getHeight() / 2 - 35, 0xffffff);
//        Lighting.setupFor3DItems();
        // TODO: Check
        guiGraphics.renderItem(this.block.getStack(), this.getWidth() / 2 + 50, this.getHeight() / 2 - 105);
//        Lighting.setupForFlatItems();
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
        return I18n.get("xray.title.edit");
    }
}
