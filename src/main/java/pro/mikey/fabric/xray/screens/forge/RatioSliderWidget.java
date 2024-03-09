package pro.mikey.fabric.xray.screens.forge;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

public class RatioSliderWidget extends AbstractSliderButton {
  private final Component message;

  RatioSliderWidget(int x, int y, int width, int height, Component text, double value) {
    super(x, y, width, height, text, value);
    this.message = text;
    this.updateMessage();
  }

  @Override
  protected void applyValue() {}

  @Override
  protected void updateMessage() {
    this.setMessage(Component.literal(this.message.getString() + ((int) (this.value * 255))));
  }

  double getValue() {
    return this.value;
  }

  void setValue(double value) {
    this.value = Math.max(0, Math.min(1, value));
    this.updateMessage();
  }
}
