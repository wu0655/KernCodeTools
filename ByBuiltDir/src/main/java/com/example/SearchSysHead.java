package com.example;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static com.example.SearchCCode.NAME_IN_KEY;
import static com.example.utils.isSymlink;

/**
 * Created by wupeng on 17-3-30.
 */

public class SearchSysHead {
    private static final boolean DEBUG = false;
    String mDir = null;
    Map<String, String> mHmap = null;
    Map<String, String> mHmapDup = null;

    public SearchSysHead(
            String dir,
            Map<String, String> Hmap,
            Map<String, String> HmapDup
    ){
        mDir = dir;
        mHmap = Hmap;
        mHmapDup = HmapDup;

        return;
    }

    public boolean init() {
        return (mHmap != null);
    }

    public int doSearchDir(File dir) {
        int count = 0;
        File flist[] = dir.listFiles();
        if (flist == null || flist.length == 0) {
            return 0;
        }
        try {
            for (File f : flist) {
                if (isSymlink(f))
                    continue;
                else if (f.isDirectory()) {
                    doSearchDir(f);
                } else {
                    if (addFile(f))
                        count ++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return count;
    }

    private boolean addFile(File f) {
        boolean ret = true;
        String key = null;
        String val = null;


        try {
            key = f.getName();
            val = f.getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (val == null) {
            System.out.println("Wrong filename = " + f.getAbsolutePath());
            return false;
        }

        if (DEBUG) {
            System.out.println("dir=" + mDir);
            System.out.println("path=" + val);
            System.out.println("substring=" + key);
        }

        if (mHmapDup == null) {
                ret = addToMap(mHmap, key ,val);
            } else {
                ret = addToMap(mHmap, key, val, mHmapDup);
            }

        return ret;
    }

    private boolean addToMap(Map<String, String> map, String key, String val, Map<String, String> map_dup) {
        try {
            if (map_dup.containsKey(key)) {
                val = map_dup.get(key) + "\n" + val;
                map_dup.put(key, val);
            } else if (map.containsKey(key)) {
                val = map.get(key) + "\n" + val;
                map.remove(key);
                map_dup.put(key,val);
            } else {
                map.put(key, val);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    private boolean addToMap (Map<String, String> map, String key, String val) {
        try {
            if (map.containsKey(key)) {
                val = map.get(key) + "\n" + val;
                map.put(key, val);
            } else {
                map.put(key, val);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }
}
