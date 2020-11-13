package pro.mikey.fabric.xray;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

public class XRay implements ModInitializer {

	public static final String MOD_ID = "advanced-xray-fabric";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
	private final KeyBinding trigger = new KeyBinding("Enable XRay", GLFW.GLFW_KEY_G, "xray");

	@Override
	public void onInitialize() {
		LOGGER.info("XRay mod has been initialized");

		ClientTickEvents.END_CLIENT_TICK.register(this::clientTickEvent);
		KeyBindingHelper.registerKeyBinding(trigger);
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

		StateStore state = StateStore.getInstance();
		if (trigger.isPressed() && !state.isActive()) {
			if (state.isActive()) {
				ScanController.shutdownTask();
				state.setActive(false);
				System.out.println("Xray Disabled");
			} else {
				state.setActive(true);
				System.out.println("Xray Enabled");
			}
		}
	}
}
