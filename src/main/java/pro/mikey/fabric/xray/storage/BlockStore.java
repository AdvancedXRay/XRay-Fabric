package pro.mikey.fabric.xray.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import pro.mikey.fabric.xray.cache.BlockSearchCache;
import pro.mikey.fabric.xray.records.BlockEntry;
import pro.mikey.fabric.xray.records.BlockGroup;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class BlockStore extends Store<List<BlockGroup>> {
    private static BlockStore instance;
    private final BlockSearchCache cache = new BlockSearchCache();
    private final List<BlockGroup> blockEntries;

    private BlockStore() {
        super("blocks");

        this.blockEntries = this.read();
        this.updateCache(this.blockEntries); // ensure the cache is up to date
    }

    static BlockStore getInstance() {
        if (instance == null) {
            instance = new BlockStore();
        }

        return instance;
    }

    public void updateCache() {
        this.updateCache(this.get());
    }

    private void updateCache(List<BlockGroup> data) {
        this.cache.processGroupedList(data);
    }

    public BlockSearchCache getCache() {
        return this.cache;
    }

    @Override
    public List<BlockGroup> get() {
        return this.blockEntries;
    }

    @Override
    public Gson getGson() {
        return new GsonBuilder()
                .registerTypeAdapter(BlockEntry.class, new BlockEntry.Serializer())
                .setPrettyPrinting()
                .create();
    }

    @Override
    public List<BlockGroup> providedDefault() {
        return new ArrayList<>();
    }

    @Override
    Type getType() {
        return new TypeToken<List<BlockGroup>>() {
        }.getType();
    }
}
