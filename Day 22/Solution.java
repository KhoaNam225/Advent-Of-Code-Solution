import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Solution {
    private static class Cube {
        private int sign;
        private int minX;
        private int maxX;
        private int minY;
        private int maxY;
        private int minZ;
        private int maxZ;

        public Cube(int sign, int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
            this.sign = sign;
            this.minX = minX;
            this.maxX = maxX;
            this.minY = minY;
            this.maxY = maxY;
            this.minZ = minZ;
            this.maxZ = maxZ;
        }

        public long volume() {
            long width = this.maxX - this.minX + 1;
            long height = this.maxY - this.minY + 1;
            long length = this.maxZ - this.minZ + 1;

            return this.sign * Math.abs(width) * Math.abs(length) * Math.abs(height);
        }

        public Cube intersect(Cube other) {
            int newMinX = Math.max(this.minX, other.minX);
            int newMaxX = Math.min(this.maxX, other.maxX);
            int newMinY = Math.max(this.minY, other.minY);
            int newMaxY = Math.min(this.maxY, other.maxY);
            int newMinZ = Math.max(this.minZ, other.minZ);
            int newMaxZ = Math.min(this.maxZ, other.maxZ);

            if (newMinX > newMaxX || newMinY > newMaxY || newMinZ > newMaxZ) return null;
            return new Cube(-this.sign, newMinX, newMaxX, newMinY, newMaxY, newMinZ, newMaxZ);
        }

        @Override
        public String toString() {
            return String.format("%d <%d..%d, %d..%d, %d..%d>", sign, minX, maxX, minY, maxY, minZ, maxZ);
        }

        @Override
        public int hashCode() {
            return this.toString().hashCode();
        }
    }

    private static List<Cube> findAllIntersections(List<Cube> cubes) {
        List<Cube> results = new LinkedList<>();

        for (Cube cube : cubes) {
            // System.out.println("New cube: " + cube.toString());
            List<Cube> newResults = new LinkedList<>();
            for (Cube addedCube : results) {
                Cube intersection = addedCube.intersect(cube);
                // System.out.println(String.format("\tAdded cube: %s, intersection: %s", addedCube.toString(), intersection != null ? intersection.toString() : "null"));
                if (intersection != null) {
                    newResults.add(intersection);
                }
            }

            results.addAll(newResults);
            if (cube.sign == 1) {
                results.add(cube);
            }
        }

        return results;
    }

    private static long calcVolume(Collection<Cube> cubes) {
        long volume = 0;
        for (Cube cube : cubes) {
            volume += cube.volume();
        }

        return volume;
    }

    private static List<Cube> readInput(String filename) {
        List<Cube> cubes = new LinkedList<>();
        try {
            File inputFile = new File(filename);
            BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFile));

            String line = "";
            Pattern pattern = Pattern.compile("(on|off)\sx=(-?\\d+)\\.\\.(-?\\d+),y=(-?\\d+)\\.\\.(-?\\d+),z=(-?\\d+)\\.\\.(-?\\d+)");
            do {
                line = bufferedReader.readLine();
                if (line != null) {
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.matches()) {
                       int sign = matcher.group(1).equals("on") ? 1 : -1;
                       int minX = Integer.parseInt(matcher.group(2));
                       int maxX = Integer.parseInt(matcher.group(3));
                       int minY = Integer.parseInt(matcher.group(4));
                       int maxY = Integer.parseInt(matcher.group(5));
                       int minZ = Integer.parseInt(matcher.group(6));
                       int maxZ = Integer.parseInt(matcher.group(7));
                    
                       cubes.add(new Cube(sign, minX, maxX, minY, maxY, minZ, maxZ));
                    }
                }
            }
            while (line != null);

            bufferedReader.close();
        }
        catch (IOException exception) {
            System.out.println("Cannot read input file");
        }

        return cubes;
    }

    public static void main(String[] args) {
        List<Cube> cubes = readInput("input.txt");
        // List<Cube> smallCubes = cubes.stream().filter(cube -> {
        //     return cube.minX >= -50 && cube.maxX <= 50 && cube.minY >= -50 && cube.maxY <= 50 && cube.minZ >= -50 && cube.maxZ <= 50;
        // }).toList();

        // System.out.println(String.format("Cube 0: %s, volume: %d", smallCubes.get(0).toString(), smallCubes.get(0).volume()));
        // System.out.println(String.format("Cube 1: %s, volume: %d", smallCubes.get(1).toString(), smallCubes.get(1).volume()));
        // System.out.println(String.format("Intersection: %s, volume: %d", smallCubes.get(0).intersect(smallCubes.get(1)).toString(), smallCubes.get(0).intersect(smallCubes.get(1)).volume()));
        System.out.println(calcVolume(findAllIntersections(cubes)));
    }
}