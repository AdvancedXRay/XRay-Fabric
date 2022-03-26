package pro.mikey.fabric.xray.storage;

public interface Stores {
    SettingsStore SETTINGS = SettingsStore.getInstance();
    BlockStore BLOCKS = BlockStore.getInstance();

    static void load() {
        SETTINGS.read();
        BLOCKS.read();
    }
}
