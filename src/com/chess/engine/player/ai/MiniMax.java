package com.chess.engine.player.ai;

import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.player.MoveTransition;

import java.util.concurrent.RecursiveTask;

/**
 * Created by Anton on 3/13/2017.
 */
public class MiniMax extends RecursiveTask implements MoveStrategy {

    private final BoardEvaluator boardEvaluator;
    private final int searchDepth;
    private final Board board;

    public MiniMax(final Board board, final int searchDepth) {
        this.boardEvaluator = new StandardBoardEvaluator();
        this.board = board;
        this.searchDepth = searchDepth;
    }

    @Override
    public String toString() {
        return "MiniMax";
    }

    @Override
    public Move compute() {
        return execute(this.board);
    }
    @Override
    public Move execute(Board board) {

        final long startTime = System.currentTimeMillis();

        Move bestMove = null;

        int highestSeenValue = Integer.MIN_VALUE;
        int lowestSeenValue = Integer.MAX_VALUE;
        int currentValue;

        System.out.println(board.currentPlayer() + "Thinking with depth = " + searchDepth);

        int numMoves = board.currentPlayer().getLegalMoves().size();

        for(final Move move : board.currentPlayer().getLegalMoves()) {
            final MoveTransition moveTransition = board.currentPlayer().makeMove(move);
            if(moveTransition.getMoveStatus().isDone()) {
                currentValue = board.currentPlayer().getAlliance().isWhite() ?
                        min(moveTransition.getTransitionBoard(), this.searchDepth - 1) :
                        max(moveTransition.getTransitionBoard(), this.searchDepth - 1);
                if(board.currentPlayer().getAlliance().isWhite() && currentValue >= highestSeenValue) {
                    highestSeenValue = currentValue;
                    bestMove = move;
                }
                else if(board.currentPlayer().getAlliance().isBlack() && currentValue <= lowestSeenValue){
                    lowestSeenValue = currentValue;
                    bestMove = move;
                }
                try {
                    Thread.sleep(0);
                }
                catch (InterruptedException ex) {
                    return bestMove;
                }
            }
        }

        final long executionTime = System.currentTimeMillis() - startTime;

        return bestMove;
    }

    public int min(final Board board, final int depth) {
        if(depth == 0 || isEndGameScenario(board)) {
            return this.boardEvaluator.evaluate(board, depth);
        }

        int lowestSeenValue = Integer.MAX_VALUE;
        for(final Move move : board.currentPlayer().getLegalMoves()) {
            final MoveTransition moveTransition = board.currentPlayer().makeMove(move);
            if(moveTransition.getMoveStatus().isDone()) {
                final int currentValue = max(moveTransition.getTransitionBoard(), depth - 1);
                if(currentValue <= lowestSeenValue) {
                    lowestSeenValue = currentValue;
                }
            }
        }
        return lowestSeenValue;
    }

    public int max(final Board board, final int depth) {
        if(depth == 0 || isEndGameScenario(board)) {
            return this.boardEvaluator.evaluate(board, depth);
        }

        int greatestSeenValue = Integer.MIN_VALUE;
        for(final Move move : board.currentPlayer().getLegalMoves()) {
            final MoveTransition moveTransition = board.currentPlayer().makeMove(move);
            if(moveTransition.getMoveStatus().isDone()) {
                final int currentValue = min(moveTransition.getTransitionBoard(), depth - 1);
                if(currentValue >= greatestSeenValue) {
                    greatestSeenValue = currentValue;
                }
            }
        }
        return greatestSeenValue;
    }

    private static boolean isEndGameScenario(final Board board) {
        return board.currentPlayer().isInCheckMate() ||
                board.currentPlayer().isInStalemate();
    }
}
