package pro.mikey.fabric.xray;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;
import pro.mikey.fabric.xray.render.XRayRenderer;
import pro.mikey.fabric.xray.scan.ScanController;
import pro.mikey.fabric.xray.screens.MainScreen;
import pro.mikey.fabric.xray.storage.Stores;

public class XRay implements ModInitializer {

  public static final String MOD_ID = "advanced-xray-fabric";
  public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

  public static final XRayRenderer xrayRenderer = new XRayRenderer();

  private final KeyBinding xrayButton =
      new KeyBinding("keybinding.enable_xray", GLFW.GLFW_KEY_BACKSLASH, "category.xray");
  private final KeyBinding guiButton =
      new KeyBinding("keybinding.open_gui", GLFW.GLFW_KEY_G, "category.xray");

  private int keyCoolDown = 0;

  @Override
  public void onInitialize() {
    LOGGER.info("XRay mod has been initialized");

    ClientTickEvents.END_CLIENT_TICK.register(this::clientTickEvent);
    ClientLifecycleEvents.CLIENT_STOPPING.register(this::gameClosing);

    KeyBindingHelper.registerKeyBinding(this.xrayButton);
    KeyBindingHelper.registerKeyBinding(this.guiButton);
  }

  /**
   * Upon game closing, attempt to save our json stores. This means we can be a little lazy with how
   * we go about saving throughout the rest of the mod
   */
  private void gameClosing(MinecraftClient client) {
    Stores.write();
  }

  /** Used to handle keybindings and fire off threaded scanning tasks */
  private void clientTickEvent(MinecraftClient mc) {
    if (mc.player == null || mc.world == null || mc.currentScreen != null) {
      return;
    }

    // Try and run the task :D
    ScanController.runTask(false);

    // Handle cooldown for the keybinding to stop it spamming
    if (this.keyCoolDown > 0) {
      this.keyCoolDown--;
      return;
    }

    if (this.guiButton.isPressed()) {
      mc.openScreen(new MainScreen());
      ScanController.activeChunks.clear();
      ScanController.scannedChunks.clear();
    }

    if (this.xrayButton.isPressed()) {
      StateSettings stateSettings = Stores.SETTINGS.get();
      stateSettings.setActive(!stateSettings.isActive());
      mc.player.sendMessage(
          new TranslatableText(
                  "message.xray_" + (!stateSettings.isActive() ? "deactivate" : "active"))
              .formatted(stateSettings.isActive() ? Formatting.GREEN : Formatting.RED),
          true);

      this.keyCoolDown = 5;
    }
  }
}
