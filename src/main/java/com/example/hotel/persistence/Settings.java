package com.example.hotel.persistence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Singleton configuration manager for application settings.
 * Loads from data/settings.json and provides default values if missing.
 */
public final class Settings {

    private static final Path SETTINGS_FILE = Paths.get("data", "settings.json");

    private static volatile Settings instance;

    private double vatRate;
    private String currency;
    private String hotelName;
    private String defaultRefundPolicy;

    private Settings() {
        load();
    }

    /**
     * Returns the singleton instance of Settings.
     */
    public static synchronized Settings getInstance() {
        if (instance == null) {
            instance = new Settings();
        }
        return instance;
    }

    /**
     * Loads settings from the JSON file, using defaults if file is missing.
     */
    private void load() {
        // Set defaults first
        vatRate = 0.10;
        currency = "USD";
        hotelName = "Grand Hotel";
        defaultRefundPolicy = "TIERED";

        try {
            if (Files.exists(SETTINGS_FILE)) {
                String json = Files.readString(SETTINGS_FILE);
                if (json != null && !json.isBlank()) {
                    SettingsData data = JsonUtils.fromJson(json, SettingsData.class);
                    if (data != null) {
                        if (data.vatRate != null) {
                            this.vatRate = data.vatRate;
                        }
                        if (data.currency != null) {
                            this.currency = data.currency;
                        }
                        if (data.hotelName != null) {
                            this.hotelName = data.hotelName;
                        }
                        if (data.defaultRefundPolicy != null) {
                            this.defaultRefundPolicy = data.defaultRefundPolicy;
                        }
                    }
                }
            }
        } catch (IOException e) {
            // Use defaults on error
        }
    }

    /**
     * Saves current settings to the JSON file.
     */
    public void save() {
        try {
            Files.createDirectories(SETTINGS_FILE.getParent());
            SettingsData data = new SettingsData();
            data.vatRate = this.vatRate;
            data.currency = this.currency;
            data.hotelName = this.hotelName;
            data.defaultRefundPolicy = this.defaultRefundPolicy;
            String json = JsonUtils.toJson(data);
            Files.writeString(SETTINGS_FILE, json);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save settings to " + SETTINGS_FILE, e);
        }
    }

    /**
     * Reloads settings from file.
     */
    public void reload() {
        load();
    }

    public double getVatRate() {
        return vatRate;
    }

    public void setVatRate(double vatRate) {
        this.vatRate = vatRate;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getHotelName() {
        return hotelName;
    }

    public void setHotelName(String hotelName) {
        this.hotelName = hotelName;
    }

    public String getDefaultRefundPolicy() {
        return defaultRefundPolicy;
    }

    public void setDefaultRefundPolicy(String defaultRefundPolicy) {
        this.defaultRefundPolicy = defaultRefundPolicy;
    }

    /**
     * Internal class for JSON serialization.
     */
    private static class SettingsData {
        Double vatRate;
        String currency;
        String hotelName;
        String defaultRefundPolicy;
    }
}
