package pro.mikey.fabric.xray;

public class StateSettings {
  private boolean isActive;
  private boolean showLava;
  private int range;

  // Singleton
  public StateSettings() {
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
}
