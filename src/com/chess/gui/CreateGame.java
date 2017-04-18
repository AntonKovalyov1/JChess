package com.chess.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Created by Anton on 3/15/2017.
 */
public class CreateGame extends Stage {

    private int numberOfGames;
    private ColorChoice colorChoice;
    private Difficulty difficulty;

    public CreateGame(String title) {
        setTitle(title);
        numberOfGames = -1;
        colorChoice = ColorChoice.WHITE;
        difficulty = Difficulty.EASY;
        initialize();
    }

    public void initialize() {
        getIcons().add(new Image("images/mainIcon.jpg"));
        final TextField numberOfGamesField = new TextField();
        numberOfGamesField.setPrefWidth(110);
        final Label numberOfGamesLabel = new Label("Number of Games: ");
        numberOfGamesLabel.setPrefWidth(160);
        final HBox numberOfGamesHB = new HBox(10);
        numberOfGamesHB.getChildren().addAll(numberOfGamesLabel, numberOfGamesField);

        final ObservableList colorList = FXCollections.observableArrayList(ColorChoice.WHITE, ColorChoice.BLACK,
                ColorChoice.RANDOM);
        final Label colorLabel = new Label("Color: ");
        colorLabel.setPrefWidth(160);
        final ComboBox colorComboBox = new ComboBox(colorList);
        colorComboBox.setPrefWidth(110);
        colorComboBox.setValue(colorList.get(0));
        final HBox colorHB = new HBox(10);
        colorHB.getChildren().addAll(colorLabel, colorComboBox);

        final ObservableList difficultyList = FXCollections.observableArrayList(Difficulty.EASY, Difficulty.MEDIUM,
                Difficulty.HARD, Difficulty.INSANE, Difficulty.RANDOM);
        final Label difficultyLabel = new Label("Difficulty: ");
        difficultyLabel.setPrefWidth(160);
        final HBox difficultyHB = new HBox(10);
        final ComboBox difficultyComboBox = new ComboBox(difficultyList);
        difficultyComboBox.setPrefWidth(110);
        difficultyComboBox.setValue(difficultyList.get(0));
        difficultyHB.getChildren().addAll(difficultyLabel, difficultyComboBox);

        Text errorText = new Text("");
        errorText.setFill(Color.RED);

        final Button playButton = new Button("Play!");
        playButton.setDefaultButton(true);
        playButton.setOnAction(e -> {
            try {
                numberOfGames = Integer.parseInt(numberOfGamesField.getText());
                if (getNumberOfGames() < 1 || getNumberOfGames() > 100) {
                    throw new Exception();
                }
                else {
                    colorChoice = (ColorChoice) colorComboBox.getValue();
                    difficulty = (Difficulty) difficultyComboBox.getValue();
                    close();
                }
            }
            catch (Exception ex) {
                errorText.setText("Please enter a number from 1 to 100");
                numberOfGamesLabel.setTextFill(Color.RED);
                numberOfGames = -1;
            }
        });

        final Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> {
            numberOfGames = -1;
            close();
        });

        setOnCloseRequest(e -> {
            numberOfGames = -1;
            close();
        });

        final HBox buttonsHB = new HBox(5);
        buttonsHB.getChildren().addAll(playButton, cancelButton);
        buttonsHB.setAlignment(Pos.BOTTOM_RIGHT);
        buttonsHB.setPadding(new Insets(30, 10 ,0 ,0));

        VBox mainVB = new VBox(20);
        mainVB.setPadding(new Insets(30,10,10,20));
        mainVB.getChildren().addAll(numberOfGamesHB, colorHB, difficultyHB, errorText, buttonsHB);

        Scene scene = new Scene(mainVB);
        setScene(scene);
        setResizable(false);
        initModality(Modality.APPLICATION_MODAL);
        showAndWait();
    }

    public int getNumberOfGames() {
        return numberOfGames;
    }

    public ColorChoice getColorChoice() {
        return colorChoice;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }
}
