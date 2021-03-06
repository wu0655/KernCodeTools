package com.example;

/**
 * Created by wupeng on 17-3-12.
 */

public class MainTest {
    //new ArrayList<Map.Entry<String, Integer>>(map.entrySet());
    public static void main(String args[]) {
        for (int i=0; i< args.length; i++) {
            System.out.println("args[" + i + "]=" + args[i]);
        }

        if (args.length < 2) {
            printUsuage();
            return;
        }

        String kern_path = args[0];
        String map_path = args[1];
        String built_path = null;
        String out = null;


        /*
        if (args.length > 2)
            out = args[2];

        if (args.length > 3)
            built_path = args[3];
        */
        /*
        String map_path = "/home/wupeng/work/8996n/out/target/product/le_x10/obj/kernel/msm-3.18/System.map.all";
        String kern_path = "/home/wupeng/work/8996n/kernel/msm-3.18";
        String built_path = "/home/wupeng/work/8996n/out/target/product/le_x10/obj/kernel/";
        String out = "list.txt";
        */

        long start = System.currentTimeMillis();
        long curr = 0;
        SystemMapAnalysis test = new SystemMapAnalysis(kern_path, map_path, built_path, out);
        if (test.init()) {
            test.runAnalysis();
            test.printResult();
        }
        long stop = System.currentTimeMillis();
        //test.test();
        System.out.println("analysis tooks " + (stop - start) + " ms");

        //SystemMapAnalysis test = new SystemMapAnalysis(map_path, kern_path, out, built_path);
        //test.rmNotBuildCFile();
    }



    public static void printUsuage() {
        System.out.println("Usage: java -jar gitsize.jar [KENEL_SRC DIR] [SYSTEM_MAP PATH] [OUT_FILE] [OPTION KERNEL_BUILT]\n");
        System.out.println("Usage: at least 2 parameters are needed.\n");
    }
}
