package com.chess.gui;

import com.chess.engine.Alliance;

/**
 * Created by Anton on 3/15/2017.
 */
public enum ColorChoice {

    WHITE {
        @Override
        public String toString() {
            return "White";
        }

        @Override
        public Alliance getAlliance() {
            return Alliance.WHITE;
        }
    },
    BLACK {
        @Override
        public String toString() {
            return "Black";
        }

        @Override
        public Alliance getAlliance() {
            return Alliance.BLACK;
        }
    },
    RANDOM {
        @Override
        public String toString() {
            return "Random";
        }

        @Override
        public Alliance getAlliance() {
            return Math.random() < 0.5 ? Alliance.BLACK : Alliance.WHITE;
        }
    };

    public abstract Alliance getAlliance();
}
