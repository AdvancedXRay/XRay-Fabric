package pro.mikey.fabric.xray.records;

import com.google.gson.*;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtHelper;
import pro.mikey.fabric.xray.cache.BlockSearchEntry;

import java.lang.reflect.Type;

public class BlockEntry {
  private final BlockState state;
  private final ItemStack stack;
  private final String name;
  private final BasicColor color;
  private final int order;
  private final boolean isDefault;
  private final boolean active;

  public BlockEntry(
      BlockState state,
      String name,
      BasicColor color,
      int order,
      boolean isDefault,
      boolean active) {
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

  public String getName() {
    return this.name;
  }

  public BasicColor getHex() {
    return this.color;
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

  public ItemStack getStack() {
    return this.stack;
  }

  public static class Serializer
      implements JsonSerializer<BlockEntry>, JsonDeserializer<BlockEntry> {
    @Override
    public BlockEntry deserialize(
        JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
      JsonObject asJsonObject = json.getAsJsonObject();
      return new BlockEntry(
          BlockSearchEntry.blockStateFromStringNBT(asJsonObject.get("state").getAsString()),
          asJsonObject.get("name").getAsString(),
          BasicColor.of(asJsonObject.get("color").getAsString()),
          asJsonObject.get("order").getAsInt(),
          asJsonObject.get("isDefault").getAsBoolean(),
          asJsonObject.get("active").getAsBoolean());
    }

    @Override
    public JsonElement serialize(BlockEntry src, Type typeOfSrc, JsonSerializationContext context) {
      JsonObject object = new JsonObject();
      object.addProperty("state", NbtHelper.fromBlockState(src.getState()).toString());
      object.addProperty("name", src.name);
      object.addProperty("color", src.color.toHex());
      object.addProperty("order", src.order);
      object.addProperty("isDefault", src.isDefault);
      object.addProperty("active", src.active);
      return object;
    }
  }
}
