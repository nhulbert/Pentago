/*
 * TCSS 435 - Summer 2019
 * Assignment 2 - Pentago
 * Neil Hulbert
 */

package state;

import java.util.ArrayList;
import java.util.List;

/**
 * The object that performs game tree searches to produce an AI move.
 * @author Neil Hulbert
 * @version 1.0
 */
public class Search {
    /**
     * Determines whether to use alpha beta search
     */
    public static final boolean USE_ALPHA_BETA = true;

    /**
     * Counts the number of nodes expanded
     */
    private int count;
    
    /**
     * the scoring weights used assign value to each possible
     * spot for a win for each color. streakScore[i][j] represents a line of cells
     * on the board i+NUM_TO_WIN long in which a given player could possibly win,
     * with j cells already filled in.
     */
    private double[][] streakScores;

    /**
     * Constructs a new Search object to search for a best move.
     * @param streakScores the weights to score each possible streak type
     */
    public Search(final double[][] streakScores) {
        this.streakScores = streakScores;
        count = 0;
    }

    /**
     * Evaluates a given board state at a given depth using minimax or alpha-beta search.
     * @param board the board state to evaluate
     * @param turn the color of the player on-move
     * @param depth the depth at which to evaluate the board
     * @param alpha the alpha parameter for alpha-beta search
     * @param beta the beta parameter for alpha-beta search
     * @return an evaluation and move recommendation for the position
     */
    public NodeEval evaluateAtDepth(final Board board,
                                     final byte turn,
                                     final int depth,
                                           NodeEval alpha,
                                           NodeEval beta) {
        count++;

        double initialEval = board.evaluation(streakScores);

        if (depth == 0
            || !Double.isFinite(initialEval)) {
            return new NodeEval(initialEval, depth, new int[] {-1, -1}, -1, 0);
        }

        NodeEval bestEval = null;

        int dir = (turn == Board.WHITE) ? 1 : -1;
        double winEval = dir * Board.MAX_EVAL;

        List<int[]> moves = getMoves(board, turn, dir);

        for (int[] loc : moves) {
            if (board.getLocation(loc) == Board.EMPTY) {
                board.setLocation(loc, turn);
                if (board.evaluation(streakScores) == winEval) {
                    bestEval = new NodeEval(winEval, 0, loc, -1, 0);
                    board.clearLocation(loc);

                    return bestEval;
                } else {
                    for (int quad = 0; quad < 4; quad++) {
                        int curRot = 0;

                        for (int rot = -1; rot <= 1; rot += 2) {
                            board.rotateQuadrant(quad, rot - curRot);
                            byte newColor = Board.oppositeColor(turn);

                            NodeEval newEval = evaluateAtDepth(board,
                                                             newColor,
                                                             depth - 1,
                                                             alpha,
                                                             beta);

                            if (bestEval == null || dir * newEval.compareTo(bestEval) > 0) {
                                newEval.loc = loc;
                                newEval.quad = quad;
                                newEval.rot = rot;

                                bestEval = newEval;

                                if (USE_ALPHA_BETA) {
                                    if (turn == Board.WHITE) {
                                        if (newEval.compareTo(alpha) > 0) {
                                            alpha = newEval;
                                        }
                                    } else {
                                        if (newEval.compareTo(beta) < 0) {
                                            beta = newEval;
                                        }
                                    }
                                }
                            }

                            curRot = rot;

                            if (USE_ALPHA_BETA && alpha.compareTo(beta) >= 0) {
                                board.rotateQuadrant(quad, -curRot);
                                board.clearLocation(loc);

                                return bestEval;
                            }

                        }
                        board.rotateQuadrant(quad, -curRot);

                    }
                }


                board.clearLocation(loc);
            }
        }

        return bestEval;
    }

    /**
     * Gets the moves without rotation possible from a given board state,
     * ordered by their 0-depth evaluations.
     * @param board the board state from which the moves are to be made
     * @param turn the color of the player on-move
     * @param dir the sign of the evaluation that would favor the player on-move
     * @return a list of moves ordered by their 0-depth evaluations
     */
    private List<int[]> getMoves(final Board board,
                                 final byte turn,
                                 final int dir) {
        List<int[]> temp = new ArrayList<>();
        List<Double> scores = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();

        for (int y = 0; y < Board.BOARD_HEIGHT; y++) {
            for (int x = 0; x < Board.BOARD_WIDTH; x++) {
                if (board.getLocation(new int[] {x, y}) == Board.EMPTY) {
                    int[] move = {x, y};
                    temp.add(move);
                    board.setLocation(move, turn);
                    scores.add(-dir * board.evaluation(streakScores));
                    board.clearLocation(move);
                    indices.add(indices.size());
                }
            }
        }

        indices.sort((ind1, ind2) -> Double.compare(scores.get(ind1), scores.get(ind2)));
        List<int[]> out = new ArrayList<>();
        for (Integer ind : indices) {
            out.add(temp.get(ind));
        }

        return out;
    }
}