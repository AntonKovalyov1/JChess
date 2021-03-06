package com.chess.engine.player;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.board.Move.*;
import com.chess.engine.board.Tile;
import com.chess.engine.pieces.Piece;
import com.chess.engine.pieces.Rook;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Anton on 1/23/2017.
 */
public class BlackPlayer extends Player{
    public BlackPlayer(Board board,
                       Collection<Move> whiteStandardLegalMoves,
                       Collection<Move> blackStandardLegalMoves,
                       PlayerType playerType) {

        super(board, blackStandardLegalMoves, whiteStandardLegalMoves, playerType);
    }

    @Override
    protected Collection<Move> calculateKingCastles(Collection<Move> playerLegals, Collection<Move> opponentsLegals) {

        final List<Move> kingCastles = new ArrayList<>();

        if (this.playerKing.isFirstMove() && this.playerKing.getPiecePosition() == 4) {
            if(!this.board.getTile(5).isTileOccupied() && !this.board.getTile(6).isTileOccupied()) {
                final Tile rookTile = this.board.getTile(7);
                if(rookTile.isTileOccupied() && rookTile.getPiece().isFirstMove()) {
                    if(Player.calculateAttacksOnTile(4, opponentsLegals).isEmpty() &&
                       Player.calculateAttacksOnTile(5, opponentsLegals).isEmpty() &&
                       Player.calculateAttacksOnTile(6, opponentsLegals).isEmpty() &&
                       rookTile.getPiece().getPieceType().isRook()) {
                        kingCastles.add(new KingsideCastleMove(this.board,
                                this.playerKing,
                                6,
                                (Rook)rookTile.getPiece(),
                                rookTile.getTileCoordinate(),
                                5));
                    }
                }
            }
            if(!this.board.getTile(3).isTileOccupied() &&
                    !this.board.getTile(2).isTileOccupied() &&
                    !this.board.getTile(1).isTileOccupied()) {
                final Tile rookTile = this.board.getTile(0);
                if(rookTile.isTileOccupied() && rookTile.getPiece().isFirstMove()) {
                    if(Player.calculateAttacksOnTile(4, opponentsLegals).isEmpty() &&
                       Player.calculateAttacksOnTile(3, opponentsLegals).isEmpty() &&
                       Player.calculateAttacksOnTile(2, opponentsLegals).isEmpty() &&
                       rookTile.getPiece().getPieceType().isRook()) {
                        kingCastles.add(new QueensideCastleMove(this.board,
                                this.playerKing,
                                2,
                                (Rook)rookTile.getPiece(),
                                rookTile.getTileCoordinate(),
                                3));
                    }
                }
            }
        }
        return ImmutableList.copyOf(kingCastles);
    }

    @Override
    public Alliance getAlliance() {
        return Alliance.BLACK;
    }

    @Override
    public Player getOpponent() {
        return this.board.whitePlayer();
    }

    @Override
    public Collection<Piece> getActivePieces() {
        return this.board.getBlackPieces();
    }
}
