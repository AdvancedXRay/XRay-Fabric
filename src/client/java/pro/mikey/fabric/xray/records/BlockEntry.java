package pro.mikey.fabric.xray.records;

import com.google.gson.*;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import pro.mikey.fabric.xray.cache.BlockSearchEntry;

import java.lang.reflect.Type;

public class BlockEntry {
    private BlockState state;
    private ItemStack stack;
    private String name;
    private BasicColor color;
    private int order;
    private boolean isDefault;
    private boolean active;

    public BlockEntry(BlockState state, String name, BasicColor color, int order, boolean isDefault, boolean active) {
        this.state = state;
        this.stack = new ItemStack(this.state.getBlock());
        this.name = name;
        this.color = color;
        this.order = order;
        this.isDefault = isDefault;
        this.active = active;
    }

    public BlockState getState() {
        return this.state;
    }

    public void setState(BlockState state) {
        this.state = state;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BasicColor getHex() {
        return this.color;
    }

    public int getOrder() {
        return this.order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public boolean isDefault() {
        return this.isDefault;
    }

    public void setDefault(boolean aDefault) {
        this.isDefault = aDefault;
    }

    public boolean isActive() {
        return this.active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public ItemStack getStack() {
        return this.stack;
    }

    public void setStack(ItemStack stack) {
        this.stack = stack;
    }

    public void setColor(BasicColor color) {
        this.color = color;
    }

    public static class Serializer implements JsonSerializer<BlockEntry>, JsonDeserializer<BlockEntry> {
        @Override
        public BlockEntry deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject asJsonObject = json.getAsJsonObject();
            BlockState blockState = BlockSearchEntry.blockStateFromStringNBT(asJsonObject.get("state").getAsString());
            return new BlockEntry(
                    blockState,
                    blockState.getBlock().getName().getString(),
                    BasicColor.of(asJsonObject.get("color").getAsString()),
                    asJsonObject.get("order").getAsInt(),
                    asJsonObject.get("isDefault").getAsBoolean(),
                    asJsonObject.get("active").getAsBoolean());
        }

        @Override
        public JsonElement serialize(BlockEntry src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject object = new JsonObject();
            object.addProperty("state", NbtUtils.writeBlockState(src.getState()).toString());
            object.addProperty("name", src.name);
            object.addProperty("color", src.color.toHex());
            object.addProperty("order", src.order);
            object.addProperty("isDefault", src.isDefault);
            object.addProperty("active", src.active);
            return object;
        }
    }
}
