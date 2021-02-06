package pro.mikey.fabric.xray.screens.forge;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import pro.mikey.fabric.xray.records.BlockEntry;

public class GuiEdit extends GuiBase {
  private final BlockEntry block;
  private TextFieldWidget oreName;
  private RatioSliderWidget redSlider;
  private RatioSliderWidget greenSlider;
  private RatioSliderWidget blueSlider;

  GuiEdit(BlockEntry block) {
    super(true); // Has a sidebar
    this.setSideTitle(I18n.translate("xray.single.tools"));

    this.block = block;
  }

  @Override
  public void init() {
    this.addButton(
        new ButtonWidget(
            (this.getWidth() / 2) + 78,
            this.getHeight() / 2 - 60,
            120,
            20,
            new TranslatableText("xray.single.delete"),
            b -> {
              //              Controller.getBlockStore().remove(this.block.getBlockName());
              //              ClientController.blockStore.write(
              //                  new ArrayList<>(Controller.getBlockStore().getStore().values()));

              this.onClose();
              this.getMinecraft().openScreen(new GuiSelectionScreen());
            }));

    this.addButton(
        new ButtonWidget(
            (this.getWidth() / 2) + 78,
            this.getHeight() / 2 + 58,
            120,
            20,
            new TranslatableText("xray.single.cancel"),
            b -> {
              this.onClose();
              this.getMinecraft().openScreen(new GuiSelectionScreen());
            }));
    this.addButton(
        new ButtonWidget(
            this.getWidth() / 2 - 138,
            this.getHeight() / 2 + 83,
            202,
            20,
            new TranslatableText("xray.single.save"),
            b -> {
              //              BlockData block =
              //                  new BlockData(
              //                      this.oreName.getText(),
              //                      this.block.getBlockName(),
              //                      (((int) (this.redSlider.getValue()) << 16)
              //                          + ((int) (this.greenSlider.getValue()) << 8)
              //                          + (int) (this.blueSlider.getValue())),
              //                      this.block.getItemStack(),
              //                      this.block.isDrawing(),
              //                      this.block.getOrder());
              //
              //              Pair<BlockData, UUID> data =
              //
              // Controller.getBlockStore().getStoreByReference(block.getBlockName());
              //              Controller.getBlockStore().getStore().remove(data.getValue());
              //              Controller.getBlockStore().getStore().put(data.getValue(), block);
              //
              //              ClientController.blockStore.write(
              //                  new ArrayList<>(Controller.getBlockStore().getStore().values()));
              this.onClose();
            }));

    this.addButton(
        this.redSlider =
            new RatioSliderWidget(
                this.getWidth() / 2 - 138,
                this.getHeight() / 2 + 7,
                202,
                20,
                new TranslatableText("xray.color.red"),
                0));
    this.addButton(
        this.greenSlider =
            new RatioSliderWidget(
                this.getWidth() / 2 - 138,
                this.getHeight() / 2 + 30,
                202,
                20,
                new TranslatableText("xray.color.green"),
                0));
    this.addButton(
        this.blueSlider =
            new RatioSliderWidget(
                this.getWidth() / 2 - 138,
                this.getHeight() / 2 + 53,
                202,
                20,
                new TranslatableText("xray.color.blue"),
                0));

    this.oreName =
        new TextFieldWidget(
            this.getMinecraft().textRenderer,
            this.getWidth() / 2 - 138,
            this.getHeight() / 2 - 63,
            202,
            20,
            LiteralText.EMPTY);
    this.oreName.setText(this.block.getName());
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
            this.block.getName(),
            this.getWidth() / 2f - 138,
            this.getHeight() / 2f - 90,
            0xffffff);

    this.oreName.render(stack, x, y, partialTicks);

    GuiAddBlock.renderPreview(
        this.getWidth() / 2 - 138,
        this.getHeight() / 2 - 40,
        (float) this.redSlider.getValue(),
        (float) this.greenSlider.getValue(),
        (float) this.blueSlider.getValue());

    //    DiffuseLighting.enable();
    //    this.itemRenderer.renderInGuiWithOverrides(
    //        this.block.getItemStack(), this.getWidth() / 2 + 50, this.getHeight() / 2 - 105);
    //    DiffuseLighting.disable();
  }

  @Override
  public boolean mouseClicked(double x, double y, int mouse) {
    if (this.oreName.mouseClicked(x, y, mouse)) {
      this.setFocused(this.oreName);
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
    return I18n.translate("xray.title.edit");
  }
}
