package com.leiden.utils;

import android.os.Environment;

public class ParameterUtil {

    /**
     * 当前的模式
     */
    public static boolean isDebug = true;


    /**
     * 得到当前的sdk根目录
     */
    public static String getSDKRoot(){
        String path = Environment.getExternalStoragePublicDirectory("")
                + "/leiden.sdk/";
        return path;
    }

}
