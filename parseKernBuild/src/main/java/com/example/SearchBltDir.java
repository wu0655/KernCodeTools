package com.example;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by wupeng on 17-3-30.
 */

public class SearchBltDir {
    Map<String, String> mMap = null;
    Pattern mPattern;
    String mRegEx = null;

    public SearchBltDir(
            Map<String, String> out,
            String reg
    ){
        mMap = out;
        mRegEx = reg;
        return;
    }

    public boolean init() {
        // 正则表达式规则
        String regEx = mRegEx;
        // 编译正则表达式
        mPattern = Pattern.compile(regEx);
        return (mMap != null);
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
                    if (HandleFile(f))
                        count ++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return count;
    }

    private boolean HandleFile(File f) {
        String key;
        String val;

        String name = f.getName();
        String path = f.getAbsolutePath();

        Matcher matcher = mPattern.matcher(name);
        boolean ret = matcher.find();
        if (ret)
            mMap.put(path, name);
        return ret;
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
