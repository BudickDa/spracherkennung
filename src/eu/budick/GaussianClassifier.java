package eu.budick;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by daniel on 16.02.17.
 */
public class GaussianClassifier {
    private int errors;
    private int matches;
    private ArrayList<float[]> trainingsData = new ArrayList<float[]>();
    private ArrayList<float[]> meanVectors = new ArrayList<float[]>();
    private ArrayList<float[]> deviationVectors = new ArrayList<float[]>();
    private ArrayList<String> trainingsDataIndex = new ArrayList<String>();
    private ArrayList<String> phonemeList = new ArrayList<String>();
    File resourcesDirectory = new File("src/eu/budick/resources");

    public void train(ArrayList<String> trainingCases, ArrayList<String> phonemeList) {
        this.phonemeList = phonemeList;
        for (String fileName : trainingCases) {
            String phonem = this.getPhonem(fileName);
            fileName = this.getVectorPath(fileName);
            float[] vector = this.getVector(fileName);
            this.trainingsData.add(vector);
            this.trainingsDataIndex.add(phonem);
        }

        for(String phoneme : this.phonemeList){
            ArrayList<Integer> indicies = this.indexOfAll(phoneme, this.trainingsDataIndex);
            ArrayList<float[]> tmp = new ArrayList<float[]>();
            for(int index: indicies){
                tmp.add(this.trainingsData.get(index));
            }
            float[] tmpMean = this.getMeanVector(tmp);
            this.meanVectors.add(tmpMean);
            this.deviationVectors.add(this.getStandardDeviation(tmp, tmpMean));
        }
    }

    public void test(ArrayList<String> testCases) {
        this.errors = 0;
        this.matches = 0;
        for (String fileName : testCases) {
            String phonem = this.getPhonem(fileName);
            fileName = this.getVectorPath(fileName);
            float[] vector = this.getVector(fileName);
            String result = this.getNearestNeighbor(vector);
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
        System.out.println("Errors: " + this.errors + ", Matches: " + this.matches + ", " + percent + " % correct.");
    }

    public String getNearestNeighbor(float[] vector) {
        int bestIndex = -1;
        double bestDistance = Double.MAX_VALUE;

        for (int i = 0; i < this.phonemeList.size(); i++) {
            double distance = this.getGaussianDistance(vector, i);
            System.out.println(distance);
            if (distance < bestDistance) {
                bestIndex = i;
                bestDistance = distance;
            }
        }
        return this.trainingsDataIndex.get(bestIndex);
    }

    public static float[] getMeanVector(ArrayList<float[]> vectors) {
        float[] meanVector = new float[13];
        Arrays.fill(meanVector, 0);
        for (float[] vector : vectors) {
            for (int i = 0; i < vector.length; i++) {
                meanVector[i] += vector[i];
            }
        }

        for (int i = 0; i < meanVector.length; i++) {
            meanVector[i] = meanVector[i] / Integer.toUnsignedLong(vectors.size());
        }

        return meanVector;
    }

    public static float[] getStandardDeviation(ArrayList<float[]> vectors, float[] meanVector) {
        float[] deviationVector = new float[13];
        Arrays.fill(deviationVector, 0);
        for (float[] vector : vectors) {
            for (int i = 0; i < vector.length; i++) {
                deviationVector[i] += Math.pow(vector[i] - meanVector[i], 2);
            }
        }
        for (int i = 0; i < deviationVector.length; i++) {
            deviationVector[i] = (float) Math.sqrt(deviationVector[i] /= vectors.size());
        }
        return deviationVector;
    }

    public double getGaussianDistance(float[] v1, int phonemeIndex) {
        float sum1 = 0;
        for (int i = 0; i < v1.length; i++) {
            sum1 += Math.log(2 * Math.PI * Math.pow(this.deviationVectors.get(phonemeIndex)[i], 2));
        }
        float sum2 = 0;
        for (int i = 0; i < v1.length; i++) {
            sum2 += Math.pow(v1[i] - this.meanVectors.get(phonemeIndex)[i], 2) / Math.pow(this.deviationVectors.get(phonemeIndex)[i], 2);
        }
        return -2 * Math.log(1. / 11.) + sum1 + sum2;

    }

    public static String getPhonem(String fileName) {
        return fileName.split("-")[0];
    }

    public String getVectorPath(String fileName) {
        String vectorDirName = this.resourcesDirectory.getAbsolutePath() + "/CEP";
        return vectorDirName + "/" + fileName.replace(".WAV", ".cep");
    }

    public static ArrayList<float[]> normalize(ArrayList<float[]> vectors) {
        return vectors;
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

    private static ArrayList<Integer> indexOfAll(Object obj, ArrayList list){
        ArrayList<Integer> indexList = new ArrayList<Integer>();
        for (int i = 0; i < list.size(); i++)
            if(obj.equals(list.get(i)))
                indexList.add(i);
        return indexList;
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
