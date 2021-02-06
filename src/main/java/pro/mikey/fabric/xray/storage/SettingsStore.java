package pro.mikey.fabric.xray.storage;

import pro.mikey.fabric.xray.StateSettings;

import java.lang.reflect.Type;

public class SettingsStore extends Store<StateSettings> {
  private static SettingsStore instance;
  private StateSettings settings;

  private SettingsStore() {
    super("settings");

    StateSettings settings = this.read();
    if (settings == null) {
      return;
    }

    this.settings = this.read();
  }

  static SettingsStore getInstance() {
    if (instance == null) {
      instance = new SettingsStore();
    }

    return instance;
  }

  @Override
  public StateSettings get() {
    return this.settings;
  }

  @Override
  Type getType() {
    return StateSettings.class;
  }
}
