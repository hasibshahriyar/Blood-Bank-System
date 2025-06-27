package com.hasib.bloodbank.utils;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/**
 * Diagnostic tool to test FXML file loading for profile views
 */
public class ProfileNavigationTest {

    public static void testFXMLLoading() {
        System.out.println("=== Profile Navigation Test ===");

        // Test req-user-profile-view.fxml
        testFXMLFile("req-user-profile-view.fxml", "Request Blood Profile View");

        // Test don-user-profile-view.fxml
        testFXMLFile("don-user-profile-view.fxml", "Donate Blood Profile View");

        System.out.println("=== Test Complete ===");
    }

    private static void testFXMLFile(String fxmlFileName, String description) {
        System.out.println("\nTesting " + description + " (" + fxmlFileName + "):");

        try {
            // Try to get the resource URL
            URL resourceUrl = ProfileNavigationTest.class.getResource("/com/hasib/bloodbank/" + fxmlFileName);
            if (resourceUrl == null) {
                System.out.println("❌ FXML file not found in resources: " + fxmlFileName);
                return;
            }

            System.out.println("✅ FXML file found at: " + resourceUrl);

            // Try to load the FXML
            FXMLLoader loader = new FXMLLoader(resourceUrl);
            Parent root = loader.load();

            System.out.println("✅ FXML loaded successfully");
            System.out.println("   Controller: " + loader.getController().getClass().getSimpleName());
            System.out.println("   Root node: " + root.getClass().getSimpleName());

        } catch (IOException e) {
            System.out.println("❌ Failed to load FXML: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("❌ Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        testFXMLLoading();
    }
}
