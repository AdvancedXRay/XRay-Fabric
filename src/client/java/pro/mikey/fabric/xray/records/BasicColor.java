package pro.mikey.fabric.xray.records;

import java.awt.*;

public record BasicColor(
        int red,
        int green,
        int blue
) {
    public static BasicColor of(String hex) {
        if (!hex.startsWith("#")) {
            return new BasicColor(0, 0, 0);
        }

        Color color = Color.decode(hex);
        return new BasicColor(color.getRed(), color.getGreen(), color.getBlue());
    }

    String toHex() {
        return String.format("#%02x%02x%02x", this.red, this.green, this.blue);
    }

    /**
     * Convert the color to an int along with a alpha value of 255
     */
    public int toInt() {
        return (255 << 24) + (this.red << 16) + (this.green << 8) + this.blue;
    }

    public int toInt(int alpha) {
        return (alpha << 24) + (this.red << 16) + (this.green << 8) + this.blue;
    }

    public static int rgbaToInt(int red, int green, int blue, float alpha) {
        return ((int) (alpha * 255) << 24) + (red << 16) + (green << 8) + blue;
    }
}
