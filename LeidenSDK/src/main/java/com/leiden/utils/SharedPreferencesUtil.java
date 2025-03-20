package com.leiden.utils;


import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesUtil {


    public static String getContent(String key, Context context){
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        return sharedPreferences.getString(key,null);
    }

    public static void setContent(String key, String content, Context context){
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        sharedPreferences.edit().putString(key,content).commit();
    }

    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences("Leiden_SDK", Context.MODE_PRIVATE);
    }
}
