package com.chess.engine.player.ai;

import com.chess.engine.board.Board;
import com.chess.engine.pieces.Piece;
import com.chess.engine.player.Player;

/**
 * Created by Anton on 3/13/2017.
 */
public final class StandardBoardEvaluator implements BoardEvaluator {

    private static final int CHECKMATE_BONUS = 10000;
    private static final int DEPTH_BONUS = 100;
    private static final int CASTLE_BONUS = 60;

    @Override
    public int evaluate(final Board board,
                        final int depth) {
        return scorePlayer(board.whitePlayer(), depth) -
               scorePlayer(board.blackPlayer(), depth);
    }

    private int scorePlayer(final Player player,
                            final int depth) {
        return pieceValue(player) +
               mobility(player) +
               checkmate(player, depth) +
               castled(player);
        // + other heuristics
    }

    private static int castled(Player player) {
        return player.isCastled() ? CASTLE_BONUS : 0;
    }

    private int checkmate(Player player, int depth) {
        return player.getOpponent().isInCheckMate() ? CHECKMATE_BONUS * depthBonus(depth) : 0;
    }

    private static int depthBonus(int depth) {
        return depth == 0 ? 1 : DEPTH_BONUS * depth;
    }

    private int mobility(final Player player) {
        return player.getLegalMoves().size();
    }

    private static int pieceValue(Player player) {
        int pieceValueScore = 0;
        for(final Piece piece : player.getActivePieces()) {
            pieceValueScore += piece.getPieceValue();
        }
        return pieceValueScore;
    }
}
