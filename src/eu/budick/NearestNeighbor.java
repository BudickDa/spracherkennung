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
            //System.out.println("Reading Trainingsfile: " + fileName);
            float[] vector = this.getVector(fileName);
            this.trainingsData.add(vector);
            this.trainingsDataIndex.add(phonem);
        }
    }

    public void test(ArrayList<String> testCases, boolean normalize) {
        this.errors = 0;
        this.matches = 0;
        for (String fileName : testCases) {
            String phonem = this.getPhonem(fileName);
            fileName = this.getVectorPath(fileName);
            //System.out.println("Reading Testfile: " + fileName);
            float[] vector = this.getVector(fileName);
            String result = this.getNearestNeighbor(vector, normalize);
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

    public String getNearestNeighbor(float[] vector, boolean normalize) {
        int bestIndex = -1;
        double bestDistance = Double.MAX_VALUE;

        if(normalize){
            float[] meanVector = this.getMeanVector(this.trainingsData);
            float[] deviationVector = this.getStandardDeviation(this.trainingsData, meanVector);


            for (int i = 0; i < this.trainingsData.size(); i++) {
                float[] tmpVector = this.trainingsData.get(i);
                double distance = this.getEuclidDistance(this.normalize(vector, meanVector, deviationVector), this.normalize(tmpVector, meanVector, deviationVector));
                if (distance < bestDistance) {
                    bestIndex = i;
                    bestDistance = distance;
                }
            }
        }else {
            for (int i = 0; i < this.trainingsData.size(); i++) {
                float[] tmpVector = this.trainingsData.get(i);
                double distance = this.getEuclidDistance(vector, tmpVector);
                if (distance < bestDistance) {
                    bestIndex = i;
                    bestDistance = distance;
                }
            }
        }
        return this.trainingsDataIndex.get(bestIndex);
    }

    public static float[] normalize(float[] vector, float[] meanVector, float[] deviationVector){
        float[] normalizedVector = new float [13];
        for(int i = 0; i < vector.length; i++){
            normalizedVector[i] = (vector[i] - meanVector[i]) / deviationVector[i];
        }
        return normalizedVector;
    }

    public static float[] getMeanVector(ArrayList<float[]> vectors){
        float [] meanVector = new float[13];
        Arrays.fill(meanVector, 0);
        for(float[] vector: vectors){
            for(int i = 0; i<vector.length; i++){
                meanVector[i] += vector[i];
            }
        }

        for(int i = 0; i<meanVector.length; i++){
            meanVector[i] = meanVector[i] / Integer.toUnsignedLong(vectors.size());
        }

        return meanVector;
    }

    public static float[] getStandardDeviation(ArrayList<float[]> vectors, float[] meanVector){
        float [] deviationVector = new float[13];
        Arrays.fill(deviationVector, 0);
        for(float[] vector: vectors){
            for(int i = 0; i<vector.length; i++){
                deviationVector[i] += Math.pow(vector[i] - meanVector[i], 2);
            }
        }
        for(int i = 0; i<deviationVector.length; i++){
            deviationVector[i] = (float)Math.sqrt(deviationVector[i] /= vectors.size());
        }
        return deviationVector;
    }

    public static double getEuclidDistance(float[] v1, float[] v2) {
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

    public static ArrayList<float[]> normalize(ArrayList<float[]> vectors){
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


    public static float getFloat(byte[] data) {
        ByteBuffer _intShifter = ByteBuffer.allocate(Float.SIZE / Byte.SIZE)
                .order(ByteOrder.LITTLE_ENDIAN);
        _intShifter.clear();
        _intShifter.put(data, 0, Float.SIZE / Byte.SIZE);
        _intShifter.flip();
        return _intShifter.getFloat();
    }
}
