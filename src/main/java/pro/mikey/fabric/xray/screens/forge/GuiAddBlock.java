package pro.mikey.fabric.xray.screens.forge;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import pro.mikey.fabric.xray.records.BasicColor;
import pro.mikey.fabric.xray.records.BlockEntry;
import pro.mikey.fabric.xray.records.BlockGroup;
import pro.mikey.fabric.xray.storage.BlockStore;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Supplier;

public class GuiAddBlock extends GuiBase {
    private final ItemStack itemStack;
    private final Supplier<GuiBase> previousScreenCallback;
    private BlockState selectBlock;
    private EditBox oreName;
    private RatioSliderWidget redSlider;
    private RatioSliderWidget greenSlider;
    private RatioSliderWidget blueSlider;
    private BlockGroup group;
    private boolean oreNameCleared = false;

    GuiAddBlock(BlockState selectedBlock, BlockGroup group, Supplier<GuiBase> previousScreenCallback) {
        super(false);
        this.group = group;
        this.selectBlock = selectedBlock;
        this.previousScreenCallback = previousScreenCallback;
        this.itemStack = new ItemStack(this.selectBlock.getBlock(), 1);
    }

    @Override
    public void init() {
        // Called when the gui should be (re)created
        boolean isDefaultState = this.selectBlock == this.selectBlock.getBlock().defaultBlockState();
        Button changeDefaultState;
        this.addRenderableWidget(changeDefaultState = new Button.Builder(Component.literal(isDefaultState ? "Already scanning for all states" : "Scan for all block states"), button -> {
            this.selectBlock = this.selectBlock.getBlock().defaultBlockState();
            button.active = false;
        }).pos(this.getWidth() / 2 - 100, this.getHeight() / 2 + 60).size(202, 20).build());

        if (isDefaultState) {
            changeDefaultState.active = false;
        }

        this.addRenderableWidget(new Button.Builder(Component.translatable("xray.single.add"), button -> {
            this.onClose();
            group = group != null ? group : new BlockGroup("default", new ArrayList<>(), 1, true);
            group.entries().add(new BlockEntry(this.selectBlock, this.oreName.getValue(), new BasicColor((int) (this.redSlider.getValue() * 255), (int) (this.greenSlider.getValue() * 255), (int) (this.blueSlider.getValue() * 255)), group.entries().size() + 1, this.selectBlock == this.selectBlock.getBlock().defaultBlockState(), true));
            BlockStore.getInstance().write();
            BlockStore.getInstance().updateCache();

            this.getMinecraft().setScreen(new GuiBlockSelectionScreen(group));
        }).pos(this.getWidth() / 2 - 100, this.getHeight() / 2 + 85).size(128, 20).build());
        this.addRenderableWidget(new Button.Builder(Component.translatable("xray.single.cancel"), b -> {
            this.onClose();
            this.getMinecraft().setScreen(this.previousScreenCallback.get());
        }).pos(this.getWidth() / 2 + 30,this.getHeight() / 2 + 85).size(72, 20).build());

        this.addRenderableWidget(this.redSlider = new RatioSliderWidget(this.getWidth() / 2 - 100, this.getHeight() / 2 - 40, 100, 20, Component.translatable("xray.color.red"), 0));
        this.addRenderableWidget(this.greenSlider = new RatioSliderWidget(this.getWidth() / 2 - 100, this.getHeight() / 2 - 18, 100, 20, Component.translatable("xray.color.green"), 0));

        this.addRenderableWidget(this.blueSlider = new RatioSliderWidget(this.getWidth() / 2 - 100, this.getHeight() / 2 + 4, 100, 20, Component.translatable("xray.color.blue"), 0));

        this.oreName = new EditBox(this.getMinecraft().font, this.getWidth() / 2 - 100, this.getHeight() / 2 - 70, 202, 20, Component.empty());

        this.oreName.setValue(this.selectBlock.getBlock().getName().getString());
        this.addRenderableWidget(this.oreName);
        this.addRenderableWidget(this.redSlider);
        this.addRenderableWidget(this.greenSlider);
        this.addRenderableWidget(this.blueSlider);
        this.addRenderableWidget(changeDefaultState);
    }

    @Override
    public void tick() {
        super.tick();
        this.oreName.tick();
    }

    @Override
    public void renderExtra(PoseStack stack, int x, int y, float partialTicks) {
        int color = (255 << 24) | ((int) (this.redSlider.getValue() * 255) << 16) | ((int) (this.greenSlider.getValue() * 255) << 8) | (int) (this.blueSlider.getValue() * 255);

        fill(stack, this.getWidth() / 2 + 2, this.getHeight() / 2 - 40, (this.getWidth() / 2 + 2) + 100, (this.getHeight() / 2 - 40) + 64, color);

        this.getFontRender().drawShadow(stack, this.selectBlock.getBlock().getName().getString(), this.getWidth() / 2f - 100, this.getHeight() / 2f - 90, 0xffffff);

        this.oreName.render(stack, x, y, partialTicks);

        this.getFontRender().drawShadow(stack, "Color", this.getWidth() / 2f + 10, this.getHeight() / 2f - 35, 0xffffff);

        Lighting.setupFor3DItems();
        this.itemRenderer.renderAndDecorateItem(stack, this.itemStack, this.getWidth() / 2 + 85, this.getHeight() / 2 - 105);
        Lighting.setupForFlatItems();
    }

    @Override
    public boolean mouseClicked(double x, double y, int mouse) {
        if (this.oreName.mouseClicked(x, y, mouse)) {
            this.setFocused(this.oreName);
        }

        if (this.oreName.isFocused() && !this.oreNameCleared) {
            this.oreName.setValue("");
            this.oreNameCleared = true;
        }

        if (!this.oreName.isFocused() && this.oreNameCleared && Objects.equals(this.oreName.getValue(), "")) {
            this.oreNameCleared = false;
            this.oreName.setValue(I18n.get("xray.input.gui"));
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
        return I18n.get("xray.title.config");
    }
}
