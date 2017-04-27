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
    private ScheduledExecutorService computerMoveExecutor = Executors.newScheduledThreadPool(1);
    private Scene scene = new Scene(mainBorderPane, 700, 760);
    private final CheckMenuItem fullScreenMenuItem = new CheckMenuItem("Full Screen");
    private final CheckMenuItem smoothTransitionMenuItem = new CheckMenuItem("Smooth Transition");
    private final Text status = new Text("No games in progress");
    private final HBox footer = makeFooter(getStatus());

    public GUI() {
        initialize();
    }

    private void initialize() {
        getIcons().add(new Image("images/mainIcon.jpg"));
        scene.getStylesheets().add("main.css");
        getSmoothTransitionMenuItem().setSelected(true);
        setHighlightLegalMoves(true);
        List<GamePane> games = new ArrayList<>();
        games.add(new GamePane(0, Board.createStandardGameBoard(PlayerType.HUMAN, PlayerType.HUMAN), Difficulty.EASY.getDepth()));
        this.gamesViewer = new GamesViewer(games);
        getGamesViewer().noGameControls();
        getMainBorderPane().setTop(createMenuBar());
        getMainBorderPane().setCenter(getGamesViewer());
        getMainBorderPane().setBottom(getFooter());
        getMainBorderPane().setId("main-border-pane");
        setScene(scene);
        setTitle("Blindfold Chess Trainer");
        show();
        setOnCloseRequest(e -> {
            getComputerMoveExecutor().shutdownNow();
            close();
        });
        fullScreenProperty().addListener(e -> {
            if (isFullScreen())
                getFullScreenMenuItem().setSelected(true);
            else
                getFullScreenMenuItem().setSelected(false);
        });
    }

    private HBox makeFooter(Text status) {
        HBox hb = new HBox();
        hb.setId("footer");
        status.setId("status-text");
        hb.setAlignment(Pos.BASELINE_LEFT);
        hb.getChildren().add(status);
        return hb;
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
                BoardPane current = getGamesViewer().getGames().get(i).getBoardPane();
                current.redrawBoard(current.getCurrentViewBoard());
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
                BoardPane current = getGamesViewer().getGames().get(i).getBoardPane();
                current.redrawBoard(current.getCurrentViewBoard());
            }
        });
        highlightCheckMenuItem.setSelected(true);
        this.setHighlightLegalMoves(true);
        // set highliting to false as default

        final MenuItem newGameMenuItem = new MenuItem("New Game/s");
        newGameMenuItem.setOnAction(e -> {
            CreateGame createGame = new CreateGame("New Game/s");
            if (createGame.getNumberOfGames() != -1) {
                getComputerMoveExecutor().shutdownNow();
                setComputerMoveExecutor(Executors.newScheduledThreadPool(1));
                final int numberOfGames = createGame.getNumberOfGames();
                final ColorChoice colorChoice = createGame.getColorChoice();
                final Difficulty difficulty = createGame.getDifficulty();
                List<GamePane> games = new ArrayList<>();
                for (int i = 0; i < numberOfGames; i++) {
                    if (colorChoice.getAlliance().isWhite())
                        games.add(new GamePane(i, Board.createStandardGameBoard(PlayerType.HUMAN, PlayerType.COMPUTER), difficulty.getDepth()));
                    else
                        games.add(new GamePane(i, Board.createStandardGameBoard(PlayerType.COMPUTER, PlayerType.HUMAN), difficulty.getDepth()));
                }
                setGamesViewer(new GamesViewer(games));
                getMainBorderPane().setCenter(getGamesViewer());
            }
        });

        final MenuItem addGameMenuItem = new MenuItem("Add Game/s");
        addGameMenuItem.setOnAction(e -> {
            CreateGame createGame = new CreateGame("Add Game/s");
            if (createGame.getNumberOfGames() != -1) {
                final int numberOfGames = createGame.getNumberOfGames();
                final ColorChoice colorChoice = createGame.getColorChoice();
                final Difficulty difficulty = createGame.getDifficulty();
                final int offset = getGamesViewer().getGames().size();
                List<GamePane> games = new ArrayList<>();
                for (int i = offset; i < numberOfGames + offset; i++) {
                    if (colorChoice.getAlliance().isWhite())
                        games.add(new GamePane(i, Board.createStandardGameBoard(PlayerType.HUMAN, PlayerType.COMPUTER), difficulty.getDepth()));
                    else
                        games.add(new GamePane(i, Board.createStandardGameBoard(PlayerType.COMPUTER, PlayerType.HUMAN), difficulty.getDepth()));
                }
                getGamesViewer().addGames(games);
            }
        });

        getFullScreenMenuItem().setOnAction(e -> {
            if (isFullScreen())
                setFullScreen(false);
            else
                setFullScreen(true);
        });

        final CheckMenuItem soundCheckMenuItem = new CheckMenuItem("Sound");
        soundCheckMenuItem.setOnAction(e -> {

        });

        playMenu.getItems().addAll(newGameMenuItem, addGameMenuItem);

        preferencesMenu.getItems().addAll(flipBoardMenuItem, highlightCheckMenuItem, getFullScreenMenuItem(), soundCheckMenuItem, cheatCheckMenuItem, getSmoothTransitionMenuItem());

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

    public CheckMenuItem getFullScreenMenuItem() {
        return fullScreenMenuItem;
    }

    public CheckMenuItem getSmoothTransitionMenuItem() {
        return smoothTransitionMenuItem;
    }

    public Text getStatus() {
        return status;
    }

    public HBox getFooter() {
        return footer;
    }

    public final class GamesViewer extends GridPane {

        private final List<GamePane> games;
        private final List<GamePane> activeGames;
        private Pagination pagination;
        private final Button btResign = resignButton();
        private final Button btDraw = drawButton();
        private final Button btWin = winButton();
        private final Button btCheat = cheatButton();
        private final Button btFlip = flipButton();
        private final Button btTakeBack = takeBackButton();
        private final Button btResetGame = resetButton();
        private final Button btMakeMove = makeMoveButton();
        private final VBox options = createOptions();
        private final SequentialTransition st = new SequentialTransition();
        private GamePane currentGame;
        private GamePane previousGame;
        private final StackPane gameContainer = new StackPane();

        public GamesViewer(List<GamePane> games) {
            this.games = games;
            this.activeGames  = new ArrayList<>(getGames());
            this.setPagination(createPaginationControl(0));
            this.currentGame = games.get(0);
            initialize();
        }

        public void initialize() {
            getStyleClass().add("games-viewer");
            getGameContainer().getChildren().add(getCurrentGame());
            add(getPagination(), 0, 0);
            add(getGameContainer(), 0, 1);
            add(getOptions(), 1, 1);
            setAlignment(Pos.CENTER);
        }

        public VBox createOptions() {
            VBox options = new VBox();
            options.setPadding(new Insets(20, 20, 0, 2));
            options.getChildren().addAll(getBtMakeMove(), getBtResign(), getBtDraw(), getBtWin(), getBtCheat(), getBtFlip(), getBtTakeBack(), getBtResetGame());
            return options;
        }

        public Pagination createPaginationControl(int currentIndex) {
            Pagination p = new Pagination(getGames().size());
            p.currentPageIndexProperty().addListener((InvalidationListener) -> {
                disableControls();
                final GamePane nextGame = getGames().get(getPagination().getCurrentPageIndex());
                if (nextGame.getBoardPane().isCheat()) {
                    nextGame.getBoardPane().setCheat(false);
                    nextGame.getBoardPane().redrawBoard(nextGame.getBoardPane().getCurrentViewBoard());
                }
                if (getSmoothTransitionMenuItem().isSelected())
                    smoothTransition(nextGame);
                else
                    fastTransition(nextGame);
            });
            return p;
        }

        private void fastTransition(GamePane nextGame) {
            resetOptions();
            getGameContainer().getChildren().set(0, nextGame);
            setCurrentGame(nextGame);
            if (getCurrentGame().getBoardPane().isGameOver())
                gameOverControls();
            else
                enableControls();
        }

        public void smoothTransition(GamePane nextGame) {
            PauseTransition pt = new PauseTransition(Duration.millis(200));
            FadeTransition ft = new FadeTransition(Duration.millis(600), getCurrentGame());
            ft.setFromValue(1.0);
            ft.setToValue(0.0);
            ft.setOnFinished(e -> {
                getGameContainer().getChildren().set(0, nextGame);
                resetOptions();
            });
            PauseTransition pt2 = new PauseTransition(Duration.millis(100));
            FadeTransition ft2 = new FadeTransition(Duration.millis(600), nextGame);
            ft2.setFromValue(0.0);
            ft2.setToValue(1.0);
            getSt().getChildren().clear();
            getSt().getChildren().addAll(pt, ft, pt2, ft2);
            getSt().play();
            getSt().setOnFinished(e -> {
                getCurrentGame().setOpacity(1.0);
                setCurrentGame(nextGame);
                if (getCurrentGame().getBoardPane().isGameOver()) {
                    gameOverControls();
                }
                else
                    enableControls();
            });
        }

        public void updatePage() {
            if (!getActiveGames().isEmpty()) {
                setPreviousGame(getCurrentGame());
                int i = getActiveGames().indexOf(getCurrentGame());
                if (i == getActiveGames().size() - 1)
                    getPagination().setCurrentPageIndex(getActiveGames().get(0).getGameID());
                else
                    getPagination().setCurrentPageIndex(getActiveGames().get(i + 1).getGameID());
//                if (getActiveGames().get(getPagination().getCurrentPageIndex()).getBoardPane().isGameOver()) {
//                    new Thread(() -> {
//                        try {
//                            if (getSmoothTransitionMenuItem().isSelected()) {
//                                Thread.sleep(3000);
//                                updatePage();
//                            }
//                            else {
//                                Thread.sleep(2000);
//                                updatePage();
//                            }
//                        }
//                        catch (InterruptedException ex) {
//
//                        }
//                    }).start();
//                }
            }
        }

        public void addGames(List<GamePane> games) {
            int currentGameIndex = getPagination().getCurrentPageIndex();
            getGames().addAll(games);
            getActiveGames().addAll(games);
            if (getSmoothTransitionMenuItem().isSelected()) {
                getSmoothTransitionMenuItem().setSelected(false);
                getPagination().setPageCount(getGames().size());
                getPagination().setCurrentPageIndex(currentGameIndex);
                getSmoothTransitionMenuItem().setSelected(true);
            }
            else {
                getPagination().setPageCount(getGames().size());
                getPagination().setCurrentPageIndex(currentGameIndex);
            }
        }


        private Button makeMoveButton() {
            Button b = new Button();
            b.setId("make-move-button");
            b.getStyleClass().add("options-buttons");
            b.setOnAction(e -> {
                getCurrentGame().makeAMove();
            });
            return b;
        }

        private Button resignButton() {
            Button b = new Button();
            b.setId("resign-button");
            b.getStyleClass().add("options-buttons");
            b.setOnAction(e -> {
                getCurrentGame().resign();
                gameOverControls();
                updatePage();
                getActiveGames().remove(getPreviousGame());
            });
            return b;
        }

        private Button drawButton() {
            Button b = new Button();
            b.setId("draw-button");
            b.getStyleClass().add("options-buttons");
            b.setOnAction(e -> {
                getCurrentGame().makeDraw();
                gameOverControls();
                updatePage();
                getActiveGames().remove(getPreviousGame());
            });
            return b;
        }

        private Button winButton() {
            Button b = new Button();
            b.setId("win-button");
            b.getStyleClass().add("options-buttons");
            b.setOnAction(e -> {
                getCurrentGame().win();
                gameOverControls();
                updatePage();
                getActiveGames().remove(getPreviousGame());
            });
            return b;
        }

        private Button cheatButton() {
            Button b = new Button();
            b.setId("cheat-button");
            b.getStyleClass().add("options-buttons");
            b.setOnAction(e -> {
                BoardPane boardPane = getCurrentGame().getBoardPane();
                if (boardPane.isCheat()) {
                    boardPane.setCheat(false);
                    boardPane.redrawBoard(boardPane.getCurrentViewBoard());
                    b.setId("cheat-button");
                }
                else {
                    boardPane.setCheat(true);
                    boardPane.redrawBoard(boardPane.getCurrentViewBoard());
                    b.setId("play-fair-button");
                }
            });
            return b;
        }

        private Button flipButton() {
            Button b = new Button();
            b.setId("flip-button");
            b.getStyleClass().add("options-buttons");
            b.setOnAction(e -> {
                getCurrentGame().flipBoard();
            });
            return b;
        }

        private Button takeBackButton() {
            Button b = new Button();
            b.setId("take-back-button");
            b.getStyleClass().add("options-buttons");
            b.setOnAction(e -> {
                getCurrentGame().takeBack();
            });
            return b;
        }

        private Button resetButton() {
            Button b = new Button();
            b.setId("reset-button");
            b.getStyleClass().add("options-buttons");
            b.setOnAction(e -> {
                getCurrentGame().resetGame();
            });
            return b;
        }

        public void resetOptions() {
            getBtCheat().setId("cheat-button");
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

        public void gameOverControls() {
            disableControls();
            getBtCheat().setDisable(false);
            getBtFlip().setDisable(false);
            getPagination().setDisable(false);
        }

        public void noGameControls() {
            disableControls();
            getBtCheat().setDisable(false);
            getBtFlip().setDisable(false);
            getBtTakeBack().setDisable(false);
            getBtResetGame().setDisable(false);
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

        public Button getBtFlip() {
            return btFlip;
        }

        public StackPane getGameContainer() {
            return gameContainer;
        }

        public List<GamePane> getActiveGames() {
            return activeGames;
        }

        public Button getBtMakeMove() {
            return btMakeMove;
        }

        public GamePane getPreviousGame() {
            return previousGame;
        }

        public void setPreviousGame(GamePane previousGame) {
            this.previousGame = previousGame;
        }

        public Button getBtResign() {
            return btResign;
        }

        public Button getBtDraw() {
            return btDraw;
        }

        public Button getBtWin() {
            return btWin;
        }

        public Button getBtTakeBack() {
            return btTakeBack;
        }

        public Button getBtResetGame() {
            return btResetGame;
        }

        public void setPagination(Pagination pagination) {
            this.pagination = pagination;
        }
    }

    public final class GamePane extends BorderPane {

        private final int gameID;
        private final StackPane boardBackground = new StackPane();
        private BoardPane boardPane;
        private Text whiteMoveText = new Text("1.?");
        private Text blackMoveText = new Text("");
        private final HBox whiteSide = new HBox(7);
        private final HBox blackSide = new HBox(7);
        private final Button btPrevPrev = prevPrevButton();
        private final Button btPrev = prevButton();
        private final Button btNext = nextButton();
        private final Button btNextNext = nextNextButton();

        public GamePane(final int gameID, Board board, final int depth) {
            this.gameID = gameID;
            this.boardPane = new BoardPane(this, board, depth);
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
            boardAndMovesPane.setBottom(bottomControls());
            this.getBoardPane().setAlignment(Pos.CENTER);
            this.getBoardBackground().getChildren().add(getBoardPane());
            boardAndMovesPane.setCenter(getBoardBackground());
            drawBackground();
            setCenter(boardAndMovesPane);
            setPadding(new Insets(0, 0, 20, 20));
            setAlignment(boardAndMovesPane, Pos.TOP_CENTER);
        }

        private void drawBackground() {
            getBoardBackground().getStyleClass().add("board-background");
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
            BoardPane boardPane = this.getBoardPane();
            Collections.reverse(boardPane.getBoardTiles());
            swapSides();
            boardPane.redrawBoard(boardPane.getCurrentViewBoard());
        }

        private void initWhiteSide() {
            Circle circle = new Circle(5, Color.WHITE);
            circle.setStroke(Color.BLACK);
            getWhiteMoveText().setLineSpacing(1);
            getWhiteSide().getChildren().addAll(circle, getWhiteMoveText());
            getWhiteSide().setAlignment(Pos.BASELINE_LEFT);
            //replay controls
            getWhiteSide().getChildren().addAll(getBtPrevPrev(), getBtPrev(), getBtNext(), getBtNextNext());
        }

        private void initBlackSide() {
            Circle circle = new Circle(5, Color.BLACK);
            circle.setStroke(Color.BLACK);
            getBlackMoveText().setLineSpacing(1);
            getBlackSide().getChildren().addAll(getBlackMoveText(), circle);
            getBlackSide().setAlignment(Pos.BASELINE_RIGHT);
        }

        private Button nextNextButton() {
            Button b = new Button();
            b.setId("next-next-button");
            b.getStyleClass().add("replay-button");
            b.setOnAction(e -> {
                if (!getBoardPane().getMoves().isEmpty()) {
                    getBoardPane().redrawBoard(getBoardPane().lastBoard());
                }
            });
            return b;
        }

        private Button prevPrevButton() {
            Button b = new Button();
            b.setId("prev-prev-button");
            b.getStyleClass().add("replay-button");
            b.setOnAction(e -> {
                if (!getBoardPane().getMoves().isEmpty()) {
                    getBoardPane().redrawBoard(getBoardPane().firstBoard());
                }
            });
            return b;
        }

        private Button nextButton() {
            Button b = new Button();
            b.setId("next-button");
            b.getStyleClass().add("replay-button");
            b.setOnAction(e -> {
                if (!getBoardPane().getMoves().isEmpty()) {
                    getBoardPane().redrawBoard(getBoardPane().nextBoard());
                }
            });
            return b;
        }

        private Button prevButton() {
            Button b = new Button();
            b.setId("prev-button");
            b.getStyleClass().add("replay-button");
            b.setOnAction(e -> {
                if (!getBoardPane().getMoves().isEmpty()) {
                    getBoardPane().redrawBoard(getBoardPane().previousBoard());
                }
            });
            return b;
        }

        public HBox replayControls() {
            HBox hb = new HBox();
            hb.getChildren().addAll(getBtPrevPrev(), getBtPrev(), getBtNext(), getBtNextNext());
            hb.setAlignment(Pos.BASELINE_RIGHT);
            return hb;
        }

        public BorderPane bottomControls() {
            BorderPane bp = new BorderPane();
            bp.setLeft(getWhiteSide());
            bp.setRight(replayControls());
            return bp;
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

        public void makeAMove() {
            if (getBoardPane().getFutureComputerMove() != null && getBoardPane().isRunning())
                getBoardPane().getFutureComputerMove().cancel(true);
        }

        public void resign() {
            if (!getBoardPane().isGameOver()) {
                interruptComputerMove();
                getBoardPane().setGameOver(true);
                if (getBoardPane().getBoard().whitePlayer().getPlayerType().isHuman()) {
                    getBoardPane().updateResultAsLoss(getBoardPane().getBoard().whitePlayer(), getBoardPane().getBoard().currentPlayer());
                }
                else {
                    getBoardPane().updateResultAsLoss(getBoardPane().getBoard().blackPlayer(), getBoardPane().getBoard().currentPlayer());
                }
            }
        }


        public void makeDraw() {
            if (!getBoardPane().isGameOver()) {
                interruptComputerMove();
                //TODO
                getBoardPane().setGameOver(true);
                getBoardPane().updateResultAsDraw(getBoardPane().getBoard().currentPlayer());
            }
        }

        public void win() {
            if (!getBoardPane().isGameOver()) {
                interruptComputerMove();
                //TODO
                getBoardPane().setGameOver(true);
                if (getBoardPane().getBoard().whitePlayer().getPlayerType().isComputer()) {
                    getBoardPane().updateResultAsLoss(getBoardPane().getBoard().whitePlayer(), getBoardPane().getBoard().currentPlayer());
                }
                else {
                    getBoardPane().updateResultAsLoss(getBoardPane().getBoard().blackPlayer(), getBoardPane().getBoard().currentPlayer());
                }
            }
        }

        public void resetGame() {
            Stack<Move> moves = getBoardPane().getMoves();
            if (!moves.isEmpty() && !getBoardPane().isGameOver()) {
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
            if (!moves.isEmpty() && !getBoardPane().isGameOver()) {
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

        private void updateMovesText(final Board board, final Move move, final int moveNumber) {
            if (move == null) {
                getWhiteMoveText().setText("1.?");
                getBlackMoveText().setText("");
            }
            else {
                String moveToString = getBoardPane().checkOrCheckMate(board, move);
                if (moveNumber % 2 == 0) {
                    getBlackMoveText().setText(moveNumber / 2 + "..." + moveToString);
                    getWhiteMoveText().setText(moveNumber / 2 + 1 + ".?");
                }
                else {
                    getBlackMoveText().setText((moveNumber + 1) / 2 + "...?");
                    getWhiteMoveText().setText((moveNumber + 1) / 2 + "." + moveToString);
                }
            }
        }

        public void thinking() {
            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                    int count = 0;
                    while (!getBoardPane().getFutureComputerMove().isDone() &&
                           !getBoardPane().getFutureComputerMove().isCancelled()) {
                        switch (count) {
                            case 0: {
                                thinking("Thinking");
                                count++;
                                break;
                            }
                            case 1: {
                                thinking("Thinking.");
                                count++;
                                break;
                            }
                            case 2: {
                                thinking("Thinking..");
                                count++;
                                break;
                            }
                            case 3: {
                                thinking("Thinking...");
                                count = 0;
                                break;
                            }
                        }
                        Thread.sleep(300);
                    }
                    notThinking();
                }
                catch (InterruptedException ex) {
                    notThinking();
                }
            }).start();
        }

        public void notThinking() {
            Platform.runLater(() -> {
                int numberOfActiveGames = getGamesViewer().getActiveGames().size();
                if (numberOfActiveGames == 0)
                    getStatus().setText("No games in progress");
                else if (numberOfActiveGames == 1)
                    getStatus().setText("1 game in progress");
                else
                    getStatus().setText(numberOfActiveGames + " games in progress");
            });
        }

        public void thinking(String s) {
            Platform.runLater(() -> {
                getStatus().setText(s);
            });
        }

        public void interruptComputerMove() {
            if (getBoardPane().getFutureComputerMove() != null) {
                if (getBoardPane().getBoard().currentPlayer().getPlayerType().isComputer()) {
                    getBoardPane().setInterrupt(true);
                    getBoardPane().getFutureComputerMove().cancel(true);
                }
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

        public Button getBtPrevPrev() {
            return btPrevPrev;
        }

        public Button getBtPrev() {
            return btPrev;
        }

        public Button getBtNext() {
            return btNext;
        }

        public Button getBtNextNext() {
            return btNextNext;
        }

        public int getGameID() {
            return gameID;
        }
    }

    public final class BoardPane extends GridPane {

        private final GamePane game;
        private final List<TilePane> boardTiles = new ArrayList<>();
        private final Stack<Move> moves = new Stack<>();
        private Board board;
        private final int depth;
        private Piece pieceToMove;
        private Tile sourceTile;
        private Tile destinationTile;
        private Move lastMove;
        private boolean gameOver;
        private boolean cheat;
        private volatile boolean interrupt;
        private Future<?> futureComputerMove;
        private volatile boolean running;
        private int currentBoardIndex = 0;

        public BoardPane(GamePane game, Board board, int depth) {
            this.game = game;
            this.board = board;
            this.depth = depth;
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
            setCurrentBoardIndex(getMoves().size());
            if (!getMoves().isEmpty()) {
                getGame().updateMovesText(board, getMoves().peek(), getMoves().size());
                setLastMove(getMoves().peek());
            }
            else
                setLastMove(null);
            isGameOver(board);
            redrawBoard(board);
            if (board.currentPlayer().getPlayerType().isComputer() && !isGameOver()) {
                makeMove();
            }
        }

        public synchronized void redrawBoard(Board board) {
            getChildren().clear();
            int index = 0;
            for (int i = 0; i < BoardUtils.NUM_TILES_PER_ROW; i++) {
                for (int j = 0; j < BoardUtils.NUM_TILES_PER_ROW; j++) {
                    this.getBoardTiles().get(index).drawTile(board);
                    add(this.getBoardTiles().get(index), j, i);
                    index++;
                }
            }
        }

        public void makeMove() {
            AIThinker makeComputerMove = new AIThinker(this, this.getDepth());
            if (getGamesViewer().getActiveGames().size() > 1) {
                //TODO
                if (!getMoves().isEmpty()) {
                    getGamesViewer().updatePage();
                    setFutureComputerMove(getComputerMoveExecutor().schedule(makeComputerMove, 900, TimeUnit.MILLISECONDS));
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
                getGamesViewer().gameOverControls();
            }
            else if (board.currentPlayer().isInStalemate()) {
                setGameOver(true);
                updateResultAsDraw(board.currentPlayer());
                getGamesViewer().gameOverControls();
            }
            if (isGameOver()) {
//                if (getGamesViewer().getCurrentGame() == getGame()) {
//                    getGamesViewer().updatePage();
//                    getGamesViewer().getActiveGames().remove(getGamesViewer().getPreviousGame());
//                }
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

        public Board previousBoard() {
            if (getCurrentBoardIndex() < 2) {
                return firstBoard();
            }
            setCurrentBoardIndex(getCurrentBoardIndex() - 1);
            Board board = getMoves().get(getCurrentBoardIndex()).getBoard();
            Move move = getMoves().get(getCurrentBoardIndex() - 1);
            setLastMove(move);
            resetMoveSelection();
            getGame().updateMovesText(board, move, getCurrentBoardIndex());
            return board;
        }

        public Board nextBoard() {
            if (getCurrentBoardIndex() >= getMoves().size() - 1) {
                return lastBoard();
            }
            setCurrentBoardIndex(getCurrentBoardIndex() + 1);
            Board board = getMoves().get(getCurrentBoardIndex()).getBoard();
            Move move = getMoves().get(getCurrentBoardIndex() - 1);
            setLastMove(move);
            resetMoveSelection();
            getGame().updateMovesText(board, move, getCurrentBoardIndex());
            return board;
        }

        public Board firstBoard() {
            setCurrentBoardIndex(0);
            Board board = getMoves().get(getCurrentBoardIndex()).getBoard();
            setLastMove(null);
            resetMoveSelection();
            getGame().updateMovesText(board, null, 0);
            return board;
        }

        public Board lastBoard() {
            setCurrentBoardIndex(getMoves().size());
            Move move = getMoves().get(getCurrentBoardIndex() - 1);
            setLastMove(move);
            resetMoveSelection();
            getGame().updateMovesText(getBoard(), move, getCurrentBoardIndex());
            return getBoard();
        }

        public Board getCurrentViewBoard() {
            if (getMoves().isEmpty() || getCurrentBoardIndex() == getMoves().size())
                return getBoard();
            if (getCurrentBoardIndex() == 0)
                return firstBoard();
            return getMoves().get(getCurrentBoardIndex()).getBoard();
        }

        public int getCurrentBoardIndex() {
            return currentBoardIndex;
        }

        public void setCurrentBoardIndex(int currentBoardIndex) {
            this.currentBoardIndex = currentBoardIndex;
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

        public void setBoard(Board board) {
            this.board = board;
        }

        public GamePane getGame() {
            return game;
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

        public boolean isGameOver() {
            return this.gameOver;
        }

        public void setGameOver(boolean gameOver) {
            this.gameOver = gameOver;
        }

        public boolean isInterrupt() {
            return interrupt;
        }

        public void setInterrupt(boolean interrupt) {
            this.interrupt = interrupt;
        }

        public int getDepth() {
            return depth;
        }

        public boolean isRunning() {
            return running;
        }

        public void setRunning(boolean running) {
            this.running = running;
        }

        public Move getLastMove() {
            return lastMove;
        }

        public void setLastMove(Move lastMove) {
            this.lastMove = lastMove;
        }
    }


    public final class TilePane extends StackPane {

        private final int tileID;
        private final BoardPane boardPane;
        private final String WHITE_TILE = "-fx-background-image: url('images/lightwoodtile.jpg');";
        private final String BLACK_TILE = "-fx-background-image: url('images/darkwoodtile.jpg');";
        private final ImageView pieceImageView = new ImageView();
        private final Region tileSelection = new Region();

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
            // Resizing
            resizeTile();
            scene.widthProperty().addListener(InvalidationListener -> {
                resizeTile();
            });

            scene.heightProperty().addListener(InvalidationListener -> {
                resizeTile();
            });
        }

        private void resizeTile() {
            double width = (scene.getWidth() - 110) / 8;
            double height = (scene.getHeight() - 230) / 8;
            if (height > 30 && width > 30) {
                if (height < width) {
                    getPieceImageView().setFitHeight(height);
                    getPieceImageView().setFitWidth(height);
                }
                else {
                    getPieceImageView().setFitHeight(width);
                    getPieceImageView().setFitWidth(width);
                }
            }
        }

        private void drawTile(final Board board) {
            getChildren().clear();
            setTileColor();
            assignSelectionRectangle(board);
            assignPieceOnTile(board);
        }

        private void assignSelectionRectangle(final Board board) {
            getTileSelection().getStyleClass().clear();
            if (getBoardPane().getSourceTile() != null) {
                if (getBoardPane().getSourceTile().getTileCoordinate() == this.getTileID()) {
                    highlightSourceTile(getTileSelection(), getBoardPane().getPieceToMove().getPieceAlliance());
                }
                else {
                    highlightLegals(getTileSelection(), board);
                }
            }
            else {
                Move lastMove = getBoardPane().getLastMove();
                if (lastMove != null) {
                    if (lastMove.getCurrentCoordinate() == getTileID()) {
                        highlightSourceTile(getTileSelection(), lastMove.getMovedPiece().getPieceAlliance());
                    }
                    else if (lastMove.getDestinationCoordinate() == getTileID()) {
                        highlightMoveDestinationTile(getTileSelection(), lastMove.getMovedPiece().getPieceAlliance());
                    }
                }
            }
            getChildren().add(getTileSelection());
        }

        private void assignPieceOnTile(final Board board) {
            getPieceImageView().setImage(null);
            if (getBoardPane().isCheat() || cheatAll) {
                if (board.getTile(this.getTileID()).isTileOccupied()) {
                    final Image pieceImage = new Image("images/pieces/" + board.getTile(this.getTileID()).getPiece().getPieceAlliance().toString().substring(0, 1)
                            + board.getTile(this.getTileID()).getPiece().toString() + ".png");
                    getPieceImageView().setImage(pieceImage);
                }
            }
            this.getChildren().add(getPieceImageView());
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

        private void highlightSourceTile(final Region tileSelection, final Alliance alliance) {
            if (alliance.isWhite())
                getTileSelection().getStyleClass().add("source-tile-white");
            else
                getTileSelection().getStyleClass().add("source-tile-black");
        }

        private void highlightMoveDestinationTile(final Region tileSelection, final Alliance alliance) {
            if (alliance.isWhite())
                getTileSelection().getStyleClass().add("move-destination-tile-white");
            else
                getTileSelection().getStyleClass().add("move-destination-tile-black");
        }

        private void highlightDestinationTile(final Region tileSelection, final Alliance alliance) {
            if (alliance.isWhite())
                tileSelection.getStyleClass().add("destination-tile-white");
            else
                tileSelection.getStyleClass().add("destination-tile-black");
        }

        private void highlightLegals(final Region tileSelection, final Board board) {
            if (isHighlightLegalMoves()) {
                if (getBoardPane().getPieceToMove() != null &&
                    getBoardPane().getPieceToMove().getPieceAlliance() == board.currentPlayer().getAlliance()) {
                    final Move move = Move.MoveFactory.createMove(board, getBoardPane().getSourceTile().getTileCoordinate(), this.getTileID());
                    final MoveTransition transition = board.currentPlayer().makeMove(move);
                    if (transition.getMoveStatus().isDone()) {
                        highlightDestinationTile(tileSelection, getBoardPane().getPieceToMove().getPieceAlliance());
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

        public ImageView getPieceImageView() {
            return pieceImageView;
        }

        public Region getTileSelection() {
            return tileSelection;
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
                getBoardPane().getGame().thinking();
                Board board = getBoardPane().getBoard();
                MiniMax minimax = new MiniMax(board, getSearchDepth());
                getBoardPane().setRunning(true);
                Move bestMove = minimax.compute();
                if (!getBoardPane().isInterrupt()) {
                    getBoardPane().getMoves().push(bestMove);
                    Platform.runLater(() -> {
                        getBoardPane().updateBoard(board.currentPlayer().makeMove(bestMove).getTransitionBoard());
                    });
                }
                getBoardPane().setInterrupt(false);
                getBoardPane().setRunning(false);
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
