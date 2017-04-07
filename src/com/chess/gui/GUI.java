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
import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.*;
import java.util.concurrent.*;

public class GUI extends Stage {

    private boolean highlightLegalMoves;
    private boolean cheatAll;
    private boolean flipBoard;
    private BorderPane mainBorderPane = new BorderPane();
    private GamesViewer gamesViewer;
    private final int TILE_DIMENSION = 60;
    private ScheduledExecutorService computerMoveExecutor = Executors.newScheduledThreadPool(1);

    public GUI() {
        initialize();
    }

    private void initialize() {
        setHighlightLegalMoves(true);
        List<GamePane> games = new ArrayList<>();
        games.add(new GamePane(Board.createStandardGameBoard(PlayerType.HUMAN, PlayerType.HUMAN), Difficulty.EASY.getDepth()));
        gamesViewer = new GamesViewer(games);
        getGamesViewer().disableControls();
        getMainBorderPane().setTop(createMenuBar());
        getMainBorderPane().setCenter(getGamesViewer());
        Scene scene = new Scene(getMainBorderPane());
        scene.getStylesheets().add("main.css");
        setScene(scene);
        setTitle("Blindfold Chess Trainer");
        show();
        setOnCloseRequest(e -> {
            getComputerMoveExecutor().shutdownNow();
            close();
        });
    }

