package pro.mikey.fabric.xray;

import net.minecraft.util.math.MathHelper;
import pro.mikey.fabric.xray.storage.SettingsStore;

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
        return MathHelper.clamp(SettingsStore.getInstance().get().range, 0, maxStepsToScan) * 3;
    }

    public static int getHalfRange() {
        return Math.max(0, getRadius() / 2);
    }

    public static int getVisualRadius() {
        return Math.max(1, getRadius());
    }

    public void increaseRange() {
        if (SettingsStore.getInstance().get().range < maxStepsToScan)
            SettingsStore.getInstance().get().range = SettingsStore.getInstance().get().range + 1;
        else
            SettingsStore.getInstance().get().range = 0;
    }

    public void decreaseRange() {
        if (SettingsStore.getInstance().get().range > 0)
            SettingsStore.getInstance().get().range = SettingsStore.getInstance().get().range - 1;
        else
            SettingsStore.getInstance().get().range = maxStepsToScan;
    }

    public boolean showOverlay() {
        return this.showOverlay;
    }

    public void setShowOverlay(boolean showOverlay) {
        this.showOverlay = showOverlay;
    }
}
