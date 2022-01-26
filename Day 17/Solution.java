import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Solution {
    private static int[] readInput(String filename) {
        String input = "";
        int[] coordinates = new int[4];

        try {
            File inputFile = new File(filename);
            BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFile));
            input = bufferedReader.readLine();
            input = input.substring("target area: x=".length());

            String xCoordinates = input.split(", y=")[0];
            String yCoordinates = input.split(", y=")[1];

            int xMin = Integer.parseInt(xCoordinates.split("\\.\\.")[0]);
            int xMax = Integer.parseInt(xCoordinates.split("\\.\\.")[1]);

            int yMin = Integer.parseInt(yCoordinates.split("\\.\\.")[0]);
            int yMax = Integer.parseInt(yCoordinates.split("\\.\\.")[1]);
        
            coordinates[0] = xMin;
            coordinates[1] = xMax;
            coordinates[2] = yMin;
            coordinates[3] = yMax;

            bufferedReader.close();
        }
        catch (IOException exception) {
            System.out.println("Cannot read input file");
        }

        return coordinates;
    }

    private static int calcMaxY(int[] input) {
        int yMin = input[2];
        int initYVelocity = - yMin - 1;

        return initYVelocity * (initYVelocity + 1) / 2;
    }

    private static void simulateY(int minY, int maxY, Map<Integer, Set<Integer>> timeStampMap, int startYVelocity) {
        int time = 0;
        int y = 0;
        int vy = startYVelocity;

        Set<Integer> timeList = new HashSet<>();

        while (y >= minY) {
            y += vy;
            time++;
            if (y >= minY && y <= maxY) {
                timeList.add(time);
            }
            vy--;
        }

        timeStampMap.put(startYVelocity, timeList);
    }

    private static void simulateX(int minX, int maxX, Map<Integer, List<Integer>> timeStampMap, int startXVelocity) {
        int time = 0;
        int x = 0;
        int vx = startXVelocity;

        List<Integer> timeList = new ArrayList<>();

        while (x <= maxX && vx >= 0) {
            x += vx;
            time++;
            if (x >= minX && x <= maxX) {
                timeList.add(time);
            }

            vx--;
        }

        if (x>= minX && x <= maxX && vx < 0) {
            timeList.add(Integer.MAX_VALUE);
        }

        if (timeList.size() > 0) {
            timeStampMap.put(startXVelocity, timeList);
        }
    }

    private static int collect(Map<Integer, Set<Integer>> yMap, Map<Integer, List<Integer>> xMap) {
        int count = 0;
        for (List<Integer> timeStamp : xMap.values()) {
            int minTime = timeStamp.get(0);
            int maxTime = timeStamp.get(timeStamp.size() - 1);

            for (Integer vy : yMap.keySet()) {
                Set<Integer> timeStampY = yMap.get(vy);
                for (Integer time : timeStampY) {
                    if (time >= minTime && time <= maxTime) {
                        count++;
                        break;
                    }
                }
            }
        }

        return count;
    }

    public static void main(String[] args) {
        int[] input = readInput("input.txt");
        int minX = input[0];
        int maxX = input[1];
        int minY = input[2];
        int maxY = input[3];

        Map<Integer, List<Integer>> xMap = new HashMap<>();
        Map<Integer, Set<Integer>> yMap = new HashMap<>();

        for (int xVec = 0; xVec <= maxX; xVec++) {
            simulateX(minX, maxX, xMap, xVec);
        }

        for (int yVec = minY; yVec <= -minY - 1; yVec++) {
            simulateY(minY, maxY, yMap, yVec);
        }

        System.out.println(collect(yMap, xMap));

        System.out.println(yMap.toString());
    }
}
