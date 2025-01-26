package pro.mikey.fabric.xray;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.material.LavaFluid;
import net.minecraft.world.level.material.WaterFluid;
import org.jetbrains.annotations.Nullable;
import pro.mikey.fabric.xray.cache.BlockSearchEntry;
import pro.mikey.fabric.xray.records.BasicColor;
import pro.mikey.fabric.xray.records.BlockPosWithColor;
import pro.mikey.fabric.xray.render.RenderOutlines;
import pro.mikey.fabric.xray.storage.BlockStore;
import pro.mikey.fabric.xray.storage.SettingsStore;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class ScanTask implements Runnable {
    private static AtomicBoolean isScanning = new AtomicBoolean(false);

    ScanTask() {
    }

    /**
     * Check if we're valid and push back the blocks color for the render queue
     */
    @Nullable
    public static BasicColor isValidBlock(BlockPos pos, Level world, Set<BlockSearchEntry> blocks) {
        BlockState state = world.getBlockState(pos);
        if (state.isAir()) {
            return null;
        }

        if (SettingsStore.getInstance().get().isShowLava() && state.getFluidState().getType() instanceof LavaFluid) {
            return new BasicColor(210, 10, 10);
        }

        BlockState defaultState = state.getBlock().defaultBlockState();

        if(SettingsStore.getInstance().get().isOnlySurface() && !isBlockExposed(world, pos)) {
            return null;
        }

        return blocks.stream()
                .filter(localState -> localState.isDefault() && defaultState == localState.getState() || !localState.isDefault() && state == localState.getState())
                .findFirst()
                .map(BlockSearchEntry::getColor)
                .orElse(null);
    }

    public static boolean isBlockExposed(Level world, BlockPos pos) {
        // Array of offsets for the 6 surrounding directions
        BlockPos[] offsets = new BlockPos[]{
                pos.north(),
                pos.south(),
                pos.east(),
                pos.west(),
                pos.above(),
                pos.below()
        };

        for (BlockPos offset : offsets) {
            BlockState state = world.getBlockState(offset);

            if(world.getBlockState(pos).is(Blocks.ANCIENT_DEBRIS)) {
                if (state.getFluidState().getType() instanceof LavaFluid)
                    return true;
            } else {
                // Check if the block is air, water, or lava
                if (state.isAir() || state.getFluidState().getType() instanceof LavaFluid || state.getFluidState().getType() instanceof WaterFluid)
                    return true;
            }
        }

        // If none of the surrounding blocks are exposed, return null
        return false;
    }

    @Override
    public void run() {
        if (isScanning.get()) {
            return;
        }

        isScanning.set(true);
        Set<BlockPosWithColor> c = this.collectBlocks();
        ScanController.renderQueue.clear();
        ScanController.renderQueue.addAll(c);
        isScanning.set(false);
        RenderOutlines.requestedRefresh.set(true);
    }

    /**
     * This is an "exact" copy from the forge version of the mod but with the optimisations that the
     * rewrite (Fabric) version has brought like chunk location based cache, etc.
     *
     * <p>This is only run if the cache is invalidated.
     *
     * @implNote Using the {@link BlockPos#betweenClosed(BlockPos, BlockPos)} may be a better system for the
     * scanning.
     */
    private Set<BlockPosWithColor> collectBlocks() {
        Set<BlockSearchEntry> blocks = BlockStore.getInstance().getCache().get();

        // If we're not looking for blocks, don't run.
        if (blocks.isEmpty() && !SettingsStore.getInstance().get().isShowLava()) {
            if (!ScanController.renderQueue.isEmpty()) {
                ScanController.renderQueue.clear();
            }
            return new HashSet<>();
        }

        Minecraft instance = Minecraft.getInstance();

        final Level world = instance.level;
        final Player player = instance.player;

        // Just stop if we can't get the player or world.
        if (world == null || player == null) {
            return new HashSet<>();
        }

        final Set<BlockPosWithColor> renderQueue = new HashSet<>();

        int cX = player.chunkPosition().x;
        int cZ = player.chunkPosition().z;

        int range = StateSettings.getHalfRange();

        for (int i = cX - range; i <= cX + range; i++) {
            int chunkStartX = i << 4;
            for (int j = cZ - range; j <= cZ + range; j++) {
                int chunkStartZ = j << 4;

                for (int k = chunkStartX; k < chunkStartX + 16; k++) {
                    for (int l = chunkStartZ; l < chunkStartZ + 16; l++) {
                        for (int m = world.getMinY(); m < world.getMaxY() + (1 << 4); m++) {
                            BlockPos pos = new BlockPos(k, m, l);
                            BasicColor validBlock = isValidBlock(pos, world, blocks);
                            if (validBlock != null) {
                                renderQueue.add(new BlockPosWithColor(pos, validBlock));
                            }
                        }
                    }
                }
            }
        }

        return renderQueue;
    }
}
