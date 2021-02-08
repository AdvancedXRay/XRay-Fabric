package pro.mikey.fabric.xray;

public class StateSettings {
  public static final int[] DISTANCE_STEPS = new int[] {2, 4, 8, 16, 32, 64, 128, 256};
  private boolean isActive;
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

  // Fail softly if the index is out of bounds
  public int getRange() {
    return Math.max(0, Math.min(DISTANCE_STEPS.length - 1, this.range));
  }

  public void increaseRange() {
    if (this.range < DISTANCE_STEPS.length - 1) {
      this.range += 1;
    } else {
      this.range = 0;
    }
  }

  public void decreaseRange() {
    if (this.range > 0) {
      this.range -= 1;
    } else {
      this.range = DISTANCE_STEPS.length - 1;
    }
  }

  public boolean showOverlay() {
    return this.showOverlay;
  }

  public void setShowOverlay(boolean showOverlay) {
    this.showOverlay = showOverlay;
  }
}
