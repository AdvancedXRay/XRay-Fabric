package pro.mikey.fabric.xray.screens.forge;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import pro.mikey.fabric.xray.XRay;
import pro.mikey.fabric.xray.records.BlockGroup;
import pro.mikey.fabric.xray.records.BlockWithStack;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GuiBlockList extends GuiBase {
    private List<BlockWithStack> blocks;
    private ScrollingBlockList blockList;
    private EditBox search;
    private String lastSearched = "";
    private BlockGroup group;

    GuiBlockList(BlockGroup group) {
        super(false);
        this.group = group;
        this.blocks = BuiltInRegistries.ITEM.stream().filter(item -> item instanceof BlockItem && item != Items.AIR).map(item -> new BlockWithStack(Block.byItem(item), new ItemStack(item))).toList();
    }

    @Override
    public void init() {
        blocks = new ArrayList<>(blocks);
        BuiltInRegistries.FLUID.stream().forEach(fluid -> {
            if(fluid.isSource(fluid.defaultFluidState())){
                BlockWithStack stack = new BlockWithStack( fluid.defaultFluidState().createLegacyBlock().getBlock(),new ItemStack(fluid.getBucket()));
                blocks.add(stack);
            }
        });

        this.blockList = new ScrollingBlockList((this.getWidth() / 2) + 1, this.getHeight() / 2 - 12, 202, 185, this.blocks,group);
        this.addRenderableWidget(this.blockList);

        this.search = new EditBox(this.getFontRender(), this.getWidth() / 2 - 100, this.getHeight() / 2 + 85, 140, 18, Component.empty());
        this.search.setFocused(true);
        this.setFocused(this.search);

        this.addRenderableWidget(new Button.Builder(Component.translatable("xray.single.cancel"), b -> {
            assert this.getMinecraft() != null;
            this.getMinecraft().setScreen(new GuiBlockSelectionScreen(group));
        }).pos(this.getWidth() / 2 + 43, this.getHeight() / 2 + 84).size(60, 20).build());
    }

    @Override
    public void tick() {
        this.search.tick();
        if (!this.search.getValue().equals(this.lastSearched)) {
            this.reloadBlocks();
        }

        super.tick();
    }

    private void reloadBlocks() {
        if (this.lastSearched.equals(this.search.getValue())) {
            return;
        }

        this.blockList.updateEntries(this.search.getValue().length() == 0 ? this.blocks : this.blocks.stream().filter(e -> e.stack().getHoverName().getString().toLowerCase().contains(this.search.getValue().toLowerCase())).collect(Collectors.toList()));

        this.lastSearched = this.search.getValue();
        this.blockList.setScrollAmount(0);
    }

    @Override
    public void renderExtra(PoseStack stack, int x, int y, float partialTicks) {
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
    public boolean mouseScrolled(double pMouseScrolled1, double pMouseScrolled2, double pMouseScrolled3) {
        this.blockList.mouseScrolled(pMouseScrolled1, pMouseScrolled2, pMouseScrolled3);
        return super.mouseScrolled(pMouseScrolled1, pMouseScrolled2, pMouseScrolled3);
    }

    static class ScrollingBlockList extends ScrollingList<ScrollingBlockList.BlockSlot> {
        static final int SLOT_HEIGHT = 35;
        private BlockGroup group;

        ScrollingBlockList(int x, int y, int width, int height, List<BlockWithStack> blocks,BlockGroup group) {
            super(x, y, width, height, SLOT_HEIGHT);
            this.group=group;
            this.updateEntries(blocks);
        }

        @Override
        public void setSelected(BlockSlot entry) {
            if (entry == null) {
                return;
            }

            assert this.minecraft.player != null;
            this.minecraft.player.clientSideCloseContainer();
            this.minecraft.setScreen(new GuiAddBlock(entry.getBlock().block().defaultBlockState(),group, () -> new GuiBlockList(group)));
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
            public void render(PoseStack stack, int entryIdx, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean boolean5, float partialTicks) {
                Font font = this.parent.minecraft.font;

                ResourceLocation resource = BuiltInRegistries.BLOCK.getKey(this.block.block());
                font.drawShadow(stack, this.block.stack().getItem().getDescription().getString(), left + 35, top + 7, Color.WHITE.getRGB());
                font.drawShadow(stack, resource.getNamespace(), left + 35, top + 17, Color.WHITE.getRGB());

                Lighting.setupFor3DItems();
                this.parent.minecraft.getItemRenderer().renderAndDecorateItem(stack, this.block.stack(), left + 10, top + 7);
                Lighting.setupForFlatItems();
            }

            @Override
            public boolean mouseClicked(double pMouseClicked1, double pMouseClicked3, int pMouseClicked5) {
                this.parent.setSelected(this);
                return false;
            }
        }
    }
}
