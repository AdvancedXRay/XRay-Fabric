package pro.mikey.fabric.xray.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import net.minecraft.client.Minecraft;
import pro.mikey.fabric.xray.XRay;

import java.io.*;
import java.lang.reflect.Type;

public abstract class Store<T> {
    private static final String CONFIG_PATH = String.format("%s/config/%s", Minecraft.getInstance().gameDirectory, XRay.MOD_ID);

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

        try (FileReader reader = new FileReader(this.file)) {
            return gson.fromJson(reader, this.getType());
        } catch (JsonIOException | JsonSyntaxException e) {
            XRay.LOGGER.fatal("Fatal error with json loading on {}.json", this.name, e);
        } catch (IOException ignored) {
            // Write a blank version of the file
            this.justCreated = true;
            if (new File(CONFIG_PATH).mkdirs()) {
                this.write(true);
            }
        }

        return this.providedDefault();
    }

    public void write() {
        this.write(false);
    }

    private void write(boolean firstWrite) {
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
