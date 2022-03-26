package pro.mikey.fabric.xray.records;

import java.util.List;
import java.util.Objects;

public final class BlockGroup {
    private String name;
    private List<BlockEntry> entries;
    private int order;
    private boolean active;

    public BlockGroup(
            String name,
            List<BlockEntry> entries,
            int order,
            boolean active
    ) {
        this.name = name;
        this.entries = entries;
        this.order = order;
        this.active = active;
    }

    public String name() {
        return name;
    }

    public List<BlockEntry> entries() {
        return entries;
    }

    public int order() {
        return order;
    }

    public boolean active() {
        return active;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<BlockEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<BlockEntry> entries) {
        this.entries = entries;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (BlockGroup) obj;
        return Objects.equals(this.name, that.name) &&
                Objects.equals(this.entries, that.entries) &&
                this.order == that.order &&
                this.active == that.active;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, entries, order, active);
    }

    @Override
    public String toString() {
        return "BlockGroup[" +
                "name=" + name + ", " +
                "entries=" + entries + ", " +
                "order=" + order + ", " +
                "active=" + active + ']';
    }

}
