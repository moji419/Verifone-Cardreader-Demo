package com.gerege.verifoncardreader;

import android.app.Application;
import android.content.Context;

import com.gerege.cardreader_verifon.PosApplication;

public class BaseApplication extends Application {

    private static Context appContext;

    @Override
    public void onCreate() {
        super.onCreate();
        PosApplication.init(getApplicationContext());
        appContext = getApplicationContext();
    }
}
