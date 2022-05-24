package com.gerege.cardreader_verifon.helpers;

import com.gerege.cardreader_verifon.TLV;
import com.gerege.cardreader_verifon.tlv.BerTag;
import com.gerege.cardreader_verifon.tlv.BerTlvBuilder;

import java.util.Map;
import java.util.Set;

public class Helper {

    private static final char[] CHARS_TABLES = "0123456789ABCDEF".toCharArray();

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

    public static byte[] h2b(String hexStr) {
        hexStr = hexStr.toLowerCase();
        int length = hexStr.length();
        byte[] bytes = new byte[length >> 1];
        int index = 0;
        for (int i = 0; i < length; i++) {
            if (index > hexStr.length() - 1) return bytes;
            byte highDit = (byte) (Character.digit(hexStr.charAt(index), 16) & 0xFF);
            byte lowDit = (byte) (Character.digit(hexStr.charAt(index + 1), 16) & 0xFF);
            bytes[i] = (byte) (highDit << 4 | lowDit);
            index += 2;
        }
        return bytes;
    }

    public static String formatBerTlv(Map<String, TLV> map) {
        try {
            BerTlvBuilder builder = new BerTlvBuilder();

            Set<String> keySet = map.keySet();
            for (String key : keySet) {
                TLV tlv = map.get(key);
                addTag(builder, key, tlv != null && tlv.getData() != null ? Helper.b2h(tlv.getData()) : "");
            }
            byte[] buf = builder.buildArray();
            return Helper.b2h(buf);
        } catch (Exception e) {
            return "";
        }
    }

    private static void addTag(BerTlvBuilder builder, String tagKey, String value) {
        int firstTag;
        int secondTag;
        BerTag berTag;
        if (tagKey.length() == 2) {
            firstTag = Integer.decode("0x" + tagKey);
            berTag = new BerTag(firstTag);
        } else {
            firstTag = Integer.decode("0x" + tagKey.substring(0, 2));
            secondTag = Integer.decode("0x" + tagKey.substring(2));
            berTag = new BerTag(firstTag, secondTag);
        }
        builder.addHex(berTag, value);
    }

    public static String toFormattedHexString(byte[] aBytes) {
        return toFormattedHexString(aBytes, 0, aBytes.length);
    }

    public static String toFormattedHexString(byte[] aBytes, int aOffset, int aLength) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append(aLength);
        sb.append("] :");
        for (int si = aOffset, di = 0; si < aOffset + aLength; si++, di++) {
            byte b = aBytes[si];
            if (di % 4 == 0) {
                sb.append("  ");
            } else {
                sb.append(' ');
            }
            sb.append(CHARS_TABLES[(b & 0xf0) >>> 4]);
            sb.append(CHARS_TABLES[(b & 0x0f)]);

        }

        return sb.toString();
    }
}
