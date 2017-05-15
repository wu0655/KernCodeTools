package com.example;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import static com.example.utils.flush_key;

/**
 * Created by wupeng on 17-5-15.
 */

public class utils {
    static final boolean DEBUG = false;

    public static boolean isSymlink(File file) throws IOException {
        if (file == null)
            throw new NullPointerException("File must not be null");
        File canon;
        if (file.getParent() == null) {
            canon = file;
        } else {
            File canonDir = file.getParentFile().getCanonicalFile();
            canon = new File(canonDir, file.getName());
        }
        return !canon.getCanonicalFile().equals(canon.getAbsoluteFile());
    }

    public static int flush_clear_map(Map<String, String> map, String out, boolean if_append, String str) {
        return flush_key(map, out, true, if_append, str);
    }

    public static int flush_key(Map<String, String> map, String out, boolean is_clear, boolean is_append, String str) {
        int count = 0;

        try {
            // write string to file
            FileWriter writer = new FileWriter(out, is_append);
            BufferedWriter bw = new BufferedWriter(writer);
            if (str != null)
                bw.write("#" + str+ "++begin++\n");
            for (String key : map.keySet()) {
                bw.write(key + "\n");
                count ++;
            }
            if (str != null)
                bw.write("#" + str+"++end++\n");
            bw.close();
            writer.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        if (DEBUG)
            System.out.println("flush num =" + map.size());
        if (is_clear)
            map.clear();
        return count;
    }

    public static int flush_val(Map<String, String> map, String out, boolean is_clear, boolean is_append, String str) {
        int count = 0;

        try {
            // write string to file
            FileWriter writer = new FileWriter(out, is_append);
            BufferedWriter bw = new BufferedWriter(writer);
            if (str != null)
                bw.write("#" + str+ "++begin++\n");
            for (String val : map.values()) {
                bw.write(val + "\n");
                count ++;
            }
            if (str != null)
                bw.write("#" + str+"++end++\n");
            bw.close();
            writer.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        if (DEBUG)
            System.out.println("flush num =" + map.size());

        if (is_clear)
            map.clear();
        return count;
    }
}
