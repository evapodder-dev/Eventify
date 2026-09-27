package com.eventify.model;

import java.util.Map;

/**
 * Interface for entities that can export their data to a Map for JSON/CSV serialization.
 * Demonstrates: Java Interfaces and abstraction of data export behavior.
 */
public interface Exportable {

    /**
     * Converts the entity's key fields to a Map for serialization.
     */
    Map<String, Object> toExportMap();

    /**
     * Returns a human-readable summary string for export previews.
     */
    String toExportSummary();
}
