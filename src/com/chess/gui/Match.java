package com.chess.gui;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.Move;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Created by Anton on 3/16/2017.
 */
public class Match {

    private final List<Stack<Board>> gameBoards = new ArrayList<>();
    private final List<Move> lastMoves = new ArrayList<>();
    private int currentIndex = 0;
    private final List<Alliance> humanColorChoices = new ArrayList<>();
    private final List<Integer> depths = new ArrayList<>();

    public Match(final int numberOfGames, final ColorChoice colorChoice, final Difficulty difficulty) {
        addGames(numberOfGames, colorChoice, difficulty);
    }

    public void addGames(final int numberOfGames, final ColorChoice colorChoice, final Difficulty difficulty) {
        initializeGameBoards(numberOfGames);
        initializeLastMoves(numberOfGames);
        initializeHumanColorChoices(numberOfGames, colorChoice);
        initializeDepths(numberOfGames, difficulty);
    }

    private void initializeDepths(final int numberOfGames, final Difficulty difficulty) {
        for (int i = 0; i < numberOfGames; i++) {
            this.getDepths().add(difficulty.getDepth());
        }
    }

    private void initializeHumanColorChoices(final int numberOfGames, final ColorChoice colorChoice) {
        for (int i = 0; i < numberOfGames; i++) {
            this.getHumanColorChoices().add(colorChoice.getAlliance());
        }
    }

    private void initializeGameBoards(final int numberOfGames) {
        for (int i = 0; i < numberOfGames; i++) {
            Stack<Board> boards = new Stack<>();
            boards.push(Board.createStandardBoard());
            this.getGameBoards().add(boards);
        }
    }

    private void initializeLastMoves(final int numberOfGames) {
        for (int i = 0; i < numberOfGames; i++) {
            this.getLastMoves().add(Move.NULL_MOVE);
        }
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public void setCurrentIndex(int currentIndex) {
        this.currentIndex = currentIndex;
    }

    public List<Alliance> getHumanColorChoices() {
        return humanColorChoices;
    }

    public List<Integer> getDepths() {
        return depths;
    }

    public List<Stack<Board>> getGameBoards() {
        return gameBoards;
    }

    public List<Move> getLastMoves() {
        return lastMoves;
    }

    public Board getCurrentBoard() {
        return getGameBoards().get(currentIndex).peek();
    }

    public Alliance getCurrentHumanAlliance() {
        return getHumanColorChoices().get(currentIndex);
    }

    public int getCurrentEngineDepth() {
        return getDepths().get(currentIndex);
    }
}
