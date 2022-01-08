import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.PriorityQueue;

public class Solution {
    // This class is used to store information when performing Dijkstra path finding
    // We will compare the cells in the grid base on the distance from (0, 0) to it
    // Whichever has smaller distance will be visited first
    private static class Node implements Comparable<Node> {
        public int row;
        public int col;
        public int dist;

        public Node(int row, int col, int dist) {
            this.row = row;
            this.col = col;
            this.dist = dist;
        }

        public int compareTo(Node otherNode) {
            return this.dist - otherNode.dist;
        }
    }

    private static boolean isValidPosition(int[][] grid, int row, int col) {
        int rows = grid.length;
        int cols = grid[0].length;

        if (row < 0 || row >= rows) return false;
        if (col < 0 || col >= cols) return false;

        return true;
    }

    private static int dijkstraSearch(int[][] grid) {
        int rows = grid.length;
        int cols = grid.length;

        PriorityQueue<Node> minPQ = new PriorityQueue<>(rows * cols);
        boolean[][] visited = new boolean[rows][cols];
        int finalDistance = 0;

        minPQ.add(new Node(0, 0, 0));
        while (!minPQ.isEmpty()) {
            Node head = minPQ.poll();
            int row = head.row;
            int col = head.col;
            int dist = head.dist;

            // Stop the search if we found the goal
            // According to Dijkstra search, every time a node is popped out of the queue
            // it's minimum distance from the start point has been found
            if (row == rows - 1 && col == cols - 1) {
                finalDistance = dist;
                break;
            }

            // Visit the neighbors and update their distances
            if (isValidPosition(grid, row - 1, col) && !visited[row - 1][col]) {
                minPQ.add(new Node(row - 1, col, dist + grid[row - 1][col]));
                visited[row - 1][col] = true;
            }

            if (isValidPosition(grid, row + 1, col) && !visited[row + 1][col]) {
                minPQ.add(new Node(row + 1, col, dist + grid[row + 1][col]));
                visited[row + 1][col] = true;
            }
            
            if (isValidPosition(grid, row, col + 1) && !visited[row][col + 1]) {
                minPQ.add(new Node(row, col + 1, dist + grid[row][col + 1]));
                visited[row][col + 1] = true;
            }

            if (isValidPosition(grid, row, col - 1) && !visited[row][col - 1]) {
                minPQ.add(new Node(row, col - 1, dist + grid[row][col - 1]));
                visited[row][col - 1] = true;
            }
        }

        return finalDistance;
    }

    private static void printMatrix(int[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                System.out.print(matrix[row][col] + " ");
            }

            System.out.println();
        }
    }

    private static int[][] duplicateGrid(int[][] originalGrid, int nTimes) {
        int originalRows = originalGrid.length;
        int originalCols = originalGrid[0].length;

        int[][] newGrid = new int[originalRows * nTimes][originalCols * nTimes];
        
        for (int row = 0; row < newGrid.length; row++) {
            for (int col = 0; col < newGrid[0].length; col++) {
                newGrid[row][col] = originalGrid[row % originalRows][col % originalCols] + (int)Math.floor(row / originalRows) + (int)Math.floor(col / originalCols);

                if (newGrid[row][col] > 9) {
                    newGrid[row][col] -= 9;
                }
            }
        }

        return newGrid;
    }

    private static int[][] readInput(String filename) {
        int[][] returnedMatrix = null;
        try {
            File inputFile = new File(filename);
            BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFile));
            ArrayList<int[]> matrix =  new ArrayList<>();

            String line = "";
            do {
                line = bufferedReader.readLine();
                if (line != null) {
                    String[] numbers = line.split("");
                    int[] number = new int[line.length()];

                    for (int i = 0; i < numbers.length; i++) {
                        number[i] = Integer.parseInt(numbers[i]);
                    }

                    matrix.add(number);
                }
            }
            while (line != null);

            returnedMatrix = new int[matrix.size()][matrix.get(0).length];
            for (int i = 0; i < matrix.size(); i++) {
                returnedMatrix[i] = matrix.get(i);
            }

            bufferedReader.close();
        }
        catch (IOException exception) {
            System.out.println("Cannot read input file");
        }

        return returnedMatrix;
    }

    public static void main(String[] args) {
        int[][] inputMatrix = readInput("input.txt");  
        int distance = dijkstraSearch(duplicateGrid(inputMatrix, 5)); 
        System.out.println(distance);
    }
}