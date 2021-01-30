package pro.mikey.fabric.xray.records;

import java.util.List;

public class BlockGroup {
  private final String name;
  private final List<BlockEntry> entries;
  private final int order;
  private final boolean active;

  public BlockGroup(String name, List<BlockEntry> entries, int order, boolean active) {
    this.name = name;
    this.entries = entries;
    this.order = order;
    this.active = active;
  }

  public List<BlockEntry> getEntries() {
    return this.entries;
  }

  public int getOrder() {
    return this.order;
  }

  public boolean isActive() {
    return this.active;
  }

  public String getName() {
    return this.name;
  }
}
