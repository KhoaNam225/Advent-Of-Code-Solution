import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class Solution {

    /**
     * This class is the general class represent a Packet (a Packet can be Literal or Operator)
     */
    private static abstract class Packet {
        protected int version;
        protected int type;

        public Packet(int version, int type) {
            this.version = version;
            this.type = type;
        }

        public String printPacket(int level) {
            String tabs = "";
            for (int i = 0; i < level; i++) {
                tabs += "\t";
            }
            return String.format("%sVersion: %d - Type: %d", tabs, this.version, this.type);
        }

        public abstract int getVersionSum();
        public abstract long evaluate();
    }

    /** This class represents Literal */
    private static class Literal extends Packet {
        private long value;

        public Literal(int version, int type, long value) {
            super(version, type);
            this.value = value;
        }

        public String printPacket(int level) {
            return String.format("%s - Value: %d", super.printPacket(level), this.value);
        }

        public int getVersionSum() {
            return this.version;
        }

        public long evaluate() {
            return this.value;
        }
    }

    /** This class represents an Operator */
    private static class Operator extends Packet {
        private int lengthType;
        private List<Packet> subPackets;

        public Operator(int version, int type, int lengthType) {
            super(version, type);
            this.lengthType = lengthType;
            this.subPackets = new ArrayList<>();
        }

        public int getLengthType() {
            return this.lengthType;
        }

        public String printPacket(int level) {
            String str = super.printPacket(level) + " - Length type: " + this.lengthType + "\n";
            String subPacketStr = "";
            for (Packet subPacket : this.subPackets) {
                subPacketStr += subPacket.printPacket(level + 1) + "\n";
            }

            return str + subPacketStr;
        }

        public int getVersionSum() {
            int sum = this.version;
            for (Packet subPacket : this.subPackets) {
                sum += subPacket.getVersionSum();
            }

            return sum;
        }

        public long evaluate() {
            switch (this.type) {
                case 0:
                    return this.sum();
                case 1:
                    return this.multiply();
                case 2:
                    return this.minimum();
                case 3:
                    return this.maximum();
                case 5:
                    return this.greaterThan();
                case 6:
                    return this.lessThan();
                case 7:
                    return this.equal();
            }

            return 0;
        }

        private long sum() {
            long sum = 0;
            for (Packet subPacket : this.subPackets) {
                sum += subPacket.evaluate();
            }

            return sum;
        }

        private long multiply() {
            long product = 1;
            for (Packet subPacket : this.subPackets) {
                product *= subPacket.evaluate();
            }

            return product;
        }

        private long minimum() {
            long min = Long.MAX_VALUE;
            for (Packet subPacket : this.subPackets) {
                long value = subPacket.evaluate();
                if (value < min) {
                    min = value;
                }
            }

            return min;
        }

        private long maximum() {
            long max = Long.MIN_VALUE;
            for (Packet subPacket : this.subPackets) {
                long value = subPacket.evaluate();
                if (value > max) {
                    max = value;
                }
            }

            return max;
        }

        private long greaterThan() {
            long value1 = this.subPackets.get(0).evaluate();
            long value2 = this.subPackets.get(1).evaluate();

            return value1 > value2 ? 1 : 0;
        }

        private long lessThan() {
            long value1 = this.subPackets.get(0).evaluate();
            long value2 = this.subPackets.get(1).evaluate();

            return value1 < value2 ? 1 : 0;
        }

        private long equal() {
            long value1 = this.subPackets.get(0).evaluate();
            long value2 = this.subPackets.get(1).evaluate();

            return value1 == value2 ? 1 : 0;
        }
    }

    private static int parseBinString(String binStr, int position, List<Packet> packets) {
        int version = Integer.parseInt(binStr.substring(position, position + 3), 2);
        position += 3;
        int packetType = Integer.parseInt(binStr.substring(position, position + 3), 2);
        position += 3;

        if (packetType == 4) {
            String packetValue = parseLiteralValue(binStr, position);
            packets.add(new Literal(version, packetType, Long.parseLong(packetValue, 2)));
            int length = 5 * packetValue.length() / 4;
            position += length;
        }
        else {
            int lengthType = binStr.charAt(position) - '0';
            Operator operator = new Operator(version, packetType, lengthType);
            position++;

            position = parseOperator(binStr, operator, position);
            packets.add(operator);
        }

        return position;
    }

    private static int parseOperator(String binStr, Operator operator, int position) {
        if (operator.getLengthType() == 0) return parseOperatorLengthType1(binStr, operator, position);
        return parseOperatorLengthType2(binStr, operator, position);
    }

    private static int parseOperatorLengthType1(String binStr, Operator operator, int position) {
        int totalPacketLength = Integer.parseInt(binStr.substring(position, position + 15), 2);
        position += 15;
        int finalPosition = totalPacketLength +  position;

        while (position < finalPosition) {
            position = parseBinString(binStr, position, operator.subPackets);
        }

        return position;
    }

    private static int parseOperatorLengthType2(String binStr, Operator operator, int position) {
        int nSubPackets = Integer.parseInt(binStr.substring(position, position + 11), 2);
        position += 11;
        
        while (operator.subPackets.size() < nSubPackets) {
            position = parseBinString(binStr, position, operator.subPackets);
        }

        return position;
    }

    private static String parseLiteralValue(String binStr, int position) {
        String packetValue = "";
        String digit = "";

        do {
            digit = binStr.substring(position, position + 5);
            packetValue += digit.substring(1);

            position += 5;
        }
        while (digit.charAt(0) != '0');

        return packetValue;
    }

    private static String hexToBin(String hex){
        hex = hex.replaceAll("0", "0000");
        hex = hex.replaceAll("1", "0001");
        hex = hex.replaceAll("2", "0010");
        hex = hex.replaceAll("3", "0011");
        hex = hex.replaceAll("4", "0100");
        hex = hex.replaceAll("5", "0101");
        hex = hex.replaceAll("6", "0110");
        hex = hex.replaceAll("7", "0111");
        hex = hex.replaceAll("8", "1000");
        hex = hex.replaceAll("9", "1001");
        hex = hex.replaceAll("A", "1010");
        hex = hex.replaceAll("B", "1011");
        hex = hex.replaceAll("C", "1100");
        hex = hex.replaceAll("D", "1101");
        hex = hex.replaceAll("E", "1110");
        hex = hex.replaceAll("F", "1111");
        return hex;
    }

    private static String readInput(String filename) {
        String input = "";
        
        try {
            File inputFile = new File(filename);
            BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFile));
            input = bufferedReader.readLine();
            
            bufferedReader.close();
        }
        catch (IOException exception) {
            System.out.println("Cannot read input file");
        }

        return input;
    }

    public static void main(String[] args) {
        String hexStr = readInput("input.txt");
        String binStr = hexToBin(hexStr);
        List<Packet> packets = new LinkedList<>();
        int stopPosition = parseBinString(binStr, 0, packets);

        int versionSum = 0;
        for (Packet packet : packets) {
            versionSum += packet.getVersionSum();
        }

        long value = packets.get(0).evaluate();

        System.out.println("Stop position: " + stopPosition + "\n");
        System.out.println("String full length: " + binStr.length() + "\n");
        System.out.println("Version sum: " + versionSum + "\n");  // Answer to Part 1
        System.out.println("Value: " + value + "\n");  // Answer to Part 2
    }
}
