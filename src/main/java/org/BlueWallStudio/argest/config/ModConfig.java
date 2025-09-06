package org.BlueWallStudio.argest.config;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ModConfig {
    private static ModConfig instance;
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    private static final String CONFIG_FILENAME = "argest.json";
    private static final File CONFIG_FILE = new File(
            FabricLoader.getInstance().getConfigDir().toFile(), CONFIG_FILENAME);

    // Debug settings
    public boolean showParticles = true;

    // Performance settings (in ticks)
    public int maxPacketLifetime = 100; // 5 seconds (assuming 20 TPS)
    public int signalProcessingDelay = 2; // Every 2 ticks
    public int maxPacketsPerTick = 100;
    public int signalEncodingDelay = 2; // Every 2 ticks

    private ModConfig() {
        // Private constructor for Singleton
    }

    public static ModConfig getInstance() {
        if (instance != null) {
            return instance;
        }

        synchronized (ModConfig.class) {
            if (instance == null) {
                instance = load();
            }
            return instance;
        }
    }

    private static ModConfig load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE, StandardCharsets.UTF_8)) {
                ModConfig loaded = GSON.fromJson(reader, ModConfig.class);
                if (loaded != null) {
                    loaded.validate();
                    return loaded;
                }
            } catch (IOException | JsonSyntaxException e) {
                System.err.println("Failed to load Argest config: " + e.getMessage());
                System.err.println("Using default configuration");
            }
        }

        ModConfig config = new ModConfig();
        config.validate();
        config.save();
        return config;
    }

    public void save() {
        try {
            // Create config directory if doesn't exist
            File configDir = CONFIG_FILE.getParentFile();
            if (!configDir.exists() && !configDir.mkdirs()) {
                throw new IOException("Could not create config directory: " + configDir);
            }

            try (FileWriter writer = new FileWriter(CONFIG_FILE, StandardCharsets.UTF_8)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            System.err.println("Failed to save Argest config: " + e.getMessage());
        }
    }

    private void validate() {
        // Config values correction and validation
        maxPacketLifetime = Math.max(-1, maxPacketLifetime);
        signalProcessingDelay = Math.max(1, signalProcessingDelay);
        maxPacketsPerTick = Math.max(-1, maxPacketsPerTick);
        signalEncodingDelay = Math.max(1, signalEncodingDelay);
    }

    public void reload() {
        synchronized (ModConfig.class) {
            instance = load();
        }
    }

    @Override
    public String toString() {
        return GSON.toJson(this);
    }
}
