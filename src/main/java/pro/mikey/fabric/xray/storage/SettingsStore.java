package pro.mikey.fabric.xray.storage;

import pro.mikey.fabric.xray.StateSettings;

import java.lang.reflect.Type;

public class SettingsStore extends Store<StateSettings> {
    private StateSettings settings = new StateSettings();
    private static SettingsStore instance;

    public static SettingsStore getInstance() {
        if (instance == null) {
            instance = new SettingsStore();
        }

        return instance;
    }

    private SettingsStore() {
        super("settings");

        StateSettings settings = this.read();
        if (settings == null) {
            return;
        }

        this.settings = this.read();
    }

    @Override
    public StateSettings get() {
        return settings;
    }

    @Override
    Type getType() {
        return StateSettings.class;
    }
}
