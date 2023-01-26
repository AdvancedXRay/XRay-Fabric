package pro.mikey.fabric.xray.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.lwjgl.opengl.GL11;
import pro.mikey.fabric.xray.records.BlockPosWithColor;
import pro.mikey.fabric.xray.storage.SettingsStore;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class RenderOutlines {
    /**
     * This queue holds the by the threadpool queued workloads for the Renderthread.
     * To keep performancy high the renderthreads only uploads one Chunk per frame
     */
    private static final ConcurrentLinkedQueue<WorkLoad> workQueue = new ConcurrentLinkedQueue<>();
    /**
     * This is the internal Vertexbuffers mapped by chunk so the removal is simple
     */
    private static final Map<Long, VertexBuffer> chunkCache = new HashMap<>();
    /**
     * This List provides the sorting for the rendering.
     * I'm not aware of a way to make this faster, as this way im avoiding unnecessarily looping through the list
     */
    private static final List<Long> sortedCache = new ArrayList<>();
    /**
     * This queue holds the by the threadpool queued removal workloads for the Renderthread.
     * To keep performancy high the renderthreads removes up to 100 per frame.
     */
    private static final ConcurrentLinkedQueue<Long> deleteQueue = new ConcurrentLinkedQueue<>();
    /**
     * If this boolen is set to true it force clears all the buffers immediately
     */
    private static final AtomicBoolean forceclear = new AtomicBoolean(false);

    private static int canvasLoaded = -1;

    private static class WorkLoad {
        public Long chunk;
        public Collection<BlockPosWithColor> pos;

        public WorkLoad(Long chunk, Collection<BlockPosWithColor> pos) {
            this.chunk = chunk;
            this.pos = pos;
        }
    }

    private RenderOutlines() {
        //hide the constructor so nobody becomes bad ideas
    }

    /**
     * This function adds a Chunk into the queue to be rendered
     */
    public static void addChunk(Long chunk, Collection<BlockPosWithColor> pos) {
        workQueue.add(new WorkLoad(chunk, pos));
    }

    /**
     * This function removes a chunk from the already rendered cache
     */
    public static void removeChunk(Long number){
        deleteQueue.add(number);
    }

    /**
     * This function can be called to initiate a clear,
     * if force is set to true if completly whipes all Buffers and queues,
     * if force is set to false it only removes onloaded chunks from the active rendered queue
     */
    public static void clearChunks(boolean force){
        if(force){
            forceclear.set(true);
        }
        else{
            Set<Long> keys = new HashSet<>(chunkCache.keySet());
            keys.forEach(chunkPos->{
                if((Minecraft.getInstance().player.clientLevel.getChunkSource().getChunk(ChunkPos.getX(chunkPos),ChunkPos.getZ(chunkPos),false)==null)){
                    deleteQueue.add(chunkPos);
                }
            });
        }
    }

    public static synchronized void render(WorldRenderContext context) {
        if (canvasLoaded == -1) {
            canvasLoaded = FabricLoader.getInstance().isModLoaded("canvas") ? 1 : 0;
        }
        if(forceclear.get()){
            workQueue.clear();
            deleteQueue.clear();
            chunkCache.clear();
            sortedCache.clear();
            forceclear.set(false);
        }

        if (!SettingsStore.getInstance().get().isActive()) {
            return;
        }
        WorkLoad work = workQueue.poll();
        if(work!=null){
            BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
            bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
            work.pos.forEach(posWithColor -> {
                renderBlock(bufferBuilder, posWithColor, 1);
            });
            bufferBuilder.clear();
            VertexBuffer vertexBuffer = new VertexBuffer();
            vertexBuffer.bind();
            BufferBuilder.RenderedBuffer buffer = bufferBuilder.end();
            vertexBuffer.upload(buffer);
            VertexBuffer.unbind();
            chunkCache.put(work.chunk, vertexBuffer);
            sortedCache.add(0, work.chunk);
            bufferBuilder.discard();
        }
        int removed = 0;
        Long toRemove;
        while ((toRemove = deleteQueue.poll()) != null && removed < 100) {
            VertexBuffer vertexBuffer = chunkCache.get(toRemove);
            if(vertexBuffer!=null){
                vertexBuffer.close();
            }
            sortedCache.remove(toRemove);
            chunkCache.remove(toRemove);
            removed++;
        }

        if (chunkCache.size()>0) {
            Camera camera = context.camera();
            Vec3 cameraPos = camera.getPosition();

            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableTexture();

            PoseStack poseStack = RenderSystem.getModelViewStack();
            poseStack.pushPose();
            poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

            if (canvasLoaded == 1) { // canvas compat
                float f = camera.getXRot() * 0.017453292F;
                poseStack.mulPose(new Quaternionf(1.0 * sin(f / 2.0f), 0.0, 0.0, cos(f / 2.0f)));
                f = (camera.getYRot() + 180f) * 0.017453292F;
                poseStack.mulPose(new Quaternionf(0.0, 1.0 * sin(f / 2.0f), 0.0, cos(f / 2.0f)));
            }

            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            RenderSystem.applyModelViewMatrix();
            RenderSystem.depthFunc(GL11.GL_ALWAYS);
            int x = (int) context.camera().getPosition().x / 16;
            int z = (int) context.camera().getPosition().z / 16;
            double distance;
            double lastDistance = Double.MAX_VALUE;
            for (int i = 0; i < sortedCache.size(); i++) {
                VertexBuffer buf = chunkCache.get(sortedCache.get(i));
                if (buf != null) {
                    buf.bind();
                    buf.drawWithShader(poseStack.last().pose(), new Matrix4f(context.projectionMatrix()), RenderSystem.getShader());
                }
                //this is a semi-bubble-sort algorithm
                //it only loops through the array once per frame
                //this might take a couple frames to reach a sorted array but lower frame-times are more important that a perfectly sorted array
                //why sorting at all? because the depth buffer does not seem to do its job properly with debug lines, so closer chunks should be rendered later
                distance = distance(sortedCache.get(i), x, z);
                if (distance > lastDistance) {
                    Long temp = sortedCache.get(i - 1);
                    sortedCache.set(i - 1, sortedCache.get(i));
                    sortedCache.set(i, temp);
                }
                lastDistance = distance;
            }
            VertexBuffer.unbind();
            RenderSystem.depthFunc(GL11.GL_LEQUAL);

            poseStack.popPose();
            RenderSystem.applyModelViewMatrix();
        }
    }

    private static double distance(long l1, int x1, int z1) {
        int x2 = (int) l1;
        int z2 = (int) (l1 >> 32);
        //technically the sqrt is required for the distance, but it does not change the ordering so i can just skip it
        return (x2 - x1) * (x2 - x1) + (z2 - z1) * (z2 - z1);
    }

    private static void renderBlock(BufferBuilder buffer, BlockPosWithColor blockProps, float opacity) {
        final float size = 1.0f;
        final double x = blockProps.pos().getX(), y = blockProps.pos().getY(), z = blockProps.pos().getZ();

        final float red = blockProps.color().red() / 255f;
        final float green = blockProps.color().green() / 255f;
        final float blue = blockProps.color().blue() / 255f;

        buffer.vertex(x, y + size, z).color(red, green, blue, opacity).endVertex();
        buffer.vertex(x + size, y + size, z).color(red, green, blue, opacity).endVertex();
        buffer.vertex(x + size, y + size, z).color(red, green, blue, opacity).endVertex();
        buffer.vertex(x + size, y + size, z + size).color(red, green, blue, opacity).endVertex();
        buffer.vertex(x + size, y + size, z + size).color(red, green, blue, opacity).endVertex();
        buffer.vertex(x, y + size, z + size).color(red, green, blue, opacity).endVertex();
        buffer.vertex(x, y + size, z + size).color(red, green, blue, opacity).endVertex();
        buffer.vertex(x, y + size, z).color(red, green, blue, opacity).endVertex();

        // BOTTOM
        buffer.vertex(x + size, y, z).color(red, green, blue, opacity).endVertex();
        buffer.vertex(x + size, y, z + size).color(red, green, blue, opacity).endVertex();
        buffer.vertex(x + size, y, z + size).color(red, green, blue, opacity).endVertex();
        buffer.vertex(x, y, z + size).color(red, green, blue, opacity).endVertex();
        buffer.vertex(x, y, z + size).color(red, green, blue, opacity).endVertex();
        buffer.vertex(x, y, z).color(red, green, blue, opacity).endVertex();
        buffer.vertex(x, y, z).color(red, green, blue, opacity).endVertex();
        buffer.vertex(x + size, y, z).color(red, green, blue, opacity).endVertex();

        // Edge 1
        buffer.vertex(x + size, y, z + size).color(red, green, blue, opacity).endVertex();
        buffer.vertex(x + size, y + size, z + size).color(red, green, blue, opacity).endVertex();

        // Edge 2
        buffer.vertex(x + size, y, z).color(red, green, blue, opacity).endVertex();
        buffer.vertex(x + size, y + size, z).color(red, green, blue, opacity).endVertex();

        // Edge 3
        buffer.vertex(x, y, z + size).color(red, green, blue, opacity).endVertex();
        buffer.vertex(x, y + size, z + size).color(red, green, blue, opacity).endVertex();

        // Edge 4
        buffer.vertex(x, y, z).color(red, green, blue, opacity).endVertex();
        buffer.vertex(x, y + size, z).color(red, green, blue, opacity).endVertex();
    }
}
