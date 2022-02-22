import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Solution {
    private static final int PLAYER_1_TURN = 0;
    private static final int PLAYER_2_TURN = 1;

    private static List<Integer> allOutcomesOfDicePart2 = new ArrayList<>();

    // ****** PART 1 ******* //
    private static class Dice {
        private int currentNum;
        private int rolledTimes;

        public Dice() {
            currentNum = 1;
            rolledTimes = 0;
        }

        public int roll() {
            int steps = 0;
            rolledTimes += 3;

            for (int i = 0; i < 3; i++) {
                steps += currentNum;
                currentNum++;
                if (currentNum > 100) currentNum = 1;
            }

            return steps;
        }

        public int getRolledTimes() {
            return rolledTimes;
        }
    }

    private static class Player {
        private int currentPos;
        private int currentScore;

        public Player(int initPos) {
            currentPos = initPos;
            currentScore = 0;
        }

        public Player(Player otherPlayer) {
            currentPos = otherPlayer.currentPos;
            currentScore = otherPlayer.currentScore;
        }

        public void moveForward(int steps) {
            currentPos += steps;
            if (currentPos % 10 == 0) currentPos = 10;
            else currentPos %= 10;

            currentScore += currentPos;
        }

        public int getScore() {
            return currentScore;
        }

        public boolean hasWon() {
            return currentScore >= 21;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Player) {
                Player otherPlayer = (Player) obj;
                return otherPlayer.currentPos == currentPos && otherPlayer.currentScore == currentScore;
            }
            return false;
        }

        @Override
        public String toString() {
            return String.format("Current Position: %d, Current Score: %d", currentPos, currentScore);
        }
    }
    // **** END PART 1 *******/

    // **** PART 2 ********/
    private static class Result {
        private long player1Wins;
        private long player2Wins;

        public Result(long p1, long p2) {
            player1Wins = p1;
            player2Wins = p2;
        }

        @Override
        public String toString() {
            return String.format("Player 1: %d, Player 2: %d", player1Wins, player2Wins);
        }
    }

    private static class GameState {
        private Player player1;
        private Player player2;
        private int turn;

        public GameState(Player p1, Player p2, int turn) {
            player1 = p1;
            player2 = p2;
            this.turn = turn;
        }

        @Override
        public int hashCode() {
            return String.format("%d_%d_%d_%d_%d", player1.currentPos, player1.currentScore, player2.currentPos, player2.currentScore, turn).hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof GameState) {
                GameState state = (GameState) obj;
                return player1.equals(state.player1) && player2.equals(state.player2) && turn == state.turn;
            }

            return false;
        }
    }

    private static List<Integer> generateAllOutcomesOfDice() {
        List<Integer> allOutcomes = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            int firstOutCome = i;
            for (int j = 1; j <= 3; j++) {
                int secondOutcome = firstOutCome + j;
                for (int k = 1; k <= 3; k++) {
                    allOutcomes.add(secondOutcome + k);
                }
            }
        }

        return allOutcomes;
    }

    private static Result calculatePossibilitiesRecursive(GameState state, HashMap<GameState, Result> cache) {
        if (cache.get(state) != null) return cache.get(state);
        if (state.player1.hasWon()) return new Result(1, 0);
        if (state.player2.hasWon()) return new Result(0, 1);

        long p1Wins = 0;
        long p2Wins = 0;
        int currentTurn = state.turn;

        for (Integer possibleStep : allOutcomesOfDicePart2) {
            Player p1Cloned = new Player(state.player1);
            Player p2Cloned = new Player(state.player2);

            if (currentTurn == PLAYER_1_TURN) p1Cloned.moveForward(possibleStep);
            else p2Cloned.moveForward(possibleStep);

            GameState newState = new GameState(p1Cloned, p2Cloned, 1 - currentTurn);

            Result results = calculatePossibilitiesRecursive(newState, cache);

            p1Wins += results.player1Wins;
            p2Wins += results.player2Wins;
        }

        Result result = new Result(p1Wins, p2Wins);
        cache.put(state, result);

        return result;
    }

    private static Result calculatePossibilities(Player p1, Player p2) {
        return calculatePossibilitiesRecursive(new GameState(p1, p2, PLAYER_1_TURN), new HashMap<>());
    }

    private static Player[] readInput(String filename) {
        Player[] players = null;

        try {
            File inputFile = new File(filename);
            BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFile));

            players = new Player[2];

            String player1 = bufferedReader.readLine().split(": ")[1];
            players[0] = new Player(Integer.parseInt(player1));

            String player2 = bufferedReader.readLine().split(": ")[1];
            players[1] = new Player(Integer.parseInt(player2));

            bufferedReader.close();
        }
        catch (IOException exception) {
            System.out.println("Cannot read input file");
        }

        return players;
    }

    public static void main(String[] args) {
        Player[] players = readInput("input.txt");
        Player player1 = players[0];
        Player player2 = players[1];

        // PART 1 - SOLUTION
        // Dice dice = new Dice();
        // int currentTurn = PLAYER_1_TURN;

        // while (!player1.hasWon() && !player2.hasWon()) {
        //     Player currentPlayer = currentTurn == PLAYER_1_TURN ? player1 : player2;
        //     int steps = dice.roll();
        //     currentPlayer.moveForward(steps);

        //     System.out.println(String.format("Player %d moved %d steps and stopped at position %d, current score %d", currentTurn + 1, steps, currentPlayer.currentPos, currentPlayer.getScore()));
        //     currentTurn = 1 - currentTurn;
        // }

        // Player looser = player1.hasWon() ? player2 : player1;
        // System.out.println(looser.getScore() * dice.getRolledTimes());

        allOutcomesOfDicePart2 = generateAllOutcomesOfDice();
        Result result = calculatePossibilities(player1, player2);
        System.out.println(String.format("%d %d", result.player1Wins, result.player2Wins));
        System.out.println(String.format("Result %d", Math.max(result.player1Wins, result.player2Wins)));


    }
}