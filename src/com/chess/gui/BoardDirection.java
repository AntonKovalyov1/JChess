package com.chess.gui;

import com.google.common.collect.Lists;
import com.chess.gui.GUI.GamePane.*;

import java.util.List;

/**
 * Created by Anton on 3/17/2017.
 */
public enum BoardDirection {

    NORMAL {
        @Override
        List<TilePane> traverse(List<TilePane> tilesList) {
            return tilesList;
        }

        @Override
        BoardDirection opposite() {
            return FLIPPED;
        }
    },
    FLIPPED {
        @Override
        List<TilePane> traverse(List<TilePane> tilesList) {
            return Lists.reverse(tilesList);
        }

        @Override
        BoardDirection opposite() {
            return NORMAL;
        }
    };

    abstract List<TilePane> traverse(final List<TilePane> tilesList);
    abstract BoardDirection opposite();
}
