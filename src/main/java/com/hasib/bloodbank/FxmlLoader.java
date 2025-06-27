package com.hasib.bloodbank;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

public class FxmlLoader {
    private static AnchorPane anchorPane;
    public static AnchorPane getAnchorPane(String filename) throws IOException {
        URL url= BloodBankApplication.class.getResource(filename);
        if (url==null){
            throw new FileNotFoundException("file not found");
        }
        FXMLLoader fxmlLoader = new FXMLLoader();
        anchorPane= FXMLLoader.load(url);
        return anchorPane;

    }



}
