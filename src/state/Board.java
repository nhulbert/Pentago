/*
 * TCSS 435 - Summer 2019
 * Assignment 2 - Pentago
 * Neil Hulbert
 */

package state;

/**
 * A definition for a Pentago board, that describes the entire state space.
 * @author Neil Hulbert
 * @version 1.0
 */
public class Board {
    /**
     * The width of the board.
     */
    public static final int BOARD_WIDTH = 6;
    /**
     * The height of the board.
     */
    public static final int BOARD_HEIGHT = 6;
    /**
     * The number of cells in the board.
     */
    public static final int BOARD_SIZE = BOARD_WIDTH * BOARD_HEIGHT;
    /**
     * The byte which represents an empty cell.
     */
    public static final byte EMPTY = 0b00;
    /**
     * The byte which represents a black cell.
     */
    public static final byte BLACK = 0b01;
    /**
     * The byte which represents a white cell.
     */
    public static final byte WHITE = 0b11;
    /**
     * The byte which represents a tied, finished game state.
     */
    public static final byte TIE = 0b10;
    /**
     * The byte which represents an unfinished game state.
     */
    public static final byte UNFINISHED = 0b00;
    /**
     * The minimum possible evaluation of the board.
     */
    public static final double MIN_EVAL = Double.NEGATIVE_INFINITY;
    /**
     * The maximum posible evaluation of the board.
     */
    public static final double MAX_EVAL = Double.POSITIVE_INFINITY;
    /**
     * The evaluation for a tied, finished game.
     */
    public static final double TIE_EVAL = Double.NaN;
    /**
     * The width of a board sub-block.
     */
    private static final int QUAD_WIDTH = BOARD_WIDTH / 2;
    /**
     * The height of a baord sub-block.
     */
    private static final int QUAD_HEIGHT = BOARD_HEIGHT / 2;
    /**
     * The number of bytes that represent the board's contents.
     */
    private static final int BOARD_BYTES = 9;
    /**
     * The number of bits used to represent each cell.
     */
    private static final int LOC_SIZE = 2;
    /**
     * The number of bits in each byte.
     */
    private static final int BYTE_SIZE = 8;
    /**
     * The number of cells that can be described with a byte.
     */
    private static final int LOCS_PER_BYTE = BYTE_SIZE / LOC_SIZE;
    /**
     * A mask used to access a single location.
     */
    private static final byte LOC_MASK = 0b11;
    /**
     * The number of cells in a row needed to win.
     */
    private static final int NUM_TO_WIN = 5;
    /**
     * Describes the possible winning diagonals by x-coordinate, y-coordinate,
     * x-offset to move to next cell, y-offset to move to next cell, number of
     * cells.
     */
    private static final int[][] DIAGONALS = {{0, 1, 1, 1, 5},
                                              {0, 0, 1, 1, 6},
                                              {1, 0, 1, 1, 5},
                                              {4, 0, -1, 1, 5},
                                              {5, 0, -1, 1, 6},
                                              {5, 1, -1, 1, 5}};

    /**
     * Describes each possible rotation of block 0 with cells numbered left-to-right,
     * top-to-bottom for the entire board. Used to index rotations in all blocks
     * using an offset. -1 appears where the corresponding indices are outside the
     * block in question.
     */
    private static final int[][] ROTATIONS =
            {{0, 1, 2, -1, -1, -1, 6, 7, 8, -1, -1, -1, 12, 13, 14},
             {12, 6, 0, -1, -1, -1, 13, 7, 1, -1, -1, -1, 14, 8, 2},
             {14, 13, 12, -1, -1, -1, 8, 7, 6, -1, -1, -1, 2, 1, 0},
             {2, 8, 14, -1, -1, -1, 1, 7, 13, -1, -1, -1, 0, 6, 12}};

    /**
     * A byte array that holds the value stored in each cell.
     */
    private byte[] contents;
    /**
     * A byte array that holds the current rotations of each block,
     * 0-3.
     */
    private byte[] curRotation;
    /**
     * Number of half-turns played on the board so far.
     */
    private int halfTurns;

    /**
     * The parameterless constructor, initializes an empty board
     */
    public Board() {
        contents = new byte[BOARD_BYTES];

        curRotation = new byte[4];
        halfTurns = 0;
    }

    /**
     * Gets the color opposite of a given color.
     * @param color the color to find the opposite of
     * @return the color opposite of color
     */
    public static byte oppositeColor(byte color) {
        if (color == BLACK) {
            return WHITE;
        }

        return BLACK;
    }

