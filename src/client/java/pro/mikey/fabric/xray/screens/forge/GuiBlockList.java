package pro.mikey.fabric.xray.screens.forge;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import pro.mikey.fabric.xray.records.BlockWithStack;

import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class GuiBlockList extends GuiBase {
    private final List<BlockWithStack> blocks;
    private ScrollingBlockList blockList;
    private EditBox search;
    private String lastSearched = "";

    GuiBlockList() {
        super(false);
        this.blocks = BuiltInRegistries.ITEM.stream().filter(item -> item instanceof BlockItem && item != Items.AIR).map(item -> new BlockWithStack(Block.byItem(item), new ItemStack(item))).toList();
    }

    @Override
    public void init() {
        this.blockList = new ScrollingBlockList((this.getWidth() / 2) + 1, this.getHeight() / 2 - 12, 202, 185, this.blocks);
        this.addRenderableWidget(this.blockList);

        this.search = new EditBox(this.getFontRender(), this.getWidth() / 2 - 100, this.getHeight() / 2 + 85, 140, 18, Component.empty());
        this.search.setFocused(true);
        this.setFocused(this.search);

        this.addRenderableWidget(new Button.Builder(Component.translatable("xray.single.cancel"), b -> {
            assert this.getMinecraft() != null;
            this.getMinecraft().setScreen(new GuiSelectionScreen());
        }).pos(this.getWidth() / 2 + 43, this.getHeight() / 2 + 84).size(60, 20).build());
    }

    @Override
    public void tick() {
//        this.search.tick
        if (!this.search.getValue().equals(this.lastSearched)) {
            this.reloadBlocks();
        }

        super.tick();
    }

    private void reloadBlocks() {
        if (this.lastSearched.equals(this.search.getValue())) {
            return;
        }

        this.blockList.updateEntries(this.search.getValue().isEmpty() ? this.blocks : this.blocks.stream().filter(e -> e.stack().getHoverName().getString().toLowerCase().contains(this.search.getValue().toLowerCase())).collect(Collectors.toList()));

        this.lastSearched = this.search.getValue();
        this.blockList.setScrollAmount(0);
    }

    @Override
    public void renderExtra(GuiGraphics guiGraphics, int x, int y, float partialTicks) {
        this.search.render(guiGraphics, x, y, partialTicks);
        this.blockList.render(guiGraphics, x, y, partialTicks);
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (this.search.mouseClicked(x, y, button)) {
            this.setFocused(this.search);
        }

        return super.mouseClicked(x, y, button);
    }

//    @Override
//    public boolean mouseScrolled(double p_mouseScrolled_1_, double p_mouseScrolled_3_, double p_mouseScrolled_5_) {
//        this.blockList.mouseScrolled(p_mouseScrolled_1_, p_mouseScrolled_3_, p_mouseScrolled_5_);
//        return super.mouseScrolled(p_mouseScrolled_1_, p_mouseScrolled_3_, p_mouseScrolled_5_);
//    }

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

            assert this.minecraft.player != null;
            this.minecraft.player.clientSideCloseContainer();
            this.minecraft.setScreen(new GuiAddBlock(entry.getBlock().block().defaultBlockState(), GuiBlockList::new));
        }

        void updateEntries(List<BlockWithStack> blocks) {
            this.clearEntries();
            blocks.forEach(block -> this.addEntry(new BlockSlot(block, this)));
        }

        public static class BlockSlot extends AbstractSelectionList.Entry<BlockSlot> {
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
            public void render(GuiGraphics graphics, int entryIdx, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean p_194999_5_, float partialTicks) {
                Font font = this.parent.minecraft.font;

                ResourceLocation resource = BuiltInRegistries.BLOCK.getKey(this.block.block());
                graphics.drawString(font, this.block.stack().getItem().getName(), left + 35, top + 7, Color.WHITE.getRGB());
                graphics.drawString(font, resource.getNamespace(), left + 35, top + 17, Color.WHITE.getRGB());

                // TODO: CHECK
//                Lighting.setupFor3DItems();
                graphics.renderItem(this.block.stack(), left + 10, top + 7);
//                this.parent.minecraft.getItemRenderer().renderAndDecorateItem(stack, this.block.stack(), left + 10, top + 7);
//                Lighting.setupForFlatItems();
            }

            @Override
            public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
                this.parent.setSelected(this);
                return false;
            }
        }
    }
}
