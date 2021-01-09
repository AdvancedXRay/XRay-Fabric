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

        return new BasicColor(
                Integer.valueOf(hex.substring(1, 3), 16),
                Integer.valueOf(hex.substring(3, 5), 16),
                Integer.valueOf(hex.substring(5, 7), 16)
        );
    }

    public int getRed() {
        return red;
    }

    public int getBlue() {
        return blue;
    }

    public int getGreen() {
        return green;
    }
}
