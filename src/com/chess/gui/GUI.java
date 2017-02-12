package com.chess.gui;/**
 * Created by Anton on 1/31/2017.
 */

import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.board.Move;
import com.chess.engine.board.Tile;
import com.chess.engine.pieces.Piece;
import com.chess.engine.player.MoveTransition;
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
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
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
import java.util.List;

public class GUI extends Stage {

    private final BoardPane boardPane;
    private Board chessBoard = Board.createStandardBoard();

    private Tile sourceTile;
    private Tile destinationTile;
    private Piece humanMovedPiece;
    MoveLog moveLog;

    private final int SCENE_WIDTH = 480;
    private final int SCENE_HEIGHT = 510;
    private final int TILE_DIMENSION = 60;

    Scene scene;

    public GUI() {
        moveLog = new MoveLog();
        this.boardPane = new BoardPane();
        this.boardPane.setAlignment(Pos.CENTER);
        BorderPane bp = new BorderPane();
        bp.setTop(createMenuBar());
        bp.setCenter(this.boardPane);
        Scene scene = new Scene(bp, SCENE_WIDTH, SCENE_HEIGHT);
        scene.getStylesheets().add("main.css");
        setScene(scene);
        setTitle("Blindfold Chess Trainer");
        show();
    }

    private MenuBar createMenuBar() {
        final MenuBar menuBar = new MenuBar();

        final Menu fileMenu = new Menu("File");
        final Menu homeMenu = new Menu("Home");
        final Menu preferencesMenu = new Menu("Preferences");
        final Menu helpMenu = new Menu("Help");

        menuBar.getMenus().addAll(fileMenu, homeMenu, preferencesMenu, helpMenu);

        return menuBar;
    }

    public final class BoardPane extends GridPane {

        List<TilePane> boardTiles;

        public BoardPane() {
            this.boardTiles = new ArrayList<>();
            int tileIndex = 0;
            for (int i = 0; i < BoardUtils.NUM_TILES_PER_ROW; i++) {
                for (int j = 0; j < BoardUtils.NUM_TILES_PER_ROW; j++) {
                    final TilePane tilePane = new TilePane(this, tileIndex);
                    this.boardTiles.add(tilePane);
                    add(tilePane, j, i);
                    tileIndex++;
                }
            }
        }

        public void drawBoard(final Board board) {
            for (final TilePane tilePane : boardTiles) {
                tilePane.drawTile(board);
            }
        }
    }

    public final class TilePane extends StackPane {

        private final int tileID;
        private final BoardPane boardPane;

        public TilePane(final BoardPane boardPane, final int tileID) {
            this.boardPane = boardPane;
            this.tileID = tileID;
            drawTile(chessBoard);

            setOnMouseClicked(e -> {
                if(e.getButton() == MouseButton.PRIMARY) {
                    if (sourceTile == null) {
                        // first click
                        sourceTile = chessBoard.getTile(tileID);
                        humanMovedPiece = sourceTile.getPiece();
                        if (humanMovedPiece == null) {
                            sourceTile = null;
                        }
                        else if (humanMovedPiece.getPieceAlliance() == chessBoard.currentPlayer().getAlliance()){
                            this.getChildren().get(0).setStyle(this.getChildren().get(0).getStyle().concat(" -fx-border-width : 3px; -fx-border-color : #CC0000;"));
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
                            chessBoard = transition.getTransitionBoard();
                            moveLog.addMove(move);
                            //TODO add the move that was made to the move log
                            Platform.runLater(new Runnable() {
                                 @Override
                                 public void run() {
                                        boardPane.drawBoard(chessBoard);
                                    }
                            });
                        }

                        sourceTile = null;
                        destinationTile = null;
                        humanMovedPiece = null;
                    }
                }

                else if(e.getButton() == MouseButton.SECONDARY) {
                    sourceTile = null;
                    destinationTile = null;
                    humanMovedPiece = null;
                }
            });
        }

        private void drawTile(final Board board) {
            this.getChildren().clear();
            setTileColor();
            assignPieceOnTile(board);
        }

        private Pane createBlackTile() {
            Pane p = new Pane();
            p.setMinSize(TILE_DIMENSION, TILE_DIMENSION);
            p.setStyle("-fx-background-image: url('images/darkwoodtile.jpg');");
            return p;
        }

        private Pane createWhiteTile() {
            Pane p = new Pane();
            p.setMinSize(TILE_DIMENSION, TILE_DIMENSION);
            p.setStyle("-fx-background-image: url('images/lightwoodtile.jpg');");
            return p;
        }

        private void assignPieceOnTile(final Board board) {
            if(board.getTile(this.tileID).isTileOccupied()) {
                final Image pieceImage = new Image("images/pieces/" + board.getTile(this.tileID).getPiece().getPieceAlliance().toString().substring(0, 1)
                + board.getTile(this.tileID).getPiece().toString() + ".png");
                this.getChildren().add(new ImageView(pieceImage));
            }
        }

        private void setTileColor() {
            if (BoardUtils.FIRST_ROW[this.tileID] ||
                BoardUtils.THIRD_ROW[this.tileID] ||
                BoardUtils.FIFTH_ROW[this.tileID] ||
                BoardUtils.SEVENTH_ROW[this.tileID]) {
                this.getChildren().add(this.tileID % 2 == 0 ? createWhiteTile() : createBlackTile());
            }
            else if (BoardUtils.SECOND_ROW[this.tileID] ||
                    BoardUtils.FOURTH_ROW[this.tileID] ||
                    BoardUtils.SIXTH_ROW[this.tileID] ||
                    BoardUtils.EIGHT_ROW[this.tileID]) {
                this.getChildren().add(this.tileID % 2 != 0 ? createWhiteTile() : createBlackTile());
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
}
