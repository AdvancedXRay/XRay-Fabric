package pro.mikey.fabric.xray.screens.forge;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import pro.mikey.fabric.xray.ScanController;
import pro.mikey.fabric.xray.StateSettings;
import pro.mikey.fabric.xray.XRay;
import pro.mikey.fabric.xray.records.BasicColor;
import pro.mikey.fabric.xray.records.BlockEntry;
import pro.mikey.fabric.xray.records.BlockGroup;
import pro.mikey.fabric.xray.storage.Stores;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class GuiSelectionScreen extends GuiBase {
    private static final List<Block> ORE_TAGS = List.of(Blocks.GOLD_ORE, Blocks.IRON_ORE, Blocks.DIAMOND_ORE, Blocks.REDSTONE_ORE, Blocks.LAPIS_ORE, Blocks.COAL_ORE, Blocks.EMERALD_ORE, Blocks.COPPER_ORE, Blocks.DEEPSLATE_GOLD_ORE, Blocks.DEEPSLATE_IRON_ORE, Blocks.DEEPSLATE_DIAMOND_ORE, Blocks.DEEPSLATE_REDSTONE_ORE, Blocks.DEEPSLATE_LAPIS_ORE, Blocks.DEEPSLATE_COAL_ORE, Blocks.DEEPSLATE_EMERALD_ORE, Blocks.DEEPSLATE_COPPER_ORE, Blocks.NETHER_GOLD_ORE, Blocks.ANCIENT_DEBRIS);

    private static final Identifier CIRCLE = new Identifier(XRay.PREFIX_GUI + "circle.png");
    private final List<BlockEntry> originalList;
    public ItemRenderer render;
    private ButtonWidget distButtons;
    private TextFieldWidget search;
    private String lastSearch = "";
    private List<BlockEntry> itemList;
    private ScrollingBlockList scrollList;

    public GuiSelectionScreen() {
        super(true);
        this.setSideTitle(I18n.translate("xray.single.tools"));

        populateDefault();

        this.itemList = Stores.BLOCKS.get().size() >= 1 ? Stores.BLOCKS.get().get(0).entries() : new ArrayList<>();
        this.itemList.sort(Comparator.comparingInt(BlockEntry::getOrder));

        this.originalList = this.itemList;
    }

    private void populateDefault() {
        if (Stores.BLOCKS.justCreated && Stores.BLOCKS.get().size() == 0) {
            AtomicInteger order = new AtomicInteger();
            Random random = new Random();

            Stores.BLOCKS.get().add(new BlockGroup("default", ORE_TAGS.stream().map(e ->
                    new BlockEntry(e.getDefaultState(), e.asItem().getName().getString(), new BasicColor(random.nextInt(255), random.nextInt(255), random.nextInt(255)), order.getAndIncrement(), true, true)).collect(Collectors.toList()), 0, true)
            );

            Stores.BLOCKS.updateCache();
        }
    }

    @Override
    public void init() {
        if (this.client.player == null) {
            return;
        }

        this.render = this.itemRenderer;
//        this.buttons.clear();

        this.scrollList = new ScrollingBlockList((this.getWidth() / 2) - 37, this.getHeight() / 2 + 10, 203, 185, this.itemList, this);
        this.addDrawableChild(this.scrollList);

        this.search = new TextFieldWidget(this.getFontRender(), this.getWidth() / 2 - 137, this.getHeight() / 2 - 105, 202, 18, LiteralText.EMPTY);
        this.search.setFocusUnlocked(true);

        // side bar buttons
        this.addDrawableChild(new SupportButtonInner((this.getWidth() / 2) + 79, this.getHeight() / 2 - 60, 120, 20, I18n.translate("xray.input.add"), "xray.tooltips.add_block", button -> {
            this.client.player.closeScreen();
            this.client.setScreen(new GuiBlockList());
        }));
        this.addDrawableChild(new SupportButtonInner(this.getWidth() / 2 + 79, this.getHeight() / 2 - 38, 120, 20, I18n.translate("xray.input.add_hand"), "xray.tooltips.add_block_in_hand", button -> {
            this.client.player.closeScreen();
            ItemStack handItem = this.client.player.getStackInHand(Hand.MAIN_HAND);

            // Check if the hand item is a block or not
            if (!(handItem.getItem() instanceof BlockItem)) {
                this.client.player.sendMessage(new LiteralText("[XRay] " + I18n.translate("xray.message.invalid_hand", handItem.getName().getString())), false);
                return;
            }

            this.client.setScreen(new GuiAddBlock(((BlockItem) handItem.getItem()).getBlock().getDefaultState(), GuiSelectionScreen::new));
        }));
        this.addDrawableChild(new SupportButtonInner(this.getWidth() / 2 + 79, this.getHeight() / 2 - 16, 120, 20, I18n.translate("xray.input.add_look"), "xray.tooltips.add_block_looking_at", button -> {
            ClientPlayerEntity player = this.client.player;
            if (this.client.world == null || player == null) {
                return;
            }

            this.close();
            try {
                HitResult look = player.raycast(100, 1f, false);

                if (look.getType() == BlockHitResult.Type.BLOCK) {
                    BlockState state = this.client.world.getBlockState(((BlockHitResult) look).getBlockPos());

                    player.closeScreen();
                    this.client.setScreen(new GuiAddBlock(state, GuiSelectionScreen::new));
                } else {
                    player.sendMessage(new LiteralText("[XRay] " + I18n.translate("xray.message.nothing_infront")), false);
                }
            } catch (NullPointerException ex) {
                player.sendMessage(new LiteralText("[XRay] " + I18n.translate("xray.message.thats_odd")), false);
            }
        }));

        this.addDrawableChild(this.distButtons = new SupportButtonInner((this.getWidth() / 2) + 79, this.getHeight() / 2 + 6, 120, 20, I18n.translate("xray.input.show-lava", Stores.SETTINGS.get().isShowLava()), "xray.tooltips.show_lava", button -> {
            Stores.SETTINGS.get().setShowLava(!Stores.SETTINGS.get().isShowLava());
            ScanController.runTask(true);
            button.setMessage(new TranslatableText("xray.input.show-lava", Stores.SETTINGS.get().isShowLava()));
        }));

        this.addDrawableChild(this.distButtons = new SupportButtonInner((this.getWidth() / 2) + 79, this.getHeight() / 2 + 36, 120, 20, I18n.translate("xray.input.distance", StateSettings.getVisualRadius()), "xray.tooltips.distance", button -> {
            Stores.SETTINGS.get().increaseRange();
            button.setMessage(new TranslatableText("xray.input.distance", StateSettings.getVisualRadius()));
        }));
        this.addDrawableChild(new ButtonWidget(this.getWidth() / 2 + 79, this.getHeight() / 2 + 58, 60, 20, new TranslatableText("xray.single.help"), button -> {
            this.client.player.closeScreen();
            this.client.setScreen(new GuiHelp());
        }));
        this.addDrawableChild(new ButtonWidget((this.getWidth() / 2 + 79) + 62, this.getHeight() / 2 + 58, 59, 20, new TranslatableText("xray.single.close"), button -> this.close()));
    }

    private void updateSearch() {
        if (this.lastSearch.equals(this.search.getText())) {
            return;
        }

        if (this.search.getText().equals("")) {
            this.itemList = this.originalList;
            this.scrollList.updateEntries(this.itemList);
            this.lastSearch = "";
            return;
        }

        this.itemList = this.originalList.stream().filter(b -> b.getName().toLowerCase().contains(this.search.getText().toLowerCase())).collect(Collectors.toCollection(ArrayList::new));

        this.itemList.sort(Comparator.comparingInt(BlockEntry::getOrder));

        this.scrollList.updateEntries(this.itemList);
        this.lastSearch = this.search.getText();
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

        if (mouse == 1 && this.distButtons.isMouseOver(x, y)) {
            Stores.SETTINGS.get().decreaseRange();

            this.distButtons.setMessage(new TranslatableText("xray.input.distance", StateSettings.getVisualRadius()));
            this.distButtons.playDownSound(this.client.getSoundManager());
        }

        return super.mouseClicked(x, y, mouse);
    }

    @Override
    public void renderExtra(MatrixStack stack, int x, int y, float partialTicks) {
        this.search.render(stack, x, y, partialTicks);
        this.scrollList.render(stack, x, y, partialTicks);

        if (!this.search.isFocused() && this.search.getText().equals("")) {
            this.client.textRenderer.drawWithShadow(stack, I18n.translate("xray.single.search"), (float) this.getWidth() / 2 - 130, (float) this.getHeight() / 2 - 101, Color.GRAY.getRGB());
        }
    }

    @Override
    public void close() {
        Stores.SETTINGS.write();
        Stores.BLOCKS.write();
        Stores.BLOCKS.updateCache();

        ScanController.runTask(true);

        super.close();
    }

    static final class SupportButtonInner extends SupportButton {
        SupportButtonInner(int widthIn, int heightIn, int width, int height, String text, String i18nKey, PressAction onPress) {
            super(widthIn, heightIn, width, height, new LiteralText(text), new TranslatableText(i18nKey), onPress);
        }
    }

    public static class ScrollingBlockList extends ScrollingList<ScrollingBlockList.BlockSlot> {
        static final int SLOT_HEIGHT = 35;
        GuiSelectionScreen parent;

        ScrollingBlockList(int x, int y, int width, int height, List<BlockEntry> blocks, GuiSelectionScreen parent) {
            super(x, y, width, height, SLOT_HEIGHT);
            this.updateEntries(blocks);
            this.parent = parent;
        }

        void setSelected(BlockSlot entry, int mouse) {
            if (entry == null) {
                return;
            }

            if (GuiSelectionScreen.hasShiftDown()) {
                this.client.player.closeScreen();
                this.client.setScreen(new GuiEdit(entry.block));
                return;
            }

            try {
                int index = Stores.BLOCKS.get().get(0).entries().indexOf(entry.getBlock());
                BlockEntry blockEntry = Stores.BLOCKS.get().get(0).entries().get(index);
                blockEntry.setActive(!blockEntry.isActive());
                Stores.BLOCKS.get().get(0).entries().set(index, blockEntry);
                Stores.BLOCKS.write();
                Stores.BLOCKS.updateCache();
            } catch (Exception ignored) {
            }
        }

        void updateEntries(List<BlockEntry> blocks) {
            this.clearEntries();
            blocks.forEach(block -> this.addEntry(new BlockSlot(block, this))); // @mcp: func_230513_b_ = addEntry
        }

        @Override
        public Optional<Element> hoveredElement(double mouseX, double mouseY) {
            return Optional.empty();
        }

        public class BlockSlot extends EntryListWidget.Entry<BlockSlot> {
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
            public void render(MatrixStack stack, int entryIdx, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean p_194999_5_, float partialTicks) {
                BlockEntry blockData = this.block;

                TextRenderer font = this.parent.client.textRenderer;

                font.drawWithShadow(stack, blockData.getName(), left + 35, top + 7, 0xFFFFFF);
                font.drawWithShadow(stack, blockData.isActive() ? "Enabled" : "Disabled", left + 35, top + 17, blockData.isActive() ? Color.GREEN.getRGB() : Color.RED.getRGB());

                DiffuseLighting.enableGuiDepthLighting();
                this.parent.client.getItemRenderer().renderInGuiWithOverrides(blockData.getStack(), left + 10, top + 7);
                DiffuseLighting.disableGuiDepthLighting();

                if (mouseX > left && mouseX < (left + entryWidth) && mouseY > top && mouseY < (top + entryHeight) && mouseY < (this.parent.top + this.parent.height) && mouseY > this.parent.top) {
                    this.parent.parent.renderTooltip(stack, Arrays.asList(new TranslatableText("xray.tooltips.edit1"), new TranslatableText("xray.tooltips.edit2")), left + 15, (entryIdx == this.parent.children().size() - 1 && entryIdx != 0 ? (top - (entryHeight - 20)) : (top + (entryHeight + 15))));
                }

                RenderSystem.enableBlend();
                RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
                RenderSystem.setShaderTexture(0, GuiSelectionScreen.CIRCLE);
                RenderSystem.setShaderColor(0, 0, 0, .5f);
                drawTexture(stack, (left + entryWidth) - 32, (int) (top + (entryHeight / 2f) - 9), 0, 0, 14, 14, 14, 14);
                RenderSystem.setShaderColor(blockData.getHex().red() / 255f, blockData.getHex().green() / 255f, blockData.getHex().blue() / 255f, 1);
                drawTexture(stack, (left + entryWidth) - 30, (int) (top + (entryHeight / 2f) - 7), 0, 0, 10, 10, 10, 10);
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
