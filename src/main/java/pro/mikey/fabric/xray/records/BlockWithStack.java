package pro.mikey.fabric.xray.records;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

public record BlockWithStack(
    Block block,
    ItemStack stack
) {
}
