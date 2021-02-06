package pro.mikey.fabric.xray.records;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

public class BlockWithStack {
  private final Block block;
  private final ItemStack stack;

  public BlockWithStack(Block block, ItemStack stack) {
    this.block = block;
    this.stack = stack;
  }

  public Block getBlock() {
    return this.block;
  }

  public ItemStack getStack() {
    return this.stack;
  }
}
