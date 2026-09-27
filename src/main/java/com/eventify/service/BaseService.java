package com.eventify.service;

import com.eventify.dao.CrudDAO;

import java.util.List;
import java.util.Optional;

/**
 * Abstract base service providing common CRUD template methods.
 * Demonstrates: Abstract classes in the service layer, Template Method pattern,
 * and generics for type-safe reuse across Event, Participant, Schedule, and Task services.
 *
 * @param <T>  Model entity type
 * @param <ID> Identifier type
 */
public abstract class BaseService<T, ID> {

    /**
     * Subclasses must provide their DAO instance.
     */
    protected abstract CrudDAO<T, ID> getDao();

    /**
     * Subclasses must implement domain-specific validation.
     */
    protected abstract void validate(T entity);

    /**
     * Template method: validates then inserts.
     */
    public boolean create(T entity) {
        validate(entity);
        return getDao().insert(entity);
    }

    /**
     * Template method: validates then updates.
     */
    public boolean update(T entity) {
        validate(entity);
        return getDao().update(entity);
    }

    public boolean delete(ID id) {
        return getDao().delete(id);
    }

    public Optional<T> findById(ID id) {
        return getDao().findById(id);
    }

    public List<T> getAll() {
        return getDao().findAll();
    }
}
