package pro.mikey.fabric.xray.screens.forge;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import pro.mikey.fabric.xray.ScanController;
import pro.mikey.fabric.xray.StateSettings;
import pro.mikey.fabric.xray.XRay;
import pro.mikey.fabric.xray.records.BasicColor;
import pro.mikey.fabric.xray.records.BlockEntry;
import pro.mikey.fabric.xray.records.BlockGroup;
import pro.mikey.fabric.xray.storage.BlockStore;
import pro.mikey.fabric.xray.storage.SettingsStore;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class GuiBlockSelectionScreen extends GuiBase {
    private static final List<Block> ORE_TAGS = List.of(Blocks.GOLD_ORE, Blocks.IRON_ORE, Blocks.DIAMOND_ORE, Blocks.REDSTONE_ORE, Blocks.LAPIS_ORE, Blocks.COAL_ORE, Blocks.EMERALD_ORE, Blocks.COPPER_ORE, Blocks.DEEPSLATE_GOLD_ORE, Blocks.DEEPSLATE_IRON_ORE, Blocks.DEEPSLATE_DIAMOND_ORE, Blocks.DEEPSLATE_REDSTONE_ORE, Blocks.DEEPSLATE_LAPIS_ORE, Blocks.DEEPSLATE_COAL_ORE, Blocks.DEEPSLATE_EMERALD_ORE, Blocks.DEEPSLATE_COPPER_ORE, Blocks.NETHER_GOLD_ORE, Blocks.ANCIENT_DEBRIS);

    private static final ResourceLocation CIRCLE = new ResourceLocation(XRay.PREFIX_GUI + "circle.png");
    public ItemRenderer render;
    private Button distButtons;
    private EditBox search;
    private String lastSearch = "";
    private List<BlockEntry> itemList;
    private BlockGroup group;
    private ScrollingBlockList scrollList;

    public GuiBlockSelectionScreen(BlockGroup group) {
        super(true);
        this.setSideTitle(I18n.get("xray.single.tools"));
        this.group=group;
        this.itemList = group.entries();
    }

    @Override
    public void init() {
        if (this.minecraft.player == null) {
            return;
        }

        this.render = this.itemRenderer;
//        this.buttons.clear();

        this.scrollList = new ScrollingBlockList((this.getWidth() / 2) - 37, this.getHeight() / 2 + 10, 203, 185, this.itemList, this);
        this.addRenderableWidget(this.scrollList);

        this.search = new EditBox(this.getFontRender(), this.getWidth() / 2 - 137, this.getHeight() / 2 - 105, 202, 18, Component.empty());
        this.search.setCanLoseFocus(true);

        // sidebar buttons
        this.addRenderableWidget(Button.builder( Component.translatable("xray.input.add"), button -> {
            this.minecraft.player.clientSideCloseContainer();
            this.minecraft.setScreen(new GuiBlockList());
        })
                .pos((this.getWidth() / 2) + 79, this.getHeight() / 2 - 60)
                .size( 120, 20)
                .tooltip(Tooltip.create(Component.translatable("xray.tooltips.add_block")))
                .build());

        this.addRenderableWidget(Button.builder(Component.translatable("xray.input.add_hand"), button -> {
            this.minecraft.player.clientSideCloseContainer();
            ItemStack handItem = this.minecraft.player.getItemInHand(InteractionHand.MAIN_HAND);

            // Check if the hand item is a block or not
            if (!(handItem.getItem() instanceof BlockItem)) {
                this.minecraft.player.displayClientMessage(Component.literal("[XRay] " + I18n.get("xray.message.invalid_hand", handItem.getHoverName().getString())), false);
                return;
            }

            this.minecraft.setScreen(new GuiAddBlock(((BlockItem) handItem.getItem()).getBlock().defaultBlockState(), () -> new GuiBlockSelectionScreen(this.group)));
        })
                .pos(this.getWidth() / 2 + 79, this.getHeight() / 2 - 38)
                .size( 120, 20)
                .tooltip(Tooltip.create(Component.translatable("xray.tooltips.add_block_in_hand")))
                .build());

        this.addRenderableWidget(Button.builder( Component.translatable("xray.single.group.edit"), button -> {
                    this.minecraft.player.clientSideCloseContainer();
                    this.minecraft.setScreen(new GuiCategoryEdit(this.group));
                })
                .pos((this.getWidth() / 2) + 79, this.getHeight() / 2 + 36)
                .size( 120, 20)
                .tooltip(Tooltip.create(Component.translatable("xray.tooltips.add_block")))
                .build());

        this.addRenderableWidget(new Button.Builder( Component.translatable("xray.single.help"), button -> {
            this.minecraft.player.clientSideCloseContainer();
            this.minecraft.setScreen(new GuiHelp());
        }).pos(this.getWidth() / 2 + 79, this.getHeight() / 2 + 58).size(60,20).build());
        this.addRenderableWidget(new Button.Builder(Component.translatable("xray.single.close"), button -> this.onClose()).pos((this.getWidth() / 2 + 79) + 62, this.getHeight() / 2 + 58).size(59,20).build());
    }

    private void updateSearch() {
        if (this.lastSearch.equals(this.search.getValue())) {
            return;
        }

        if (this.search.getValue().equals("")) {
            this.itemList = group.entries();
            this.scrollList.updateEntries(this.itemList);
            this.lastSearch = "";
            return;
        }

        this.itemList = this.group.entries().stream().filter(b -> b.getName().toLowerCase().contains(this.search.getValue().toLowerCase())).collect(Collectors.toCollection(ArrayList::new));

        this.itemList.sort(Comparator.comparingInt(BlockEntry::getOrder));

        this.scrollList.updateEntries(this.itemList);
        this.lastSearch = this.search.getValue();
    }

    @Override
    public void tick() {
        super.tick();
        this.search.tick();

        this.updateSearch();
    }

    @Override
    public boolean mouseClicked(double x, double y, int mouse) {
        if (this.search.mouseClicked(x, y, mouse)) {
            this.setFocused(this.search);
        }

        return super.mouseClicked(x, y, mouse);
    }

    @Override
    public void renderExtra(PoseStack stack, int x, int y, float partialTicks) {
        this.search.render(stack, x, y, partialTicks);
        this.scrollList.render(stack, x, y, partialTicks);

        if (!this.search.isFocused() && this.search.getValue().equals("")) {
            this.minecraft.font.drawShadow(stack, I18n.get("xray.single.search"), (float) this.getWidth() / 2 - 130, (float) this.getHeight() / 2 - 101, Color.GRAY.getRGB());
        }
    }

    @Override
    public void onClose() {
        SettingsStore.getInstance().write();
        BlockStore.getInstance().write();
        BlockStore.getInstance().updateCache();
        ScanController.RebuildCache();
        super.onClose();
    }

    public static class ScrollingBlockList extends ScrollingList<ScrollingBlockList.BlockSlot> {
        static final int SLOT_HEIGHT = 35;
        GuiBlockSelectionScreen parent;

        ScrollingBlockList(int x, int y, int width, int height, List<BlockEntry> blocks, GuiBlockSelectionScreen parent) {
            super(x, y, width, height, SLOT_HEIGHT);
            this.updateEntries(blocks);
            this.parent = parent;
        }

        void setSelected(BlockSlot entry, int mouse) {
            if (entry == null) {
                return;
            }

            if (GuiBlockSelectionScreen.hasShiftDown() || mouse==1) {
                this.minecraft.player.clientSideCloseContainer();
                this.minecraft.setScreen(new GuiEdit(entry.block,this.parent.group));
                return;
            }

            try {
                BlockGroup group = BlockStore.getInstance().getByName(this.parent.group.name());
                int index = group.entries().indexOf(entry.getBlock());
                BlockEntry blockEntry = group.entries().get(index);
                blockEntry.setActive(!blockEntry.isActive());
                group.entries().set(index, blockEntry);
                BlockStore.getInstance().write();
                BlockStore.getInstance().updateCache();
            } catch (Exception ignored) {
            }
        }

        void updateEntries(List<BlockEntry> blocks) {
            this.clearEntries();
            blocks.forEach(block -> this.addEntry(new BlockSlot(block, this))); // @mcp: func_230513_b_ = addEntry
        }

        @Override
        public Optional<GuiEventListener> getChildAt(double mouseX, double mouseY) {
            return Optional.empty();
        }

        public class BlockSlot extends Entry<BlockSlot> {
            BlockEntry block;
            ScrollingBlockList parent;

            BlockSlot(BlockEntry block, ScrollingBlockList parent) {
                this.block = block;
                this.parent = parent;
            }

            public BlockEntry getBlock() {
                return this.block;
            }

            @Override
            public void render(PoseStack stack, int entryIdx, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean p_194999_5_, float partialTicks) {
                BlockEntry blockData = this.block;

                Font font = this.parent.minecraft.font;

                font.drawShadow(stack, blockData.getName(), left + 35, top + 7, 0xFFFFFF);
                font.drawShadow(stack, blockData.isActive() ? "Enabled" : "Disabled", left + 35, top + 17, blockData.isActive() ? Color.GREEN.getRGB() : Color.RED.getRGB());

                Lighting.setupFor3DItems();
                this.parent.minecraft.getItemRenderer().renderAndDecorateItem(blockData.getStack(), left + 10, top + 7);
                Lighting.setupForFlatItems();

                if (mouseX > left && mouseX < (left + entryWidth) && mouseY > top && mouseY < (top + entryHeight) && mouseY < (this.parent.y0 + this.parent.height) && mouseY > this.parent.y0) {
                    this.parent.parent.renderTooltip(stack, Arrays.asList(Component.translatable("xray.tooltips.edit1").getVisualOrderText(), Component.translatable("xray.tooltips.edit2").getVisualOrderText()), left + 15, (entryIdx == this.parent.children().size() - 1 && entryIdx != 0 ? (top - (entryHeight - 20)) : (top + (entryHeight + 15))));
                }

                RenderSystem.enableBlend();
                RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                RenderSystem.setShaderTexture(0, GuiBlockSelectionScreen.CIRCLE);
                RenderSystem.setShaderColor(0, 0, 0, .5f);
                blit(stack, (left + entryWidth) - 32, (int) (top + (entryHeight / 2f) - 9), 0, 0, 14, 14, 14, 14);
                RenderSystem.setShaderColor(blockData.getHex().red() / 255f, blockData.getHex().green() / 255f, blockData.getHex().blue() / 255f, 1);
                blit(stack, (left + entryWidth) - 30, (int) (top + (entryHeight / 2f) - 7), 0, 0, 10, 10, 10, 10);
                RenderSystem.disableBlend();
                RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            }

            @Override
            public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int mouse) {
                this.parent.setSelected(this, mouse);
                return false;
            }
        }
    }
}
