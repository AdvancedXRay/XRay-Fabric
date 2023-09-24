package pro.mikey.fabric.xray.records;

import net.minecraft.core.BlockPos;

public record BlockPosWithColor(
        BlockPos pos,
        BasicColor color
) {
}
