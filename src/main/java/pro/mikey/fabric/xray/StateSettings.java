package pro.mikey.fabric.xray;

import net.minecraft.util.Mth;
import pro.mikey.fabric.xray.storage.SettingsStore;

public class StateSettings {
    private static final int MAX_STEPS_TO_SCAN = 5;

    private transient boolean isActive;
    private boolean showLava;
    private int range;
    private boolean showOverlay;
    private int threadCount;

    public StateSettings() {
        this.isActive = false;
        this.showLava = false;
        this.showOverlay = true;
        this.range = 3;
        this.threadCount = 3;
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
        return Mth.clamp(SettingsStore.getInstance().get().range, 0, MAX_STEPS_TO_SCAN) * 3;
    }

    public static int getThreadCount(){
        return SettingsStore.getInstance().get().threadCount;
    }

    public static int getHalfRange() {
        return Math.max(0, getRadius() / 2);
    }

    public static int getVisualRadius() {
        return Math.max(1, getRadius());
    }

    public void increaseRange() {
        if (SettingsStore.getInstance().get().range < MAX_STEPS_TO_SCAN)
            SettingsStore.getInstance().get().range = SettingsStore.getInstance().get().range + 1;
        else
            SettingsStore.getInstance().get().range = 0;
    }

    public void decreaseRange() {
        if (SettingsStore.getInstance().get().range > 0)
            SettingsStore.getInstance().get().range = SettingsStore.getInstance().get().range - 1;
        else
            SettingsStore.getInstance().get().range = MAX_STEPS_TO_SCAN;
    }

    public boolean showOverlay() {
        return this.showOverlay;
    }

    public void setShowOverlay(boolean showOverlay) {
        this.showOverlay = showOverlay;
    }
}
