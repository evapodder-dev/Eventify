package com.eventify.model;

/**
 * Interface for entities that can be searched by keyword.
 * Demonstrates: Java Interfaces with default methods and polymorphism.
 */
public interface Searchable {

    /**
     * Returns true if this entity matches the given search keyword.
     */
    boolean matchesKeyword(String keyword);

    /**
     * Default method — returns a search-friendly label for this entity.
     */
    default String getSearchLabel() {
        return this.toString();
    }
}
