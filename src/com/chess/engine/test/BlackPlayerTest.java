package com.chess.engine.test;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.Board.*;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.pieces.*;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Anton on 3/17/2017.
 */
public class BlackPlayerTest {

    @Test
    public void testCastleUnderCheck() {
        Builder builder = new Builder();
        builder.setPiece(new King(Alliance.BLACK, BoardUtils.getCoordinateAtPosition("e8")));
        builder.setPiece(new Rook(Alliance.BLACK, 7));
        builder.setPiece(new Bishop(Alliance.BLACK, BoardUtils.getCoordinateAtPosition("b4")));
        builder.setPiece(new King(Alliance.WHITE, 60));
        builder.setPiece(new Pawn(Alliance.WHITE, BoardUtils.getCoordinateAtPosition("h6")));
        builder.setPiece(new Rook(Alliance.WHITE, BoardUtils.getCoordinateAtPosition("h1")));
        builder.setPiece(new Pawn(Alliance.BLACK, BoardUtils.getCoordinateAtPosition("h7")));
        builder.setMoveMaker(Alliance.WHITE);
        Board board = builder.build();
        assertFalse(board.blackPlayer().isInCheck());
        assertTrue(board.currentPlayer().isInCheck());
        assertTrue(board.whitePlayer().isInCheck());
        assertEquals(11, board.currentPlayer().getLegalMoves().size());
    }
}