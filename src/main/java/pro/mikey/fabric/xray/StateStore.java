package pro.mikey.fabric.xray;

public class StateStore {
    private static StateStore instance;

    private boolean isActive;
    private boolean showLava;
    private int range;

    // Singleton
    private StateStore() {
        this.isActive = false;
        this.showLava = false;
        this.range = 3;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public void setShowLava(boolean showLava) {
        this.showLava = showLava;
    }

    public void setRange(int range) {
        this.range = range;
    }

    public boolean isActive() {
        return isActive;
    }

    public boolean isShowLava() {
        return showLava;
    }

    public int getRange() {
        return range;
    }

    public static StateStore getInstance() {
        // Lazy Initialization
        if (instance == null) {
            instance = new StateStore();
        }

        return instance;
    }
}