    private MenuBar createMenuBar() {
        final MenuBar menuBar = new MenuBar();

        final Menu playMenu = new Menu("Play");
        final Menu preferencesMenu = new Menu("Preferences");
        final Menu helpMenu = new Menu("Help");

        final CheckMenuItem cheatCheckMenuItem = new CheckMenuItem("See Pieces");
        cheatCheckMenuItem.setOnAction(e -> {
            cheatAll = cheatCheckMenuItem.isSelected();
            for (int i = 0; i < getGamesViewer().getGames().size(); i++) {
                getGamesViewer().getGames().get(i).getBoardPane().redrawBoard();
            }
        });

        final CheckMenuItem flipBoardMenuItem = new CheckMenuItem("Flip Board/s");
        flipBoardMenuItem.setOnAction(e -> {
            setFlipBoard(!isFlipBoard());
            for (int i = 0; i < getGamesViewer().getGames().size(); i++) {
                getGamesViewer().getGames().get(i).flipBoard();
            }
        });

        final CheckMenuItem highlightCheckMenuItem = new CheckMenuItem("Highlight legal moves");
        highlightCheckMenuItem.setOnAction(e -> {
            this.setHighlightLegalMoves(highlightCheckMenuItem.isSelected());
            for (int i = 0; i < getGamesViewer().getGames().size(); i++) {
                getGamesViewer().getGames().get(i).getBoardPane().redrawBoard();
            }
        });
        highlightCheckMenuItem.setSelected(true);
        this.setHighlightLegalMoves(true);
        // set highliting to false as default

        final MenuItem newGameMenuItem = new MenuItem("New Game/s");
        newGameMenuItem.setOnAction(e -> {
            CreateGame createGame = new CreateGame();
            if (createGame.getNumberOfGames() != -1) {
                getComputerMoveExecutor().shutdownNow();
                setComputerMoveExecutor(Executors.newScheduledThreadPool(1));
                final int numberOfGames = createGame.getNumberOfGames();
                final ColorChoice colorChoice = createGame.getColorChoice();
                final Difficulty difficulty = createGame.getDifficulty();
                List<GamePane> games = new ArrayList<>();
                for (int i = 1; i < numberOfGames + 1; i++) {
                    if (colorChoice.getAlliance() == Alliance.WHITE)
                        games.add(new GamePane(Board.createStandardGameBoard(PlayerType.HUMAN, PlayerType.COMPUTER), difficulty.getDepth()));
                    else
                        games.add(new GamePane(Board.createStandardGameBoard(PlayerType.COMPUTER, PlayerType.HUMAN), difficulty.getDepth()));
                }
                setGamesViewer(new GamesViewer(games));
                getMainBorderPane().setCenter(getGamesViewer());
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

    public GamesViewer getGamesViewer() {
        return gamesViewer;
    }

    public void setGamesViewer(GamesViewer gamesViewer) {
        this.gamesViewer = gamesViewer;
    }

    public ScheduledExecutorService getComputerMoveExecutor() {
        return computerMoveExecutor;
    }

    public void setComputerMoveExecutor(ScheduledExecutorService computerMoveExecutor) {
        this.computerMoveExecutor = computerMoveExecutor;
    }

    public boolean isHighlightLegalMoves() {
        return highlightLegalMoves;
    }

    public void setHighlightLegalMoves(boolean highlightLegalMoves) {
        this.highlightLegalMoves = highlightLegalMoves;
    }

    public BorderPane getMainBorderPane() {
        return mainBorderPane;
    }

    public boolean isFlipBoard() {
        return flipBoard;
    }

    public void setFlipBoard(boolean flipBoard) {
        this.flipBoard = flipBoard;
    }

    public final class GamesViewer extends GridPane {

        private final List<GamePane> games;
        private final Pagination pagination;
        private final Button btResign = resignButton();
        private final Button btDraw = drawButton();
        private final Button btWin = winButton();
        private final Button btCheat = cheatButton();
        private final Button btFlip = flipButton();
        private final Button btTakeBack = takeBackButton();
        private final Button btResetGame = resetButton();
        private final VBox options = createOptions();
        private final SequentialTransition st = new SequentialTransition();
        private GamePane currentGame;

        public GamesViewer(List<GamePane> games) {
            this.games = games;
            this.pagination = createPaginationControl(games);
            this.currentGame = games.get(0);
            initialize(currentGame);
        }

        public void initialize(GamePane game) {
            getChildren().clear();
            add(getPagination(), 0, 0);
            add(game, 0, 1);
            add(options, 1, 1);
        }

        public VBox createOptions() {
            VBox options = new VBox(5);
            options.setPadding(new Insets(20, 20, 0, 5));
            options.getChildren().addAll(btResign, btDraw, btWin, getBtCheat(), btFlip, btTakeBack, btResetGame);
            return options;
        }

        public Pagination createPaginationControl(List<GamePane> gamePanes) {
            Pagination p = new Pagination(gamePanes.size());
            p.currentPageIndexProperty().addListener((InvalidationListener) -> {
                disableControls();
                final GamePane nextGame = getGames().get(getPagination().getCurrentPageIndex());
                nextGame.setOpacity(0.0);
                if (nextGame.getBoardPane().isCheat()) {
                    nextGame.getBoardPane().setCheat(false);
                    nextGame.getBoardPane().redrawBoard();
                }
                PauseTransition pt = new PauseTransition(Duration.millis(200));
                FadeTransition ft = new FadeTransition(Duration.millis(1000), getCurrentGame());
                ft.setFromValue(1.0);
                ft.setToValue(0.0);
                ft.setOnFinished(e -> {
                    getChildren().clear();
                    initialize(nextGame);
                    resetOptions();
                });
                PauseTransition pt2 = new PauseTransition(Duration.millis(200));
                FadeTransition ft2 = new FadeTransition(Duration.millis(1000), nextGame);
                ft2.setFromValue(0.0);
                ft2.setToValue(1.0);
                getSt().getChildren().clear();
                getSt().getChildren().addAll(pt, ft, pt2, ft2);
                getSt().play();
                getSt().setOnFinished(e -> {
                    getCurrentGame().setOpacity(1.0);
                    setCurrentGame(nextGame);
                    enableControls();
                });
            });
            return p;
        }

        private Button resignButton() {
            Button b = new Button("Resign");
            b.setPrefWidth(100);
            b.setOnAction(e -> {
                if (!getCurrentGame().getBoardPane().isGameOver())
                    getCurrentGame().resign();
            });
            return b;
        }

        private Button drawButton() {
            Button b = new Button("Draw");
            b.setPrefWidth(100);
            b.setOnAction(e -> {
                if (!getCurrentGame().getBoardPane().isGameOver())
                    getCurrentGame().makeDraw();
            });
            return b;
        }

        private Button winButton() {
            Button b = new Button("Claim Win");
            b.setPrefWidth(100);
            b.setOnAction(e -> {
                if (!getCurrentGame().getBoardPane().isGameOver())
                    getCurrentGame().win();
            });
            return b;
        }

        private Button cheatButton() {
            Button b = new Button("Cheat");
            b.setPrefWidth(100);
            b.setOnAction(e -> {
                if (getCurrentGame().getBoardPane().isCheat()) {
                    getCurrentGame().getBoardPane().setCheat(false);
                    getCurrentGame().getBoardPane().redrawBoard();
                    getBtCheat().setText("Cheat");
                }
                else {
                    getCurrentGame().getBoardPane().setCheat(true);
                    getCurrentGame().getBoardPane().redrawBoard();
                    getBtCheat().setText("Play Fair");
                }
            });
            return b;
        }

        private Button flipButton() {
            Button b = new Button("Flip");
            b.setPrefWidth(100);
            b.setOnAction(e -> {
                getCurrentGame().flipBoard();
            });
            return b;
        }

        private Button takeBackButton() {
            Button b = new Button("Take Back");
            b.setPrefWidth(100);
            b.setOnAction(e -> {
                getCurrentGame().takeBack();
            });
            return b;
        }

        private Button resetButton() {
            Button b = new Button("Reset");
            b.setPrefWidth(100);
            b.setOnAction(e -> {
                getCurrentGame().resetGame();
            });
            return b;
        }

        public void resetOptions() {
            getBtCheat().setText("Cheat");
        }

        public void disableControls() {
            for (Node current : getOptions().getChildren()) {
                current.setDisable(true);
            }
            getPagination().setDisable(true);
        }

        private void enableControls() {
            for (Node current : getOptions().getChildren()) {
                current.setDisable(false);
            }
            getPagination().setDisable(false);
        }

        public List<GamePane> getGames() {
            return games;
        }

        public Pagination getPagination() {
            return pagination;
        }

        public SequentialTransition getSt() {
            return st;
        }

        public GamePane getCurrentGame() {
            return currentGame;
        }

        public void setCurrentGame(GamePane currentGame) {
            this.currentGame = currentGame;
        }

        public VBox getOptions() {
            return options;
        }

        public Button getBtCheat() {
            return btCheat;
        }
    }

    public final class GamePane extends BorderPane {

        private final StackPane boardBackground = new StackPane();
        private BoardPane boardPane;
        private Text whiteMoveText = new Text("1.?");
        private Text blackMoveText = new Text("");
        private final HBox whiteSide = new HBox(7);
        private final HBox blackSide = new HBox(7);
        private final int depth;

        public GamePane(Board board, final int depth) {
            this.boardPane = new BoardPane(this, board);
            this.depth = depth;
            initialize(board);
        }

        private void initialize(Board board) {
            initWhiteSide();
            initBlackSide();
            BorderPane boardAndMovesPane = new BorderPane();
            if (board.whitePlayer().getPlayerType().isComputer())
                flipBoard();
            if (isFlipBoard()) {
                flipBoard();
            }
            boardAndMovesPane.setTop(this.getBlackSide());
            boardAndMovesPane.setAlignment(this.getBlackSide(), Pos.BOTTOM_RIGHT);
            boardAndMovesPane.setBottom(this.getWhiteSide());
            this.getBoardPane().setAlignment(Pos.CENTER);
            this.getBoardBackground().getChildren().add(getBoardPane());
            boardAndMovesPane.setCenter(getBoardBackground());
            drawBackground();
            setCenter(boardAndMovesPane);
            setPadding(new Insets(0, 0, 20, 20));
            setAlignment(boardAndMovesPane, Pos.TOP_CENTER);
        }

        private void drawBackground() {
            getBoardBackground().setPadding(new Insets(15, 15, 15, 15));
            getBoardBackground().setAlignment(Pos.CENTER);
            getBoardBackground().setStyle("-fx-background-image: url('images/boardbackground.jpg'); " +
                    "-fx-background-repeat: no-repeat; " +
                    "-fx-background-size: cover");
            setBoardInsideBackgroundEffect();
        }

        private void setBoardInsideBackgroundEffect() {
            InnerShadow innerShadow = new InnerShadow();
            innerShadow.setOffsetX(-1);
            innerShadow.setOffsetY(-1);
            innerShadow.setColor(Color.web("#EBCCAD", 0.6));

            getBoardPane().setEffect(innerShadow);
        }

        private void flipBoard() {
            Collections.reverse(this.getBoardPane().getBoardTiles());
            swapSides();
            this.getBoardPane().redrawBoard();
        }

        private void initWhiteSide() {
            Circle circle = new Circle(5, Color.WHITE);
            circle.setStroke(Color.BLACK);
            this.getWhiteMoveText().setLineSpacing(1);
            this.getWhiteSide().getChildren().addAll(circle, getWhiteMoveText());
            this.getWhiteSide().setAlignment(Pos.BASELINE_LEFT);
        }

        private void initBlackSide() {
            Circle circle = new Circle(5, Color.BLACK);
            circle.setStroke(Color.BLACK);
            this.getBlackMoveText().setLineSpacing(1);
            this.getBlackSide().getChildren().addAll(getBlackMoveText(), circle);
            this.getBlackSide().setAlignment(Pos.BASELINE_RIGHT);
        }

        public void swapSides() {
            Stack<Node> tempStack = new Stack();
            tempStack.push(getBlackSide().getChildren().get(0));
            tempStack.push(getBlackSide().getChildren().get(1));
            getBlackSide().getChildren().clear();
            getBlackSide().getChildren().addAll(getWhiteSide().getChildren().remove(1), getWhiteSide().getChildren().remove(0));
            getWhiteSide().getChildren().clear();
            getWhiteSide().getChildren().addAll(tempStack.pop(), tempStack.pop());
        }

        public void resign() {
            if (!getBoardPane().isGameOver()) {
                getBoardPane().setGameOver(true);
                if (getBoardPane().getBoard().whitePlayer().getPlayerType().isHuman()) {
                    getBoardPane().updateResultAsLoss(getBoardPane().getBoard().whitePlayer(), getBoardPane().getBoard().currentPlayer());
                } else {
                    getBoardPane().updateResultAsLoss(getBoardPane().getBoard().blackPlayer(), getBoardPane().getBoard().currentPlayer());
                }
            }
        }


        public void makeDraw() {
            if (!getBoardPane().isGameOver()) {
                //TODO
                getBoardPane().setGameOver(true);
                getBoardPane().updateResultAsDraw(getBoardPane().getBoard().currentPlayer());
            }
        }

        public void win() {
            if (!getBoardPane().isGameOver()) {
                //TODO
                getBoardPane().setGameOver(true);
                if (getBoardPane().getBoard().whitePlayer().getPlayerType().isComputer()) {
                    getBoardPane().updateResultAsLoss(getBoardPane().getBoard().whitePlayer(), getBoardPane().getBoard().currentPlayer());
                } else {
                    getBoardPane().updateResultAsLoss(getBoardPane().getBoard().blackPlayer(), getBoardPane().getBoard().currentPlayer());
                }
            }
        }

        public void resetGame() {
            Stack<Move> moves = getBoardPane().getMoves();
            if (!moves.isEmpty() || !getBoardPane().isGameOver()) {
                interruptComputerMove();
                Board startOverBoard = Board.createStandardGameBoard(getBoardPane().getBoard().whitePlayer().getPlayerType(),
                        getBoardPane().getBoard().blackPlayer().getPlayerType());
                getBoardPane().resetMoveSelection();
                getBoardPane().getMoves().clear();
                getBoardPane().updateBoard(startOverBoard);
                getWhiteMoveText().setText("1.?");
                getBlackMoveText().setText("");
            }
        }

        public void takeBack() {
            Stack<Move> moves = getBoardPane().getMoves();
            if (!moves.isEmpty() || !getBoardPane().isGameOver()) {
                interruptComputerMove();
                Player currentPlayer = getBoardPane().getBoard().currentPlayer();
                if (currentPlayer.getPlayerType().isComputer()) {
                    Board board = moves.pop().getBoard();
                    getBoardPane().updateBoard(board);
                }
                else if (currentPlayer.getPlayerType().isHuman() && moves.size() > 1) {
                    moves.pop();
                    Board board = moves.pop().getBoard();
                    getBoardPane().updateBoard(board);
                }
            }
        }

        public void interruptComputerMove() {
            if (getBoardPane().getFutureComputerMove() != null) {
                getBoardPane().getFutureComputerMove().cancel(true);
            }
        }

        public StackPane getBoardBackground() {
            return boardBackground;
        }

        public BoardPane getBoardPane() {
            return boardPane;
        }

        public Text getWhiteMoveText() {
            return whiteMoveText;
        }

        public Text getBlackMoveText() {
            return blackMoveText;
        }

        public HBox getWhiteSide() {
            return whiteSide;
        }

        public HBox getBlackSide() {
            return blackSide;
        }

        public int getDepth() {
            return depth;
        }
    }

    public final class BoardPane extends GridPane {

        private final GamePane game;
        private final List<TilePane> boardTiles = new ArrayList<>();
        private final Stack<Move> moves = new Stack<>();
        private Board board;
        private Piece pieceToMove;
        private Tile sourceTile;
        private Tile destinationTile;
        private boolean gameOver;
        private boolean cheat;
        private Future<?> futureComputerMove;

        public BoardPane(GamePane game, Board board) {
            this.game = game;
            this.board = board;
            initialize();
        }

        public void initialize() {
            int tileIndex = 0;
            for (int i = 0; i < BoardUtils.NUM_TILES_PER_ROW; i++) {
                for (int j = 0; j < BoardUtils.NUM_TILES_PER_ROW; j++) {
                    final TilePane tilePane = new TilePane(this, tileIndex);
                    getBoardTiles().add(tilePane);
                    add(tilePane, j, i);
                    tileIndex++;
                }
            }
            updateBoard(getBoard());
        }

        public synchronized void updateBoard(Board board) {
            setBoard(board);
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    updateMovesText(board);
                    isGameOver(board);
                    redrawBoard();
                    if (board.currentPlayer().getPlayerType().isComputer()) {
                        AIThinker makeComputerMove = new AIThinker(getGame().getBoardPane(), getGame().getDepth());
                        if (getGamesViewer().getGames().size() > 1) {
                            if (!getMoves().isEmpty()) {
                                updatePage();
                                setFutureComputerMove(getComputerMoveExecutor().schedule(makeComputerMove, 1300, TimeUnit.MILLISECONDS));
                            }
                            else {
                                setFutureComputerMove(getComputerMoveExecutor().schedule(makeComputerMove, 1500, TimeUnit.MILLISECONDS));
                            }

                        }
                        else {
                            if (!getMoves().isEmpty()) {
                                setFutureComputerMove(getComputerMoveExecutor().schedule(makeComputerMove, 200, TimeUnit.MILLISECONDS));
                            }
                            else {
                                setFutureComputerMove(getComputerMoveExecutor().schedule(makeComputerMove, 1500, TimeUnit.MILLISECONDS));
                            }
                        }
                    }
                }
            });
        }

        public synchronized void redrawBoard() {
            getChildren().clear();
            int index = 0;
            for (int i = 0; i < BoardUtils.NUM_TILES_PER_ROW; i++) {
                for (int j = 0; j < BoardUtils.NUM_TILES_PER_ROW; j++) {
                    this.getBoardTiles().get(index).drawTile(getBoard());
                    add(this.getBoardTiles().get(index), j, i);
                    index++;
                }
            }
        }

        public void updatePage() {
            if (getGamesViewer().getPagination().getCurrentPageIndex() == getGamesViewer().getGames().size() - 1)
                getGamesViewer().getPagination().setCurrentPageIndex(0);
            else
                getGamesViewer().getPagination().setCurrentPageIndex(getGamesViewer().getPagination().getCurrentPageIndex() + 1);
        }

        public boolean isCheat() {
            return cheat;
        }

        public void setCheat(boolean cheat) {
            this.cheat = cheat;
        }

        public List<TilePane> getBoardTiles() {
            return boardTiles;
        }

        public Stack<Move> getMoves() {
            return moves;
        }

        public Board getBoard() {
            return board;
        }

        public Piece getPieceToMove() {
            return pieceToMove;
        }

        public Tile getSourceTile() {
            return sourceTile;
        }

        public Tile getDestinationTile() {
            return destinationTile;
        }

        public boolean isGameOver() {
            return gameOver;
        }

        public void setBoard(Board board) {
            this.board = board;
        }

        public void setGameOver(boolean gameOver) {
            this.gameOver = gameOver;
        }

        public GamePane getGame() {
            return game;
        }

        private void updateMovesText(final Board board) {
            if (!this.getMoves().isEmpty()) {
                String lastMoveToString = checkOrCheckMate(board, getMoves().peek());
                if (this.getMoves().size() % 2 == 0) {
                    getGame().getBlackMoveText().setText(this.getMoves().size() / 2 + "..." + lastMoveToString);
                    getGame().getWhiteMoveText().setText(this.getMoves().size() / 2 + 1 + ".?");
                }
                else {
                    getGame().getBlackMoveText().setText((this.getMoves().size() + 1) / 2 + "...?");
                    getGame().getWhiteMoveText().setText((this.getMoves().size() + 1) / 2 + "." + lastMoveToString);
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
                setGameOver(true);
                updateResultAsLoss(board.currentPlayer(), board.currentPlayer());
            }
            else if (board.currentPlayer().isInStalemate()) {
                setGameOver(true);
                updateResultAsDraw(board.currentPlayer());
                //TODO
            }
        }

        private void updateResultAsLoss(final Player playerThatLost, final Player currentPlayer) {
            if (playerThatLost.getAlliance().isWhite()) {
                if (currentPlayer.getAlliance().isWhite())
                    getGame().getWhiteMoveText().setText("0-1");
                else
                    getGame().getBlackMoveText().setText("0-1");
            }
            else {
                if (currentPlayer.getAlliance().isWhite())
                    getGame().getWhiteMoveText().setText("1-0");
                else
                    getGame().getBlackMoveText().setText("1-0");
            }
        }

        private void updateResultAsDraw(final Player playerToMove) {
            if (playerToMove.getAlliance().isWhite())
                getGame().getWhiteMoveText().setText("1/2-1/2");
            else
                getGame().getBlackMoveText().setText("1/2-1/2");
        }

        private void resetMoveSelection() {
            setSourceTile(null);
            setDestinationTile(null);
            setPieceToMove(null);
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

        public Future<?> getFutureComputerMove() {
            return futureComputerMove;
        }

        public void setFutureComputerMove(Future<?> futureComputerMove) {
            this.futureComputerMove = futureComputerMove;
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
            drawTile(boardPane.getBoard());

            setOnMouseClicked(e -> {
                Board chessBoard = getBoardPane().getBoard();
                if (!getBoardPane().isGameOver() &&
                    chessBoard.currentPlayer().getPlayerType().isHuman() &&
                    getGamesViewer().getSt().getStatus() != Animation.Status.RUNNING) {

                    if (e.getButton() == MouseButton.PRIMARY) {
                        if (getBoardPane().getSourceTile() == null) {
                            // first click
                            getBoardPane().setSourceTile(chessBoard.getTile(getTileID()));
                            getBoardPane().setPieceToMove(getBoardPane().getSourceTile().getPiece());
                            if (getBoardPane().getPieceToMove() == null ||
                                    getBoardPane().getPieceToMove().getPieceAlliance() != chessBoard.currentPlayer().getAlliance()) {
                                getBoardPane().setSourceTile(null);
                            }
                        }
                        else {
                            // second click
                            getBoardPane().setDestinationTile(chessBoard.getTile(getTileID()));
                            final Move move = Move.MoveFactory.createMove(chessBoard,
                                    getBoardPane().getSourceTile().getTileCoordinate(),
                                    getBoardPane().getDestinationTile().getTileCoordinate());
                            final MoveTransition transition = chessBoard.currentPlayer().makeMove(move);
                            if (transition.getMoveStatus().isDone()) {
                                getBoardPane().getMoves().push(move);
                                chessBoard = transition.getTransitionBoard();
                            }
                            getBoardPane().resetMoveSelection();
                        }

                    }
                    else if (e.getButton() == MouseButton.SECONDARY) {
                        getBoardPane().resetMoveSelection();
                    }
                    this.getBoardPane().updateBoard(chessBoard);
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
            if (getBoardPane().getSourceTile() != null) {
                if (getBoardPane().getSourceTile().getTileCoordinate() == this.getTileID()) {
                    highlightSourceSquare(selectionRectangle, getBoardPane().getPieceToMove().getPieceAlliance());
                } else {
                    highlightLegals(selectionRectangle, board);
                }
            } else if (!getBoardPane().getMoves().isEmpty()) {
                Move lastMove = getBoardPane().getMoves().peek();
                if (lastMove != null) {
                    if (lastMove.getCurrentCoordinate() == getTileID()) {
                        highlightSourceSquare(selectionRectangle, lastMove.getMovedPiece().getPieceAlliance());
                    } else if (lastMove.getDestinationCoordinate() == getTileID()) {
                        highlightMoveDestinationSquare(selectionRectangle, lastMove.getMovedPiece().getPieceAlliance());
                    }
                }
            }
            getChildren().add(selectionRectangle);
        }

        private void assignPieceOnTile(final Board board) {
            ImageView pieceView = new ImageView();
            if (getBoardPane().isCheat() || cheatAll) {
                if (board.getTile(this.getTileID()).isTileOccupied()) {
                    final Image pieceImage = new Image("images/pieces/" + board.getTile(this.getTileID()).getPiece().getPieceAlliance().toString().substring(0, 1)
                            + board.getTile(this.getTileID()).getPiece().toString() + ".png");
                    pieceView.setImage(pieceImage);
                }
            }
            this.getChildren().add(pieceView);
        }

        private void setTileColor() {
            if (BoardUtils.FIRST_ROW[this.getTileID()] ||
                    BoardUtils.THIRD_ROW[this.getTileID()] ||
                    BoardUtils.FIFTH_ROW[this.getTileID()] ||
                    BoardUtils.SEVENTH_ROW[this.getTileID()]) {
                setStyle(this.getTileID() % 2 == 0 ? WHITE_TILE : BLACK_TILE);
            } else if (BoardUtils.SECOND_ROW[this.getTileID()] ||
                    BoardUtils.FOURTH_ROW[this.getTileID()] ||
                    BoardUtils.SIXTH_ROW[this.getTileID()] ||
                    BoardUtils.EIGHT_ROW[this.getTileID()]) {
                setStyle(this.getTileID() % 2 != 0 ? WHITE_TILE : BLACK_TILE);
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
            if (isHighlightLegalMoves()) {
                if (getBoardPane().getPieceToMove() != null &&
                    getBoardPane().getPieceToMove().getPieceAlliance() == board.currentPlayer().getAlliance()) {
                    final Move move = Move.MoveFactory.createMove(board, getBoardPane().getSourceTile().getTileCoordinate(), this.getTileID());
                    final MoveTransition transition = board.currentPlayer().makeMove(move);
                    if (transition.getMoveStatus().isDone()) {
                        highlightDestinationSquare(selectionRectangle, getBoardPane().getPieceToMove().getPieceAlliance());
                    }
                }
            }
        }

        public int getTileID() {
            return this.tileID;
        }

        public BoardPane getBoardPane() {
            return this.boardPane;
        }
    }

    private class AIThinker implements Runnable {

        private final BoardPane boardPane;
        private final int searchDepth;

        private AIThinker(BoardPane boardPane, int depth) {
            this.boardPane = boardPane;
            this.searchDepth = depth;
        }

        @Override
        public void run() {
            if (!getBoardPane().isGameOver()) {
                try {
                    Board board = getBoardPane().getBoard();
                    MiniMax minimax = new MiniMax(board, getSearchDepth());
                    Move bestMove = minimax.compute();
                    Thread.sleep(0);
                    getBoardPane().getMoves().push(bestMove);
                    getBoardPane().updateBoard(board.currentPlayer().makeMove(bestMove).getTransitionBoard());
                }
                catch (InterruptedException ex) {

                }
            }
        }

        public BoardPane getBoardPane() {
            return boardPane;
        }

        public int getSearchDepth() {
            return searchDepth;
        }
    }
}
