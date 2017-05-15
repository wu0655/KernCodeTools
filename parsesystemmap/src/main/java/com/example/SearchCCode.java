package com.example;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Created by wupeng on 17-3-30.
 */

public class SearchCCode {
    public final static int NAME_IN_KEY = 0;
    public final static int PATH_IN_KEY = 1;
    int mKeyType;

    Map<String, String> mCmap = null;
    Map<String, String> mHmap = null;
    Map<String, String> mCmapDup = null;
    Map<String, String> mHmapDup = null;

    public SearchCCode(
            int key_type,
            Map<String, String> Cmap,
            Map<String, String> Hmap,
            Map<String, String> CmapDup,
            Map<String, String> HmapDup
    ){
        mKeyType = key_type;

        mCmap = Cmap;
        mCmapDup = CmapDup;
        mHmap = Hmap;
        mHmapDup = HmapDup;

        return;
    }

    public boolean init() {
        boolean ret;

        if ((mCmap == null) && (mHmap == null))
            ret = false;
        else
            ret = true;

        return ret;
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
        String key;
        String val;

        String name = f.getName();
        String path = f.getAbsolutePath();

        if (mKeyType == NAME_IN_KEY) {
            key = name;
            val = path;
        } else {
            key = path;
            val = name;
        }

        if (path.endsWith(".c")) {
            if (mCmap == null) {
                //do nothing
            } else if (mCmapDup == null)
                ret = addToMap(mCmap, key, val);
            else
                ret = addToMap(mCmap, key ,val, mCmapDup);
        } else if (path.endsWith(".h")) {
            if (mHmap == null){
                //do nothing
            } else if (mHmapDup == null) {
                ret = addToMap(mHmap, key ,val);
            } else {
                ret = addToMap(mHmap, key, val, mHmapDup);
            }
        } else
            ret = false;

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

    private boolean isSymlink(File file) throws IOException {
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
}
