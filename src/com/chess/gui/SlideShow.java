package com.chess.gui;

import javafx.animation.TranslateTransition;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import com.chess.gui.GUI.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Anton on 3/16/2017.
 */
public class SlideShow extends ScrollPane {

    private int currentViewIndex = 0;
    private final HBox gamePanes = new HBox();
    private final List<GamePane> activeGamePanes = new ArrayList<>();
    private final TranslateTransition slideTransition = new TranslateTransition(Duration.millis(600), getGamePanes());

    public SlideShow(final List<GamePane> gamePanes) {
        addGamePanes(gamePanes);
        setContent(getGamePanes());
        setPannable(false);
        prefWidthProperty().bind(gamePanes.get(0).widthProperty());
        prefHeightProperty().bind(gamePanes.get(0).heightProperty());
        minWidthProperty().bind(gamePanes.get(0).widthProperty());
        minHeightProperty().bind(gamePanes.get(0).heightProperty());
        maxWidthProperty().bind(gamePanes.get(0).widthProperty());
        maxHeightProperty().bind(gamePanes.get(0).heightProperty());
        setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    }

    public void addGamePanes(List<GamePane> list) {
        this.getGamePanes().getChildren().addAll(list);
        getActiveGamePanes().addAll(list);
    }

    public void removeFromView(final int index) {
        GamePane gamePane = (GamePane)getGamePanes().getChildren().get(index);
        getGamePanes().getChildren().remove(index);
        getGamePanes().getChildren().add(0, gamePane);
        this.currentViewIndex = getCurrentViewIndex() + 1;
        getActiveGamePanes().remove(index);
    }

    public void showNext() {
        if (activeGamePanes.size() > 1) {
            if (getCurrentViewIndex() == getGamePanes().getChildren().size() - 1) {
                getSlideTransition().setByX(getViewableWidth() * getActiveGamePanes().size() - getViewableWidth());
                getSlideTransition().setCycleCount(1);
                this.currentViewIndex = getCurrentViewIndex() - (getActiveGamePanes().size() - 1);
            } else {
                getSlideTransition().setByX(-getViewableWidth());
                getSlideTransition().setCycleCount(1);
                this.currentViewIndex = getCurrentViewIndex() + 1;
            }
            getSlideTransition().play();
        }
    }

    public void showPrevious() {
        if (activeGamePanes.size() > 1) {
            if (getCurrentViewIndex() == this.getGamePanes().getChildren().size() - (this.getActiveGamePanes().size() - 1)) {
                getSlideTransition().setByX(-getViewableWidth() * getActiveGamePanes().size() + getViewableWidth());
                getSlideTransition().setCycleCount(1);
                currentViewIndex = getGamePanes().getChildren().size();
            } else {
                getSlideTransition().setByX(getViewableWidth());
                getSlideTransition().setCycleCount(1);
                currentViewIndex = getCurrentViewIndex() - 1;
            }
            getSlideTransition().play();
        }
    }

    public double getViewableWidth() {
        return widthProperty().get();
    }

    public int getCurrentViewIndex() {
        return currentViewIndex;
    }

    public HBox getGamePanes() {
        return gamePanes;
    }

    public List<GamePane> getActiveGamePanes() {
        return activeGamePanes;
    }

    public TranslateTransition getSlideTransition() {
        return slideTransition;
    }

    public int getCurrentActiveGameIndex() {
        return currentViewIndex - (gamePanes.getChildren().size() - activeGamePanes.size());
    }

    public void replaceGames(List<GamePane> list) {
        getGamePanes().getChildren().clear();
        getActiveGamePanes().clear();
        addGamePanes(list);
    }
}
