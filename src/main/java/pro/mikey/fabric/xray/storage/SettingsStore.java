package pro.mikey.fabric.xray.storage;

import pro.mikey.fabric.xray.StateSettings;

import java.lang.reflect.Type;

public class SettingsStore extends Store<StateSettings> {
    private static SettingsStore instance;
    private final StateSettings settings;

    private SettingsStore() {
        super("settings");
        this.settings = this.read();
    }

    static SettingsStore getInstance() {
        if (instance == null) {
            instance = new SettingsStore();
        }

        return instance;
    }

    @Override
    public StateSettings providedDefault() {
        return new StateSettings();
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
