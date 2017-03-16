package com.chess.engine.player.ai;

import com.chess.engine.board.Board;

/**
 * Created by Anton on 3/13/2017.
 */
public interface BoardEvaluator {

    int evaluate(Board board, int depth);
}
