package pro.mikey.fabric.xray.records;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public record BlockWithStack(
    Block block,
    ItemStack stack
) {
}
