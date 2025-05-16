package pro.mikey.fabric.xray;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;
import pro.mikey.fabric.xray.render.RenderOutlines;
import pro.mikey.fabric.xray.screens.forge.GuiOverlay;
import pro.mikey.fabric.xray.screens.forge.GuiSelectionScreen;
import pro.mikey.fabric.xray.storage.BlockStore;
import pro.mikey.fabric.xray.storage.SettingsStore;

public class XRay implements ClientModInitializer {
    public static final String MOD_ID = "advanced-xray-fabric";
    public static final String PREFIX_GUI = String.format("%s:textures/gui/", MOD_ID);
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    private final KeyMapping xrayButton = new KeyMapping("keybinding.enable_xray", GLFW.GLFW_KEY_BACKSLASH, "category.xray");

    private final KeyMapping guiButton = new KeyMapping("keybinding.open_gui", GLFW.GLFW_KEY_G, "category.xray");

    @Override
    public void onInitializeClient() {
        LOGGER.info("XRay mod has been initialized");

        ClientTickEvents.END_CLIENT_TICK.register(this::clientTickEvent);
        ClientLifecycleEvents.CLIENT_STOPPING.register(this::gameClosing);
        ClientLifecycleEvents.CLIENT_STARTED.register(this::started);
        HudRenderCallback.EVENT.register(GuiOverlay::RenderGameOverlayEvent);

        WorldRenderEvents.LAST.register(RenderOutlines::render);
        PlayerBlockBreakEvents.AFTER.register(ScanController::blockBroken);

        KeyBindingHelper.registerKeyBinding(this.xrayButton);
        KeyBindingHelper.registerKeyBinding(this.guiButton);

    }

    private void started(Minecraft minecraft) {
        LOGGER.info("Client started, setting up xray store");
    }

    /**
     * Upon game closing, attempt to save our json stores. This means we can be a little lazy with how
     * we go about saving throughout the rest of the mod
     */
    private void gameClosing(Minecraft client) {
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

        // Try and run the task :D
        ScanController.runTask(false);

        while (this.guiButton.consumeClick()) {
            mc.setScreen(new GuiSelectionScreen());
        }

        while (this.xrayButton.consumeClick()) {
            BlockStore.getInstance().updateCache();

            StateSettings stateSettings = SettingsStore.getInstance().get();
            stateSettings.setActive(!stateSettings.isActive());

            ScanController.runTask(true);

            mc.player.displayClientMessage(Component.translatable("message.xray_" + (!stateSettings.isActive() ? "deactivate" : "active")).withStyle(stateSettings.isActive() ? ChatFormatting.GREEN : ChatFormatting.RED), true);
        }
    }
}
