package com.example;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by wupeng on 17-5-14.
 */

public class SrcTypeList {
    public ArrayList<SrcTypeNode> m_list;
    String m_kern_path;

    public SrcTypeList(String path) {
        m_kern_path = path;
    }

    public boolean init() {
        File f = new File(m_kern_path);
        boolean ret = false;
        try {
            m_kern_path = f.getCanonicalPath() + "/";
            ret = true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ret;
    }

    public boolean handleFileName(String path) {
        SrcTypeNode node = null;
        String key = null;
        String val = null;

        //System.out.println(path);

        boolean is_gen = ! path.startsWith("/");
        if (is_gen) {
            node = getTypeNode(is_gen, "");
            key = path;
            val = "";
        } else {
            String arr[] = path.split("\\.");
            String ext = null;

            try {
                ext = arr[arr.length - 1];
                //System.out.println(ext);
            } catch (Exception e) {
                System.out.println("exception=" + path);
                for(int i=0; i<arr.length;i++) {
                    System.out.println(arr[i]);
                }
                e.printStackTrace();
            }

            node = getTypeNode(is_gen, ext);
            key = path.substring(m_kern_path.length());
            val = path;
        }


        node.getMap().put(key, val);
        return true;
    }

    public SrcTypeNode getTypeNode(boolean is_gen, String ext) {
        if (m_list == null) {
            m_list = new ArrayList<>();
            SrcTypeNode t = new SrcTypeNode(is_gen, ext);
            m_list.add(t);
            return t;
        }

        if (is_gen) {
            for (SrcTypeNode node: m_list) {
                if (node.m_is_gen)
                    return node;
            }
        } else {
            for (SrcTypeNode node : m_list) {
                if (node.m_ext_name.equals(ext))
                    return node;
            }
        }

        SrcTypeNode t = new SrcTypeNode(is_gen, ext);
        m_list.add(t);
        return t;
    }

    public void dump() {
        for (SrcTypeNode node: m_list) {
            Map<String, String> map = node.getMap();
            System.out.println("++begin++ " + node.m_ext_name + " ++");
            for (Map.Entry<String, String> entry: map.entrySet()) {
                System.out.println(entry.getKey() + "=" + entry.getValue());
            }
            System.out.println("++end++ " + node.m_ext_name + " ++");
        }
    }
}
