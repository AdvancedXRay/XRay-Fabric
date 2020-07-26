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

	public static final String MOD_ID = "xray";
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
	 * @param mc
	 */
	private void clientTickEvent(MinecraftClient mc) {
		PlayerEntity player = mc.player;
		World world = mc.world;

		if (player == null || mc.currentScreen != null || world == null ) {
			return;
		}

		ScanController.runTask(false);
//
//		int range = 2;
//
//		try {
//			System.out.println(cacheByChunk.get(new ChunkPos(player.chunkX, player.chunkZ), () -> {
//				Set<BlockPos> posList = new HashSet<>();
//
//				int chunkX = player.getBlockPos().getX() >> 4;
//				int chunkZ = player.getBlockPos().getZ() >> 4;
//
//				for (int i = chunkX - range; i <= chunkX + range; i++) {
//					int x = i << 4;
//					int maxX = x + 15;
//
//					for (int j = chunkZ - range; j <= chunkZ + range; j++) {
//						int z = j << 4;
//						int maxZ = j + 15;
//
//						if (!world.isChunkLoaded(i, j)) {
//							continue;
//						}
//
//						Chunk chunk = world.getChunk(i, j);
//						ChunkSection[] sections = chunk.getSectionArray();
//
//
//
//
//						System.out.println(String.format("%d %d %d %d", i, j, i << 4, j << 4));
//					}
//				}
//
//				System.out.println(chunkX);
//				System.out.println(chunkZ);
//
////				for (int i = player.chunkX - range; i < player.chunkX + range; i++) {
////					for (int j = player.chunkZ - range; j < player.chunkZ + range; j++) {
////						Chunk chunk = minecraftClient.world.getChunk(i, j);
////						ChunkPos pos = chunk.getPos();
////
////						for (int k = (pos.x << 4); k < (pos.x << 4) + 15; k++) {
////							for (int l = (pos.z << 4); l < (pos.z << 4) + 15; l++) {
////								posList.add(new BlockPos(k, 0, l));
////							}
////						}
//////						posList.add(minecraftClient.world.getChunk(i, j).getPos());
////					}
////				}
//
//				return posList;
//			}));
//		} catch (ExecutionException e) {
//			e.printStackTrace();
//		}
		if (trigger.isPressed()) {
			System.out.println("Triggered");

//			if (!thread.isAlive() && thread.getState() == Thread.State.NEW)
//				thread.start();
//			if (thread.isAlive()) {
//				thread.interrupt();
//				System.out.println("Thread killed");
//			} else {
//				thread.start();
//				System.out.println("Thread Started");
//			}
		}
	}
}
