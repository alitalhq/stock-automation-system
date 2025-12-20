package com.example.stockautomationsystem.service;

import com.example.stockautomationsystem.exception.*;
import com.example.stockautomationsystem.model.Product;
import com.example.stockautomationsystem.model.LogEntry;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JsonService {
    private static final String PRODUCT_FILE = "inventory.json";
    private static final String LOG_FILE = "logs.json";

    private final ObjectMapper mapper = new ObjectMapper();

    public JsonService() {
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public void saveData(List<Product> products) {
        try {
            mapper.writeValue(new File(PRODUCT_FILE), products);
        } catch (IOException e) {
            // Alt seviye hatayı yakalayıp daha anlamlı bir hata tipine dönüştürüyoruz
            throw new DataPersistenceException("Failed to save data to JSON file.", e);
        }
    }

    public List<Product> loadData() {
        try {
            File file = new File(PRODUCT_FILE);
            if (!file.exists()) return new ArrayList<>();
            return mapper.readValue(file, new TypeReference<List<Product>>() {});
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public void saveLogs(List<LogEntry> logs) {
        try {
            mapper.writeValue(new File(LOG_FILE), logs);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<LogEntry> loadLogs() {
        try {
            File file = new File(LOG_FILE);
            if (!file.exists()) return new ArrayList<>();
            return mapper.readValue(file, new TypeReference<List<LogEntry>>() {});
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}