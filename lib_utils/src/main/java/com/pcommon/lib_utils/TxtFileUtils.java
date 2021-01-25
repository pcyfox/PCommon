package com.pcommon.lib_utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;

public class TxtFileUtils {


    public static String readTxt(String filePth) {
        try {
            File urlFile = new File(filePth);
            if (!urlFile.exists()) {
                return null;
            }
            if (urlFile.exists()) {
                InputStreamReader isr = new InputStreamReader(new FileInputStream(urlFile), "UTF-8");
                BufferedReader br = new BufferedReader(isr);
                StringBuilder str = new StringBuilder();
                String mimeTypeLine = null;
                while ((mimeTypeLine = br.readLine()) != null) {
                    str.append(mimeTypeLine);
                }
                return str.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void writeTxt(String filePth, String txt, boolean append) {
        String str = txt;
        try {
            FileWriter fw = new FileWriter(filePth, append);
            fw.flush();
            fw.write(str);
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
