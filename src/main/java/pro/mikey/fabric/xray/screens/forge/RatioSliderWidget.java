package pro.mikey.fabric.xray.screens.forge;

import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public class RatioSliderWidget extends SliderWidget {
  private final Text message;

  RatioSliderWidget(int x, int y, int width, int height, Text text, double value) {
    super(x, y, width, height, text, value);
    this.message = text;
    this.updateMessage();
  }

  @Override
  protected void applyValue() {}

  @Override
  protected void updateMessage() {
    this.setMessage(new LiteralText(this.message.getString() + ((int) (this.value * 255))));
  }

  double getValue() {
    return this.value;
  }

  void setValue(double value) {
    this.value = Math.max(0, Math.min(1, value));
    this.updateMessage();
  }
}
