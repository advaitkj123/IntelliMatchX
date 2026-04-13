package com.intellimatch.ui;

import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.prefs.Preferences;

/**
 * Handles first-launch responsive sizing and persists user-resized window bounds.
 */
public final class WindowManager {

    private static final String KEY_WIDTH = "window.width";
    private static final String KEY_HEIGHT = "window.height";
    private static final String KEY_MAXIMIZED = "window.maximized";

    private static final Preferences PREFS = Preferences.userNodeForPackage(WindowManager.class);

    private WindowManager() {
    }

    public static void configureAndRestore(Stage stage) {
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();

        stage.setMinWidth(Math.min(1000.0, bounds.getWidth() * 0.9));
        stage.setMinHeight(Math.min(640.0, bounds.getHeight() * 0.85));

        boolean restored = applySavedBoundsIfValid(stage, bounds);
        if (!restored) {
            applyResponsiveBounds(stage, bounds);
        }

        stage.setOnHiding(event -> persist(stage));
    }

    private static boolean applySavedBoundsIfValid(Stage stage, Rectangle2D bounds) {
        boolean maximized = PREFS.getBoolean(KEY_MAXIMIZED, false);
        double width = PREFS.getDouble(KEY_WIDTH, -1.0);
        double height = PREFS.getDouble(KEY_HEIGHT, -1.0);

        if (maximized) {
            stage.setMaximized(true);
            return true;
        }

        if (width <= 0 || height <= 0) {
            return false;
        }

        if (width > bounds.getWidth() || height > bounds.getHeight()) {
            return false;
        }

        stage.setWidth(width);
        stage.setHeight(height);
        stage.centerOnScreen();
        return true;
    }

    private static void applyResponsiveBounds(Stage stage, Rectangle2D bounds) {
        // Keep startup window proportional to the current device display ratio.
        double width = bounds.getWidth() * 0.94;
        double height = bounds.getHeight() * 0.94;

        stage.setWidth(Math.max(width, stage.getMinWidth()));
        stage.setHeight(Math.max(height, stage.getMinHeight()));
        stage.centerOnScreen();
    }

    private static void persist(Stage stage) {
        PREFS.putBoolean(KEY_MAXIMIZED, stage.isMaximized());
        if (!stage.isMaximized()) {
            PREFS.putDouble(KEY_WIDTH, stage.getWidth());
            PREFS.putDouble(KEY_HEIGHT, stage.getHeight());
        }
    }
}