    /**
     * Rotates the specified quadrant.
     * @param quadrant the quadrant number to rotate, 0-3
     * @param dir the direction to rotate the specified quadrant
     */
    public void rotateQuadrant(final int quadrant, final int dir) {
        curRotation[quadrant] =
                (byte) ((curRotation[quadrant]
                         + dir
                         + curRotation.length)
                        % curRotation.length);
    }

    /**
     * Gets the index of the specified location in contents.
     * @param loc the location to convert to an index
     * @return the location in the form of a scalar index for contents
     */
    private int getLocationByteInd(final int[] loc) {
        int quadX = loc[0] / QUAD_WIDTH;
        int quadY = loc[1] / QUAD_HEIGHT;
        int quad = 2 * quadY + quadX;
        int quadOffset = QUAD_WIDTH * quadX
                       + QUAD_HEIGHT * BOARD_WIDTH * quadY;

        return ROTATIONS[curRotation[quad]]
                [loc[1] * BOARD_WIDTH + loc[0] - quadOffset] + quadOffset;
    }

    /**
     * Gets the byte (really two bits) representing the contents of the
     * cell at the given location.
     * @param loc the location of the cell to retrieve
     * @return a byte representing the contents of the cell
     */
    public byte getLocation(final int[] loc) {
        int locInd = getLocationByteInd(loc);

        return (byte) (LOC_MASK & (contents[locInd / LOCS_PER_BYTE]
                       >>> (LOC_SIZE * (locInd % LOCS_PER_BYTE))));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < BOARD_HEIGHT; y++) {
            for (int x = 0; x < BOARD_WIDTH; x++) {
                byte val = getLocation(new int[] {x, y});
                if (val == WHITE) {
                    sb.append("w|");
                } else if (val == BLACK) {
                    sb.append("b|");
                } else {
                    sb.append("_|");
                }

                if (x == BOARD_WIDTH - 1) {
                    sb.append("  ");
                }
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * Returns a terminal evaluation of the game state, used for evaluating
     * terminal nodes in a minimax search, or determining whether/how the game
     * has ended.
     * @param streakScores the scoring weights used assign value to each possible
     * spot for a win for each color. streakScore[i][j] represents a line of cells
     * on the board i+NUM_TO_WIN long in which a given player could possibly win,
     * with j cells already filled in.
     * @return the evaluation of the game board, with states favoring white getting
     * positive values and states favoring black negative values. A certain black
     * win is Double.NEGATIVE_INFINITY, a certain white win is
     * Double.POSITIVE_INFINITY and a certain tie is Double.NaN
     */
    public double evaluation(final double[][] streakScores) {
        StreakCollector streakCollector = new StreakCollector(NUM_TO_WIN, BOARD_WIDTH, streakScores);
        
        for (int y = 0; y < BOARD_HEIGHT; y++) {
            for (int x = 0; x < BOARD_WIDTH; x++) {
                streakCollector.addVal(getLocation(new int[] {x, y}));
            }
            streakCollector.finishLine();
            streakCollector.newLine();
        }

        for (int x = 0; x < BOARD_WIDTH; x++) {
            for (int y = 0; y < BOARD_HEIGHT; y++) {
                streakCollector.addVal(getLocation(new int[] {x, y}));
            }
            streakCollector.finishLine();
            streakCollector.newLine();
        }

        for (int[] diagonal : DIAGONALS) {
            int[] loc = new int[] {diagonal[0], diagonal[1]};

            for (int i = 0; i < diagonal[4]; i++) {
                streakCollector.addVal(getLocation(loc));

                loc[0] += diagonal[2];
                loc[1] += diagonal[3];
            }
            streakCollector.finishLine();
            streakCollector.newLine();
        }

        double score = streakCollector.getScore();

        if (!Double.isFinite(score)) {
            return score;
        }

        if (isFull()) {
            return TIE_EVAL;
        }

        return score;
    }

    /**
     * Gets whether or not the board is full.
     * @return a boolean representing whether or not the board is full
     */
    public boolean isFull() {
        return halfTurns >= BOARD_SIZE;
    }

    /**
     * Sets the given board location with a given, non-empty color
     * @param loc the location at which to set the color
     * @param val the color to set
     */
    public void setLocation(final int[] loc, final byte val) {
        int locInd = getLocationByteInd(loc);

        int byteInd = locInd / LOCS_PER_BYTE;
        int shift = LOC_SIZE * (locInd % LOCS_PER_BYTE);

        contents[byteInd] |= val << shift;
        halfTurns++;
    }

    /**
     * Sets the cell at the given location to empty
     * @param loc the location to clear
     */
    public void clearLocation(final int[] loc) {
        int locInd = getLocationByteInd(loc);

        int byteInd = locInd / LOCS_PER_BYTE;
        int shift = LOC_SIZE * (locInd % LOCS_PER_BYTE);

        contents[byteInd] &= ~(LOC_MASK << shift);
        halfTurns--;
    }

    /**
     * Performs a full move, comprised of placing a color and rotating
     * if the placing step does not result in a win.
     * @param turn the color on-move
     * @param loc the location at which the color is placed
     * @param quad the number of the quad to rotate, or -1 if no rotation
     * is to be performed
     * @param rot the direction to rotate the quad, with 1 unit being 90
     * degrees clockwise
     */
    public void move(byte turn, int[] loc, int quad, boolean rot) {
        setLocation(loc, turn);

        if (!Double.isInfinite(evaluation(new double[2][6])) && quad != -1) {
            rotateQuadrant(quad, rot ? 1 : -1);
        }
    }
    
    /**
     * A class which defines a "streak collector" which accumulates possible
     * locations of a win for either player into a score, given the scoring
     * weights.
     * @author Neil Hulbert
     * @version 1.0
     *
     */
    private class StreakCollector {
        /**
         * The minimum possible streak.
         */
        private final int MIN_STREAK;
        /**
         * The maximum possible streak.
         */
        private final int MAX_STREAK;
        /**
         * The weights with which to score streaks.
         */
        private final double[][] streakScores;
        /**
         * The current streak being tracked.
         */
        private int curStreak;
        /**
         * The number of filled in cells in the current streak.
         */
        private int actual;
        /**
         * The number of filled in cells currently seen in a row.
         */
        private int actualInARow;
        /**
         * The most recently seen color in the streak.
         */
        private int prev;
        /**
         * The position of the first empty cell in a string of all empty
         * cells.
         */
        private int firstEmpty;
        /**
         * A count of the number of cells currently seen in a line.
         */
        private int count;
        /**
         * The accumulated score for all lines.
         */
        private double score;
        /**
         * Whether the board is certain to be in a completed state.
         */
        boolean gameCompleted;

        /**
         * The constructor which initializes an empty Streak Collector.
         * @param minStreak the minimum possible streak to consider.
         * @param maxStreak the maximum possible streak to consider.
         * @param streakScores the weights of the different kinds of streaks.
         */
        StreakCollector(final int minStreak, final int maxStreak, double[][] streakScores) {
            MIN_STREAK = minStreak;
            MAX_STREAK = maxStreak;
            this.streakScores = streakScores;

            score = 0;
            
            newLine();
            
            gameCompleted = false;
        }

        /**
         * Adds the contents of the next cell in a line to the streak collector.
         * @param val the color to add
         */
        public void addVal(final byte val){
            if (val == EMPTY) {
                curStreak++;
                actualInARow = 0;
            } else if (prev == EMPTY || val == prev) {
                updateScore(val != WHITE, count - firstEmpty, 0);
                curStreak++;
                actual++;
                actualInARow++;
                
                if (actualInARow == NUM_TO_WIN) {
                    double newScore = (val == WHITE) ? MAX_EVAL : MIN_EVAL;
                    if (!gameCompleted) {
                        score = newScore;
                        gameCompleted = true;
                    } else if (newScore != score){
                        score = TIE_EVAL;
                    }
                }
                
                firstEmpty = count + 1;
                prev = val;
            } else {
                updateScore(prev == WHITE, curStreak, actual);
                curStreak = count - firstEmpty + 1;
                actual = 1;
                actualInARow = 1;
                prev = val;
                firstEmpty = count + 1;
            }

            count++;
        }

        /**
         * Finish the line and accumulate all current streaks.
         */
        public void finishLine(){
            updateScore(prev == WHITE, curStreak, actual);
            updateScore(prev != WHITE, count - firstEmpty, 0);
        }

        /**
         * Gets the current score accumulated.
         * @return the current score accumulated
         */
        public double getScore(){
            return score;
        }

        /**
         * Partially resets the streak collector for a new line of input.
         */
        public void newLine() {
            curStreak = 0;
            actual = 0;
            actualInARow = 0;
            prev = EMPTY;
            firstEmpty = 0;
            count = 0;
        }
        
        /**
         * Updates the score based on a newly completed streak.
         * @param isWhite a boolean representing the color of the streak
         * @param streakLength the length of the streak to add
         * @param actual the number of filled in cells in the streak
         */
        private void updateScore(final boolean isWhite, final int streakLength, final int actual) {
            if (!gameCompleted && streakLength >= MIN_STREAK
                    && streakLength <= MAX_STREAK) {

                if (isWhite) {
                    score += streakScores[streakLength - MIN_STREAK][actual];
                } else {
                    score -= streakScores[streakLength - MIN_STREAK][actual];
                }
            }
        }
    }
}
