package pro.mikey.fabric.xray;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import pro.mikey.fabric.xray.jobs.ChunkJob;

public class XRay implements ModInitializer {
	public ChunkJob chunkJob = new ChunkJob();

	@Override
	public void onInitialize() {
		System.out.println("Hello Fabric world!");

		ServerChunkEvents.CHUNK_LOAD.register(chunkJob::load);
		ServerChunkEvents.CHUNK_UNLOAD.register(chunkJob::unload);
	}
}
