module com.intellimatch {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.swing;
    requires jdk.jsobject;
    requires java.logging;
    requires java.prefs;
    requires org.kordamp.bootstrapfx.core;

    opens com.intellimatch to javafx.fxml;
    opens com.intellimatch.ui.controller to javafx.fxml;

    exports com.intellimatch;
    exports com.intellimatch.model;
    exports com.intellimatch.factory;
    exports com.intellimatch.observer;
    exports com.intellimatch.strategy;
    exports com.intellimatch.engine;
    exports com.intellimatch.service;
    exports com.intellimatch.ui.controller;
}
