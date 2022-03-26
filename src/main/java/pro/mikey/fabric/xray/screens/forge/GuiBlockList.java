package pro.mikey.fabric.xray.screens.forge;

import net.minecraft.block.Block;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import pro.mikey.fabric.xray.records.BlockWithStack;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GuiBlockList extends GuiBase {
    private final List<BlockWithStack> blocks;
    private ScrollingBlockList blockList;
    private TextFieldWidget search;
    private String lastSearched = "";

    GuiBlockList() {
        super(false);

        this.blocks = Registry.ITEM.stream().filter(item -> item instanceof BlockItem && item != Items.AIR).map(item -> new BlockWithStack(Block.getBlockFromItem(item), new ItemStack(item))).toList();
    }

    @Override
    public void init() {
        this.blockList = new ScrollingBlockList((this.getWidth() / 2) + 1, this.getHeight() / 2 - 12, 202, 185, this.blocks);
        this.addDrawableChild(this.blockList);

        this.search = new TextFieldWidget(this.getFontRender(), this.getWidth() / 2 - 100, this.getHeight() / 2 + 85, 140, 18, LiteralText.EMPTY);
        this.search.changeFocus(true);
        this.setFocused(this.search);

        this.addDrawableChild(new ButtonWidget(this.getWidth() / 2 + 43, this.getHeight() / 2 + 84, 60, 20, new TranslatableText("xray.single.cancel"), b -> {
            this.getMinecraft().setScreen(new GuiSelectionScreen());
        }));
    }

    @Override
    public void tick() {
        this.search.tick();
        if (!this.search.getText().equals(this.lastSearched)) {
            this.reloadBlocks();
        }

        super.tick();
    }

    private void reloadBlocks() {
        if (this.lastSearched.equals(this.search.getText())) {
            return;
        }

        this.blockList.updateEntries(this.search.getText().length() == 0 ? this.blocks : this.blocks.stream().filter(e -> e.stack().getName().getString().toLowerCase().contains(this.search.getText().toLowerCase())).collect(Collectors.toList()));

        this.lastSearched = this.search.getText();
        this.blockList.setScrollAmount(0);
    }

    @Override
    public void renderExtra(MatrixStack stack, int x, int y, float partialTicks) {
        this.search.render(stack, x, y, partialTicks);
        this.blockList.render(stack, x, y, partialTicks);
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (this.search.mouseClicked(x, y, button)) {
            this.setFocused(this.search);
        }

        return super.mouseClicked(x, y, button);
    }

    @Override
    public boolean mouseScrolled(double p_mouseScrolled_1_, double p_mouseScrolled_3_, double p_mouseScrolled_5_) {
        this.blockList.mouseScrolled(p_mouseScrolled_1_, p_mouseScrolled_3_, p_mouseScrolled_5_);
        return super.mouseScrolled(p_mouseScrolled_1_, p_mouseScrolled_3_, p_mouseScrolled_5_);
    }

    static class ScrollingBlockList extends ScrollingList<ScrollingBlockList.BlockSlot> {
        static final int SLOT_HEIGHT = 35;

        ScrollingBlockList(int x, int y, int width, int height, List<BlockWithStack> blocks) {
            super(x, y, width, height, SLOT_HEIGHT);
            this.updateEntries(blocks);
        }

        @Override
        public void setSelected(BlockSlot entry) {
            if (entry == null) {
                return;
            }

            this.client.player.closeScreen();
            this.client.setScreen(new GuiAddBlock(entry.getBlock().block().getDefaultState(), GuiBlockList::new));
        }

        void updateEntries(List<BlockWithStack> blocks) {
            this.clearEntries();
            blocks.forEach(block -> this.addEntry(new BlockSlot(block, this)));
        }

        public static class BlockSlot extends EntryListWidget.Entry<BlockSlot> {
            BlockWithStack block;
            ScrollingBlockList parent;

            BlockSlot(BlockWithStack block, ScrollingBlockList parent) {
                this.block = block;
                this.parent = parent;
            }

            BlockWithStack getBlock() {
                return this.block;
            }

            @Override
            public void render(MatrixStack stack, int entryIdx, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean p_194999_5_, float partialTicks) {
                TextRenderer font = this.parent.client.textRenderer;

                Identifier resource = Registry.BLOCK.getId(this.block.block());
                font.drawWithShadow(stack, this.block.stack().getItem().getName().getString(), left + 35, top + 7, Color.WHITE.getRGB());
                font.drawWithShadow(stack, resource.getNamespace(), left + 35, top + 17, Color.WHITE.getRGB());

                DiffuseLighting.enableGuiDepthLighting();
                this.parent.client.getItemRenderer().renderInGuiWithOverrides(this.block.stack(), left + 10, top + 7);
                DiffuseLighting.disableGuiDepthLighting();
            }

            @Override
            public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
                this.parent.setSelected(this);
                return false;
            }
        }
    }
}
