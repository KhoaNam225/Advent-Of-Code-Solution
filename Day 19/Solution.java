import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Solution {
    private static final int MINIMUM_REQUIRED_OVERLAPPING_DISTANCE = 12 * (12 - 1) / 2;

    private static int multiplySingleCell(int[][] matrixA, int[][] matrixB, int rowA, int colB) {
        int[] row = matrixA[rowA];
        int[] col = new int[row.length];

        for (int i = 0; i < matrixB.length; i++) {
            col[i] = matrixB[i][colB];
        }

        int sum = 0;
        for (int i = 0; i < row.length; i++) {
            sum += row[i] * col[i];
        }

        return sum;
    }

    private static int[][] multiplyMatrices(int[][] matrixA, int[][] matrixB) {
        int rowA = matrixA.length;
        int colA = matrixA[0].length;

        int rowB = matrixB.length;
        int colB = matrixB[0].length;

        if (colA != rowB) return null;

        int[][] result = new int[rowA][colB];
        for (int i = 0; i < rowA; i++) {
            for (int j = 0; j < colB; j++) {
                result[i][j] = multiplySingleCell(matrixA, matrixB, i, j);
            }
        }

        return result;
    }

    private static String toStringMatrix(int[][] matrix) {
        String str = "";
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                str += matrix[i][j] + " ";
            }
            str += "\n";
        }

        return str;
    }

    private static int[][] matrixPow(int[][] matrix, int pow) {
        if (pow == 0) {
            int[][] result = new int[matrix.length][matrix[0].length];
            for (int i = 0; i < result.length; i++) {
                result[i][0 + i] = 1;
            }

            return result;
        }

        int[][] result = matrix;
        for (int i = 1; i < pow; i++) {
            result = multiplyMatrices(result, matrix);
        }

        return result;
    }

    private static List<int[][]> generateOrientations() {
        int[][] matrixA = {
            new int[] {1, 0, 0},
            new int[] {0, 0, -1},
            new int[] {0, 1, 0}
        };

        int[][] matrixB = {
            new int[] {0, 0, 1},
            new int[] {0, 1, 0},
            new int[] {-1, 0, 0}
        };

        int[][] matrixC = {
            new int[] {0, 1, 0},
            new int[] {-1, 0, 0},
            new int[] {0, 0, 1}
        };

        Set<String> rotations = new HashSet<>();
        List<int[][]> result = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                for (int k = 0; k < 4; k++) {
                    int[][] product = multiplyMatrices(multiplyMatrices(matrixPow(matrixA, i), matrixPow(matrixB, j)), matrixPow(matrixC, k));
                    if (!rotations.contains(toStringMatrix(product))) {
                        rotations.add(toStringMatrix(product));
                        result.add(product);
                    }
                }
            }
        }

        return result;
    }

    private static class Point {
        private int x;
        private int y;
        private int z;

        public Point(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public int squareDistance(Point other) {
            return (int)Math.pow(x - other.x, 2) + (int)Math.pow(y - other.y, 2) + (int)Math.pow(z - other.z, 2);
        }

        public Point rotate(int[][] matrix) {
            int[][] result = multiplyMatrices(new int[][] { new int[] { this.x, this.y, this.z }}, matrix);
            return new Point(result[0][0], result[0][1], result[0][2]);
        }

        @Override
        public String toString() {
            return String.format("<%d, %d, %d>", x, y, z);
        }

        public Point subtract(Point other) {
            return new Point(this.x - other.x, this.y - other.y, this.z - other.z);
        }

        public Point add(Point other) {
            return new Point(this.x + other.x, this.y + other.y, this.z + other.z);
        }

        public long manhattanDistance(Point other) {
            return Math.abs(this.x - other.x) + Math.abs(this.y - other.y) + Math.abs(this.z - other.z);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Point)) return false;
            Point other = (Point)obj;
            return this.x == other.x && this.y == other.y && this.z == other.z;
        }
    }

    private static class Scanner {
        List<Point> points;
        Map<Integer, List<Point[]>> squareDistances;
        String id;
        Point correctPosition;
        int[][] orientation;

        public Scanner(String id) {
            this.id = id;
            this.points = new ArrayList<>();
            this.squareDistances = new HashMap<>();
            this.correctPosition = null;
            this.orientation = null;
        }

        public void addPoint(Point p) {
            this.points.add(p);
        }

        public void calculateSquareDistances() {
            for (int i = 0; i < points.size(); i++) {
                Point a = points.get(i);
                for (int j = i + 1; j < points.size(); j++) {
                    Point b = points.get(j);
                    int dist = a.squareDistance(b);
                    List<Point[]> pairs = squareDistances.getOrDefault(dist, new ArrayList<>());
                    pairs.add(new Point[] { a, b });
                    squareDistances.put(dist, pairs);
                }
            }
        }

        public int countOverlappingDistance(Scanner other) {
            Map<Integer, List<Point[]>> otherDistance = other.squareDistances;
            int count = 0;
            for (int dist : squareDistances.keySet()) {
                List<Point[]> thisPairs = squareDistances.get(dist);
                List<Point[]> otherPairs = otherDistance.getOrDefault(dist, new ArrayList<>());

                count += Math.min(thisPairs.size(), otherPairs.size());
            }

            return count;
        }

        public boolean isOverlappedWith(Scanner other) {
            return countOverlappingDistance(other) >= MINIMUM_REQUIRED_OVERLAPPING_DISTANCE;
        }

        @Override
        public String toString() {
            String result = id + "\n";
            for (Point point : points) {
                result += point.toString() + "\n";
            }

            return result;
        }

        private Point checkTwoSamePairs(Point[] pair1, Point[] pair2) {
            Point p1a = pair1[0];
            Point p1b = pair1[1];
            Point p2a = pair2[0];
            Point p2b = pair2[1];

            if (p1a.subtract(p2a).equals(p1b.subtract(p2b))) return p1a.subtract(p2a);
            if (p1a.subtract(p2b).equals(p1b.subtract(p2a))) return p1a.subtract(p2b);
            
            return null;
        }

        public void findCorrectOrientation(Scanner other) {
            Map<Integer, List<Point[]>> otherSquareDistance = other.squareDistances;
            List<int[][]> orientations = generateOrientations();

            for (Integer dist : this.squareDistances.keySet()) {
                List<Point[]> otherPairs = otherSquareDistance.getOrDefault(dist, new ArrayList<>());
                List<Point[]> thisPairs = this.squareDistances.get(dist);

                for (int[][] orientation : orientations) {
                    for (Point[] thisPair : thisPairs) {
                        Point[] rotated = new Point[] { thisPair[0].rotate(orientation), thisPair[1].rotate(orientation) };
                        
                        for (Point[] otherPair : otherPairs) {
                            if (checkTwoSamePairs(otherPair, rotated) != null) {
                                System.out.println(String.format("Correct position of scanner %s", checkTwoSamePairs(otherPair, rotated).toString()));
                                System.out.println(String.format("Correct orientation: \n%s", toStringMatrix(orientation)));
                            
                                this.correctPosition = checkTwoSamePairs(otherPair, rotated);
                                this.orientation = orientation;
                                return;
                            }
                        }
                    }
                }
            }
        }

        public void alignPosition(Scanner other) {
            List<Point> newPoints = new ArrayList<>();
            for (Point point : this.points) {
                Point newPoint = point.rotate(this.orientation);
                newPoints.add(this.correctPosition.add(newPoint));
            }

            
            this.points = newPoints;

            this.squareDistances = new HashMap<>();
            calculateSquareDistances();
        }
    }

    private static List<Scanner> readInput(String filename) {
        List<Scanner> scanners = new ArrayList<>();
        try {
            File inputFile = new File(filename);
            BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFile));

            String line = "";
            Scanner currentScanner = null;
            do {
                line = bufferedReader.readLine();
                if (line != null && line.contains("---")) {
                    currentScanner = new Scanner(line);
                }
                else if (line != null && line.length() > 0) {
                    String[] coordinates = line.split(",");
                    currentScanner.addPoint(new Point(Integer.parseInt(coordinates[0]), Integer.parseInt(coordinates[1]), Integer.parseInt(coordinates[2])));
                }
                else if (line != null && line.length() == 0) {
                    scanners.add(currentScanner);
                }
            }
            while (line != null);

            scanners.add(currentScanner);

            bufferedReader.close();
        }
        catch (IOException exception) {
            System.out.println("Cannot read input");
        }

        return scanners;
    }

    private static long manhattanDistance(Scanner sc1, Scanner sc2) {
        return sc1.correctPosition.manhattanDistance(sc2.correctPosition);
    }

    private static void visitScanner(Scanner root, List<Scanner> allScanners, Set<String> visited) {
        for (Scanner sc : allScanners) {
            if (!visited.contains(sc.id) && sc.isOverlappedWith(root)) {
                visited.add(sc.id);
                System.out.println(String.format("%s overlapped with %s", root.id, sc.id));
                sc.findCorrectOrientation(root);
                sc.alignPosition(root);
                visitScanner(sc, allScanners, visited);
            }
        }
    }

    public static void main(String[] args) {
        List<Scanner> scanners = readInput("input.txt");
        for (Scanner sc : scanners) {
            sc.calculateSquareDistances();
        }

        Set<String> visitedScanner = new HashSet<>();
        visitedScanner.add(scanners.get(0).id);
        visitScanner(scanners.get(0), scanners, visitedScanner);

        Set<String> beacons = new HashSet<>();
        for (Scanner sc : scanners) {
            for (Point beacon : sc.points) {
                beacons.add(beacon.toString());
            }
        }

        System.out.println(String.format("Number of beacons: %d", beacons.size()));
        long maxDistance = Integer.MIN_VALUE;
        for (int i = 0; i < scanners.size(); i++) {
            for (int j = i + 1; j < scanners.size(); j++) {
                if (scanners.get(i).correctPosition != null && scanners.get(j).correctPosition != null) {
                    maxDistance = Math.max(maxDistance, manhattanDistance(scanners.get(i), scanners.get(j)));
                }
            }
        }

        System.out.println(String.format("Max Manhattan distance: %d", maxDistance));
    }
}