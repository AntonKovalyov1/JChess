package com.chess.gui;

/**
 * Created by Anton on 1/31/2017.
 */

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.board.Move;
import com.chess.engine.board.Tile;
import com.chess.engine.pieces.Piece;
import com.chess.engine.player.MoveTransition;
import com.chess.engine.player.ai.MiniMax;
import com.google.common.collect.Lists;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class GUI extends Stage {

    private final BoardPane boardPane;
    private Board chessBoard = Board.createStandardBoard();

    private Tile sourceTile;
    private Tile destinationTile;
    private Piece humanMovedPiece;
    private BoardDirection boardDirection;
    private MoveLog moveLog;
    private boolean highlightLegalMoves;
    private boolean cheat;
    private Move lastMove;

    private final int TILE_DIMENSION = 60;

    public GUI() {
        this.highlightLegalMoves = false;
        this.boardDirection = BoardDirection.NORMAL;
        this.moveLog = new MoveLog();
        this.lastMove = null;
        this.boardPane = new BoardPane();
        initialize();
    }

    private void initialize() {
        this.boardPane.setAlignment(Pos.CENTER);
        StackPane boardBackground = new StackPane();
        boardBackground.setPadding(new Insets(20,20,20,20));
        boardBackground.getChildren().add(this.boardPane);
        boardBackground.setStyle("-fx-background-image: url('images/boardbackground.jpg'); " +
                "-fx-background-repeat: no-repeat; " +
                "-fx-background-size: cover");
        InnerShadow innerShadow = new InnerShadow();
        innerShadow.setOffsetX(-1);
        innerShadow.setOffsetY(-1);
        innerShadow.setColor(Color.web("#EBCCAD", 0.6));

        boardPane.setEffect(innerShadow);
        boardBackground.setAlignment(Pos.CENTER);
        BorderPane mainBorderPane = new BorderPane();
        mainBorderPane.setTop(createMenuBar());
        mainBorderPane.setCenter(boardBackground);
        mainBorderPane.setMargin(boardBackground, new Insets(40,40,40,40));
        Scene scene = new Scene(mainBorderPane);
        scene.getStylesheets().add("main.css");
        setScene(scene);
        setTitle("Blindfold Chess Trainer");
        show();
    }

    private MenuBar createMenuBar() {
        final MenuBar menuBar = new MenuBar();

        final Menu playMenu = new Menu("Play");
        final Menu preferencesMenu = new Menu("Preferences");
        final Menu helpMenu = new Menu("Help");

        final CheckMenuItem cheatCheckMenuItem = new CheckMenuItem("Cheat");
        cheatCheckMenuItem.setOnAction(e -> {
            cheat = cheatCheckMenuItem.isSelected();
            updateBoard();
        });

        final MenuItem flipBoardMenuItem = new MenuItem("Flip Board");
        flipBoardMenuItem.setOnAction(e -> {
            boardDirection = boardDirection.opposite();
            updateBoard();
        });

        final CheckMenuItem highlightCheckMenuItem = new CheckMenuItem("Highlight legal moves");
        highlightCheckMenuItem.setOnAction(e -> {
            highlightLegalMoves = highlightCheckMenuItem.isSelected();
            updateBoard();
        });
        highlightCheckMenuItem.setSelected(true);
        highlightLegalMoves = true;
        // set highliting to false as default

        final MenuItem newGameMenuItem = new MenuItem("New Game/s");
        newGameMenuItem.setOnAction(e -> {
            CreateGame createGame = new CreateGame();
        });

        final MenuItem addGameMenuItem = new MenuItem("Add Game/s");
        addGameMenuItem.setOnAction(e -> {
            CreateGame createGame = new CreateGame();
        });

        final MenuItem fullScreenMenuItem = new MenuItem("Full Screen");
        fullScreenMenuItem.setOnAction(e -> {

        });

        final CheckMenuItem soundCheckMenuItem = new CheckMenuItem("Sound");
        soundCheckMenuItem.setOnAction(e -> {

        });

        playMenu.getItems().addAll(newGameMenuItem, addGameMenuItem, cheatCheckMenuItem);

        preferencesMenu.getItems().addAll(flipBoardMenuItem, highlightCheckMenuItem, fullScreenMenuItem, soundCheckMenuItem);

        menuBar.getMenus().addAll(playMenu, preferencesMenu, helpMenu);

        return menuBar;
    }

    private void updateBoard() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                boardPane.drawBoard(chessBoard);
            }
        });
    }

    enum PlayerType {
        HUMAN,
        COMPUTER
    }

    public final class BoardPane extends GridPane {

        final List<TilePane> boardTiles;

        public BoardPane() {
            this.boardTiles = new ArrayList<>();
            int tileIndex = 0;
            for (int i = 0; i < BoardUtils.NUM_TILES_PER_ROW; i++) {
                for (int j = 0; j < BoardUtils.NUM_TILES_PER_ROW; j++) {
                    final TilePane tilePane = new TilePane(tileIndex);
                    this.boardTiles.add(tilePane);
                    add(tilePane, j, i);
                    tileIndex++;
                }
            }
        }

        public void drawBoard(final Board board) {
            getChildren().clear();
            int index = 0;
            for (int i = 0; i < BoardUtils.NUM_TILES_PER_ROW; i++) {
                for (int j = 0; j < BoardUtils.NUM_TILES_PER_ROW; j++) {
                    boardDirection.traverse(boardTiles).get(index).drawTile(board);
                    add(boardDirection.traverse(boardTiles).get(index), j, i);
                    index++;
                }
            }
        }
    }

    public final class TilePane extends StackPane {

        private final int tileID;
        private final String WHITE_TILE = "-fx-background-image: url('images/lightwoodtile.jpg');";
        private final String BLACK_TILE = "-fx-background-image: url('images/darkwoodtile.jpg');";

        public TilePane(final int tileID) {
            this.tileID = tileID;
            drawTile(chessBoard);

            setOnMouseClicked(e -> {
                if (chessBoard.currentPlayer().getAlliance().isWhite()) {
                    if (e.getButton() == MouseButton.PRIMARY) {
                        if (sourceTile == null) {
                            // first click
                            sourceTile = chessBoard.getTile(tileID);
                            humanMovedPiece = sourceTile.getPiece();
                            if (humanMovedPiece == null ||
                                humanMovedPiece.getPieceAlliance() != chessBoard.currentPlayer().getAlliance()) {
                                sourceTile = null;
                            }
                        }
                        else {
                            // second click
                            destinationTile = chessBoard.getTile(tileID);
                            final Move move = Move.MoveFactory.createMove(chessBoard,
                                    sourceTile.getTileCoordinate(),
                                    destinationTile.getTileCoordinate());
                            final MoveTransition transition = chessBoard.currentPlayer().makeMove(move);
                            if (transition.getMoveStatus().isDone()) {
                                lastMove = move;
                                chessBoard = transition.getTransitionBoard();
                                moveLog.addMove(move);
                                AIThinker ai = new AIThinker();
                                Thread thread = new Thread(ai);
                                thread.setPriority(Thread.MAX_PRIORITY);
                                thread.start();

                                //TODO add the move that was made to the move log
                            }
                            sourceTile = null;
                            destinationTile = null;
                            humanMovedPiece = null;
                        }

                    }

                    else if (e.getButton() == MouseButton.SECONDARY) {
                        sourceTile = null;
                        destinationTile = null;
                        humanMovedPiece = null;
                    }

                    updateBoard();
                }
            });
        }

        private void drawTile(final Board board) {
            getChildren().clear();
            setTileColor();
            assignSelectionRectangle(board);
            assignPieceOnTile(board);
        }

        private void assignSelectionRectangle(final Board board) {
            Rectangle selectionRectangle = new Rectangle(TILE_DIMENSION, TILE_DIMENSION);
            selectionRectangle.setFill(Color.TRANSPARENT);
            if (sourceTile != null) {
                if (sourceTile.getTileCoordinate() == this.tileID) {
                    highlightSourceSquare(selectionRectangle, humanMovedPiece.getPieceAlliance());
                }
                else {
                    highlightLegals(selectionRectangle, board);
                }
            }
            else {
                if (lastMove != null) {
                    if (lastMove.getCurrentCoordinate() == tileID) {
                        highlightSourceSquare(selectionRectangle, lastMove.getMovedPiece().getPieceAlliance());
                    }
                    else if (lastMove.getDestinationCoordinate() == tileID){
                        highlightDestinationSquare(selectionRectangle, lastMove.getMovedPiece().getPieceAlliance());
                    }
                }
            }
            getChildren().add(selectionRectangle);
        }

        private void assignPieceOnTile(final Board board) {
            ImageView pieceView = new ImageView();
            if (cheat) {
                if (board.getTile(this.tileID).isTileOccupied()) {
                    final Image pieceImage = new Image("images/pieces/" + board.getTile(this.tileID).getPiece().getPieceAlliance().toString().substring(0, 1)
                            + board.getTile(this.tileID).getPiece().toString() + ".png");
                    pieceView.setImage(pieceImage);
                }
            }
            this.getChildren().add(pieceView);
        }

        private void setTileColor() {
            if (BoardUtils.FIRST_ROW[this.tileID] ||
                BoardUtils.THIRD_ROW[this.tileID] ||
                BoardUtils.FIFTH_ROW[this.tileID] ||
                BoardUtils.SEVENTH_ROW[this.tileID]) {
                setStyle(this.tileID % 2 == 0 ? WHITE_TILE : BLACK_TILE);
            }
            else if (BoardUtils.SECOND_ROW[this.tileID] ||
                     BoardUtils.FOURTH_ROW[this.tileID] ||
                     BoardUtils.SIXTH_ROW[this.tileID] ||
                     BoardUtils.EIGHT_ROW[this.tileID]) {
                setStyle(this.tileID % 2 != 0 ? WHITE_TILE : BLACK_TILE);
            }
        }

        private void highlightSourceSquare(final Rectangle selectionRectangle, final Alliance alliance) {
            selectionRectangle.setWidth(TILE_DIMENSION - 3);
            selectionRectangle.setHeight(TILE_DIMENSION - 3);
            selectionRectangle.setStrokeWidth(3);
            if (alliance.isWhite()) {
                selectionRectangle.setStroke(Color.web("#859C27"));
            }
            else {
                selectionRectangle.setStroke(Color.web("#CD0000"));
            }
        }

        private void highlightDestinationSquare(final Rectangle selectionRectangle, final Alliance alliance) {
            if (alliance.isWhite()) {
                selectionRectangle.setFill(Color.web("#859C27", 0.9));
            }
            else {
                selectionRectangle.setFill(Color.web("#CD0000", 0.9));
            }
        }

        private void highlightLegals(final Rectangle selectionRectangle, final Board board) {
            if(highlightLegalMoves) {
                if(humanMovedPiece != null && humanMovedPiece.getPieceAlliance() == board.currentPlayer().getAlliance()) {
                    final Move move = Move.MoveFactory.createMove(chessBoard, sourceTile.getTileCoordinate(), this.tileID);
                    final MoveTransition transition = chessBoard.currentPlayer().makeMove(move);
                    if (transition.getMoveStatus().isDone()) {
                        highlightDestinationSquare(selectionRectangle, humanMovedPiece.getPieceAlliance());
                    }
                }
            }
        }

    }

    private class AIThinker implements Runnable {

        @Override
        public void run() {
            if (chessBoard.currentPlayer().isInCheckMate()) {
                System.out.println("Human won");
            }
            else if (chessBoard.currentPlayer().isInStalemate()) {
                System.out.println("Draw");
            }
            else {
                MiniMax minimax = new MiniMax(2);
                Move bestMove = minimax.execute(chessBoard);
                chessBoard = chessBoard.currentPlayer().makeMove(bestMove).getTransitionBoard();
                updateBoard();
                if (chessBoard.currentPlayer().isInCheckMate()) {
                    System.out.println("Computer won");
                }
                else if (chessBoard.currentPlayer().isInStalemate()){
                    System.out.println("Draw");
                }
                lastMove = bestMove;
            }
        }
    }

    private static class MoveLog {
        private final List<Move> moves;

        MoveLog() {
            this.moves = new ArrayList<>();
        }

        public List<Move> getMoves() {
            return this.moves;
        }

        public void addMove(final Move move) {
            this.moves.add(move);
        }

        public int size() {
            return this.moves.size();
        }

        public void clear() {
            this.moves.clear();
        }

        public Move removeMove(int index) {
            return this.moves.remove(index);
        }

        public boolean removeMove(final Move move) {
            return this.moves.remove(move);
        }
    }

    public enum BoardDirection {

        NORMAL {
            @Override
            List<TilePane> traverse(List<TilePane> tilesList) {
                return tilesList;
            }

            @Override
            BoardDirection opposite() {
                return FLIPPED;
            }
        },
        FLIPPED {
            @Override
            List<TilePane> traverse(List<TilePane> tilesList) {
                return Lists.reverse(tilesList);
            }

            @Override
            BoardDirection opposite() {
                return NORMAL;
            }
        };

        abstract List<TilePane> traverse(final List<TilePane> tilesList);
        abstract BoardDirection opposite();
    }
}
