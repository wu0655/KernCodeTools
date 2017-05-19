package com.example;

import java.io.File;

public class MainTest {
    //new ArrayList<Map.Entry<String, Integer>>(map.entrySet());
    public static void main(String args[]) {
        for (int i = 0; i < args.length; i++) {
            System.out.println("args[" + i + "]=" + args[i]);
        }

        File f = new File(args[0]);
        if (!f.isDirectory()) {
            System.out.println("input dir is not valid\n");
            printUsuage();
            return;
        }
        String kern_path = args[0];
        String in_path = args[1];

        long start = System.currentTimeMillis();
        long curr = 0;
        BuildAnalysis test = new BuildAnalysis(kern_path, in_path);
        if (test.init()) {
            test.runAnalysis();
            test.printResult();
        }
        long stop = System.currentTimeMillis();
        //test.test();
        System.out.println("analysis tooks " + (stop - start) + " ms");
    }

    public static void printUsuage() {
        System.out.println("Usage: java -jar xx.jar [KERN_SOURCE_DIR] [KERNEL_BUILD_DIR]\n");
    }
}