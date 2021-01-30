package pro.mikey.fabric.xray.records;

public class BlockEntry {
  private String state;
  private String name;
  private String hex;
  private int order;
  private boolean isDefault;
  private boolean active;

  public BlockEntry(String state, String name, String hex, int order, boolean isDefault, boolean active) {
    this.state = state;
    this.name = name;
    this.hex = hex;
    this.order = order;
    this.isDefault = isDefault;
    this.active = active;
  }

  public String getState() {
    return this.state;
  }

  public String getName() {
    return this.name;
  }

  public String getHex() {
    return this.hex;
  }

  public int getOrder() {
    return this.order;
  }

  public boolean isDefault() {
    return this.isDefault;
  }

  public boolean isActive() {
    return this.active;
  }
}
