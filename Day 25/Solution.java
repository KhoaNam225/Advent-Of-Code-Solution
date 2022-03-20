import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class Solution {
    private static final int EMPTY = 0;
    private static final int EAST = 1;
    private static final int SOUTH = 2;

    private static int simulate(int[][] source) {
        int steps = 0;
        int movesCount = -1;

        int totalRows = source.length;
        int totalCols = source[0].length;
        int[][] prev = source;
        while (movesCount != 0) {
            steps++;
            int[][] dest = new int[totalRows][totalCols];
            movesCount = simulateOneStep(prev, dest);
            prev = dest;
    
            System.out.println(String.format("Step %d, moves: %d", steps, movesCount));
        }
        return steps;
    }

    private static int simulateOneStep(int[][] source, int[][] dest) {
        int totalRows = source.length;
        int totalCols = source[0].length;

        int[][] buffer = new int[totalRows][totalCols];
        int totalMoves = 0;
        totalMoves += countMove(source, buffer, EAST);
        totalMoves += countMove(buffer, dest, SOUTH);

        return totalMoves;
    }

    private static int countMove(int[][] source, int[][] dest, int direction) {
        int totalRows = source.length;
        int totalCols = source[0].length;

        int totalMoves = 0;
        for (int row = 0; row < totalRows; row++) {
            for (int col = 0; col < totalCols; col++) {
                totalMoves += move(source, dest, row, col, direction);
            }
        }
        // System.out.println(String.format("Direction: %s", direction == SOUTH ? "SOUTH" : "EAST"));
        // printMatrix(dest);
        return totalMoves;
    }

    private static boolean canMove(int[][] matrix, int row, int col, int direction) {
        int totalRows = matrix.length;
        int totalCols = matrix[0].length;

        int currVal = matrix[row][col];
        if (currVal == EMPTY || currVal != direction) return false;

        int nextRow = direction == EAST ? row : (row + 1) % totalRows;
        int nextCol = direction == SOUTH ? col : (col + 1) % totalCols;

        return matrix[nextRow][nextCol] == EMPTY;
    }

    private static int move(int[][] source, int[][] dest, int row, int col, int direction) {
        int totalRows = source.length;
        int totalCols = source[0].length;

        if (canMove(source, row, col, direction)) {
            int currVal = source[row][col];
            int nextRow = direction == EAST ? row : ((row + 1) % totalRows);
            int nextCol = direction == SOUTH ? col : ((col + 1) % totalCols);

            dest[row][col] = EMPTY;
            dest[nextRow][nextCol] = currVal;
            return 1;
        }
        else if (dest[row][col] == EMPTY) {
            dest[row][col] = source[row][col];
        }

        return 0;
    }

    private static int[][] readInput(String filename) {
        int[][] matrix = null;

        try {
            File inputFile = new File(filename);
            BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFile));

            List<String> matrixStr = new LinkedList<>();
            String line = "";
            do {
                line = bufferedReader.readLine();
                if (line != null) {
                    matrixStr.add(line);
                }
            }
            while (line != null);

            matrix = new int[matrixStr.size()][];
            int count = 0;
            for (String lineStr : matrixStr) {
                matrix[count] = new int[lineStr.length()];
                for (int i = 0; i < lineStr.length(); i++) {
                    char c = lineStr.charAt(i);

                    if (c == '>') matrix[count][i] = EAST;
                    else if (c == 'v') matrix[count][i] = SOUTH;
                }

                count++;
            }
            bufferedReader.close();
        }
        catch (IOException exception) {
            System.out.println("Cannot read input file");
        }

        return matrix;
    }

    private static void printMatrix(int[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                System.out.print(matrix[i][j] == EAST ? '>' : matrix[i][j] == SOUTH ? 'v' : '.');
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        int[][] inputMatrix = readInput("input.txt");
        printMatrix(inputMatrix); 
        
        simulate(inputMatrix);
    }
}