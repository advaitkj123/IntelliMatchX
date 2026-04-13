package com.intellimatch.ui;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.kordamp.bootstrapfx.BootstrapFX;

public final class SceneNavigator {

    private SceneNavigator() {
    }

    public static void setScenePreservingWindow(Stage stage, Parent root) {
        boolean wasMaximized = stage.isMaximized();
        double currentWidth = stage.getWidth();
        double currentHeight = stage.getHeight();

        Scene scene = stage.getScene();
        if (scene == null) {
            scene = new Scene(root);
            scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
            stage.setScene(scene);
        } else {
            scene.setRoot(root);
            if (!scene.getStylesheets().contains(BootstrapFX.bootstrapFXStylesheet())) {
                scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
            }
        }

        if (wasMaximized) {
            stage.setMaximized(true);
            return;
        }

        if (currentWidth > 0 && currentHeight > 0) {
            stage.setWidth(currentWidth);
            stage.setHeight(currentHeight);
        }
    }
}
