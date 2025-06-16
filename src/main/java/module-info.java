module peruvianpesto.schoolmanagementsystem {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.base; // Explicitly added for PropertyValueFactory support

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.sql;

    // Main package exports and opens
    opens peruvianpesto.schoolmanagementsystem to javafx.fxml;
    exports peruvianpesto.schoolmanagementsystem;

    // Database package
    exports peruvianpesto.database;
    opens peruvianpesto.database to javafx.fxml;

    // UI and Model packages needed for JavaFX reflection
    exports peruvianpesto.ui;
    opens peruvianpesto.ui to javafx.base, javafx.fxml;

    exports peruvianpesto.model;
    opens peruvianpesto.model to javafx.base, javafx.fxml;
}