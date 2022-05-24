package com.gerege.cardreader_verifon;

import android.content.Context;


public class PosApplication {

    private static Context appContext;

    public static void init(Context context) {
        PosStorage.init(context);
        appContext = context;
    }
}
