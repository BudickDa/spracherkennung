package eu.budick;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.util.ArrayList;

/**
 * Created by daniel on 16.02.17.
 */
public class NearestNeighbor {
    private int errors;
    private int matches;
    private ArrayList<float[]> trainingsData = new ArrayList<float[]>();
    private ArrayList<String> trainingsDataIndex = new ArrayList<String>();
    File resourcesDirectory = new File("src/eu/budick/resources");

    public void train(ArrayList<String> trainingCases) {
        for (String fileName : trainingCases) {
            String phonem = this.getPhonem(fileName);
            fileName = this.getVectorPath(fileName);
            System.out.println("Reading Trainingsfile: " + fileName);
            float[] vector = this.getVector(fileName);
            this.trainingsData.add(vector);
            this.trainingsDataIndex.add(phonem);
        }
    }

    public void test(ArrayList<String> testCases) {
        this.errors = 0;
        this.matches = 0;
        for (String fileName : testCases) {
            String phonem = this.getPhonem(fileName);
            fileName = this.getVectorPath(fileName);
            System.out.println("Reading Testfile: " + fileName);
            float[] vector = this.getVector(fileName);
            String result = this.getNearestNeighbor(vector);
            System.out.println(result);
            if (phonem.equals(result)) {
                this.matches++;
            } else {
                this.errors++;
            }
        }
    }

    public void displayResults() {
        float sum = this.matches + this.errors;
        float percent = (Integer.toUnsignedLong(this.matches) / sum) * 100;
        System.out.println(percent);
        System.out.println("Errors: " + this.errors + ", Matches: " + this.matches + ", " + percent + " % correct.");
    }

    public String getNearestNeighbor(float[] vector) {
        int bestIndex = -1;
        double bestDistance = Double.MAX_VALUE;
        for (int i = 0; i < this.trainingsData.size(); i++) {
            float[] tmpVector = this.trainingsData.get(i);
            double distance = this.euclidDistance(vector, tmpVector);
            if (distance < bestDistance) {
                bestIndex = i;
                bestDistance = distance;
            }
        }
        return this.trainingsDataIndex.get(bestIndex);
    }

    public static double euclidDistance(float[] v1, float[] v2) {
        float sum = 0;
        for (int i = 0; i < v1.length; i++) {
            sum += Math.pow(v1[i] - v2[i], 2);
        }
        return Math.sqrt(sum);

    }

    public static String getPhonem(String fileName) {
        return fileName.split("-")[0];
    }

    public String getVectorPath(String fileName) {
        String vectorDirName = this.resourcesDirectory.getAbsolutePath() + "/CEP";
        return vectorDirName + "/" + fileName.replace(".WAV", ".cep");
    }

    private float[] getVector(String fileName) {
        float[] result = new float[13];
        byte[] tmp;
        try {
            byte[] bytes = Files.readAllBytes(new File(fileName).toPath());

            int start = 4 + 52 * ((int) Math.floor((double) (bytes.length - 4) / 104));
            for (int i = 0; i < 13; i++) {
                tmp = new byte[4];
                tmp[3] = bytes[start];
                tmp[2] = bytes[start + 1];
                tmp[1] = bytes[start + 2];
                tmp[0] = bytes[start + 3];

                result[i] = this.getFloat(tmp);
                start += 4;

            }

        } catch (IOException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
        return result;

    }


    public static float getFloat(byte[] data) {
        ByteBuffer _intShifter = ByteBuffer.allocate(Float.SIZE / Byte.SIZE)
                .order(ByteOrder.LITTLE_ENDIAN);
        _intShifter.clear();
        _intShifter.put(data, 0, Float.SIZE / Byte.SIZE);
        _intShifter.flip();
        return _intShifter.getFloat();
    }
}
