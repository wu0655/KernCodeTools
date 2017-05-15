package com.example;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.example.MainTest.printUsuage;

public class SystemMapAnalysis {
    private static final boolean DEBUG = false;

    String m_kernel_path;
    String m_built_path;

    String m_curr_path;
    String m_out_temp;
    String m_out;
    String m_map_path;

    Map<String, String> m_c_map = new HashMap<>();
    Map<String, String> m_h_map = new HashMap<>();
    Map<String, String> m_other_map = new HashMap<>();

    Map<String, String> m_sys_h_map = new HashMap<>();
    Map<String, String> m_built_h_map = new HashMap<>();
    Map<String, String> m_kern_c_map = new HashMap<>();
    Map<String, String> m_kern_h_map = new HashMap<>();
    Map<String, String> m_kern_c_map_dup = new HashMap<>();
    Map<String, String> m_kern_h_map_dup = new HashMap<>();


    int m_total_temp;
    int m_total;

    public SystemMapAnalysis(String kernel, String map_path, String out, String built_path) {
        m_kernel_path = kernel;
        m_map_path = map_path;
        m_total_temp = 0;
        m_total = 0;
    }

    public boolean init() {
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
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        if (m_curr_path != null) {
            if (m_out == null) {
                File dir = new File(m_curr_path + "/out");
                dir.deleteOnExit();
                dir.mkdir();

                m_out = dir.getAbsolutePath() + "/list.txt";
                m_out_temp = dir.getAbsolutePath() + "/list.txt.tmp";
            }

            ret = true;
        }

        return ret;
    }

    public boolean runAnalysis() {
        int count = parseMap();
        if (count == 0) {
            System.out.println("not valid info founded.\n");
            printUsuage();
            return false;
        }
        handle_C_Files();
        extractOrdinaryHead();
        extractSysHead();
        handle_other_map();
        convertToResult();

        return true;
    }

