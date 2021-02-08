package pro.mikey.fabric.xray.screens.forge;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.BlockState;
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
import pro.mikey.fabric.xray.records.BlockEntry;
import pro.mikey.fabric.xray.storage.Stores;

import java.awt.*;
import java.util.List;
import java.util.*;

public class GuiSelectionScreen extends GuiBase {
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

    //    // Inject this hear as everything is loaded
    //    if (ClientController.blockStore.created) {
    //      List<BlockData.SerializableBlockData> blocks =
    // ClientController.blockStore.populateDefault();
    //      Controller.getBlockStore().setStore(BlockStore.getFromSimpleBlockList(blocks));
    //
    //      ClientController.blockStore.created = false;
    //    }

    this.itemList =
        Stores.BLOCKS.get().size() >= 1
            ? Stores.BLOCKS.get().get(0).getEntries()
            : new ArrayList<>();
    this.itemList.sort(Comparator.comparingInt(BlockEntry::getOrder));

    this.originalList = this.itemList;
  }

  @Override
  public void init() {
    if (this.client.player == null) {
      return;
    }

    this.render = this.itemRenderer;
    this.buttons.clear();

    this.scrollList =
        new ScrollingBlockList(
            (this.getWidth() / 2) - 37, this.getHeight() / 2 + 10, 203, 185, this.itemList, this);
    this.children.add(this.scrollList);

    this.search =
        new TextFieldWidget(
            this.getFontRender(),
            this.getWidth() / 2 - 137,
            this.getHeight() / 2 - 105,
            202,
            18,
            LiteralText.EMPTY);
    this.search.setFocusUnlocked(true);

    // side bar buttons
    this.addButton(
        new SupportButtonInner(
            (this.getWidth() / 2) + 79,
            this.getHeight() / 2 - 60,
            120,
            20,
            I18n.translate("xray.input.add"),
            "xray.tooltips.add_block",
            button -> {
              this.client.player.closeScreen();
              this.client.openScreen(new GuiBlockList());
            }));
    this.addButton(
        new SupportButtonInner(
            this.getWidth() / 2 + 79,
            this.getHeight() / 2 - 38,
            120,
            20,
            I18n.translate("xray.input.add_hand"),
            "xray.tooltips.add_block_in_hand",
            button -> {
              this.client.player.closeScreen();
              ItemStack handItem = this.client.player.getStackInHand(Hand.MAIN_HAND);

              // Check if the hand item is a block or not
              if (!(handItem.getItem() instanceof BlockItem)) {
                this.client.player.sendMessage(
                    new LiteralText(
                        "[XRay] "
                            + I18n.translate("xray.message.invalid_hand", handItem.getName())),
                    false);
                return;
              }

              this.client.openScreen(
                  new GuiAddBlock(
                      ((BlockItem) handItem.getItem()).getBlock().getDefaultState(),
                      GuiSelectionScreen::new));
            }));
    this.addButton(
        new SupportButtonInner(
            this.getWidth() / 2 + 79,
            this.getHeight() / 2 - 16,
            120,
            20,
            I18n.translate("xray.input.add_look"),
            "xray.tooltips.add_block_looking_at",
            button -> {
              ClientPlayerEntity player = this.client.player;
              if (this.client.world == null || player == null) {
                return;
              }

              this.onClose();
              try {
                HitResult look = player.raycast(100, 1f, false);

                if (look.getType() == BlockHitResult.Type.BLOCK) {
                  BlockState state =
                      this.client.world.getBlockState(((BlockHitResult) look).getBlockPos());

                  player.closeScreen();
                  this.client.openScreen(new GuiAddBlock(state, GuiSelectionScreen::new));
                } else {
                  player.sendMessage(
                      new LiteralText("[XRay] " + I18n.translate("xray.message.nothing_infront")),
                      false);
                }
              } catch (NullPointerException ex) {
                player.sendMessage(
                    new LiteralText("[XRay] " + I18n.translate("xray.message.thats_odd")), false);
              }
            }));

    this.addButton(
        this.distButtons =
            new SupportButtonInner(
                (this.getWidth() / 2) + 79,
                this.getHeight() / 2 + 6,
                120,
                20,
                I18n.translate("xray.input.show-lava", Stores.SETTINGS.get().isShowLava()),
                "xray.tooltips.show_lava",
                button -> {
                  Stores.SETTINGS.get().setShowLava(!Stores.SETTINGS.get().isShowLava());
                  ScanController.runTask(true);
                  button.setMessage(
                      new TranslatableText(
                          "xray.input.show-lava", Stores.SETTINGS.get().isShowLava()));
                }));

    this.addButton(
        this.distButtons =
            new SupportButtonInner(
                (this.getWidth() / 2) + 79,
                this.getHeight() / 2 + 36,
                120,
                20,
                I18n.translate(
                    "xray.input.distance",
                    StateSettings.DISTANCE_STEPS[Stores.SETTINGS.get().getRange()]),
                "xray.tooltips.distance",
                button -> {
                  Stores.SETTINGS.get().increaseRange();
                  button.setMessage(
                      new TranslatableText(
                          "xray.input.distance",
                          StateSettings.DISTANCE_STEPS[Stores.SETTINGS.get().getRange()]));
                }));
    this.addButton(
        new ButtonWidget(
            this.getWidth() / 2 + 79,
            this.getHeight() / 2 + 58,
            60,
            20,
            new TranslatableText("xray.single.help"),
            button -> {
              this.client.player.closeScreen();
              this.client.openScreen(new GuiHelp());
            }));
    this.addButton(
        new ButtonWidget(
            (this.getWidth() / 2 + 79) + 62,
            this.getHeight() / 2 + 58,
            59,
            20,
            new TranslatableText("xray.single.close"),
            button -> this.onClose()));
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

    //    this.itemList =
    //        this.originalList.stream()
    //            .filter(
    //                b ->
    // b.getEntryName().toLowerCase().contains(this.search.getText().toLowerCase()))
    //            .collect(Collectors.toCollection(ArrayList::new));
    //
    //    this.itemList.sort(Comparator.comparingInt(BlockData::getOrder));

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

      this.distButtons.setMessage(
          new TranslatableText(
              "xray.input.distance",
              StateSettings.DISTANCE_STEPS[Stores.SETTINGS.get().getRange()]));
      this.distButtons.playDownSound(this.client.getSoundManager());
    }

    return super.mouseClicked(x, y, mouse);
  }

  @Override
  public void renderExtra(MatrixStack stack, int x, int y, float partialTicks) {
    this.search.render(stack, x, y, partialTicks);
    this.scrollList.render(stack, x, y, partialTicks);

    if (!this.search.isFocused() && this.search.getText().equals("")) {
      this.client.textRenderer.drawWithShadow(
          stack,
          I18n.translate("xray.single.search"),
          (float) this.getWidth() / 2 - 130,
          (float) this.getHeight() / 2 - 101,
          Color.GRAY.getRGB());
    }
  }

  @Override
  public void onClose() {
    Stores.SETTINGS.write();
    Stores.BLOCKS.write();

    ScanController.runTask(true);

    super.onClose();
  }

  static final class SupportButtonInner extends SupportButton {
    SupportButtonInner(
        int widthIn,
        int heightIn,
        int width,
        int height,
        String text,
        String i18nKey,
        PressAction onPress) {
      super(
          widthIn,
          heightIn,
          width,
          height,
          new LiteralText(text),
          new TranslatableText(i18nKey),
          onPress);
    }
  }

  public class ScrollingBlockList extends ScrollingList<ScrollingBlockList.BlockSlot> {
    static final int SLOT_HEIGHT = 35;
    GuiSelectionScreen parent;

    ScrollingBlockList(
        int x, int y, int width, int height, List<BlockEntry> blocks, GuiSelectionScreen parent) {
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
        //        this.client.openScreen(new GuiEdit(entry.block));
        return;
      }

      //      entry.getBlock()
      //      Controller.getBlockStore().toggleDrawing(entry.block);
      //      ClientController.blockStore.write(
      //          new ArrayList<>(Controller.getBlockStore().getStore().values()));
    }

    void updateEntries(List<BlockEntry> blocks) {
      this.clearEntries();
      blocks.forEach(
          block -> this.addEntry(new BlockSlot(block, this))); // @mcp: func_230513_b_ = addEntry
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
      public void render(
          MatrixStack stack,
          int entryIdx,
          int top,
          int left,
          int entryWidth,
          int entryHeight,
          int mouseX,
          int mouseY,
          boolean p_194999_5_,
          float partialTicks) {
        BlockEntry blockData = this.block;

        TextRenderer font = this.parent.client.textRenderer;

        font.draw(stack, blockData.getName(), left + 35, top + 7, 0xFFFFFF);
        font.draw(
            stack,
            blockData.isActive() ? "Enabled" : "Disabled",
            left + 35,
            top + 17,
            blockData.isActive() ? Color.GREEN.getRGB() : Color.RED.getRGB());

        DiffuseLighting.enable();
        this.parent
            .client
            .getItemRenderer()
            .renderInGuiWithOverrides(blockData.getStack(), left + 10, top + 7);
        DiffuseLighting.disable();

        if (mouseX > left
            && mouseX < (left + entryWidth)
            && mouseY > top
            && mouseY < (top + entryHeight)
            && mouseY < (this.parent.top + this.parent.height)
            && mouseY > this.parent.top) {
          this.parent.parent.renderTooltip(
              stack,
              Arrays.asList(
                  new TranslatableText("xray.tooltips.edit1"),
                  new TranslatableText("xray.tooltips.edit2")),
              left + 15,
              (entryIdx == this.parent.children().size() - 1 && entryIdx != 0
                  ? (top - (entryHeight - 20))
                  : (top + (entryHeight + 15))));
        }

        RenderSystem.enableAlphaTest();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(
            GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        this.parent.client.getTextureManager().bindTexture(GuiSelectionScreen.CIRCLE);
        RenderSystem.color4f(
            blockData.getHex().getRed() / 255f,
            blockData.getHex().getGreen() / 255f,
            blockData.getHex().getBlue() / 255f,
            .3f);
        ScrollingBlockList.this.drawTexture(
            stack,
            (left + entryWidth) - 27,
            (int) (top + (entryHeight / 2f) - 9),
            0,
            0,
            14,
            14,
            14,
            14);
        RenderSystem.color4f(
            blockData.getHex().getRed() / 255f,
            blockData.getHex().getGreen() / 255f,
            blockData.getHex().getBlue() / 255f,
            1);
        ScrollingBlockList.this.drawTexture(
            stack,
            (left + entryWidth) - 25,
            (int) (top + (entryHeight / 2f) - 7),
            0,
            0,
            10,
            10,
            10,
            10);
        RenderSystem.disableAlphaTest();
        RenderSystem.disableBlend();
      }

      @Override
      public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int mouse) {
        this.parent.setSelected(this, mouse);
        return false;
      }
    }
  }
}
