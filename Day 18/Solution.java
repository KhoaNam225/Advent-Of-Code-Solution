import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Solution {
    private static abstract class Term {
        protected int depth;
        protected Term parent;

        public Term(int depth) {
            this.depth = depth;
            this.parent = null;
        }

        public abstract long eval();
        public abstract boolean isLiteral();
        public abstract String toString();
        public abstract void explode();
        public abstract void split();
        public abstract void nestOneLevel();
        public abstract boolean needReduce();
        public abstract Term clone();
    }

    private static class Literal extends Term {
        private int value;

        public Literal(int depth, int value) {
            super(depth);
            this.value = value;
        }

        @Override
        public long eval() {
            return this.value;
        }

        @Override
        public boolean isLiteral() {
            return true;
        }

        @Override
        public String toString() {
            return Integer.toString(this.value);
        }

        @Override
        public void split() {
            if (this.value > 9) {
                // System.out.println(String.format("Split value: %d", this.value));
                int left = value / 2;
                int right = value - left;

                Pair newTerm = new Pair(depth);
                Literal leftLiteral = new Literal(depth + 1, left);
                Literal rightLiteral = new Literal(depth + 1, right);
                newTerm.left = leftLiteral;
                newTerm.right = rightLiteral;
                leftLiteral.parent = newTerm;
                rightLiteral.parent = newTerm;


                Pair parent = (Pair) this.parent;
                if (parent.left == this) {
                    parent.left = newTerm;
                }
                else {
                    parent.right = newTerm;
                }

                newTerm.parent = parent;
            }
        }

        @Override
        public void nestOneLevel() {
            this.depth++;
        }

        @Override
        public boolean needReduce() {
            return this.value > 9;
        }

        @Override
        public void explode() {}

        @Override
        public Term clone() {
            return new Literal(depth, value);
        }
    }

    private static class Pair extends Term {
        private Term left;
        private Term right;

        public Pair(int depth) {
            super(depth);
            this.left = null;
            this.right = null;
        }

        @Override
        public boolean isLiteral() {
            return false;
        }

        @Override
        public long eval() {
            long leftValue = this.left.eval();
            long rightValue = this.right.eval();

            return leftValue * 3 + rightValue * 2;
        }

        @Override
        public void explode() {
            if (this.left.isLiteral() && this.right.isLiteral() && this.depth >= 4) {
                List<Literal> allLiterals = this.getAllLiterals();
                int leftIndex = 0;
                for (Literal lit : allLiterals) {
                    if (lit == this.left) {
                        break;
                    }

                    leftIndex++;
                }

                if (leftIndex > 0) {
                    Literal leftMost = allLiterals.get(leftIndex - 1);
                    leftMost.value += this.left.eval();
                }

                if (leftIndex + 1 < allLiterals.size() - 1) {
                    Literal rightMost = allLiterals.get(leftIndex + 2);
                    rightMost.value += this.right.eval();
                }

                Pair parent = (Pair) this.parent;
                // System.out.println(String.format("%s - Level: %d", this.toString(), this.depth));
                Literal newLit = new Literal(depth, 0);
                if (parent.left == this) {
                    parent.left = newLit;
                }
                else {
                    parent.right = newLit;
                }
                newLit.parent = parent;
            }
            else {
                if (!this.left.isLiteral()) this.left.explode();
                if (!this.right.isLiteral()) this.right.explode();
            }
        }

        @Override
        public String toString() {
            return String.format("[%s,%s]", left.toString(), right.toString());
        }

        @Override
        public void nestOneLevel() {
            this.depth++;
            this.left.nestOneLevel();
            this.right.nestOneLevel();
        }

        @Override
        public boolean needReduce() {
            if (this.left.isLiteral() && this.right.isLiteral() && this.depth >= 4) return true;

            return this.left.needReduce() || this.right.needReduce();
        }

        @Override
        public void split() {
            List<Literal> allLiterals = this.getAllLiterals();
            for (Literal literal : allLiterals) {
                if (literal.needReduce()) {
                    literal.split();
                    break;
                }
            }
        }

        @Override
        public Term clone() {
            Pair cloned = new Pair(depth);
            Term left = this.left.clone();
            Term right = this.right.clone();

            cloned.left = left;
            cloned.right = right;
            left.parent = cloned;
            right.parent = cloned;

            return cloned;
        }

        private List<Literal> getAllLiterals() {
            Pair root = this.getRootTerm();
            List<Literal> allLits = new ArrayList<>();
            this.getAllLiteralsRecursive(root, allLits);

            return allLits;
        }

        private Pair getRootTerm() {
            Pair currentTerm = this;
            while (currentTerm.parent != null) {
                currentTerm = (Pair) currentTerm.parent;
            }

            return currentTerm;
        }

        private void getAllLiteralsRecursive(Term root, List<Literal> allLiterals) {
            if (root.isLiteral()) {
                allLiterals.add((Literal) root);
                return;
            }

            Pair rootPair = (Pair) root;
            getAllLiteralsRecursive(rootPair.left, allLiterals);
            getAllLiteralsRecursive(rootPair.right, allLiterals);
        }
    }

    private static Term parseInput(String input) {
        int pos = 0;
        Stack<Term> stack = new Stack<>();
        Term currentTerm = null;

        while (pos < input.length()) {
            char currChar = input.charAt(pos);
            if (currChar == '[') {
                stack.push(new Pair(stack.size()));
                currentTerm = null;
            }

            if (currChar >= '0' && currChar <= '9') {
                currentTerm = new Literal(stack.size(), currChar - '0');
            }

            if (currChar == ',') {
                Pair parentTerm = (Pair) stack.peek();
                parentTerm.left = currentTerm;
                currentTerm.parent = parentTerm;
                currentTerm = null;
            }

            if (currChar == ']') {
                Pair parentTerm = (Pair) stack.peek();
                parentTerm.right = currentTerm;
                currentTerm.parent = parentTerm;
                currentTerm = stack.pop();
            }

            pos++;
        }

        return currentTerm;
    }

    private static List<Term> readInput(String filename) {
        String input = "";
        List<Term> allTerms = new ArrayList<>();

        try {
            File inputFile = new File(filename);
            BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFile));

            do {
                input = bufferedReader.readLine();
                if (input != null) {
                    Term term = parseInput(input);
                    allTerms.add(term);
                }
            }
            while (input != null);

            bufferedReader.close();
        }
        catch (IOException exception) {
            System.out.println("Cannot read input file");
        }

        return allTerms;
    }

    private static Term addTwoTerm(Term a, Term b) {
        Pair parent = new Pair(0);
        a.nestOneLevel();
        b.nestOneLevel();

        parent.left = a;
        parent.right = b;

        a.parent = parent;
        b.parent = parent;

        // System.out.println(parent.toString());

        parent = (Pair) reduce(parent);
        
        return parent;
    }

    private static Term reduce(Term root) {
        while (root.needReduce()) {
            root.explode();
            root.split();
        }
        return root;
    }

    public static void main(String[] args) {
        List<Term> allTerms = readInput("input.txt");
        long largestMagnitude = Long.MIN_VALUE;
        for (int i = 0; i < allTerms.size(); i++) {
            for (int j = 0; j < allTerms.size(); j++) {
                if (i != j) {
                    Term left = allTerms.get(i).clone();
                    Term right = allTerms.get(j).clone();
                    Term sum = addTwoTerm(left, right);
                    // System.out.println(String.format("%s + %s = %d", left.toString(), right.toString(), sum.eval()));
                    largestMagnitude = Math.max(sum.eval(), largestMagnitude);
                }
            }
        }

        System.out.println(largestMagnitude);
    }
}
