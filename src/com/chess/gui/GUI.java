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
import com.chess.engine.player.Player;
import com.chess.engine.player.PlayerType;
import com.chess.engine.player.ai.MiniMax;
import com.google.common.collect.Lists;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
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
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.*;

public class GUI extends Stage {

    private BoardDirection boardDirection;
    private boolean highlightLegalMoves;
    private boolean cheat;
    private BorderPane mainBorderPane = new BorderPane();
    private List<GamePane> gamePanes = new ArrayList<>();
    private int currentGameIndex = 0;
//    private SlideShow slideshow;
    private final int TILE_DIMENSION = 60;
    private final int GAME_DIMENSION = 60 * 9;

    public GUI() {
        this.highlightLegalMoves = false;
        this.boardDirection = BoardDirection.NORMAL;
        initialize();
    }

    private void initialize() {
        gamePanes.add(new GamePane(Board.createStandardBoard(), 1, Difficulty.EASY));
//        this.slideshow = new SlideShow(gamePanes);
        mainBorderPane.setTop(createMenuBar());
        mainBorderPane.setCenter(gamePanes.get(currentGameIndex));
        Scene scene = new Scene(mainBorderPane);
        setScene(scene);
        setTitle("Blindfold Chess Trainer");
        show();
    }

    private MenuBar createMenuBar() {
        final MenuBar menuBar = new MenuBar();

        final Menu playMenu = new Menu("Play");
        final Menu preferencesMenu = new Menu("Preferences");
        final Menu helpMenu = new Menu("Help");

        final CheckMenuItem cheatCheckMenuItem = new CheckMenuItem("See Pieces");
        cheatCheckMenuItem.setOnAction(e -> {
            this.cheat = cheatCheckMenuItem.isSelected();
            for (int i = 0; i < gamePanes.size(); i++) {
                GamePane tempGamePane = gamePanes.get(i);
                tempGamePane.boardPane.updateBoard(tempGamePane.boardPane.board);
            }
        });

        final MenuItem flipBoardMenuItem = new MenuItem("Flip Board");
        flipBoardMenuItem.setOnAction(e -> {
            this.boardDirection = boardDirection.opposite();
            for (int i = 0; i < gamePanes.size(); i++) {
                GamePane tempGamePane = gamePanes.get(i);
                tempGamePane.boardPane.updateBoard(tempGamePane.boardPane.board);
                tempGamePane.swapSides();
            }
        });

        final CheckMenuItem highlightCheckMenuItem = new CheckMenuItem("Highlight legal moves");
        highlightCheckMenuItem.setOnAction(e -> {
            this.highlightLegalMoves = highlightCheckMenuItem.isSelected();
            for (int i = 0; i < gamePanes.size(); i++) {
                GamePane tempGamePane = gamePanes.get(i);
                tempGamePane.boardPane.updateBoard(tempGamePane.boardPane.board);
            }
        });
        highlightCheckMenuItem.setSelected(true);
        this.highlightLegalMoves = true;
        // set highliting to false as default

        final MenuItem newGameMenuItem = new MenuItem("New Game/s");
        newGameMenuItem.setOnAction(e -> {
            CreateGame createGame = new CreateGame();
            if (createGame.getNumberOfGames() != -1) {
                final int numberOfGames = createGame.getNumberOfGames();
                final ColorChoice colorChoice = createGame.getColorChoice();
                final Difficulty difficulty = createGame.getDifficulty();
                gamePanes.clear();
                for (int i = 1; i < numberOfGames + 1; i++) {
                    if (colorChoice.getAlliance() == Alliance.WHITE)
                        gamePanes.add(new GamePane(Board.createStandardGameBoard(PlayerType.HUMAN, PlayerType.COMPUTER), i, difficulty));
                    else
                        gamePanes.add(new GamePane(Board.createStandardGameBoard(PlayerType.COMPUTER, PlayerType.HUMAN), i, difficulty));
                }
                mainBorderPane.setCenter(gamePanes.get(currentGameIndex));
                System.out.println("test");
            }
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

        playMenu.getItems().addAll(newGameMenuItem, addGameMenuItem);

        preferencesMenu.getItems().addAll(flipBoardMenuItem, highlightCheckMenuItem, fullScreenMenuItem, soundCheckMenuItem, cheatCheckMenuItem);

        menuBar.getMenus().addAll(playMenu, preferencesMenu, helpMenu);

        return menuBar;
    }

    public final class GamePane extends BorderPane {

        private final StackPane boardBackground = new StackPane();
        private final BoardPane boardPane;
        private Text whiteMoveText = new Text("1.?");
        private Text blackMoveText = new Text("");
        private final Text gameNumber;
        private final int boardNumber;
        private final HBox whiteSide = new HBox(7);
        private final HBox blackSide = new HBox(7);
        private final Difficulty difficulty;

        public GamePane(Board board) {
            this.boardPane = new BoardPane(board);
            this.gameNumber = new Text("");
            this.boardNumber = -1;
            this.difficulty = Difficulty.EASY;
            initialize();
        }

        public GamePane(Board board, final int boardNumber, final Difficulty difficulty) {
            this.boardPane = new BoardPane(board);
            this.boardNumber = boardNumber;
            this.gameNumber = new Text("Board " + boardNumber);
            this.difficulty = difficulty;
            initialize();
        }

        private void initialize() {
            initWhiteSide();
            initBlackSide();
            BorderPane boardAndMovesPane = new BorderPane();
            boardAndMovesPane.setTop(this.blackSide);
            boardAndMovesPane.setAlignment(this.blackSide, Pos.BOTTOM_RIGHT);
            boardAndMovesPane.setBottom(this.whiteSide);
            this.boardPane.setAlignment(Pos.CENTER);
            this.boardBackground.getChildren().add(boardPane);
            boardAndMovesPane.setCenter(boardBackground);
            drawBackground();
            setTop(this.gameNumber);
            setCenter(boardAndMovesPane);
            setPadding(new Insets(0, 20, 10, 20));
            setAlignment(this.gameNumber, Pos.BOTTOM_CENTER);
            setAlignment(boardAndMovesPane, Pos.TOP_CENTER);
            this.gameNumber.setFont(Font.font("Times New Roman", FontWeight.BOLD, 30));
        }

        private void drawBackground() {
            boardBackground.setPadding(new Insets(15, 15, 15, 15));
            boardBackground.setAlignment(Pos.CENTER);
            boardBackground.setStyle("-fx-background-image: url('images/boardbackground.jpg'); " +
                    "-fx-background-repeat: no-repeat; " +
                    "-fx-background-size: cover");
            setBoardInsideBackgroundEffect();
        }

        private void setBoardInsideBackgroundEffect() {
            InnerShadow innerShadow = new InnerShadow();
            innerShadow.setOffsetX(-1);
            innerShadow.setOffsetY(-1);
            innerShadow.setColor(Color.web("#EBCCAD", 0.6));

            boardPane.setEffect(innerShadow);
        }

        private void initWhiteSide() {
            Circle circle = new Circle(5, Color.WHITE);
            circle.setStroke(Color.BLACK);
            this.whiteMoveText.setLineSpacing(1);
            this.whiteSide.getChildren().addAll(circle, whiteMoveText);
            this.whiteSide.setAlignment(Pos.BASELINE_LEFT);
        }

        private void initBlackSide() {
            Circle circle = new Circle(5, Color.BLACK);
            circle.setStroke(Color.BLACK);
            this.blackMoveText.setLineSpacing(1);
            this.blackSide.getChildren().addAll(blackMoveText, circle);
            this.blackSide.setAlignment(Pos.BASELINE_RIGHT);
        }

        public void swapSides() {
            Stack<Node> tempStack = new Stack();
            tempStack.push(blackSide.getChildren().get(0));
            tempStack.push(blackSide.getChildren().get(1));
            blackSide.getChildren().clear();
            blackSide.getChildren().addAll(whiteSide.getChildren().remove(1), whiteSide.getChildren().remove(0));
            whiteSide.getChildren().clear();
            whiteSide.getChildren().addAll(tempStack.pop(), tempStack.pop());
        }


        public final class BoardPane extends GridPane {

            private final List<TilePane> boardTiles = new ArrayList<>();
            private final Stack<Move> moves = new Stack<>();
            private Board board;
            private Piece pieceToMove;
            private Tile sourceTile;
            private Tile destinationTile;
            private boolean gameOver;

            public BoardPane(Board board) {
                this.board = board;
                init();
            }

            public void init() {
                int tileIndex = 0;
                for (int i = 0; i < BoardUtils.NUM_TILES_PER_ROW; i++) {
                    for (int j = 0; j < BoardUtils.NUM_TILES_PER_ROW; j++) {
                        final TilePane tilePane = new TilePane(this, tileIndex);
                        this.boardTiles.add(tilePane);
                        add(tilePane, j, i);
                        tileIndex++;
                    }
                }
                updateBoard(this.board);
            }

            public void updateBoard(Board board) {
                Platform.runLater(() -> {
                    this.board = board;
                    updateMovesText(board);
                    isGameOver(board);
                    getChildren().clear();
                    int index = 0;
                    for (int i = 0; i < BoardUtils.NUM_TILES_PER_ROW; i++) {
                        for (int j = 0; j < BoardUtils.NUM_TILES_PER_ROW; j++) {
                            boardDirection.traverse(boardTiles).get(index).drawTile(board);
                            add(boardDirection.traverse(boardTiles).get(index), j, i);
                            index++;
                        }
                    }
                    if (!board.currentPlayer().getPlayerType().isHuman()) {
//                        slideshow.showNext();
                        executeComputerMove(board, difficulty.getDepth());
                    }
                });
            }

            public void executeComputerMove(final Board board, final int depth) {
                AIThinker ai = new AIThinker(board, depth);
                Thread thread = new Thread(ai);
                thread.setPriority(Thread.MAX_PRIORITY);
                thread.start();
            }

            private class AIThinker implements Runnable {

                private final Board board;
                private final int searchDepth;

                private AIThinker(Board board, int depth) {
                    this.board = board;
                    this.searchDepth = depth;
                }

                @Override
                public void run() {
                    if (!gameOver) {
                        MiniMax minimax = new MiniMax(this.board, this.searchDepth);
                        Move bestMove = minimax.compute();
                        updateBoard(board.currentPlayer().makeMove(bestMove).getTransitionBoard());
                        moves.push(bestMove);
                    }
                }
            }

            private void updateMovesText(final Board board) {
                if (!this.moves.isEmpty()) {
                    String lastMoveToString = checkOrCheckMate(board, moves.peek());
                    if (this.moves.size() % 2 == 0) {
                        blackMoveText.setText(this.moves.size() / 2 + "..." + lastMoveToString);
                        whiteMoveText.setText(this.moves.size() / 2 + 1 + ".?");
                    }
                    else {
                        blackMoveText.setText((this.moves.size() + 1) / 2 + "...?");
                        whiteMoveText.setText((this.moves.size() + 1) / 2 + "." + lastMoveToString);
                    }
                }
            }

            private String checkOrCheckMate(final Board board, final Move move) {
                if (board.currentPlayer().isInCheckMate())
                    return move + "#";
                if (board.currentPlayer().isInCheck())
                    return move + "+";
                return move.toString();
            }

            private void isGameOver(final Board board) {
                if (board.currentPlayer().isInCheckMate()) {
                    gameOver = true;
                    updateResultAsLoss(board.currentPlayer());
                } else if (board.currentPlayer().isInStalemate()) {
                    gameOver = true;
                    //TODO
                }
            }

            private void updateResultAsLoss(final Player playerThatLost) {
                if (playerThatLost.getAlliance().isWhite())
                    whiteMoveText.setText("0-1");
                else
                    blackMoveText.setText("1-0");
            }

            private void updateResultAsDraw(final Player playerToMove) {
                if (playerToMove.getAlliance().isWhite())
                    whiteMoveText.setText("1/2-1/2");
                else
                    blackMoveText.setText("1/2-1/2");
            }

            private void setPieceToMove(Piece pieceToMove) {
                this.pieceToMove = pieceToMove;
            }

            private void setSourceTile(Tile sourceTile) {
                this.sourceTile = sourceTile;
            }

            private void setDestinationTile(Tile destinationTile) {
                this.destinationTile = destinationTile;
            }
        }

        public final class TilePane extends StackPane {

            private final int tileID;
            private final BoardPane boardPane;
            private final String WHITE_TILE = "-fx-background-image: url('images/lightwoodtile.jpg');";
            private final String BLACK_TILE = "-fx-background-image: url('images/darkwoodtile.jpg');";

            public TilePane(final BoardPane boardPane, final int tileID) {
                this.boardPane = boardPane;
                this.tileID = tileID;
                drawTile(boardPane.board);

                setOnMouseClicked(e -> {
                    Board chessBoard = boardPane.board;
                    if (!boardPane.gameOver && chessBoard.currentPlayer().getPlayerType().isHuman()) {
                        if (e.getButton() == MouseButton.PRIMARY) {
                            if (boardPane.sourceTile == null) {
                                // first click
                                boardPane.sourceTile = chessBoard.getTile(tileID);
                                boardPane.pieceToMove = boardPane.sourceTile.getPiece();
                                if (boardPane.pieceToMove == null ||
                                        boardPane.pieceToMove.getPieceAlliance() != chessBoard.currentPlayer().getAlliance()) {
                                    boardPane.setSourceTile(null);
                                }
                            } else {
                                // second click
                                boardPane.setDestinationTile(chessBoard.getTile(tileID));
                                final Move move = Move.MoveFactory.createMove(chessBoard,
                                        boardPane.sourceTile.getTileCoordinate(),
                                        boardPane.destinationTile.getTileCoordinate());
                                final MoveTransition transition = chessBoard.currentPlayer().makeMove(move);
                                if (transition.getMoveStatus().isDone()) {
                                    boardPane.moves.push(move);
                                    chessBoard = transition.getTransitionBoard();
                                }
                                resetMoveSelection();
                            }

                        } else if (e.getButton() == MouseButton.SECONDARY) {
                            resetMoveSelection();
                        }
                        this.boardPane.updateBoard(chessBoard);
                    }
                });
            }

            private void resetMoveSelection() {
                boardPane.setSourceTile(null);
                boardPane.setDestinationTile(null);
                boardPane.setPieceToMove(null);
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
                if (boardPane.sourceTile != null) {
                    if (boardPane.sourceTile.getTileCoordinate() == this.tileID) {
                        highlightSourceSquare(selectionRectangle, boardPane.pieceToMove.getPieceAlliance());
                    } else {
                        highlightLegals(selectionRectangle, board);
                    }
                } else if (!boardPane.moves.isEmpty()) {
                    Move lastMove = boardPane.moves.peek();
                    if (lastMove != null) {
                        if (lastMove.getCurrentCoordinate() == tileID) {
                            highlightSourceSquare(selectionRectangle, lastMove.getMovedPiece().getPieceAlliance());
                        } else if (lastMove.getDestinationCoordinate() == tileID) {
                            highlightMoveDestinationSquare(selectionRectangle, lastMove.getMovedPiece().getPieceAlliance());
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
                } else if (BoardUtils.SECOND_ROW[this.tileID] ||
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
                } else {
                    selectionRectangle.setStroke(Color.web("#CD0000"));
                }
            }

            private void highlightMoveDestinationSquare(final Rectangle selectionRectangle, final Alliance alliance) {
                if (alliance.isWhite()) {
                    selectionRectangle.setFill(Color.web("#859C27", 0.9));
                } else {
                    selectionRectangle.setFill(Color.web("#CD0000", 0.9));
                }
            }

            private void highlightDestinationSquare(final Rectangle selectionRectangle, final Alliance alliance) {
                if (alliance.isWhite()) {
                    selectionRectangle.setFill(Color.web("#859C27", 0.65));
                } else {
                    selectionRectangle.setFill(Color.web("#CD0000", 0.65));
                }
            }

            private void highlightLegals(final Rectangle selectionRectangle, final Board board) {
                if (highlightLegalMoves) {
                    if (boardPane.pieceToMove != null && boardPane.pieceToMove.getPieceAlliance() == board.currentPlayer().getAlliance()) {
                        final Move move = Move.MoveFactory.createMove(board, boardPane.sourceTile.getTileCoordinate(), this.tileID);
                        final MoveTransition transition = board.currentPlayer().makeMove(move);
                        if (transition.getMoveStatus().isDone()) {
                            highlightDestinationSquare(selectionRectangle, boardPane.pieceToMove.getPieceAlliance());
                        }
                    }
                }
            }

        }
    }
}
