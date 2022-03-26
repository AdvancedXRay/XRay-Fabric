package pro.mikey.fabric.xray.records;

import net.minecraft.util.math.BlockPos;

public record BlockPosWithColor(
        BlockPos pos,
        BasicColor color
) {
}
