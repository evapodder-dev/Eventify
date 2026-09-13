package com.eventify.dao;

import java.util.List;
import java.util.Optional;

/**
 * Generic DAO interface demonstrating Java interfaces and generics.
 *
 * @param <T>  Model entity type
 * @param <ID> Identifier type
 */
public interface CrudDAO<T, ID> {
    boolean insert(T entity);
    boolean update(T entity);
    boolean delete(ID id);
    Optional<T> findById(ID id);
    List<T> findAll();
}