    int parseMap() {
        String path = m_map_path;
        int count = 0;
        try {
            FileReader reader = new FileReader(path);
            BufferedReader br = new BufferedReader(reader);


            String line = null;
            while((line = br.readLine()) != null) {
                String str[] = line.split("\\t");
                /*
                for (String tmp:str) {
                    System.out.println(tmp);
                }
                count ++;
                if (count > 8)
                    break;
                */
                if (str.length < 2)
                    continue;
                String codepath = str[1].split(":")[0];
                if (codepath.endsWith(".c")) {
                    m_c_map.put(codepath, null);
                }
                else
                    m_h_map.put(codepath, null);
                count++;
            }

            br.close();
            reader.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        count =  m_c_map.size() + m_h_map.size();
        m_total_temp += flush_clear_map(m_h_map, false, "code files except h in System.map");
        return count;
    }

    public int handle_C_Files() {
        int count = 0;
        for (String key : m_c_map.keySet()) {
            grepCFile(key);
        }

        count = flush_clear_map(m_h_map, true, "header in c code");
        count += flush_clear_map(m_c_map, true, "c code in system.map");
        m_total_temp += count;
        return count;
    }

    public String extractInclueStr(String str) {
        byte tmp[] = str.getBytes();
        int begin = 1;
        int end = begin;
        if (tmp[0] != '\"')
            return null;
        int i = 0;
        for (i=1; i<tmp.length; i++)
            if (tmp[i] == '\"')
                break;
        return (new String(tmp, 1, i-1));
    }

    public int grepCFile(String path) {
        int ret = 0;
        //System.out.println("xxxxx path =" + path);
        File f = new File(path);
        try {
            FileReader reader = new FileReader(path);
            BufferedReader br = new BufferedReader(reader);
            String l;
            String line;
            String include_str = "#include";
            String h_name;
            String inc_str;
            while((l = br.readLine()) != null) {
                line = l.trim();
                if (line.length() <= include_str.length())
                    continue;

                inc_str = line.substring(0, include_str.length());
                h_name = line.substring(include_str.length()).trim();

                if (inc_str.equals(include_str)) {
                    String local_h = extractInclueStr(h_name);
                    if (local_h != null) {
                        String abs_path = f.getParent()  + "/" + local_h;
                        File h_file = new File(abs_path);
                        if (h_file.exists()) {
                            m_h_map.put(abs_path, null);
                            ret++;
                        } else {
                            String xx = f.getAbsolutePath() + ";" + local_h;
                            String s[] = local_h.split("/");
                            String name = s[s.length - 1];
                            m_other_map.put(xx, name);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public int handle_other_map() {
        int count = 0;

        Iterator iter = m_other_map.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String key = (String)entry.getKey();
            String val = (String)entry.getValue();

            if (m_kern_c_map.containsKey(val)) {
                String v = m_kern_c_map.get(val);
                m_c_map.put(v, val);
                iter.remove();
            } else if (m_kern_h_map.containsKey(val)) {
                String v = m_kern_h_map.get(val);
                m_h_map.put(v, val);
                iter.remove();
            }
        }
        count += flush_clear_map(m_c_map, true, "other c file no dup");
        count += flush_clear_map(m_h_map, true, "other h file no dup");

        iter = m_other_map.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String key = (String)entry.getKey();
            String val = (String)entry.getValue();

            if (m_kern_c_map_dup.containsKey(val)) {
                String v = m_kern_c_map_dup.get(val);
                m_c_map.put(v, val);
                iter.remove();
            } else if (m_kern_h_map_dup.containsKey(val)) {
                String v = m_kern_h_map_dup.get(val);
                m_h_map.put(v, val);
                iter.remove();
            }
        }
        count += flush_clear_map(m_c_map, true, "other c file has dup");
        count += flush_clear_map(m_h_map, true, "other h file has dup");

        iter = m_other_map.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String key = (String)entry.getKey();
            String val = (String)entry.getValue();

            if (m_sys_h_map.containsKey(val)) {
                String v = m_sys_h_map.get(val);
                m_h_map.put(v, val);
                iter.remove();
            }
        }
        count += flush_clear_map(m_h_map, true, "other h file in system path");
        m_total_temp += count;

        //if other files is generated when compile
        if (m_built_path != null) {
            File dir = new File(m_built_path);
            if (dir.exists() && dir.isDirectory()) {
                SearchCCode search = new SearchCCode(0, null, m_built_h_map, null, null);
                if (search.init()) {
                    search.doSearchDir(dir);

                    iter = m_other_map.entrySet().iterator();
                    while (iter.hasNext()) {
                        Map.Entry entry = (Map.Entry) iter.next();
                        String key = (String)entry.getKey();
                        String val = (String)entry.getValue();

                        if (m_built_h_map.containsKey(val)) {
                            String v = m_built_h_map.get(val);
                            m_h_map.put(v, val);
                            iter.remove();
                        }
                    }

                    //print files which is not found. maybe in build_path
                    try {
                        FileWriter writer = new FileWriter(new File(m_out_temp), true);
                        BufferedWriter bw = new BufferedWriter(writer);
                        bw.write("#begin other files in kernel built path\n");
                        iter = m_h_map.entrySet().iterator();
                        while (iter.hasNext()) {
                            Map.Entry entry = (Map.Entry) iter.next();
                            String key = (String) entry.getKey();
                            String val = (String) entry.getValue();
                            bw.write("#" + key + ";" + val + "\n");
                        }
                        bw.write("#end other files in kernel built path\n");
                        bw.close();
                        writer.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    m_h_map.clear();
                }
            }
        }
        //print files which is not found. maybe in build_path
        try {
            FileWriter writer = new FileWriter(new File(m_out_temp), true);
            BufferedWriter bw = new BufferedWriter(writer);

            iter = m_other_map.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                String key = (String) entry.getKey();
                String val = (String) entry.getValue();
                bw.write("#" + key + ";" + val + "\n");
            }

            bw.close();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
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


    public int extractOrdinaryHead() {
        int count = 0;
        String p1 = "include";
        String p2 = "arch";

        File kern_path = new File(m_kernel_path);
        File flist[] = kern_path.listFiles();
        if (flist == null || flist.length == 0) {
            return 0;
        }

        try {
            for (File f : flist) {
                if (f.isDirectory()) {
                    //System.out.println("Dir==" + f.getName());
                    if (f.getName().equals("include") || f.getName().equals("arch"))
                        continue;

                    SearchCCode search = new SearchCCode(
                            0,
                            m_kern_c_map,
                            m_kern_h_map,
                            m_kern_c_map_dup,
                            m_kern_h_map_dup);

                    if (! search.init()) {
                        System.out.println("Error while init search. path = " + f.getAbsolutePath());
                        return -1;
                    }

                    count += search.doSearchDir(f);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return count;
    }

    int extractSysHead() {
        int count = 0;
        boolean exception_happen = false;

        String[] path = new String[3];
        path[0] = m_kernel_path + "/include";
        path[1] = m_kernel_path + "/arch/arm/include";
        path[2] = m_kernel_path + "/arch/arm64/include";

        for (String p: path) {
            SearchCCode search = new SearchCCode(0, null, m_sys_h_map, null, null);
            if (! search.init()) {
                System.out.println("Error while init search. path = " + p);
                return -1;
            }

            try {
                count += search.doSearchDir(new File(p));
            } catch (Exception e) {
                exception_happen = true;
                e.printStackTrace();
            }

            if (exception_happen) {
                m_h_map.clear();
                return -1;
            }
        }

        m_total_temp += flush_val(m_sys_h_map, false, true, "header in system path");
        return count;
    }

    public int flush_clear_map(Map<String, String> map, boolean if_append, String str) {
        return flush_key(map, true, if_append, str);
    }

    public int flush_key(Map<String, String> map, boolean is_clear, boolean is_append, String str) {
        int count = 0;
        String out_path = m_out_temp;

        try {
            // write string to file
            FileWriter writer = new FileWriter(out_path, is_append);
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

    public int flush_val(Map<String, String> map, boolean is_clear, boolean is_append, String str) {
        int count = 0;
        String out_path = m_out_temp;

        try {
            // write string to file
            FileWriter writer = new FileWriter(out_path, is_append);
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

    public int convertToResult() {
        int count = 0;
        boolean exception_happen = false;

        FileReader reader;
        String line;
        m_c_map.clear();
        // read from temp file
        try {
            reader = new FileReader(new File(m_out_temp));
            BufferedReader br = new BufferedReader(reader);

            while((line = br.readLine()) != null) {
                if(line.startsWith("#"))
                    continue;

                if (! line.startsWith("/")) {
                    throw new NullPointerException();
                }

                m_c_map.put(line, null);
            }

            br.close();
            reader.close();
        } catch (Exception e) {
            exception_happen = true;
            e.printStackTrace();
        }

        if (exception_happen) {
            return count -1;
        }

        //write to out
        FileWriter writer;
        try {
            writer = new FileWriter(new File(m_out));
            BufferedWriter bw = new BufferedWriter(writer);

            for (String key : m_c_map.keySet()) {
                bw.write(key + "\n");
                count ++;
            }

            bw.close();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        m_total = m_c_map.size();
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

    public int rmNotBuildCFile() {
        int count = 0;

        m_kern_c_map.clear();
        SearchCCode search = new SearchCCode(1, m_kern_c_map, null, null, null);
        if (search.init())
            search.doSearchDir(new File(m_kernel_path));

        m_c_map.clear();
        try {
            FileReader reader = new FileReader(new File(m_out));
            BufferedReader br = new BufferedReader(reader);
            String line;
            while ((line = br.readLine()) != null) {
                if (line.endsWith(".h")) {
                    m_c_map.put(line, null);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Iterator iter = m_kern_c_map.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String path = (String)entry.getKey();

            String cmd;
            Process ps;
            if (! m_c_map.containsKey(path)) {
                try {
                    cmd = "rm -f " + path;
                    ps = Runtime.getRuntime().exec(cmd);
                    ps.waitFor();
                    count ++;
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return count;
    }
}

