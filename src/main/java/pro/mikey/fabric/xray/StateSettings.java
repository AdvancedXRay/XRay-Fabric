package pro.mikey.fabric.xray;

public class StateSettings {
  private boolean isActive;
  private boolean showLava;
  private int range;
  private boolean showOverlay;

  // Singleton
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

  public int getRange() {
    return this.range;
  }

  public void setRange(int range) {
    this.range = range;
  }

  public boolean showOverlay() {
    return this.showOverlay;
  }

  public void setShowOverlay(boolean showOverlay) {
    this.showOverlay = showOverlay;
  }
}
