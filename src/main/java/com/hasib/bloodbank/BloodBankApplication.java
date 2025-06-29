package com.hasib.bloodbank;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Objects;

public class BloodBankApplication extends Application {
    public static void main(String[] args) {
        // Note: Database tables should be created by running blood_donation_database.sql
        // No need to create tables programmatically since we have a complete SQL schema
        System.out.println("🩸 Blood Bank Application Starting...");
        System.out.println("📋 Make sure to run blood_donation_database.sql to set up the database");

        launch();
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("login-view.fxml")));
        primaryStage.setTitle("Blood Bank Management System");
        primaryStage.setScene(new Scene(root));
        primaryStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("img/Rokto2.png"))));
        primaryStage.setResizable(false);
        primaryStage.show();

        System.out.println("✅ Blood Bank Application launched successfully");
    }
}