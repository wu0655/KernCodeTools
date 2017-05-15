package com.example;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wupeng on 17-5-14.
 */

public class SrcTypeNode {
    String m_ext_name;
    boolean m_is_gen;
    Map<String, String> m_map = null;


    public SrcTypeNode(boolean is_gen, String ext_name) {
        m_ext_name = ext_name;
        m_is_gen = is_gen;
        m_map = new HashMap<>();
    }

    Map<String, String> getMap(){
        return m_map;
    }
}
