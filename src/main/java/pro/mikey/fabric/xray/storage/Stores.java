package pro.mikey.fabric.xray.storage;

public class Stores {
  public static final SettingsStore SETTINGS = SettingsStore.getInstance();
  public static final BlockStore BLOCKS = BlockStore.getInstance();

  public static void load() {
    SETTINGS.read();
    BLOCKS.read();
  }
}
