package com.chess.gui;

import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import com.chess.gui.GUI.*;

import java.util.List;

/**
 * Created by Anton on 3/16/2017.
 */
public class SlideShow extends ScrollPane {

    private final int viewableWidth;
    private int currentViewIndex = 0;
    private final HBox nodes = new HBox();
    private final List<Game> activeNodes;
    private final TranslateTransition slideTransition = new TranslateTransition(Duration.millis(400), getNodes());

    public SlideShow(final List<Game> nodes, final int viewableWidth, final int viewableHeight) {
        getNodes().getChildren().addAll(nodes);
        this.activeNodes = nodes;
        this.viewableWidth = viewableWidth;
        setContent(getNodes());
        setPannable(false);
        setMaxSize(viewableWidth, viewableHeight);
        setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    }

    public void addNodes(List<Game> list) {
        this.getNodes().getChildren().addAll(list);
        getActiveNodes().addAll(list);
    }

    public void removeFromView(final int index) {
        Node node = getNodes().getChildren().get(index);
        getNodes().getChildren().remove(index);
        getNodes().getChildren().add(0, node);
        this.currentViewIndex = getCurrentViewIndex() + 1;
        getActiveNodes().remove(index);
    }

    public void showNext() {
        if (activeNodes.size() > 1) {
            if (getCurrentViewIndex() == getNodes().getChildren().size()) {
                getSlideTransition().setByX(getViewableWidth());
                getSlideTransition().setCycleCount(getActiveNodes().size());
                this.currentViewIndex = getCurrentViewIndex() - getActiveNodes().size();
            } else {
                getSlideTransition().setByX(-getViewableWidth());
                getSlideTransition().setCycleCount(1);
                this.currentViewIndex = getCurrentViewIndex() + 1;
            }
            getSlideTransition().play();
        }
    }

    public void showPrevious() {
        if (activeNodes.size() > 1) {
            if (getCurrentViewIndex() == this.getNodes().getChildren().size() - this.getActiveNodes().size()) {
                getSlideTransition().setByX(-getViewableWidth());
                getSlideTransition().setCycleCount(getActiveNodes().size());
                currentViewIndex = getNodes().getChildren().size();
            } else {
                getSlideTransition().setByX(getViewableWidth());
                getSlideTransition().setCycleCount(1);
                currentViewIndex = getCurrentViewIndex() - 1;
            }
            getSlideTransition().play();
        }
    }

    public int getViewableWidth() {
        return viewableWidth;
    }

    public int getCurrentViewIndex() {
        return currentViewIndex;
    }

    public HBox getNodes() {
        return nodes;
    }

    public List<Game> getActiveNodes() {
        return activeNodes;
    }

    public TranslateTransition getSlideTransition() {
        return slideTransition;
    }

    public int getCurrentActiveGameIndex() {
        return currentViewIndex - (nodes.getChildren().size() - activeNodes.size());
    }
}
