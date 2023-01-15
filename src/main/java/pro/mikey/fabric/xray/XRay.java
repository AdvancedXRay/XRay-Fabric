package pro.mikey.fabric.xray;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;
import pro.mikey.fabric.xray.render.RenderOutlines;
import pro.mikey.fabric.xray.screens.forge.GuiOverlay;
import pro.mikey.fabric.xray.screens.forge.GuiSelectionScreen;
import pro.mikey.fabric.xray.storage.BlockStore;
import pro.mikey.fabric.xray.storage.SettingsStore;

import java.util.HashSet;
import java.util.Set;

public class XRay implements ModInitializer {

    public static final String MOD_ID = "advanced-xray-fabric";
    public static final String PREFIX_GUI = String.format("%s:textures/gui/", MOD_ID);
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    private final KeyMapping xrayButton = new KeyMapping("keybinding.enable_xray", GLFW.GLFW_KEY_BACKSLASH, "category.xray");

    private final KeyMapping guiButton = new KeyMapping("keybinding.open_gui", GLFW.GLFW_KEY_G, "category.xray");

    public static volatile Set<LevelChunk> chunks = new HashSet<>();

    @Override
    public void onInitialize() {
        LOGGER.info("XRay mod has been initialized");

        ClientTickEvents.END_CLIENT_TICK.register(this::clientTickEvent);
        ClientLifecycleEvents.CLIENT_STOPPING.register(this::gameClosing);
        ClientChunkEvents.CHUNK_LOAD.register(this::ChunkLoad);
        ClientChunkEvents.CHUNK_UNLOAD.register(this::ChunkUnload);
        HudRenderCallback.EVENT.register(GuiOverlay::RenderGameOverlayEvent);

        WorldRenderEvents.LAST.register(RenderOutlines::render);
        PlayerBlockBreakEvents.AFTER.register(this::blockBroken);

        KeyBindingHelper.registerKeyBinding(this.xrayButton);
        KeyBindingHelper.registerKeyBinding(this.guiButton);
        ScanController.setup();
    }

    /**
     * Upon unloading a chunk the chunk can be removed from the rendering
     */
    private void ChunkUnload(ClientLevel clientLevel, LevelChunk levelChunk) {
        if (!SettingsStore.getInstance().get().isActive()) return;
        chunks.remove(levelChunk);
        ScanController.removeChunk(levelChunk.getPos());
    }

    /**
     * Upon loading a chunk the chunk needs to be queued for scanning and rendering
     */
    private void ChunkLoad(ClientLevel clientLevel, LevelChunk levelChunk) {
        if (!SettingsStore.getInstance().get().isActive()) return;
        chunks.add(levelChunk);
        ScanController.updateChunk(levelChunk.getPos());
    }

    /**
     * Upon breaking a block the Chunk of the Block should be updated immediately
     */
    private void blockBroken(Level world, Player playerEntity, BlockPos pos, BlockState blockState, BlockEntity blockEntity) {
        if (!SettingsStore.getInstance().get().isActive()) return;
        ScanController.updateChunk(pos);
    }

    /**
     * Upon placing a block the Chunk of the Block should be updated immediately
     */
    public static void blockPlaced(BlockPlaceContext context) {
        if (!SettingsStore.getInstance().get().isActive()) return;
        BlockPos pos = context.getClickedPos();
        ScanController.updateChunk(pos);
    }

    /**
     * Upon game closing, attempt to save our json stores. This means we can be a little lazy with how
     * we go about saving throughout the rest of the mod
     */
    private void gameClosing(Minecraft client) {
        ScanController.CloseGame();
        SettingsStore.getInstance().write();
        BlockStore.getInstance().write();
    }

    /**
     * Used to handle keybindings and fire off threaded scanning tasks
     */
    private void clientTickEvent(Minecraft mc) {
        if (mc.player == null || mc.level == null || mc.screen != null) {
            return;
        }
        int counter = 0;
        if (counter % 200 == 0) {
            RenderOutlines.clearChunks(false);
            counter=0;
        }
        counter++;

        while (this.guiButton.consumeClick()) {
            mc.setScreen(new GuiSelectionScreen());
        }

        while (this.xrayButton.consumeClick()) {
            BlockStore.getInstance().updateCache();

            StateSettings stateSettings = SettingsStore.getInstance().get();
            stateSettings.setActive(!stateSettings.isActive());
            if(stateSettings.isActive()){
                ScanController.RebuildCache();
            }
            else{
                RenderOutlines.clearChunks(true);
            }

            mc.player.displayClientMessage(Component.translatable("message.xray_" + (!stateSettings.isActive() ? "deactivate" : "active")).withStyle(stateSettings.isActive() ? ChatFormatting.GREEN : ChatFormatting.RED), true);
        }
    }
}
