package com.chess.gui;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Created by Anton on 2/1/2017.
 */
public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) {
        primaryStage = new GUI();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
