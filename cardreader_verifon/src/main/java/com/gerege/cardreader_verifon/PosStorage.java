package com.gerege.cardreader_verifon;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SharedPreferences -ийг зохицуулна
 */
public class PosStorage {

    private static PosStorage instance;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public static final String DEFAULT_STRING_VALUE = "";
    private static final int DEFAULT_INTEGER_VALUE = -1;
    private static final boolean DEFAULT_BOOLEAN_VALUE = false;

    public static final String TRANSMISSION_NUMBER = "transmission_number";
    public static final String KEY = "key";
    public static final String DEV_KEY = "devKey";
    public static final String MODE = "mode";
    public static final String KEY_DOWNLOADED_DATE = "cr_key_date";
    public static final String DEV_KEY_DOWNLOADED_DATE = "cr_d_key_date";
    public static final String TERMINAL_ID = "cr_g_p_t_id";
    public static final String DEV_TERMINAL_ID = "d_cr_g_p_t_id";
    public static final String TRANSACTION_COUNTER = "a35_tr_counter";


    private PosStorage(Context context) {
        sharedPreferences = context.getSharedPreferences("gerege_pos__", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.apply();
    }

    /**
     * SharedPreferences -г эхлүүлэх
     */
    public static void init(Context context) {
        instance = getInstance(context);
    }

    /**
     * Singleton
     */
    public static PosStorage getInstance(Context context) {
        if (instance == null) {
            instance = new PosStorage(context);
        }
        return instance;
    }

    /**
     * SharedPreferences -д String утга бичих
     *
     * @param key   Key
     * @param value SharedPreferences -д хадгалах утга
     */
    public static void putStringInSP(String key, String value) {
        instance.editor.putString(key, value);
        instance.editor.commit();
    }

    /**
     * SharedPreferences -д Integer утга бичих
     *
     * @param key   Key
     * @param value SharedPreferences -д хадгалах утга
     */
    public static void putIntegerInSP(String key, int value) {
        instance.editor.putInt(key, value);
        instance.editor.commit();
    }

    /**
     * SharedPreferences -д Boolean утга бичих
     *
     * @param key   Key
     * @param value SharedPreferences -д хадгалах утга
     */
    public static void putBooleanInSP(String key, Boolean value) {
        instance.editor.putBoolean(key, value);
        instance.editor.commit();
    }

    /**
     * SharedPreferences -с String утга авах
     *
     * @param key Авах String -н key
     */
    public static String getStringFromSP(String key) {
        return instance.sharedPreferences.getString(key, PosStorage.DEFAULT_STRING_VALUE);
    }

    /**
     * SharedPreferences -с String утга авах
     *
     * @param key Авах String -н key
     */
    public static String getStringFromSP(String key, String defaultValue) {
        return instance.sharedPreferences.getString(key, PosStorage.DEFAULT_STRING_VALUE);
    }

    /**
     * SharedPreferences -с Integer утга авах
     *
     * @param key Авах Integer -н key
     */
    public static int getIntegerFromSP(String key) {
        return getIntegerFromSP(key, PosStorage.DEFAULT_INTEGER_VALUE);
    }

    public static int getIntegerFromSP(String key, int def) {
        return instance.sharedPreferences.getInt(key, def);
    }

    /**
     * SharedPreferences -с Boolean утга авах
     *
     * @param key Авах Boolean -н key
     */
    public static boolean getBooleanFromSP(String key) {
        return instance.sharedPreferences.getBoolean(key, PosStorage.DEFAULT_BOOLEAN_VALUE);
    }

    /**
     * SharedPreferences -с Boolean утга авах
     *
     * @param key Авах Boolean -н key
     */
    public static boolean getBooleanFromSP(String key, boolean defaultValue) {
        return instance.sharedPreferences.getBoolean(key, defaultValue);
    }

    /**
     * SharedPreferences -г цэвэрлэх
     */
    public static void clearAllPreference() {
        instance.editor.clear();
        instance.editor.commit();
    }
}