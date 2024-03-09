package pro.mikey.fabric.xray.screens.forge;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
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

public class GuiSelectionScreen extends GuiBase {
    private static final List<Block> ORE_TAGS = List.of(Blocks.GOLD_ORE, Blocks.IRON_ORE, Blocks.DIAMOND_ORE, Blocks.REDSTONE_ORE, Blocks.LAPIS_ORE, Blocks.COAL_ORE, Blocks.EMERALD_ORE, Blocks.COPPER_ORE, Blocks.DEEPSLATE_GOLD_ORE, Blocks.DEEPSLATE_IRON_ORE, Blocks.DEEPSLATE_DIAMOND_ORE, Blocks.DEEPSLATE_REDSTONE_ORE, Blocks.DEEPSLATE_LAPIS_ORE, Blocks.DEEPSLATE_COAL_ORE, Blocks.DEEPSLATE_EMERALD_ORE, Blocks.DEEPSLATE_COPPER_ORE, Blocks.NETHER_GOLD_ORE, Blocks.ANCIENT_DEBRIS);
    private static final List<Block> TAGS_STORAGE = List.of(Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.BARREL);
    private static final List<Block> PLAYER_TAGS_INTEREST = List.of(Blocks.CRAFTING_TABLE, Blocks.ANVIL, Blocks.CHIPPED_ANVIL, Blocks.DAMAGED_ANVIL,Blocks.BREWING_STAND,Blocks.BLAST_FURNACE, Blocks.FURNACE,Blocks.ENCHANTING_TABLE,Blocks.CARTOGRAPHY_TABLE,Blocks.FLETCHING_TABLE,Blocks.COMPOSTER,Blocks.LOOM,Blocks.SMOKER);
    private static final List<Block> LAVA_TAGS = List.of(Blocks.LAVA);

    private static final ResourceLocation CIRCLE = new ResourceLocation(XRay.PREFIX_GUI + "circle.png");
    private final List<BlockGroup> originalList;
    private Button distButtons;
    private EditBox search;
    private String lastSearch = "";
    private List<BlockGroup> itemList;
    private ScrollingCategoryList scrollList;

    public GuiSelectionScreen() {
        super(true);
        this.setSideTitle(I18n.get("xray.single.tools"));

        populateDefault();

        this.itemList = BlockStore.getInstance().get().size() >= 1 ? BlockStore.getInstance().get() : new ArrayList<>();

        this.originalList = this.itemList;
    }

    private void populateDefault() {
        if (BlockStore.getInstance().justCreated && BlockStore.getInstance().get().size() == 0) {
            AtomicInteger order = new AtomicInteger();
            Random random = new Random();


            //Ores
            BlockGroup ores = new BlockGroup("Ores", ORE_TAGS.stream().map(e ->
                    new BlockEntry(e.defaultBlockState(), e.asItem().getDescription().getString(), new BasicColor(random.nextInt(255), random.nextInt(255), random.nextInt(255)), order.getAndIncrement(), true, true)).collect(Collectors.toList()), 0, true);
            BlockStore.getInstance().get().add(ores);

            //Storage
            order.set(0);
            BlockGroup storage = new BlockGroup("Storage", TAGS_STORAGE.stream().map(e ->
                    new BlockEntry(e.defaultBlockState(), e.asItem().getDescription().getString(), new BasicColor(20,30,200), order.getAndIncrement(), true, true)).collect(Collectors.toList()), 0, true);
            Block echest = Blocks.ENDER_CHEST;
            storage.entries().add(new BlockEntry(echest.defaultBlockState(), echest.asItem().getDescription().getString(), new BasicColor(200,30,200), order.getAndIncrement(), true, true));
            BlockStore.getInstance().get().add(storage);

            //Lava
            BlockGroup lava = new BlockGroup("Lava", LAVA_TAGS.stream().map(e ->
                    new BlockEntry(e.defaultBlockState(), e.asItem().getDescription().getString(), new BasicColor(210,30,30), order.getAndIncrement(), true, true)).collect(Collectors.toList()), 0, false,new BasicColor(210,20,20));
            BlockStore.getInstance().get().add(lava);
            //Player / Interest
            order.set(0);
            BlockStore.getInstance().get().add(new BlockGroup("Player / Interest", PLAYER_TAGS_INTEREST.stream().map(e ->
                    new BlockEntry(e.defaultBlockState(), e.asItem().getDescription().getString(), new BasicColor(30,210,30), order.getAndIncrement(), true, true)).collect(Collectors.toList()), 0, false)
            );

            BlockStore.getInstance().updateCache();
        }
    }

