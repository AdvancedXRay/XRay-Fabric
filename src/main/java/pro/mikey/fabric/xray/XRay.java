package pro.mikey.fabric.xray;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;
import pro.mikey.fabric.xray.screens.MainScreen;
import pro.mikey.fabric.xray.storage.Stores;

public class XRay implements ModInitializer {

	public static final String MOD_ID = "advanced-xray-fabric";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	private final KeyBinding xrayButton = new KeyBinding("keybinding.enable_xray", GLFW.GLFW_KEY_G, "category.xray");
	private final KeyBinding guiButton = new KeyBinding("keybinding.open_gui", GLFW.GLFW_KEY_BACKSLASH, "category.xray");

	private int keyCoolDown = 0;

	@Override
	public void onInitialize() {
		LOGGER.info("XRay mod has been initialized");

		ClientTickEvents.END_CLIENT_TICK.register(this::clientTickEvent);
		KeyBindingHelper.registerKeyBinding(xrayButton);
		KeyBindingHelper.registerKeyBinding(guiButton);
	}
	
	/**
	 * Handles the actual scanning process :D
	 */
	private void clientTickEvent(MinecraftClient mc) {
		PlayerEntity player = mc.player;
		World world = mc.world;

		if (player == null || world == null) {
			return;
		}

		// Don't handle key bindings
		if (mc.currentScreen != null) {
			return;
		}

		// Try and run the task :D
		ScanController.runTask(false);

		// Handle cooldown for the keybinding to stop it spamming
		if (keyCoolDown > 0) {
			keyCoolDown --;
			return;
		}

		if (guiButton.isPressed()) {
			mc.openScreen(new MainScreen());
		}

		if (xrayButton.isPressed()) {
			StateSettings stateSettings = Stores.SETTINGS.get();

			if (stateSettings.isActive()) {
				stateSettings.setActive(false);
				mc.player.sendMessage(new TranslatableText("message.xray_deactivate").formatted(Formatting.RED), true);
			} else {
				stateSettings.setActive(true);
				mc.player.sendMessage(new TranslatableText("message.xray_active").formatted(Formatting.GREEN), true);
			}

			keyCoolDown = 10;
		}
	}
}
