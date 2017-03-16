package com.chess.engine.player.ai;

import com.chess.engine.board.Board;
import com.chess.engine.board.Move;

/**
 * Created by Anton on 3/13/2017.
 */
public interface MoveStrategy {

    Move execute(Board board);

}
