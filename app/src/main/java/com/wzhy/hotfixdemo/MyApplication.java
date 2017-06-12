package com.wzhy.hotfixdemo;

import android.app.Application;
import android.support.annotation.NonNull;
import android.widget.Toast;

/**
 * Created by techfit on 2017/6/8.
 */

public class MyApplication extends Application {

    private static MyApplication mApp;

    public static MyApplication getApp(){
        return mApp;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mApp = this;

    }

    public static void showToast(@NonNull String text){
        Toast.makeText(mApp, text, Toast.LENGTH_SHORT).show();
    }


}
