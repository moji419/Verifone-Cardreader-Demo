package com.gerege.verifoncardreader.pos;

import android.util.Log;

import com.gerege.cardreader_verifon.PosStorage;

public class BankMode {

    private static final String BANK_MODE = "cr_b_m_k_";
    private static final String BANK_TERMINAL_ID = "cr_b_t_i_";

    public static void setBank(Banks bankMode) {
        PosStorage.putStringInSP(BANK_MODE, bankMode.name());
    }

    public static Banks getBank() {
        String bankStr = PosStorage.getStringFromSP(BANK_MODE);
        Log.d("bankStr", bankStr);
        return !bankStr.isEmpty() ? Banks.valueOf(bankStr) : Banks.TDB_BANK;
    }

//    public static String isTDB() {
//        return PosStorage.getStringFromSP(BANK_MODE);
//    }

    public static void setBankTerminalId(String s) {
        if (s != null) {
            PosStorage.putStringInSP(BANK_TERMINAL_ID, s);
        }
    }

    public static String getBankTerminalCode() {
        String s = PosStorage.getStringFromSP(BANK_TERMINAL_ID, "");

        if (!s.trim().isEmpty()) {
            return s;
        }

        switch (BankMode.getBank()) {
            case GOLOMT_BANK:
                return "13151071";
            case STATE_BANK:
                return "GereTest";
            case TDB_BANK:
            default:
                return "90000011";      // Prod
//            return "91200010";        // Dev
        }


    }
}
