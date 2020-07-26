package pro.mikey.fabric.xray;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.netty.util.internal.ConcurrentSet;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import org.lwjgl.glfw.GLFW;
import pro.mikey.fabric.xray.jobs.ChunkJob;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class XRay implements ModInitializer {
	public ChunkJob chunkJob = new ChunkJob();
	private KeyBinding trigger = new KeyBinding("Crap", GLFW.GLFW_KEY_G, "xray");

	private static final Cache<ChunkPos, Set<BlockPos>> cacheByChunk = CacheBuilder
			.newBuilder()
			.expireAfterWrite(5, TimeUnit.SECONDS)
			.build();

	private final Thread thread = new Thread(this::xrayRender);

	@Override
	public void onInitialize() {
		System.out.println("Hello Fabric world!");

		ClientChunkEvents.CHUNK_LOAD.register(chunkJob::load);
		ClientChunkEvents.CHUNK_UNLOAD.register(chunkJob::unload);

		ClientTickEvents.END_CLIENT_TICK.register(this::clientTickEvent);

		KeyBindingHelper.registerKeyBinding(trigger);
	}

	private void clientTickEvent(MinecraftClient mc) {
		PlayerEntity player = mc.player;
		World world = mc.world;

		if (player == null || mc.currentScreen != null || world == null ) {
			return;
		}

		int range = 2;

		try {
			System.out.println(cacheByChunk.get(new ChunkPos(player.chunkX, player.chunkZ), () -> {
				Set<BlockPos> posList = new HashSet<>();

				int chunkX = player.getBlockPos().getX() >> 4;
				int chunkZ = player.getBlockPos().getZ() >> 4;

				for (int i = chunkX - range; i <= chunkX + range; i++) {
					int x = i << 4;
					int maxX = x + 15;

					for (int j = chunkZ - range; j <= chunkZ + range; j++) {
						int z = j << 4;
						int maxZ = j + 15;

						if (!world.isChunkLoaded(i, j)) {
							continue;
						}

						Chunk chunk = world.getChunk(i, j);
						ChunkSection[] sections = chunk.getSectionArray();

						


						System.out.println(String.format("%d %d %d %d", i, j, i << 4, j << 4));
					}
				}

				System.out.println(chunkX);
				System.out.println(chunkZ);

//				for (int i = player.chunkX - range; i < player.chunkX + range; i++) {
//					for (int j = player.chunkZ - range; j < player.chunkZ + range; j++) {
//						Chunk chunk = minecraftClient.world.getChunk(i, j);
//						ChunkPos pos = chunk.getPos();
//
//						for (int k = (pos.x << 4); k < (pos.x << 4) + 15; k++) {
//							for (int l = (pos.z << 4); l < (pos.z << 4) + 15; l++) {
//								posList.add(new BlockPos(k, 0, l));
//							}
//						}
////						posList.add(minecraftClient.world.getChunk(i, j).getPos());
//					}
//				}

				return posList;
			}));
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
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

	private synchronized void xrayRender() {
		while( !thread.isInterrupted() ) {
		}
	}
}
