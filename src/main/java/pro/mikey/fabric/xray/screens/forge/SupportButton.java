package pro.mikey.fabric.xray.screens.forge;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import java.util.ArrayList;
import java.util.List;

class SupportButton extends ButtonWidget {
  private final List<Text> support = new ArrayList<>();

  SupportButton(
      int widthIn,
      int heightIn,
      int width,
      int height,
      Text text,
      TranslatableText support,
      PressAction onPress) {
    super(widthIn, heightIn, width, height, text, onPress);

    for (String line : support.getString().split("\n")) {
      this.support.add(new TranslatableText(line));
    }
  }

  List<Text> getSupport() {
    return this.support;
  }
}
