package pro.mikey.fabric.xray.records;

public class BasicColor {
  private final int red;
  private final int blue;
  private final int green;

  public BasicColor(int red, int blue, int green) {
    this.red = red;
    this.blue = blue;
    this.green = green;
  }

  public static BasicColor of(int[] color) {
    return new BasicColor(color[0], color[1], color[2]);
  }

  public static BasicColor of(String hex) {
    if (!hex.startsWith("#")) {
      return new BasicColor(0, 0, 0);
    }

    int hexInt = Integer.parseInt(hex.replace("#", "0x"));
    return new BasicColor((hexInt & 0xFF0000) >> 16, (hexInt & 0xFF00) >> 8, (hexInt & 0xFF));
  }

  public int getRed() {
    return this.red;
  }

  public int getBlue() {
    return this.blue;
  }

  public int getGreen() {
    return this.green;
  }

  String toHex() {
    return String.format("#%02x%02x%02x", this.red, this.green, this.blue);
  }
}
