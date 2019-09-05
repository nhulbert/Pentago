/*
 * TCSS 435 - Summer 2019
 * Assignment 2 - Pentago
 * Neil Hulbert
 */

package state;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Random;
import java.util.Scanner;

/**
 * The entry-point class for a console front-end to the Pentago AI.
 * @author Neil Hulbert
 * @version 1.0
 */
public class PentagoMain {
    /**
     * The depth at which the AI will perform all move searches.
     */
    public static final int AI_SEARCH_DEPTH = 3;
    
    public static void main(String[] args) {
        final double[][] SCORES = new double[][] {{1,2,3,4,5,6},
                                                  {2,3,4,5,6,7}};
                                                  
        try {
            PrintStream printStream = new PrintStream(new File("Output.txt"));
        
            Scanner console = new Scanner(System.in);
            String[] names = new String[2];
            byte[] colors;
            
            print(printStream, "Enter human name: ");
            names[0] = console.nextLine();
            printStream.println(names[0]);
            
            print(printStream, "Enter human color (B/W): ");
            String s = console.nextLine().toLowerCase();
            printStream.println(s);
            
            while (!s.equals("b") && !s.equals("w")) {
                print(printStream, "Invalid color, try again: ");
                s = console.nextLine().toLowerCase();
                printStream.println(s);
            }
            
            colors = s.equals("b") ? new byte[] {Board.BLACK, Board.WHITE}
                                   : new byte[] {Board.WHITE, Board.BLACK};
            
            print(printStream, "Enter AI player name: ");
            names[1] = console.nextLine();
            printStream.println(names[1]);
            
            Random random = new Random();
            
            int turnInd = random.nextInt(2);
            
            println(printStream, "\n\nPlayer 1: "
                    + names[turnInd]
                    + ((turnInd == 0) ? "(Human)" : "(AI)"));

            println(printStream, "Player 2: "
                    + names[1 - turnInd]
                    + ((turnInd == 1) ? "(Human)" : "(AI)\n"));
            println(printStream, "*****************************");

            Board board = new Board();
            Search search = new Search(SCORES);
            println(printStream, "Starting Board:\n" + board + "\n");
    
            double eval = 0;
            while (Double.isFinite(eval)) {
                if (turnInd == 0) {
                    println(printStream, names[turnInd] + "'s(Human) turn");
                    println(printStream, "Color: " + ((colors[turnInd] == Board.WHITE) ? "W" : "B"));
                    print(printStream, "Enter human move: ");
                    
                    String moveStr = console.nextLine();
                    while (!parseAndMakeMove(colors[turnInd], board, moveStr)) {
                        printStream.println(moveStr);
                        print(printStream, "Invalid move, try again: ");
                        moveStr = console.nextLine();
                    }
                    printStream.println(moveStr);
    
                    eval = board.evaluation(SCORES);
    
                    print(printStream, "\n" + board + "\n");
                } else {
                    println(printStream, names[turnInd] + "'s(AI) turn");
                    println(printStream, "Color: " + ((colors[turnInd] == Board.WHITE) ? "W" : "B"));
                    NodeEval compMove = search.evaluateAtDepth(board,
                            colors[turnInd],
                            AI_SEARCH_DEPTH,
                            new NodeEval(Board.MIN_EVAL, Integer.MAX_VALUE, null, 0, 0),
                            new NodeEval(Board.MAX_EVAL, Integer.MAX_VALUE, null, 0, 0));
                    board.move(colors[turnInd], compMove.loc, compMove.quad, compMove.rot == 1);
                    println(printStream, "AI move: " + compMoveToString(compMove));
                    println(printStream, "\n" + board.toString());
                    
                    eval = board.evaluation(SCORES);
                }
    
                turnInd = 1 - turnInd;
            }
    
            if (Double.isInfinite(eval)) {
                int winInd = ((eval == Double.POSITIVE_INFINITY) == (colors[0] == Board.WHITE)) ? 0 : 1;
                print(printStream, names[winInd] + " beat " + names[1 - winInd]+ "!");
            } else {
                print(printStream, names[0] + " and " + names[1] + " tied!");
            }
            
            console.close();
            printStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Parses a user-provided move string to possibly make a move.
     * @param turn the color of the player making the move
     * @param board the prior board state
     * @param str the user-provided move string
     * @return a boolean value representing whether the move was successfully
     * parsed.
     */
    private static boolean parseAndMakeMove(byte turn, Board board, String str) {
        String[] strs = str.split(" ");
        if (strs.length != 2) {
            return false;
        }
        
        try {
            String[] locStrs = strs[0].split("/");
            int block = Integer.parseInt(locStrs[0]);
            int cell = Integer.parseInt(locStrs[1]);
            
            if (block < 1 || block > 4 || cell < 1 || cell > 9) {
                return false;
            }
            
            if (strs[1].length() != 2) {
                return false;
            }
            
            int rotBlock = Integer.parseInt(strs[1].substring(0,1));
            String dirStr = strs[1].substring(1,2).toLowerCase();
            
            if (rotBlock < 1 || rotBlock > 4 || (!dirStr.equals("l") && !dirStr.equals("r"))) {
                return false;
            }
            
            block -= 1;
            cell -= 1;
            rotBlock -= 1;
            
            int[] loc = {3 * (block % 2) + cell % 3, 3 * (block / 2) + cell / 3};
            
            if (board.getLocation(loc) != Board.EMPTY) {
                return false;
            }
            
            board.move(turn, loc, rotBlock, dirStr.equals("r"));
            
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Converts the NodeEval provided by the AI into a move in the
     * standard form.
     * @param eval An evaluation and move recommendation provided by
     * the AI defined in the Search class.
     * @return A String representing the AI move in the standard form
     */
    private static String compMoveToString(NodeEval eval) {
        StringBuffer sb = new StringBuffer();
        
        int block = 2 * (eval.loc[1] / 3) + eval.loc[0] / 3 + 1;
        int cell = 3 * (eval.loc[1] % 3) + eval.loc[0] % 3 + 1;
        
        int rotBlock = eval.quad + 1;
        String dir = (eval.rot == 1) ? "R" : "L";
        
        sb.append(block);
        sb.append("/");
        sb.append(cell);
        sb.append(" ");
        sb.append(rotBlock);
        sb.append(dir);

        return sb.toString();
    }

    /**
     * Prints a string to the console as well as the provided PrintStream.
     * @param fos the PrintStream object to print to
     * @param str the string to print
     */
    private static void print(PrintStream fos, String str) {
        System.out.print(str);
        fos.print(str);
    }

    /**
     * Prints a string to the console as well as the provided PrintStream.
     * @param fos the PrintStream object to print to
     * @param str the string to print
     */
    private static void println(PrintStream fos, String str) {
        System.out.println(str);
        fos.println(str);
    }
}
