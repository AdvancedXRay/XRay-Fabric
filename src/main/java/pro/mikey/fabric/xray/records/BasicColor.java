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
}
