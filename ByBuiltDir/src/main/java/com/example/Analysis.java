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
import java.util.List;
import java.util.Map;

import static com.example.MainTest.printUsuage;
import static com.example.utils.*;

public class Analysis {
    private static final boolean DEBUG = false;

    String m_kernel_path;
    String m_vmlinux_path;
    String m_built_path;

    String m_curr_path;
    String m_out_temp_file;
    String m_out;
    String m_in;

    public SrcTypeList m_src_list = null;

    Map<String, String> m_c_map = new HashMap<>();
    Map<String, String> m_out_map = new HashMap<>();
    Map<String, String> m_other_map = new HashMap<>();
    Map<String, String> m_sys_other_map = new HashMap<>();
    Map<String, String> m_h_map = new HashMap<>();

    Map<String, String> m_sys_h_map = new HashMap<>();
    Map<String, String> m_built_h_map = new HashMap<>();
    Map<String, String> m_kern_c_map = new HashMap<>();
    Map<String, String> m_kern_h_map = new HashMap<>();
    Map<String, String> m_kern_c_map_dup = new HashMap<>();
    Map<String, String> m_kern_h_map_dup = new HashMap<>();


    int m_total_temp;
    int m_total;

    public Analysis(String kernel, String in, String out) {
        m_kernel_path = kernel;
        m_in = in;
        if (out != null) {
            m_out = out + "/list.txt";
            m_out_temp_file = out + "/list.txt.tmp";
        }

        m_src_list = new SrcTypeList(kernel);
        m_total_temp = 0;
        m_total = 0;
    }

    public boolean init() {
        boolean ret = m_src_list.init();
        if (! ret)
            return ret;

        if (m_out != null)
            return true;

        String cmd;
        Process ps;
        ret = false;
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
                File dir = new File(m_curr_path + "/out");
                dir.deleteOnExit();
                dir.mkdir();

                m_out = dir.getAbsolutePath() + "/list.txt";
                m_out_temp_file = dir.getAbsolutePath() + "/list.txt.tmp";

            ret = true;
        }

        return ret;
    }

    public boolean runAnalysis() {
        int count = parseInput();
        if (count == 0) {
            System.out.println("not valid info founded.\n");
            printUsuage();
            return false;
        }
        extractSysHead();
        handle_C_Files();

        //extractOrdinaryHead();
        //
        //handle_other_map();
        //convertToResult();

        return true;
    }

    int parseInput() {
        String path = m_in;
        int count = 0;
        try {
            FileReader reader = new FileReader(path);
            BufferedReader br = new BufferedReader(reader);


            String line = null;
            while((line = br.readLine()) != null) {
                String str[] = line.split(":=");
                String codepath = str[1].trim();

                m_src_list.handleFileName(codepath);
                count++;
            }

            br.close();
            reader.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        //m_src_list.dump();
        return count;
    }

    public int handle_C_Files() {
        int count = 0;
        Map<String, String> map = m_src_list.getTypeNode(false, "c").getMap();

        for (String val : map.values()) {
            grepCFile(val);
        }

        count = flush_clear_map(m_h_map, m_out_temp_file, true, "header in c code");
        count += flush_clear_map(m_c_map, m_out_temp_file, true, "c code in system.map");
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

    public  String extractIncluedSysHead(String str) {
        String arr[] = str.split(">");
        String arr1[] = arr[0].split("/");
        String out = arr1[arr1.length-1].trim();
        return out;
    }

    public int grepCFile(String path) {
        int ret = 0;
        File f = new File(path);
        try {
            FileReader reader = new FileReader(path);
            BufferedReader br = new BufferedReader(reader);
            String l;
            String line;
            String include_str = "#include";
            int include_str_len = include_str.length();
            String h_name;
            String inc_str;
            while((l = br.readLine()) != null) {
                line = l.trim();
                if (line.length() <= include_str_len)
                    continue;

                if (! line.startsWith(include_str))
                    continue;

                h_name = line.substring(include_str.length()).trim();
                if (h_name.startsWith("\"")) {
                    /*
                    String local_h = extractInclueStr(h_name);
                    if (local_h != null) {
                        String abs_path = f.getParent() + "/" + local_h;
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
                    */
                } else if (h_name.startsWith("<")) {
                    String inclued_str = extractIncluedSysHead(h_name);

                    if (m_sys_h_map.containsKey(inclued_str))
                        m_out_map.put(m_sys_h_map.get(inclued_str), "");
                    else {
                        String s[] = inclued_str.split("/");
                        String name = s[s.length - 1];
                        m_sys_other_map.put(name, inclued_str);
                        System.out.println("path = " + path + " string = " + l);
                    }
                } else {
                    System.out.println("Error: str=" + h_name + " filename=" + path);
                    throw new Exception("Error");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    int printResult() {
        File out = new File(m_out);
        System.out.println("output = " + out.getAbsolutePath());
        System.out.println("total=" + m_total);

        return 0;
    }

    int test() {
        return 0;
    }

    int test1() {
        System.out.println("---------test--------");
        String dir = "/home/wupeng/work/8996n/kernel/msm-3.18/include/";
        File f = new File("/home/wupeng//work/8996n/kernel/msm-3.18/./include/linux/start_kernel.h");
        String key;
        String val;

        String path = null;
        try {
            path = f.getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (path == null) {
            System.out.println("Wrong filename = " + f.getAbsolutePath());
            return -1;
        }

        key = path.substring(dir.length());
        System.out.println("dir=" + dir);
        System.out.println("path=" + path);
        System.out.println("substring=" + key);
        System.out.println("---------test--------");
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

        String[] path = new String[4];
        path[0] = m_kernel_path + "/include/uapi";
        path[1] = m_kernel_path + "/include";
        path[2] = m_kernel_path + "/arch/arm/include";
        path[3] = m_kernel_path + "/arch/arm64/include";

        for (String p: path) {
            File f = new File(p);
            String dir = null;
            try {
                dir = f.getCanonicalPath() + "/";
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (dir == null) {
                System.out.println("Wrong dir = " + f.getAbsolutePath());
                return -1;
            }

            System.out.println("sys dir = " + dir);
            SearchSysHead search = new SearchSysHead(dir, m_sys_h_map, null);
            if (! search.init()) {
                System.out.println("Error while init search. path = " + dir);
                return -1;
            }

            try {
                count += search.doSearchDir(new File(p));
            } catch (Exception e) {
                exception_happen = true;
                e.printStackTrace();
            }

            if (exception_happen) {
                m_sys_h_map.clear();
                return -1;
            }
        }

        m_total_temp += flush_val(m_sys_h_map, m_out_temp_file, false, true, "header in system path");
        //dump(m_sys_h_map);
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
            reader = new FileReader(new File(m_out_temp_file));
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

