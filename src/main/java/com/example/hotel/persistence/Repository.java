package com.example.hotel.persistence;

import java.util.List;
import java.util.Optional;

/**
 * Generic repository interface defining the data access contract.
 *
 * @param <T>  the entity type
 * @param <ID> the ID type
 */
public interface Repository<T, ID> {

    /**
     * Returns all entities.
     */
    List<T> findAll();

    /**
     * Finds an entity by its ID.
     *
     * @param id the ID to search for
     * @return an Optional containing the entity if found
     */
    Optional<T> findById(ID id);

    /**
     * Saves an entity (insert or update).
     *
     * @param entity the entity to save
     * @return the saved entity
     */
    T save(T entity);

    /**
     * Deletes an entity by its ID.
     *
     * @param id the ID of the entity to delete
     * @return true if deleted, false if not found
     */
    boolean delete(ID id);

    /**
     * Checks if an entity exists by its ID.
     *
     * @param id the ID to check
     * @return true if exists
     */
    boolean existsById(ID id);

    /**
     * Returns the count of all entities.
     */
    long count();
}
