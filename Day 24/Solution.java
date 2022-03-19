import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Solution {
    private static class Function {
        private int extraParams;
        private int type;

        public Function(int extraParams, int type) {
            this.extraParams = extraParams;
            this.type = type;
        }

        public List<Integer> getConstraints(int zValue) {
            if (this.type == 1) return Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9);

            return Arrays.asList(zValue % 26 + this.extraParams);
        }

        public int calc(int zValue, int userInput) {
            if (this.type == 1) return zValue * 26 + userInput + this.extraParams;

            return zValue / 26;
        }

        @Override
        public String toString() {
            return String.format("Type: %d, Extra param: %d", this.type, this.extraParams);
        }
    }

    private static List<Function> readInput(String filename) {
        List<Function> procedures = new ArrayList<>();
        try {
            File inputFile = new File(filename);
            BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFile));

            String line = "";
            do {
                line = bufferedReader.readLine();
                if (line != null && line.equals("inp w")) {
                    for (int i = 0; i < 3; i++) {
                        bufferedReader.readLine();
                    }

                    line = bufferedReader.readLine();
                    if (line.equals("div z 1")) {
                        for (int i = 0; i < 10; i++) {
                            bufferedReader.readLine();
                        }

                        int extraParam = Integer.parseInt(bufferedReader.readLine().split(" ")[2]);
                        procedures.add(new Function(extraParam, 1));
                    }
                    else {
                        int extraParams = Integer.parseInt(bufferedReader.readLine().split(" ")[2]);
                        procedures.add(new Function(extraParams, 2));
                    }
                }
            }
            while (line != null);
        }
        catch (IOException exception) {
            System.out.println("Cannot read input file");
        }

        return procedures;
    }

    private static void collectAnswersRecursive(List<Function> functions, int currFunc, int prevOutput, String currString, List<Long> ans) {
        if (currFunc == functions.size() && prevOutput == 0) {
            ans.add(Long.parseLong(currString));
            return;
        }

        if (currFunc == functions.size()) return;

        Function currentFunction = functions.get(currFunc);
        for (Integer userInput : currentFunction.getConstraints(prevOutput)) {
            if (userInput > 0 && userInput < 10) {
                int output = currentFunction.calc(prevOutput, userInput);
                collectAnswersRecursive(functions, currFunc + 1, output, currString + userInput, ans);
            }
        }
    }

    private static List<Long> collectAnswers(List<Function> functions) {
        List<Long> ans = new LinkedList<>();
        collectAnswersRecursive(functions, 0, 0, "", ans);

        return ans;
    }

    public static void main(String[] args) {
        List<Function> procedures = readInput("input.txt");
        for (Function func : procedures) {
            System.out.println(func.toString() + "\n");
        }

        List<Long> ans = collectAnswers(procedures);
        for (Long result : ans) {
            System.out.println(result);
        }
        System.out.println(String.format("Size: %d", ans.size()));
    }
}