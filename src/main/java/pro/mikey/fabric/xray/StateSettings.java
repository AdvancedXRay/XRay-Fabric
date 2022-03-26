package pro.mikey.fabric.xray;

import net.minecraft.util.math.MathHelper;
import pro.mikey.fabric.xray.storage.Stores;

public class StateSettings {
    private static final int maxStepsToScan = 5;

    private transient boolean isActive;
    private boolean showLava;
    private int range;
    private boolean showOverlay;

    public StateSettings() {
        this.isActive = false;
        this.showLava = false;
        this.showOverlay = true;
        this.range = 3;
    }

    public boolean isActive() {
        return this.isActive;
    }

    void setActive(boolean active) {
        this.isActive = active;
    }

    public boolean isShowLava() {
        return this.showLava;
    }

    public void setShowLava(boolean showLava) {
        this.showLava = showLava;
    }

    public static int getRadius() {
        return MathHelper.clamp(Stores.SETTINGS.get().range, 0, maxStepsToScan) * 3;
    }

    public static int getHalfRange() {
        return Math.max(0, getRadius() / 2);
    }

    public static int getVisualRadius() {
        return Math.max(1, getRadius());
    }

    public void increaseRange() {
        if (Stores.SETTINGS.get().range < maxStepsToScan)
            Stores.SETTINGS.get().range = Stores.SETTINGS.get().range + 1;
        else
            Stores.SETTINGS.get().range = 0;
    }

    public void decreaseRange() {
        if (Stores.SETTINGS.get().range > 0)
            Stores.SETTINGS.get().range = Stores.SETTINGS.get().range - 1;
        else
            Stores.SETTINGS.get().range = maxStepsToScan;
    }

    public boolean showOverlay() {
        return this.showOverlay;
    }

    public void setShowOverlay(boolean showOverlay) {
        this.showOverlay = showOverlay;
    }
}
