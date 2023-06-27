package pro.mikey.fabric.xray.records;

public record BasicColor(
        int red,
        int green,
        int blue,
        int alpha
) {
    public static BasicColor of(String hex) {
        if (!hex.startsWith("#")) {
            return new BasicColor(0, 0, 0, 255);
        }

        try {
            final long color = Long.decode(hex);
            final int alpha = (int)(color >> 24) & 0xFF;
            final int red   = (int)(color >> 16) & 0xFF;
            final int green = (int)(color >> 8)  & 0xFF;
            final int blue  = (int)(color)       & 0xFF;
            return new BasicColor(red, green, blue, alpha);
        } catch (NumberFormatException e) {
            return new BasicColor(0, 0, 0, 255);
        }
    }

    String toHex() {
        return String.format("#%02x%02x%02x%02x", this.alpha, this.red, this.green, this.blue);
    }
}
