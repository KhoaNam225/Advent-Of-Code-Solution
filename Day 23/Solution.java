import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.IIOException;

public class Solution {
    private static final char AMPHIPOD_AMBER = 'A';
    private static final char AMPHIPOD_BRONZE = 'B';
    private static final char AMPHIPOD_COPPER = 'C';
    private static final char AMPHIPOD_DESERT = 'D';

    private static final int POLE_HALLWAY = 1;
    private static final int POLE_ROOM = 2;

    private static class Amphipod {
        private char type;
        private boolean canMove;
        private int cost;

        public Amphipod(char type, boolean canMove, int cost) {
            this.type = type;
            this.canMove = canMove;
            this.cost = cost;
        }

        @Override
        protected Amphipod clone() {
            return new Amphipod(type, canMove, cost);
        }
    }

    private static class Pole {
        private List<Amphipod> amphipods;
        private int capacity;
        private int type;
        private char forAmphipod;

        public Pole(int capacity, int type, char forAmphipod) {
            this.amphipods = new ArrayList<>();
            this.capacity = capacity;
            this.type = type;
            this.forAmphipod = forAmphipod;
        }

        public Pole clone() {
            Pole cloned = new Pole(capacity, type, forAmphipod);
            for (Amphipod amphipod : amphipods) {
                cloned.amphipods.add(amphipod.clone());
            }

            return cloned;
        }

        public boolean isFull() {
            return this.amphipods.size() == this.capacity;
        }

        public boolean isTarget() {
            if (this.type == POLE_HALLWAY && !this.amphipods.isEmpty()) return false;
            if (this.type == POLE_ROOM && !this.isFull()) return false;

            for (Amphipod amp : this.amphipods) {
                if (amp.type != this.forAmphipod) {
                    return false;
                }
            }

            return true;
        }

        public boolean isDirty() {
            for (Amphipod amphipod : amphipods) {
                if (amphipod.type != forAmphipod) return true;
            }

            return false;
        }

        @Override
        public String toString() {
            String roomType = this.type == POLE_HALLWAY ? "H" : "R";
            String ampStr = "";
            for (int i = 0; i < this.capacity; i++) {
                if (i < this.amphipods.size()) {
                    ampStr += this.amphipods.get(i).type + " ";
                }
                else {
                    ampStr += ". ";
                }
            }

            return String.format("%s %c %s", roomType, forAmphipod, ampStr);
        }
    }

    private static int getCostFromAmphipodType(int type) {
        if (type == AMPHIPOD_AMBER) return 1;
        if (type == AMPHIPOD_BRONZE) return 10;
        if (type == AMPHIPOD_COPPER) return 100;

        return 1000;
    }

    private static class GameState implements Comparable<GameState> {
        List<Pole> poles;
        private int cost;
        private GameState parent;

        public GameState(List<Pole> poles, int cost) {
            this.poles = poles;
            this.cost = cost;
        }

        public GameState clone() {
            List<Pole> newPoles = new ArrayList<>();
            for (Pole pole : this.poles) {
                newPoles.add(pole.clone());
            }

            return new GameState(newPoles, cost);
        }

        public boolean isTarget() {
            for (Pole pole : this.poles) {
                if (!pole.isTarget()) return false;
            }

            return true;
        }

        @Override
        public String toString() {
            // String str = String.format("Cost: %d \n", this.cost);
            String str = "";
            for (Pole pole : this.poles) {
                str += pole.toString() + "\n";
            }

            return str;
        }

        public List<GameState> generateNewState() {
            List<GameState> newStates = new ArrayList<>();
            for (int i = 0; i < poles.size(); i++) {
                if (poles.get(i).amphipods.isEmpty()) continue;
                
                for (int j = i - 1; j >= 0; j--) {
                    Pole to = poles.get(j);
                    if (to.type == POLE_HALLWAY && to.isFull()) break;

                    if (canMove(i, j)) {
                        newStates.add(moveAmphipod(i, j));
                    }
                }

                for (int j = i + 1; j < poles.size(); j++) {
                    Pole to = poles.get(j);
                    if (to.type == POLE_HALLWAY && to.isFull()) break;

                    if (canMove(i, j)) {
                        newStates.add(moveAmphipod(i, j));
                    }
                }
            }

            return newStates;
        }

