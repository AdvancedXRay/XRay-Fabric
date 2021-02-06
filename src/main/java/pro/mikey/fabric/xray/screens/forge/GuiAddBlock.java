package pro.mikey.fabric.xray.screens.forge;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.Block;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;

import java.util.Objects;
import java.util.function.Supplier;

public class GuiAddBlock extends GuiBase {
  private final Block selectBlock;
  private final ItemStack itemStack;
  private final Supplier<GuiBase> previousScreenCallback;
  private TextFieldWidget oreName;
  private ButtonWidget addBtn;
  private RatioSliderWidget redSlider;
  private RatioSliderWidget greenSlider;
  private RatioSliderWidget blueSlider;
  private boolean oreNameCleared = false;

  GuiAddBlock(Block selectedBlock, Supplier<GuiBase> previousScreenCallback) {
    super(false);
    this.selectBlock = selectedBlock;
    this.previousScreenCallback = previousScreenCallback;
    this.itemStack = new ItemStack(this.selectBlock, 1);
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
    tessellate.begin(7, VertexFormats.POSITION);
    tessellate.vertex(x, y, 0.0D).next();
    tessellate.vertex(x, y + 45, 0.0D).next();
    tessellate.vertex(x + 202, y + 45, 0.0D).next();
    tessellate.vertex(x + 202, y, 0.0D).next();
    tessellator.draw();
    RenderSystem.enableTexture();
    RenderSystem.disableBlend();
  }

  @Override
  public void init() {
    // Called when the gui should be (re)created
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

                  if (this.selectBlock.getTranslationKey() == null) {
                    return;
                  }

                  // Push the block to the render stack
                  //                  Controller.getBlockStore()
                  //                      .put(
                  //                          new BlockData(
                  //                              this.oreName.getText(),
                  //                              this.selectBlock.getRegistryName().toString(),
                  //                              (((int) (this.redSlider.getValue()) << 16)
                  //                                  + ((int) (this.greenSlider.getValue()) << 8)
                  //                                  + (int) (this.blueSlider.getValue())),
                  //                              this.itemStack,
                  //                              true,
                  //                              Controller.getBlockStore().getStore().size() +
                  // 1));
                  //
                  //                  ClientController.blockStore.write(
                  //                      new
                  // ArrayList<>(Controller.getBlockStore().getStore().values()));
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
                this.getHeight() / 2 + 7,
                202,
                20,
                new TranslatableText("xray.color.red"),
                0));
    this.addButton(
        this.greenSlider =
            new RatioSliderWidget(
                this.getWidth() / 2 - 100,
                this.getHeight() / 2 + 30,
                202,
                20,
                new TranslatableText("xray.color.green"),
                0));
    this.addButton(
        this.blueSlider =
            new RatioSliderWidget(
                this.getWidth() / 2 - 100,
                this.getHeight() / 2 + 53,
                202,
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

    this.oreName.setText(this.selectBlock.getTranslationKey());
    this.children.add(this.oreName);
    this.children.add(this.redSlider);
    this.children.add(this.greenSlider);
    this.children.add(this.blueSlider);
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
            this.selectBlock.getTranslationKey(),
            this.getWidth() / 2f - 100,
            this.getHeight() / 2f - 90,
            0xffffff);

    this.oreName.render(stack, x, y, partialTicks);
    renderPreview(
        this.getWidth() / 2 - 100,
        this.getHeight() / 2 - 40,
        (float) this.redSlider.getValue(),
        (float) this.greenSlider.getValue(),
        (float) this.blueSlider.getValue());

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
    //    if (this.redSlider.dragging && !this.redSlider.isFocused()) {
    //      this.redSlider.dragging = false;
    //    }
    //
    //    if (this.greenSlider.dragging && !this.greenSlider.isFocused()) {
    //      this.greenSlider.dragging = false;
    //    }
    //
    //    if (this.blueSlider.dragging && !this.blueSlider.isFocused()) {
    //      this.blueSlider.dragging = false;
    //    }

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
