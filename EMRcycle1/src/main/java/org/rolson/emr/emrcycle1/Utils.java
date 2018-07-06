package org.rolson.emr.emrcycle1;

import java.io.File;

public class Utils {
public static String getFileExtension(File f){
	String ext = null;
    String s = f.getName();
    int i = s.lastIndexOf('.');

    if (i > 0 &&  i < s.length() - 1) {
        ext = s.substring(i+1).toLowerCase();
    }
    return ext;
}
}
