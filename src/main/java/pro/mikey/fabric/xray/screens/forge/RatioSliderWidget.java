package pro.mikey.fabric.xray.screens.forge;

import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

public class RatioSliderWidget extends SliderWidget {

  RatioSliderWidget(int x, int y, int width, int height, Text text, double value) {
    super(x, y, width, height, text, value);
    this.updateMessage();
  }

  @Override
  protected void applyValue() {}

  @Override
  protected void updateMessage() {}

  double getValue() {
    return this.value;
  }
}
