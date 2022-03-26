package pro.mikey.fabric.xray.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import net.minecraft.client.MinecraftClient;
import pro.mikey.fabric.xray.XRay;

import java.io.*;
import java.lang.reflect.Type;

public abstract class Store<T> {
    private static final String CONFIG_PATH = String.format("%s/config/%s", MinecraftClient.getInstance().runDirectory, XRay.MOD_ID);

    private final String name;
    private final String file;

    public boolean justCreated = false;

    Store(String name) {
        this.name = name;
        this.file = String.format("%s/%s.json", CONFIG_PATH, this.name);

        this.read();
    }

    public Gson getGson() {
        return new GsonBuilder().setPrettyPrinting().create();
    }

    public T read() {
        Gson gson = this.getGson();

        try {
            try {
                return gson.fromJson(new FileReader(this.file), this.getType());
            } catch (JsonIOException | JsonSyntaxException e) {
                XRay.LOGGER.fatal("Fatal error with json loading on {}.json", this.name, e);
            }
        } catch (FileNotFoundException ignored) {
            this.justCreated = true;

            // Write a blank version of the file
            if (new File(CONFIG_PATH).mkdirs()) {
                this.write(true);
            }
        }

        return this.providedDefault();
    }

    public void write() {
        this.write(false);
    }

    private void write(Boolean firstWrite) {
        Gson gson = this.getGson();

        try {
            try (FileWriter writer = new FileWriter(this.file)) {
                gson.toJson(firstWrite ? this.providedDefault() : this.get(), writer);
                writer.flush();
            }
        } catch (IOException | JsonIOException e) {
            XRay.LOGGER.catching(e);
        }
    }

    public abstract T providedDefault();

    public abstract T get();

    abstract Type getType();
}