        public GameState moveAmphipod(int poleFrom, int poleTo) {
            GameState newState = this.clone();
            Pole from = newState.poles.get(poleFrom);
            Pole to = newState.poles.get(poleTo);

            if (from.amphipods.isEmpty()) {
                throw new IllegalArgumentException("Moving amphipod from an empty pole");
            }

            if (to.isFull()) {
                throw new IllegalArgumentException("Moving amphipod to a full pole");
            }

            int steps = Math.abs(poleFrom - poleTo);
            if (from.type == POLE_ROOM) steps += from.capacity - from.amphipods.size() + 1;
            if (to.type == POLE_ROOM) steps += to.capacity - to.amphipods.size();
            Amphipod amp = from.amphipods.remove(from.amphipods.size() - 1);
            newState.cost += steps * amp.cost;

            to.amphipods.add(amp);
            newState.parent = this;
            return newState;
        }

        @Override
        public int compareTo(GameState other) {
            int thisCost = this.cost + this.getEstimationCost();
            int thatCost = other.cost + other.getEstimationCost();

            return thisCost - thatCost;
        }

        private int searchRightRoomForAmphipod(Amphipod amp) {
            for (int i = 0; i < poles.size(); i++) {
                if (poles.get(i).forAmphipod == amp.type) return i;
            }

            return -1;
        }

        private int getEstimationCost() {
            int estimatedCost = 0;
            Map<Character, Integer> roomMap = new HashMap<>(Map.of(
                'A', 0,
                'B', 0,
                'C', 0,
                'D', 0
            ));

            for (Pole pole : poles) {
                for (Amphipod amp : pole.amphipods) {
                    if (amp.type == pole.forAmphipod) {
                       int ampAlreadyInRightRoom = roomMap.get(amp.type);
                       roomMap.put(amp.type, ampAlreadyInRightRoom + 1); 
                    }
                }
            }

            for (int i = 0; i < poles.size(); i++) {
                Pole currentPole = poles.get(i);
                if (currentPole.amphipods.isEmpty()) continue;

                int cost = 0;
                for (int j = 0; j < currentPole.amphipods.size(); j++) {
                    Amphipod currentAmphipod = currentPole.amphipods.get(j);
                    int rightPoleIdx = searchRightRoomForAmphipod(currentAmphipod);
                    if (rightPoleIdx == i) continue;

                    int steps = Math.abs(rightPoleIdx - i);
                    if (currentPole.type == POLE_ROOM) {
                        steps += 4 - j;
                    }

                    int ampAlreadyInRightRoom = roomMap.get(currentAmphipod.type);
                    steps += 4 - ampAlreadyInRightRoom;
                    roomMap.put(currentAmphipod.type, ampAlreadyInRightRoom + 1);

                    cost += steps * getCostFromAmphipodType(currentAmphipod.type);
                }

                estimatedCost += cost;
            }

            return estimatedCost;
        }

        private boolean canMove(int poleFrom, int poleTo) {
            Pole from = poles.get(poleFrom);
            Pole to = poles.get(poleTo);

            if (to.type == POLE_ROOM) {
                Amphipod amp = from.amphipods.get(from.amphipods.size() - 1);
                if (amp.type == to.forAmphipod && !to.isDirty() && !to.isFull()) return true;
            }

            if (from.type == POLE_ROOM && to.type == POLE_HALLWAY && !to.isFull()) {
                return true;
            }

            return false;
        }
    }

    private static GameState searchForSolution(GameState initState) {
        Set<String> visited = new HashSet<>();
        PriorityQueue<GameState> pq = new PriorityQueue<>();
        pq.add(initState);
        // BufferedWriter writer = new BufferedWriter(new FileWriter("output.txt"));

        while (!pq.isEmpty()) {
            GameState currState = pq.poll();
            visited.add(currState.toString());
            if (currState.getEstimationCost() <= 0) {
                System.out.println(String.format("Cost: %d\n Estimated: %d\n Total: %d\n \n%s", currState.cost, currState.getEstimationCost(), currState.getEstimationCost() + currState.cost, currState.toString()));
            }
            if (currState.isTarget()) return currState;

            for (GameState nextState : currState.generateNewState()) {
                if (visited.contains(nextState.toString())) continue;

                // visited.add(nextState.toString());
                pq.add(nextState);
            }
            // pq.addAll(currState.generateNewState());
        }

        return null;
    }

