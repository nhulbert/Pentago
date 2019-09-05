/*
 * TCSS 435 - Summer 2019
 * Assignment 2 - Pentago
 * Neil Hulbert
 */

package state;

/**
 * Defines an evaluation and move recommendation for a board state.
 * @author Neil Hulbert
 * @version 1.0
 */
public class NodeEval implements Comparable<NodeEval> {
    /**
     * The raw evaluation score
     */
    Double eval;
    /**
     * The depth from which the evaluation originates
     */
    int terminalDepth;
    /**
     * The location of the recommended move
     */
    int[] loc;
    /**
     * The recommended quad to be rotated
     */
    int quad;
    /**
     * The recommended direction in which the quad is
     * to be rotated
     */
    int rot;

    /**
     * The constructor, initializes a new NodeEval object with the provided values.
     * @param eval the raw evaluation score
     * @param terminalDepth the depth from which the evaluation originates
     * @param loc the location of the recommended move
     * @param quad the recommended quad to be rotated
     * @param rot the recommended direction in which the quad is to be
     * rotated
     */
    public NodeEval(double eval, int terminalDepth, int[] loc, int quad, int rot) {
        this.eval = eval;
        this.terminalDepth = terminalDepth;
        this.loc = loc;
        this.quad = quad;
        this.rot = rot;
    }

    @Override
    public int compareTo(final NodeEval o) {
        double eval1 = Double.isNaN(eval) ? 0 : eval;
        double eval2 = Double.isNaN(o.eval) ? 0 : o.eval;

        int comp = Double.compare(eval1, eval2);

        if (comp != 0 || eval1 == 0) {
            return comp;
        }

        if (eval1 > 0) {
            return Integer.compare(terminalDepth, o.terminalDepth);
        }

        return Integer.compare(o.terminalDepth, terminalDepth);
    }
}
