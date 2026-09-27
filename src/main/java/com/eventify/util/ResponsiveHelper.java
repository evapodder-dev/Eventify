package com.eventify.util;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * Utility for binding TableColumn widths to a percentage of the TableView width.
 * Demonstrates: JavaFX property binding and responsive UI design using
 * property constraints relative to the parent container's width.
 */
public final class ResponsiveHelper {

    private ResponsiveHelper() {
    }

    /**
     * Binds each column's prefWidth to a percentage of the TableView's width.
     * This ensures the table columns resize proportionally when the window is resized,
     * demonstrating layout responsiveness with property constraints.
     *
     * @param table       the TableView to bind columns for
     * @param percentages an array of percentage values (0.0 to 1.0) for each column
     */
    @SafeVarargs
    public static <T> void bindColumnWidths(TableView<T> table, double... percentages) {
        var columns = table.getColumns();
        for (int i = 0; i < columns.size() && i < percentages.length; i++) {
            final double pct = percentages[i];
            columns.get(i).prefWidthProperty().bind(
                    table.widthProperty().multiply(pct)
            );
        }
    }
}
