package pro.mikey.fabric.xray.records;

import net.minecraft.util.math.BlockPos;

public class BlockPosWithColor {
  private final BlockPos pos;
  private final BasicColor color;

  public BlockPosWithColor(BlockPos pos, BasicColor color) {
    this.pos = pos;
    this.color = color;
  }

  public BlockPos getPos() {
    return this.pos;
  }

  public BasicColor getColor() {
    return this.color;
  }
}
