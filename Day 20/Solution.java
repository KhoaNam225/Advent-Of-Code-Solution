import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.io.FileReader;


public class Solution {
    private static int[][] readInputImage(String filename) {
        int[][] matrix = null;

        try {
            File inputFile = new File(filename);
            BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFile));
            
            String result = null;
            int lineCount = 0;
            do {
                result = bufferedReader.readLine();
                if (result != null && result.length() > 0) {
                    lineCount++;
                }
            }
            while (result != null);

            matrix = new int[lineCount - 1][];
            bufferedReader.close();

            BufferedReader contentReader = new BufferedReader(new FileReader(inputFile));
            contentReader.readLine();
            contentReader.readLine();
            String line = null;
            lineCount = 0;
            do {
                line = contentReader.readLine();
                if (line != null) {
                    int[] pixels = new int[line.length()];
                    for (int i = 0; i < line.length(); i++) {
                        pixels[i] = line.charAt(i) == '#' ? 1 : 0;
                    }

                    matrix[lineCount] = pixels;
                    lineCount++;
                }
            }
            while (line != null); 

            contentReader.close();
        }
        catch (IOException exception) {
            System.out.println("Cannot read enhancement algorithms");
        }

        return matrix;
    }

    private static String readEnhancementAlgorithm(String fileName) {
        String result = "";

        try {
            File inputFile = new File(fileName);
            BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFile));

            result = bufferedReader.readLine();

            bufferedReader.close();
        }
        catch (IOException exception) {
            System.out.println("Cannot read enhancement algorithms");
        }

        return result;
    }

    private static void printImage(int[][] image) {
        for (int i = 0; i < image.length; i++) {
            for (int j = 0; j < image[0].length; j++) {
                System.out.print(image[i][j] + " ");
            }

            System.out.println();
        }
    }

    private static int[][] padImage(int[][] image, int padSize, int padPixel) {
        int rows = image.length;
        int cols = image[0].length;

        int newRows = rows + padSize * 2;
        int newCols = cols + padSize * 2;

        int[][] resultImg = new int[newRows][newCols];

        for (int i = 0; i < newRows; i++) {
            Arrays.fill(resultImg[i], padPixel);
        }

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                resultImg[i + padSize][j + padSize] = image[i][j];
            }
        }

        return resultImg;
    }

    private static String getBit(int[][] img, int row, int col, int defaultBit) {
        if (row < 0 || row >= img.length) return defaultBit + "";
        if (col < 0 || col >= img[0].length) return defaultBit + "";

        return img[row][col] + "";
    }

    private static int getConvolutedPixel(int[][] img, int row, int col, int defaultBit) {
        String resultPixel = "";

        for (int i = row - 1; i <= row + 1; i++) {
            for (int j = col - 1; j <= col + 1; j++) {
                resultPixel += getBit(img, i, j, defaultBit);
            }
        }

        return Integer.parseInt(resultPixel, 2);
    }

    private static int[][] getConvolutedImage(int[][] img, int defaultBit) {
        int rows = img.length;
        int cols = img[0].length;

        int[][] finalImage = new int[rows][cols];
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                finalImage[row][col] = getConvolutedPixel(img, row, col, defaultBit);
            }
        }

        return finalImage;
    }

    private static int[][] parseEnhancedImage(String enhancementString, int[][] convolutedImage) {
        int rows = convolutedImage.length;
        int cols = convolutedImage[0].length;

        int[][] finalImage = new int[rows][cols];

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int pos = convolutedImage[row][col];
                finalImage[row][col] = enhancementString.charAt(pos) == '#' ? 1 : 0;
            }
        }

        return finalImage;
    }

    private static int[][] performConvolution(int[][] image, String enhancementString, int defaultBit) {
        int[][] convolutedImage = getConvolutedImage(image, defaultBit);

        int[][] enhancedImage = parseEnhancedImage(enhancementString, convolutedImage);

        return enhancedImage;
    }

    private static int countLitPixel(int[][] img) {
        int count = 0;
        for (int i = 0; i < img.length; i++) {
            for (int j = 0; j < img[0].length; j++) {
                count += img[i][j];
            }
        }

        return count;
    }

    public static void main(String[] args) {
        String filename = "input.txt";
        String enhancementString = readEnhancementAlgorithm(filename);
        int[][] image = readInputImage(filename);
        int defaultBit = 0;
        for (int i = 0; i < 50; i++) {
            int[][] paddedImg = padImage(image, 2, defaultBit);
            image = performConvolution(paddedImg, enhancementString, defaultBit);

            if (defaultBit == 1) {
                defaultBit = enhancementString.charAt(511) == '#' ? 1 : 0;
            }
            else {
                defaultBit = enhancementString.charAt(0) == '#' ? 1 : 0;
            }
        }
        // int[][] paddedImg = padImage(image, 2);

        // int[][] firstEnhancement = performConvolution(paddedImg, enhancementString);
        // int[][] secondEnhancement = performConvolution(firstEnhancement, enhancementString);

        System.out.println(countLitPixel(image));
        // // printImage(image);
        // // System.out.println();
        // printImage(secondEnhancement);
    }    
}
