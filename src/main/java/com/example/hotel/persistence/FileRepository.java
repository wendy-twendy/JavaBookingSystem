package com.example.hotel.persistence;

import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Generic file-based repository implementation using JSON storage.
 *
 * @param <T>  the entity type
 * @param <ID> the ID type
 */
public class FileRepository<T, ID> implements Repository<T, ID> {

    private final Path filePath;
    private final Function<T, ID> idExtractor;
    private final Type listType;
    private final List<T> cache;

    /**
     * Creates a new FileRepository.
     *
     * @param filePath    the path to the JSON file
     * @param idExtractor function to extract ID from entity
     * @param elementType the class of the entity type
     */
    public FileRepository(Path filePath, Function<T, ID> idExtractor, Class<T> elementType) {
        this.filePath = filePath;
        this.idExtractor = idExtractor;
        this.listType = TypeToken.getParameterized(List.class, elementType).getType();
        this.cache = new ArrayList<>();
        load();
    }

    /**
     * Loads data from the JSON file into the cache.
     */
    private void load() {
        cache.clear();
        try {
            if (Files.exists(filePath)) {
                String json = Files.readString(filePath);
                if (json != null && !json.isBlank()) {
                    List<T> loaded = JsonUtils.fromJson(json, listType);
                    if (loaded != null) {
                        cache.addAll(loaded);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load data from " + filePath, e);
        }
    }

    /**
     * Saves the cache to the JSON file.
     */
    private void persist() {
        try {
            Files.createDirectories(filePath.getParent());
            String json = JsonUtils.toJson(cache);
            Files.writeString(filePath, json);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save data to " + filePath, e);
        }
    }

    /**
     * Reloads data from file (discards unsaved changes).
     */
    public void refresh() {
        load();
    }

    /**
     * Forces a save to file.
     */
    public void flush() {
        persist();
    }

    @Override
    public List<T> findAll() {
        return new ArrayList<>(cache);
    }

    @Override
    public Optional<T> findById(ID id) {
        return cache.stream()
                .filter(entity -> idExtractor.apply(entity).equals(id))
                .findFirst();
    }

    @Override
    public T save(T entity) {
        ID id = idExtractor.apply(entity);
        Optional<T> existing = findById(id);
        if (existing.isPresent()) {
            // Update: remove old and add new
            cache.removeIf(e -> idExtractor.apply(e).equals(id));
        }
        cache.add(entity);
        persist();
        return entity;
    }

    @Override
    public boolean delete(ID id) {
        boolean removed = cache.removeIf(entity -> idExtractor.apply(entity).equals(id));
        if (removed) {
            persist();
        }
        return removed;
    }

    @Override
    public boolean existsById(ID id) {
        return cache.stream()
                .anyMatch(entity -> idExtractor.apply(entity).equals(id));
    }

    @Override
    public long count() {
        return cache.size();
    }
}
