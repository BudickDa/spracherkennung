package eu.budick;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        File resourcesDirectory = new File("src/eu/budick/resources");
        String vectorDirName = resourcesDirectory.getAbsolutePath() + "/CEP";

        ArrayList<String> trainingCases = Main.getListFromFile(resourcesDirectory + "/Listen/training.txt");
        ArrayList<String> testCases = Main.getListFromFile(resourcesDirectory + "/Listen/test.txt");
        ArrayList<String> phonemeList = Main.getListFromFile(resourcesDirectory + "/Listen/phonemes.txt");

        NearestNeighbor nn = new NearestNeighbor();

        /**
         * Do not normalize
         */
        nn.train(trainingCases);
        nn.test(testCases, false);
        nn.displayResults();

        /**
         * Normalize
         */
        nn.test(testCases, true);
        nn.displayResults();


        /**
         * Gaussian Classifier
         */
        GaussianClassifier gc = new GaussianClassifier();
        gc.train(trainingCases, phonemeList);
        gc.test(testCases);
        gc.displayResults();

    }

    public static ArrayList<String> getListFromFile(String fileName) {
        ArrayList<String> result = new ArrayList<String>();
        try {
            BufferedReader in = new BufferedReader(new FileReader(fileName));

            while (in.ready()) {
                result.add(in.readLine());
            }
            in.close();
        } catch (Exception e) {
            System.out.print("Error: " + e.getMessage());
        }
        return result;
    }


}
