package pro.mikey.fabric.xray.records;

public class BlockEntry {
    String state;
    String name;
    String hex;
    int order;
    boolean isDefault;
    boolean active;

    public BlockEntry(String state, String name, String hex, int order, boolean isDefault, boolean active) {
        this.state = state;
        this.name = name;
        this.hex = hex;
        this.order = order;
        this.isDefault = isDefault;
        this.active = active;
    }

    public String getState() {
        return state;
    }

    public String getName() {
        return name;
    }

    public String getHex() {
        return hex;
    }

    public int getOrder() {
        return order;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public boolean isActive() {
        return active;
    }
}
