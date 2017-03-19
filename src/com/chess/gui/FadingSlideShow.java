package com.chess.gui;

import javafx.animation.SequentialTransition;
import javafx.scene.control.ScrollPane;
import com.chess.gui.GUI.*;
import java.util.List;

/**
 * Created by Anton on 3/18/2017.
 */
public class FadingSlideShow extends ScrollPane {

    private final List<GamePane> gamesList;
    private final int currentGameIndex;

    public FadingSlideShow(final List<GamePane> list) {
        this.gamesList = list;
        this.currentGameIndex = 0;
        initialize();
    }

    public void bindToSize(GamePane gamePane) {
        prefWidthProperty().bind(gamePane.widthProperty());
        prefHeightProperty().bind(gamePane.heightProperty());
        minWidthProperty().bind(gamePane.widthProperty());
        minHeightProperty().bind(gamePane.heightProperty());
        maxWidthProperty().bind(gamePane.widthProperty());
        maxHeightProperty().bind(gamePane.heightProperty());
    }

    private void initialize() {

    }
}
