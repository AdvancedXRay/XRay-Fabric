package pro.mikey.fabric.xray.cache;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.util.JsonHelper;
import pro.mikey.fabric.xray.records.BasicColor;

public class BlockSearchEntry {
    private final BlockState state;
    private final BasicColor color;
    private final boolean isDefault;

    public BlockSearchEntry(BlockState state, BasicColor color, boolean isDefault) {
        this.state = state;
        this.color = color;
        this.isDefault = isDefault;
    }

    public static BlockState blockStateFromStringNBT(String nbt) {
        CompoundTag tag;
        try {
            tag = StringNbtReader.parse(nbt);
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return Blocks.AIR.getDefaultState();
        }

        return NbtHelper.toBlockState(tag);
    }

    public static String blockStateToStringNBT(BlockState state) {
        return NbtHelper.fromBlockState(state).toString();
    }

    public BlockState getState() {
        return state;
    }

    public BasicColor getColor() {
        return color;
    }

    public boolean isDefault() {
        return isDefault;
    }

    // We don't care about the color and isDefault
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockSearchEntry that = (BlockSearchEntry) o;
        return Objects.equal(state, that.state);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(state);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("state", state)
                .add("color", color)
                .add("isDefault", isDefault)
                .toString();
    }
}
