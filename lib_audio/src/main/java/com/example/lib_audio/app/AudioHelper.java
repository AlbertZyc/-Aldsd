package com.example.lib_audio.app;

import android.content.Context;

/**
 * @author AlbertZyc
 * @CreatTime 2020/3/28
 * @Description
 **/
public  final class AudioHelper {
    private static Context mContext;
    public static void init(Context context){
        mContext = context;
    }
    public  static  Context getContext(){
        return mContext;
    }

}
