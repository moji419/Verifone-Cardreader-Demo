package com.gerege.verifoncardreader.pos;


import android.util.Log;

import com.gerege.cardreader_verifon.PosStorage;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PosConstants {
    //-----------------------------------TDB bank----------------------------------------/
    public static final String URL_TDB = "https://tdbp.gerege.mn/tdb/"; // "http://49.0.132.152:8085/tdb/";
    public static String MASTER_KEY_TDB = "FB89C1086BBC3E1A4F8AEC1C94C41929";
    public static final String MASTER_KEY = "D9E95170F125BA9BC42C075873914370";//; "FB89C1086BBC3E1A4F8AEC1C94C41929";
    //    public static final String URL_TDB = "http://192.168.0.64:8085/tdb/";
//    public static String MASTER_KEY_TDB = "D9E95170F125BA9BC42C075873914370";
//    public static final String MASTER_KEY = "D9E95170F125BA9BC42C075873914370";
    //-----------------------------------Golomt bank---------------------- ------------------/
    public static final String URL_GOLOMT = "https://golomtpay.gerege.mn/golomt/";
    public static String MASTER_KEY_GOLOMT = "B0BF51FD8ACE52381364EFBA462A8345";
    public static String DEV_MASTER_KEY = "D9E95170F125BA9BC42C075873914370";
    //-----------------------------------State bank----------------------------------------/
//    public static String MASTER_KEY_STATE_COM_FIRST = "07831F1CB9AD0BC26E3876CDB34FB331";
//    public static String MASTER_KEY_STATE_COM_SECOND = "31B34F7F2CF48C5DA2C8F15464C4EA3B";
//    public static String MASTER_KEY_STATE_COM_THIRD = "E6D91AA83B199BD934D66E80E98520A8";
    public static String MASTER_KEY_STATE = "D0E94ACBAE401C46F826E9193E0E79A2";
    public static final String URL_STATE = "https://sbpay.gerege.mn/state/";
    //-------------------------------------------------------------------------------------/
    public static String MERCHANT_ID = "000000000080921";
    public static String DOMAIN = "49.0.223.57";    // 53
    public static String DEV_DOMAIN = "49.0.223.53"; //  50
    public static String TITLE_NAME = "TDB POS";
    public static String DEV_TITLE_NAME = "Development mode";
    private static final int PORT = 20015;   // 20015
    private static final int DEV_PORT = 16091; // 16091

    public static final String GOLOMT_BANK = "150000";
    public static final String TDB_BANK = "040000";
    public static final String STATE_BANK = "340000";

    public static String getUrl() {
        Log.d("bankMode", BankMode.getBank().getBankCode());
        switch (BankMode.getBank()) {
            case STATE_BANK:
                return URL_STATE;
            case GOLOMT_BANK:
                return URL_GOLOMT;
            case TDB_BANK:
                return URL_TDB;
            default:
                return URL_TDB;
        }
    }

    public static boolean isProductMode() {
        return PosStorage.getBooleanFromSP(PosStorage.MODE);
    }

    public static void setProductionMode(boolean bool) {
        PosStorage.putBooleanInSP(PosStorage.MODE, bool);
    }

    public static String getMasterKey() {
        if (PosConstants.isProductMode()) {

            String s = PosStorage.getStringFromSP(MASTER_KEY, "");

            Log.d("SSSSSS", s);

            if (!s.trim().isEmpty()) {
                return s;
            }
            Log.d("Master_key", BankMode.getBank() + "");
            Log.d("Master_key1", MASTER_KEY_STATE + "");
            switch (BankMode.getBank()) {
                case STATE_BANK:
                    return MASTER_KEY_STATE;
                case GOLOMT_BANK:
                    return MASTER_KEY_GOLOMT;
                case TDB_BANK:
                default:
                    return MASTER_KEY_TDB;
            }

        } else {
            String s = PosStorage.getStringFromSP(MASTER_KEY, "");
            Log.d("SSSSSS", s);
            if (!s.trim().isEmpty()) {
                return s;
            }
            return PosStorage.getStringFromSP(DEV_MASTER_KEY);
        }
    }

    public static String getMerchantId() {
        String s = PosStorage.getStringFromSP(MERCHANT_ID, "");
        if (!s.trim().isEmpty()) {
            return s;
        }
        return MERCHANT_ID;
    }

    public static void setMasterKey(String masterKey) {
        PosStorage.putStringInSP(MASTER_KEY, masterKey);
    }

    public static void setMerchantId(String merchantId) {
        PosStorage.putStringInSP(MERCHANT_ID, merchantId);
    }

    public static String getDomain() {
        return PosConstants.isProductMode() ? DOMAIN : DEV_DOMAIN;
    }

    public static int getPort() {
        return PosConstants.isProductMode() ? PORT : DEV_PORT;
    }

    public static String getTerminalId() {
//         PosConstants.isProductMode() ? TERMINAL_ID : DEV_TERMINAL_ID;
        return PosStorage.getStringFromSP(PosConstants.isProductMode() ? PosStorage.TERMINAL_ID : PosStorage.DEV_TERMINAL_ID);
    }

    public static void setTerminalId(String terminalId) {
        PosStorage.putStringInSP(PosConstants.isProductMode() ? PosStorage.TERMINAL_ID : PosStorage.DEV_TERMINAL_ID, terminalId);
    }

    public static String getTitleName() {
        return PosConstants.isProductMode() ? TITLE_NAME : DEV_TITLE_NAME;
    }

    public static String getPinKey() {
        return PosStorage.getStringFromSP(PosConstants.isProductMode() ? PosStorage.KEY : PosStorage.DEV_KEY);
    }

    public static void setPinKey(String key) {
        PosStorage.putStringInSP(PosConstants.isProductMode() ? PosStorage.KEY : PosStorage.DEV_KEY, key);
    }

    public static String getKeyDate() {
        return PosStorage.getStringFromSP(PosConstants.isProductMode() ? PosStorage.KEY_DOWNLOADED_DATE : PosStorage.DEV_KEY_DOWNLOADED_DATE);
    }

    public static void setKeyDate(String date) {
        if (PosConstants.isProductMode()) {
            PosStorage.putStringInSP(PosStorage.KEY_DOWNLOADED_DATE, date);
        } else {
            PosStorage.putStringInSP(PosStorage.DEV_KEY_DOWNLOADED_DATE, date);
        }
    }

    public static Integer getTransactionNumber() {
        return PosStorage.getIntegerFromSP(PosStorage.TRANSMISSION_NUMBER);
    }

    public static void setTransactionNumber(Integer number) {
        PosStorage.putIntegerInSP(PosStorage.TRANSMISSION_NUMBER, number);
    }

    public static boolean isKeyExpired() {
        String s = PosConstants.getKeyDate().trim();
        return s.isEmpty() || !s.equals(currentDate());
    }

    public static String currentDate() {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        Date now = new Date();
        return format.format(now);
    }
}