    @Override
    public void init() {
        if (this.minecraft.player == null) {
            return;
        }

        this.scrollList = new ScrollingCategoryList((this.getWidth() / 2) - 37, this.getHeight() / 2 + 10, 203, 185, this.itemList, this);
        this.addRenderableWidget(this.scrollList);

        this.search = new EditBox(this.getFontRender(), this.getWidth() / 2 - 137, this.getHeight() / 2 - 105, 202, 18, Component.empty());
        this.search.setCanLoseFocus(true);

        this.addRenderableWidget(Button.builder( Component.translatable("xray.single.group.add"), button -> {
                    this.minecraft.player.clientSideCloseContainer();
                    BlockGroup group = new BlockGroup("New Group",new ArrayList<>(),10,true);
                    this.minecraft.setScreen(new GuiCategoryEdit(group));
                })
                .pos((this.getWidth() / 2) + 79, this.getHeight() / 2 - 60)
                .size( 120, 20)
                .tooltip(Tooltip.create(Component.translatable("xray.tooltips.add_block")))
                .build());

        this.addRenderableWidget(this.distButtons = Button.builder(Component.translatable("xray.input.distance", StateSettings.getVisualRadius()), button -> {
            SettingsStore.getInstance().get().increaseRange();
            button.setMessage(Component.translatable("xray.input.distance", StateSettings.getVisualRadius()));
        })
                .pos((this.getWidth() / 2) + 79, this.getHeight() / 2 + 36)
                .size(120, 20)
                .tooltip(Tooltip.create(Component.translatable("xray.tooltips.distance")))
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
            this.itemList = this.originalList;
            this.scrollList.updateEntries(this.itemList);
            this.lastSearch = "";
            return;
        }

        this.itemList = this.originalList.stream().filter(b -> b.getName().toLowerCase().contains(this.search.getValue().toLowerCase())).collect(Collectors.toCollection(ArrayList::new));

        this.itemList.sort(Comparator.comparingInt(BlockGroup::getOrder));

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

        if (mouse == 1 && this.distButtons.isMouseOver(x, y)) {
            SettingsStore.getInstance().get().decreaseRange();

            this.distButtons.setMessage(Component.translatable("xray.input.distance", StateSettings.getVisualRadius()));
            this.distButtons.playDownSound(this.minecraft.getSoundManager());
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
        ScanController.reBuildCache(true);
        super.onClose();
    }

    public static class ScrollingCategoryList extends ScrollingList<ScrollingCategoryList.CategoryEntry> {
        static final int SLOT_HEIGHT = 35;
        GuiSelectionScreen parent;

        ScrollingCategoryList(int x, int y, int width, int height, List<BlockGroup> blocks, GuiSelectionScreen parent) {
            super(x, y, width, height, SLOT_HEIGHT);
            this.updateEntries(blocks);
            this.parent = parent;
        }

        void setSelected(CategoryEntry entry, int mouse) {
            if (entry == null) {
                return;
            }

            if (GuiSelectionScreen.hasShiftDown() || mouse == 1) {
                this.minecraft.player.clientSideCloseContainer();
                this.minecraft.setScreen(new GuiBlockSelectionScreen(entry.getGroup()));
                return;
            }

            BlockStore.getInstance().get().stream().filter(group -> (group.getName().equals(entry.block.name()))).findFirst().ifPresent(group -> {
                group.setActive(!group.active());
            });
            BlockStore.getInstance().write();
            BlockStore.getInstance().updateCache();
        }

        void updateEntries(List<BlockGroup> blocks) {
            this.clearEntries();
            blocks.forEach(group -> this.addEntry(new CategoryEntry(group, this))); // @mcp: func_230513_b_ = addEntry
        }

        @Override
        public Optional<GuiEventListener> getChildAt(double mouseX, double mouseY) {
            return Optional.empty();
        }

        public static class CategoryEntry extends AbstractSelectionList.Entry<CategoryEntry> {
            BlockGroup block;
            ScrollingCategoryList parent;

            CategoryEntry(BlockGroup block, ScrollingCategoryList parent) {
                this.block = block;
                this.parent = parent;
            }

            public BlockGroup getGroup() {
                return this.block;
            }

            @Override
            public void render(PoseStack stack, int entryIdx, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean unknown, float partialTicks) {
                BlockGroup group = this.block;
                BlockEntry display = group.entries().stream().findFirst().orElse(BlockEntry.getAir());

                Font font = this.parent.minecraft.font;

                font.drawShadow(stack, group.getName(), left + 35, top + 7, 0xFFFFFF);
                font.drawShadow(stack, group.isActive() ? "Enabled" : "Disabled", left + 35, top + 17, group.isActive() ? Color.GREEN.getRGB() : Color.RED.getRGB());

                Lighting.setupFor3DItems();
                this.parent.minecraft.getItemRenderer().renderAndDecorateItem(stack, display.getStack(), left + 10, top + 7);
                Lighting.setupForFlatItems();

                if (mouseX > left && mouseX < (left + entryWidth) && mouseY > top && mouseY < (top + entryHeight) && mouseY < (this.parent.y0 + this.parent.height) && mouseY > this.parent.y0) {
                    this.parent.parent.renderTooltip(stack, Arrays.asList(Component.translatable("xray.tooltips.edit1").getVisualOrderText(), Component.translatable("xray.tooltips.edit2").getVisualOrderText()), left + 15, (entryIdx == this.parent.children().size() - 1 && entryIdx != 0 ? (top - (entryHeight - 20)) : (top + (entryHeight + 15))));
                }

                RenderSystem.enableBlend();
                RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                RenderSystem.setShaderTexture(0, GuiSelectionScreen.CIRCLE);
                RenderSystem.setShaderColor(0, 0, 0, .5f);
                blit(stack, (left + entryWidth) - 32, (int) (top + (entryHeight / 2f) - 9), 0, 0, 14, 14, 14, 14);
                RenderSystem.setShaderColor(group.getColor().red() / 255f, group.getColor().green() / 255f, group.getColor().blue() / 255f, 1);
                blit(stack, (left + entryWidth) - 30, (int) (top + (entryHeight / 2f) - 7), 0, 0, 10, 10, 10, 10);
                RenderSystem.disableBlend();
                RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            }

            @Override
            public boolean mouseClicked(double pMouseClicked1, double pMouseClicked3, int mouse) {
                this.parent.setSelected(this, mouse);
                return false;
            }
        }
    }
}
