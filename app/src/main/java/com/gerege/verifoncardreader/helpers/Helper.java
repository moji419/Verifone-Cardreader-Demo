package com.gerege.verifoncardreader.helpers;

import android.annotation.SuppressLint;
import android.util.Log;

import org.apache.commons.codec.binary.Base64;

import java.nio.charset.StandardCharsets;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Helper {

    public static String b2h(byte b) {
        return String.format("%02X ", b);
    }

    public static String b2h(byte[] arr) {
        return b2h(arr, false);
    }

    public static String b2h(byte[] arr, boolean hasSpace) {
        StringBuilder sb = new StringBuilder();
        for (byte b : arr) {
            sb.append(String.format("%02X", b));
            if (hasSpace) {
                sb.append(" ");
            }
        }

        return sb.toString();
    }

    public static String encrypt(String strToEncrypt) {
        String s = "G3PYiR2lC9poQhc+";
        try {
            @SuppressLint("GetInstance") Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            final SecretKeySpec secretKey = new SecretKeySpec(s.getBytes(), "AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return new String(new Base64().encode(cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8))));
        } catch (Exception e) {
            Log.e("Error encryping", e.toString());
        }
        return null;
    }
}
