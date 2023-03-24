package pro.mikey.fabric.xray.screens.forge;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import pro.mikey.fabric.xray.records.BasicColor;
import pro.mikey.fabric.xray.records.BlockEntry;
import pro.mikey.fabric.xray.records.BlockGroup;
import pro.mikey.fabric.xray.storage.BlockStore;

public class GuiCategoryEdit extends GuiBase {
    private final BlockGroup group;
    private RatioSliderWidget redSlider;
    private RatioSliderWidget greenSlider;
    private RatioSliderWidget blueSlider;
    private EditBox groupName;

    GuiCategoryEdit(BlockGroup block) {
        super(true); // Has a sidebar
        this.setSideTitle(I18n.get("xray.single.tools"));

        this.group = block;
    }

    @Override
    public void init() {

        this.addRenderableWidget(new Button.Builder(Component.translatable("xray.single.delete"), b -> {
            BlockStore.getInstance().get().remove(group);
            BlockStore.getInstance().write();
            BlockStore.getInstance().updateCache();
            this.getMinecraft().setScreen(new GuiSelectionScreen());
        }).pos((this.getWidth() / 2) + 78, this.getHeight() / 2 - 60).size(120, 20).build());

        this.addRenderableWidget(new Button.Builder( Component.translatable("xray.single.cancel"), b -> {
            this.getMinecraft().setScreen(new GuiSelectionScreen());
        }).pos((this.getWidth() / 2) + 78, this.getHeight() / 2 + 58).size(120, 20).build());

        this.addRenderableWidget(new Button.Builder(Component.translatable("xray.single.save"), b -> {
            group.setName(groupName.getValue());
            BasicColor color = new BasicColor((int) (this.redSlider.getValue() * 255), (int) (this.greenSlider.getValue() * 255), (int) (this.blueSlider.getValue() * 255));
            group.setColor(color);
            group.save();

            this.getMinecraft().setScreen(new GuiSelectionScreen());
        }).pos(this.getWidth() / 2 - 138, this.getHeight() / 2 + 83).size(202, 20).build());

        this.addRenderableWidget(this.redSlider = new RatioSliderWidget(this.getWidth() / 2 - 138, this.getHeight() / 2 - 40, 100, 20, Component.translatable("xray.color.red"), 0));
        this.addRenderableWidget(this.greenSlider = new RatioSliderWidget(this.getWidth() / 2 - 138, this.getHeight() / 2 - 18, 100, 20, Component.translatable("xray.color.green"), 0));
        this.addRenderableWidget(this.blueSlider = new RatioSliderWidget(this.getWidth() / 2 - 138, this.getHeight() / 2 + 4, 100, 20, Component.translatable("xray.color.blue"), 0));


        this.groupName = new EditBox(this.getMinecraft().font, this.getWidth() / 2 - 138, this.getHeight() / 2 - 63, 202, 20, Component.empty());
        this.groupName.setValue(this.group.getName());
        this.addRenderableWidget(this.groupName);
        this.redSlider.setValue(this.group.getColor().red() / 255f);
        this.greenSlider.setValue(this.group.getColor().green() / 255f);
        this.blueSlider.setValue(this.group.getColor().blue() / 255f);
    }

    @Override
    public void tick() {
        super.tick();
        this.groupName.tick();
    }

    @Override
    public void renderExtra(PoseStack stack, int x, int y, float partialTicks) {
        this.getFontRender().drawShadow(stack, this.group.getName(), this.getWidth() / 2f - 138, this.getHeight() / 2f - 90, 0xffffff);

        this.groupName.render(stack, x, y, partialTicks);

        int color = (255 << 24) | ((int) (this.redSlider.getValue() * 255) << 16) | ((int) (this.greenSlider.getValue() * 255) << 8) | (int) (this.blueSlider.getValue() * 255);

        fill(stack, this.getWidth() / 2 - 35, this.getHeight() / 2 - 40, (this.getWidth() / 2 - 35) + 100, (this.getHeight() / 2 - 40) + 64, color);

        this.getFontRender().drawShadow(stack, "Color", this.getWidth() / 2f - 30, this.getHeight() / 2f - 35, 0xffffff);
        Lighting.setupFor3DItems();
        if(!this.group.getFirst().getStack().getItem().equals(BlockEntry.getAir().getStack().getItem())){
            this.itemRenderer.renderAndDecorateItem(this.group.getFirst().getStack(), this.getWidth() / 2 + 50, this.getHeight() / 2 - 105);
        }
        Lighting.setupForFlatItems();
    }

    @Override
    public boolean mouseClicked(double x, double y, int mouse) {
        if (this.groupName.mouseClicked(x, y, mouse)) {
            this.setFocused(this.groupName);
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
        return I18n.get("xray.single.group.edit");
    }
}
