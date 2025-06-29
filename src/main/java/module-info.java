module com.hasib.bloodbank {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires mysql.connector.java;
    requires java.mail;
    requires activation;
    requires com.jfoenix;

    // WebSocket and JSON processing libraries
    requires Java.WebSocket;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    requires okhttp3;

    // Remove transitive modifiers - they're not needed here
    requires java.desktop;
    requires java.base;

    // Combine the opens declarations to avoid duplicates
    opens com.hasib.bloodbank to javafx.fxml, com.fasterxml.jackson.databind;

    exports com.hasib.bloodbank;
    exports com.hasib.bloodbank.singleton;
    opens com.hasib.bloodbank.singleton to javafx.fxml, com.fasterxml.jackson.databind;

    exports com.hasib.bloodbank.server.entity;
    opens com.hasib.bloodbank.server.entity to javafx.fxml, com.fasterxml.jackson.databind;

    exports com.hasib.bloodbank.server.model;
    opens com.hasib.bloodbank.server.model to javafx.fxml, com.fasterxml.jackson.databind;

    exports com.hasib.bloodbank.utils;
    opens com.hasib.bloodbank.utils to javafx.fxml, com.fasterxml.jackson.databind;
}