    private static GameState readInput(String filename) {
        GameState initialState = null;
        try {
            File inputFile = new File(filename);
            BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFile));

            List<Pole> poles = new ArrayList<>(11);
            char forAmphipod = AMPHIPOD_AMBER;
            for (int i = 0; i < 11; i++) {
                if (i != 0 && i != 10 && i % 2 == 0) {
                    poles.add(new Pole(4, POLE_ROOM, forAmphipod));
                    forAmphipod++;
                }
                else {
                    poles.add(new Pole(1, POLE_HALLWAY, '_'));
                }
            }

            // Skip the first 2 lines as they are useless in this case
            bufferedReader.readLine();
            bufferedReader.readLine();

            String thirdLine = bufferedReader.readLine();  // Save the third line for later processing
            String fourthLine = bufferedReader.readLine();  // Start processing from the bottom

            Pattern pattern = Pattern.compile("\s+#([ABCD])#([ABCD])#([ABCD])#([ABCD])#");
            Matcher matcher = pattern.matcher(fourthLine);
            if (matcher.matches()) {
                for (int i = 1; i <= matcher.groupCount(); i++) {
                    char ampType = matcher.group(i).charAt(0);
                    boolean canMove = true;
                    int cost = getCostFromAmphipodType(ampType);
                    poles.get(i * 2).amphipods.add(new Amphipod(ampType, canMove, cost));
                }
            }

            poles.get(2).amphipods.add(new Amphipod(AMPHIPOD_DESERT, true, getCostFromAmphipodType(AMPHIPOD_DESERT)));
            poles.get(4).amphipods.add(new Amphipod(AMPHIPOD_BRONZE, true, getCostFromAmphipodType(AMPHIPOD_BRONZE)));
            poles.get(6).amphipods.add(new Amphipod(AMPHIPOD_AMBER, true, getCostFromAmphipodType(AMPHIPOD_AMBER)));
            poles.get(8).amphipods.add(new Amphipod(AMPHIPOD_COPPER, true, getCostFromAmphipodType(AMPHIPOD_COPPER)));


            poles.get(2).amphipods.add(new Amphipod(AMPHIPOD_DESERT, true, getCostFromAmphipodType(AMPHIPOD_DESERT)));
            poles.get(6).amphipods.add(new Amphipod(AMPHIPOD_BRONZE, true, getCostFromAmphipodType(AMPHIPOD_BRONZE)));
            poles.get(8).amphipods.add(new Amphipod(AMPHIPOD_AMBER, true, getCostFromAmphipodType(AMPHIPOD_AMBER)));
            poles.get(4).amphipods.add(new Amphipod(AMPHIPOD_COPPER, true, getCostFromAmphipodType(AMPHIPOD_COPPER)));

            pattern = Pattern.compile("###([ABCD])#([ABCD])#([ABCD])#([ABCD])###");
            matcher = pattern.matcher(thirdLine);
            if (matcher.matches()) {
                for (int i = 1; i <= matcher.groupCount(); i++) {
                    char ampType = matcher.group(i).charAt(0);
                    boolean canMove = true;
                    int cost = getCostFromAmphipodType(ampType);
                    poles.get(i * 2).amphipods.add(new Amphipod(ampType, canMove, cost));
                }
            }

            initialState = new GameState(poles, 0);
            bufferedReader.close();
        }
        catch (IOException exception) {
            System.out.println("Cannot read input file");
        }

        return initialState;
    }

    public static void main(String[] args) {
        GameState initState = readInput("input.txt");
        // System.out.println(initState.toString());
        long startTime = System.nanoTime();
        GameState target = searchForSolution(initState);
        long endTime = System.nanoTime();
        GameState currState = target;
        while (currState != null) {
            System.out.println(String.format("Cost: %d\n Estimated: %d\n Total: %d\n \n%s", currState.cost, currState.getEstimationCost(), currState.getEstimationCost() + currState.cost, currState.toString()));

            currState = currState.parent;
        }

        System.out.println(String.format("Runtime: %.3f ms", (endTime - startTime) / Math.pow(10, 6)));
    }
}