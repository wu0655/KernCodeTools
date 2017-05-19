package com.example;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.example.MainTest.printUsuage;

public class BuildAnalysis {
    private static final boolean DEBUG = false;

    static final int UNINT = -1;
    static final int DEPS_LINE = 3;
    static final int SOURCE_LINE = 2;
    String m_deps_line = null;

    String m_blt_path;
    String m_kern_path;
    String m_curr_path;
    String m_out;
    String m_map_path;

    Map<String, String> m_c_map = new HashMap<>();
    Map<String, String> m_h_map = new HashMap<>();
    Map<String, String> m_other_map = new HashMap<>();

    Map<String, String> m_cmd_map = new HashMap<>();


    int m_total_temp;
    int m_total;

    public BuildAnalysis(String kern_path, String built_path) {
        File f = new File(built_path);
        try {
            m_blt_path = f.getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
        }

        f = new File(kern_path);
        try {
            m_kern_path = f.getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
        }

        m_total = 0;
    }

    public boolean init() {
        if ((m_kern_path == null) || (m_blt_path == null))
            return false;

        String cmd;
        Process ps;
        boolean ret = false;
        try {
            cmd = "pwd";
            ps = Runtime.getRuntime().exec(cmd);
            ps.waitFor();

            BufferedReader br = new BufferedReader(new InputStreamReader(ps.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                if (m_curr_path == null)
                    m_curr_path = line;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (m_curr_path != null) {
            if (m_out == null) {
                File dir = new File(m_curr_path + "/out");
                dir.deleteOnExit();
                dir.mkdir();

                m_out = dir.getAbsolutePath() + "/list.txt";
            }

            ret = true;
        }

        return ret;
    }

    public boolean runAnalysis() {
        int count = SearchBuildCmdFile();
        if (count == 0) {
            System.out.println("not valid info founded.\n");
            printUsuage();
            return false;
        }

        for (String name : m_cmd_map.keySet()) {
            parseCmdFile(name);
        }

        m_total = m_c_map.size() + m_h_map.size();
        flush_clear_map(m_c_map, false, "source file in cmd file");
        flush_clear_map(m_h_map, true, "header file in cmd file");

        //dump(m_other_map);
        return true;
    }

    int handle_deps(String line) {
        if (line.startsWith("deps_"))
            return DEPS_LINE;

        String path = line.split(" ")[0].trim();
        if (path.startsWith(m_kern_path))
            m_h_map.put(path, "");

        return (line.endsWith("\\")) ? DEPS_LINE : UNINT;
    }

    int handle_source(String line) {
        String arr[] = line.split(":=");
        String filename = arr[arr.length - 1].trim();

        if (filename.startsWith(m_kern_path))
            m_c_map.put(filename, "");
        else
            m_other_map.put(filename, "");
        return UNINT;
    }

    int parseCmdFile(String path) {
        int count = 0;
        try {
            FileReader reader = new FileReader(path);
            BufferedReader br = new BufferedReader(reader);
            int line_type = -1;

            String line = null;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line_type == UNINT) {
                    if (line.startsWith("source_")) {
                        line_type = handle_source(line);
                    } else if (line.startsWith("deps_")) {
                        line_type = handle_deps(line);
                    }
                } else {
                    switch (line_type) {
                        case DEPS_LINE:
                            line_type = handle_deps(line);
                            break;
                    }
                }
            }

            br.close();
            reader.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    int printResult() {
        File out = new File(m_out);
        System.out.println("output = " + out.getAbsolutePath());
        System.out.println("total=" + m_total);

        return 0;
    }

    int test() {
        System.out.println("-------------------");
        dump(m_other_map);
        System.out.println("-------------------");
        return 0;
    }

    public int SearchBuildCmdFile() {
        int count = 0;
        File f = new File(m_blt_path);

        try {
            SearchBltDir search = new SearchBltDir(m_cmd_map, ".*.cmd");
            if (!search.init()) {
                System.out.println("Error while init search. path = " + f.getAbsolutePath());
                return -1;
            }

            count += search.doSearchDir(f);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return count;
    }

    public int flush_clear_map(Map<String, String> map, boolean if_append, String str) {
        return flush_key(map, true, if_append, str);
    }

    public int flush_key(Map<String, String> map, boolean is_clear, boolean is_append, String str) {
        int count = 0;
        String out_path = m_out;

        try {
            // write string to file
            FileWriter writer = new FileWriter(out_path, is_append);
            BufferedWriter bw = new BufferedWriter(writer);
            if (str != null)
                bw.write("#" + str + "++begin++\n");
            for (String key : map.keySet()) {
                bw.write(key + "\n");
                count++;
            }
            if (str != null)
                bw.write("#" + str + "++end++\n");
            bw.close();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (DEBUG)
            System.out.println("flush num =" + map.size());
        if (is_clear)
            map.clear();
        return count;
    }

    public int flush_val(Map<String, String> map, boolean is_clear, boolean is_append, String str) {
        int count = 0;
        String out_path = m_out;

        try {
            // write string to file
            FileWriter writer = new FileWriter(out_path, is_append);
            BufferedWriter bw = new BufferedWriter(writer);
            if (str != null)
                bw.write("#" + str + "++begin++\n");
            for (String val : map.values()) {
                bw.write(val + "\n");
                count++;
            }
            if (str != null)
                bw.write("#" + str + "++end++\n");
            bw.close();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (DEBUG)
            System.out.println("flush num =" + map.size());

        if (is_clear)
            map.clear();
        return count;
    }

    int dump() {
        int total = 0;
        total += dump(m_c_map);
        System.out.println("++++++++++++++");
        total += dump(m_h_map);
        System.out.println("++++++++++++++");
        return total;
    }

    int dump(Map<String, String> map) {
        Iterator iter = map.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            Object key = entry.getKey();
            Object val = entry.getValue();

            System.out.println(key.toString() + " = " + val.toString());
        }

        return map.size();
    }
}

