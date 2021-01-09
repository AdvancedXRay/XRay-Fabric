package pro.mikey.fabric.xray.storage;

import com.google.gson.reflect.TypeToken;
import pro.mikey.fabric.xray.cache.BlockSearchCache;
import pro.mikey.fabric.xray.records.BlockGroup;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class BlockStore extends Store<List<BlockGroup>> {
    private List<BlockGroup> blockEntries = new ArrayList<>();
    public final BlockSearchCache cache = new BlockSearchCache();

    private static BlockStore instance;

    public static BlockStore getInstance() {
        if (instance == null) {
            instance = new BlockStore();
        }

        return instance;
    }

    private BlockStore() {
        super("blocks");

        List<BlockGroup> entries = this.read();
        if (entries == null) {
            return;
        }

        this.blockEntries = entries;
    }

    public void updateCache(List<BlockGroup> data) {
        this.cache.processGroupedList(data);
    }

    @Override
    public List<BlockGroup> get() {
        return this.blockEntries;
    }

    @Override
    Type getType() {
        return new TypeToken<List<BlockGroup>>() {}.getType();
    }
}
