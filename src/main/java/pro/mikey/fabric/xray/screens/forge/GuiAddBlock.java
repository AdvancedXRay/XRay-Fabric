package pro.mikey.fabric.xray.screens.forge;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.*;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import pro.mikey.fabric.xray.records.BasicColor;
import pro.mikey.fabric.xray.records.BlockEntry;
import pro.mikey.fabric.xray.records.BlockGroup;
import pro.mikey.fabric.xray.storage.Stores;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Supplier;

public class GuiAddBlock extends GuiBase {
  private final ItemStack itemStack;
  private final Supplier<GuiBase> previousScreenCallback;
  private BlockState selectBlock;
  private TextFieldWidget oreName;
  private ButtonWidget addBtn;
  private RatioSliderWidget redSlider;
  private RatioSliderWidget greenSlider;
  private RatioSliderWidget blueSlider;
  private ButtonWidget changeDefaultState;
  private BlockState lastState;
  private boolean oreNameCleared = false;

  GuiAddBlock(BlockState selectedBlock, Supplier<GuiBase> previousScreenCallback) {
    super(false);
    this.selectBlock = selectedBlock;
    this.lastState = null;
    this.previousScreenCallback = previousScreenCallback;
    this.itemStack = new ItemStack(this.selectBlock.getBlock(), 1);
  }

  // FIXME: 28/06/2020 replace with matrix system instead of the tess
  static void renderPreview(int x, int y, float r, float g, float b) {
    Tessellator tessellator = Tessellator.getInstance();
    BufferBuilder tessellate = tessellator.getBuffer();
    RenderSystem.enableBlend();
    RenderSystem.disableTexture();
    RenderSystem.blendFuncSeparate(
        GlStateManager.SrcFactor.SRC_ALPHA,
        GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA,
        GlStateManager.SrcFactor.ONE,
        GlStateManager.DstFactor.ZERO);
    RenderSystem.color4f(r / 255, g / 255, b / 255, 1);
    tessellate.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
    tessellate.vertex(x, y, 0.0D).next();
    tessellate.vertex(x, y + 64, 0.0D).next();
    tessellate.vertex(x + 100, y + 64, 0.0D).next();
    tessellate.vertex(x + 100, y, 0.0D).next();
    tessellator.draw();
    RenderSystem.enableTexture();
    RenderSystem.disableBlend();
  }

  @Override
  public void init() {
    // Called when the gui should be (re)created
    boolean isDefaultState = this.selectBlock == this.selectBlock.getBlock().getDefaultState();
    this.addButton(
        this.changeDefaultState =
            new ButtonWidget(
                this.getWidth() / 2 - 100,
                this.getHeight() / 2 + 60,
                202,
                20,
                new LiteralText(
                    isDefaultState
                        ? "Already scanning for all states"
                        : "Scan for all block states"),
                button -> {
                  this.lastState = this.selectBlock;
                  this.selectBlock = this.selectBlock.getBlock().getDefaultState();
                  button.active = false;
                }));

    if (isDefaultState) {
      this.changeDefaultState.active = false;
    }

    this.addButton(
        this.addBtn =
            new ButtonWidget(
                this.getWidth() / 2 - 100,
                this.getHeight() / 2 + 85,
                128,
                20,
                new TranslatableText("xray.single.add"),
                b -> {
                  this.onClose();

                  BlockGroup group =
                      Stores.BLOCKS.get().size() >= 1
                          ? Stores.BLOCKS.get().get(0)
                          : new BlockGroup("default", new ArrayList<>(), 1, true);
                  group
                      .getEntries()
                      .add(
                          new BlockEntry(
                              this.selectBlock,
                              this.oreName.getText(),
                              new BasicColor(
                                  (int) (this.redSlider.getValue() * 255),
                                  (int) (this.greenSlider.getValue() * 255),
                                  (int) (this.blueSlider.getValue() * 255)),
                              group.getEntries().size() + 1,
                              this.selectBlock == this.selectBlock.getBlock().getDefaultState(),
                              true));

                  if (Stores.BLOCKS.get().size() > 0) {
                    Stores.BLOCKS.get().set(0, group);
                  } else {
                    Stores.BLOCKS.get().add(group);
                  }
                  Stores.BLOCKS.write();
                  Stores.BLOCKS.updateCache();

                  this.getMinecraft().openScreen(new GuiSelectionScreen());
                }));
    this.addButton(
        new ButtonWidget(
            this.getWidth() / 2 + 30,
            this.getHeight() / 2 + 85,
            72,
            20,
            new TranslatableText("xray.single.cancel"),
            b -> {
              this.onClose();
              this.getMinecraft().openScreen(this.previousScreenCallback.get());
            }));

    this.addButton(
        this.redSlider =
            new RatioSliderWidget(
                this.getWidth() / 2 - 100,
                this.getHeight() / 2 - 40,
                100,
                20,
                new TranslatableText("xray.color.red"),
                0));
    this.addButton(
        this.greenSlider =
            new RatioSliderWidget(
                this.getWidth() / 2 - 100,
                this.getHeight() / 2 - 18,
                100,
                20,
                new TranslatableText("xray.color.green"),
                0));
    this.addButton(
        this.blueSlider =
            new RatioSliderWidget(
                this.getWidth() / 2 - 100,
                this.getHeight() / 2 + 4,
                100,
                20,
                new TranslatableText("xray.color.blue"),
                0));

    this.oreName =
        new TextFieldWidget(
            this.getMinecraft().textRenderer,
            this.getWidth() / 2 - 100,
            this.getHeight() / 2 - 70,
            202,
            20,
            LiteralText.EMPTY);

    this.oreName.setText(this.selectBlock.getBlock().getName().getString());
    this.children.add(this.oreName);
    this.children.add(this.redSlider);
    this.children.add(this.greenSlider);
    this.children.add(this.blueSlider);
    this.children.add(this.changeDefaultState);
  }

  @Override
  public void tick() {
    super.tick();
    this.oreName.tick();
  }

  @Override
  public void renderExtra(MatrixStack stack, int x, int y, float partialTicks) {
    this.getFontRender()
        .drawWithShadow(
            stack,
            this.selectBlock.getBlock().getName().getString(),
            this.getWidth() / 2f - 100,
            this.getHeight() / 2f - 90,
            0xffffff);

    this.oreName.render(stack, x, y, partialTicks);

    renderPreview(
        this.getWidth() / 2 + 2,
        this.getHeight() / 2 - 40,
        (float) this.redSlider.getValue() * 255,
        (float) this.greenSlider.getValue() * 255,
        (float) this.blueSlider.getValue() * 255);

    this.getFontRender()
        .drawWithShadow(
            stack, "Color", this.getWidth() / 2f + 10, this.getHeight() / 2f - 35, 0xffffff);

    DiffuseLighting.enable();
    this.itemRenderer.renderInGuiWithOverrides(
        this.itemStack, this.getWidth() / 2 + 85, this.getHeight() / 2 - 105);
    DiffuseLighting.disable();
  }

  @Override
  public boolean mouseClicked(double x, double y, int mouse) {
    if (this.oreName.mouseClicked(x, y, mouse)) {
      this.setFocused(this.oreName);
    }

    if (this.oreName.isFocused() && !this.oreNameCleared) {
      this.oreName.setText("");
      this.oreNameCleared = true;
    }

    if (!this.oreName.isFocused()
        && this.oreNameCleared
        && Objects.equals(this.oreName.getText(), "")) {
      this.oreNameCleared = false;
      this.oreName.setText(I18n.translate("xray.input.gui"));
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
    return I18n.translate("xray.title.config");
  }
}
