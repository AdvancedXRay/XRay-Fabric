package pro.mikey.fabric.xray.storage;

public class Stores {
  public static final SettingsStore SETTINGS = SettingsStore.getInstance();
  public static final BlockStore BLOCKS = BlockStore.getInstance();

  public static void reload() {
    SETTINGS.read();
    BLOCKS.read();
  }

  public static void write() {
    SETTINGS.write();
    BLOCKS.write();
  }
}
