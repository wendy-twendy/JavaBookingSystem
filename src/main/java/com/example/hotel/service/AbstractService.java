package com.example.hotel.service;

import com.example.hotel.persistence.FileRepository;

import java.util.List;
import java.util.Optional;

/**
 * Abstract base class for services.
 * Provides common CRUD operations shared by all service classes,
 * reducing boilerplate code duplication.
 *
 * @param <T> the entity type managed by this service
 */
public abstract class AbstractService<T> {

    protected final FileRepository<T, String> repository;

    protected AbstractService(FileRepository<T, String> repository) {
        this.repository = repository;
    }

    /**
     * Get all entities.
     */
    public List<T> getAll() {
        return repository.findAll();
    }

    /**
     * Find an entity by ID.
     */
    public Optional<T> findById(String id) {
        return repository.findById(id);
    }

    /**
     * Delete an entity by ID.
     * @return true if deleted, false if not found
     */
    public boolean delete(String id) {
        return repository.delete(id);
    }

    /**
     * Get total count of entities.
     */
    public long count() {
        return repository.count();
    }
}
