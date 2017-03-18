package com.chess.engine.player;

/**
 * Created by Anton on 3/17/2017.
 */
public enum PlayerType {
    HUMAN {
        @Override
        public boolean isHuman() {
            return true;
        }

        @Override
        public PlayerType opposite() {
            return PlayerType.COMPUTER;
        }
    },
    COMPUTER {
        @Override
        public boolean isHuman() {
            return false;
        }

        @Override
        public PlayerType opposite() {
            return PlayerType.HUMAN;
        }
    };

    public abstract boolean isHuman();
    public abstract PlayerType opposite();
}
