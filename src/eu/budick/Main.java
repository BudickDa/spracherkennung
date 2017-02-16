package eu.budick;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {

    public static void main(String[] args) {
        File resourcesDirectory = new File("src/eu/budick/resources");
        String vectorDirName = resourcesDirectory.getAbsolutePath() + "/CEP";

        ArrayList<String> trainingCases = Main.getListFromFile(resourcesDirectory + "/Listen/training.txt");
        ArrayList<String> testCases = Main.getListFromFile(resourcesDirectory + "/Listen/test.txt");

        NearestNeighbor nn = new NearestNeighbor();
        nn.train(trainingCases);
        nn.test(testCases);
        nn.displayResults();

        /*String fileName = dirName + "/0-AB.cep";
        System.out.println("Reading: " + fileName);
        float[] vector = Main.getVector(fileName);
        for (float v : vector) {
            System.out.println(v);
        }
        */

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
