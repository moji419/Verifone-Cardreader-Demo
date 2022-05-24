package com.gerege.cardreader_verifon;


import com.gerege.cardreader_verifon.helpers.Helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TLVUtils {

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

    public static byte[] hex2Bytes(String hexStr) {
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

    public static void parseData(Map<String, TLV> map, String data) {
        parseData(map, hex2Bytes(data));
    }

    public static void parseData(Map<String, TLV> map, byte[] data) {
        int i = 0;

        if (data == null || data.length <= i) {
            return;
        }

        while (i < data.length) {
            boolean isConstructed = isConstructed(data[i]);
            Tuple<String, Integer> tag = getTagName(data, i);
            i += tag.b;
            Tuple<Integer, Integer> len = getTagLen(data, i);
            i += len.b;
            TLV tlv = new TLV(
                    tag.a,
                    len.a,
                    Arrays.copyOfRange(data, i, i + len.a)
            );
            i += len.a;

            if (isConstructed) {
                parseData(map, tlv.getData());
            } else if (!tlv.getTag().isEmpty()) {
                map.put(tlv.getTag(), tlv);
            }
        }
    }

    public static List<TLV> parsePdolData(byte[] data) {
        List<TLV> list = new ArrayList<>();
        int i = 0;

        if (data == null || data.length <= i) {
            return list;
        }

        while (i < data.length) {
            Tuple<String, Integer> tag = getTagName(data, i);
            i += tag.b;
            Tuple<Integer, Integer> len = getTagLen(data, i);
            i += len.b;
            TLV tlv = new TLV(tag.a, len.a, null);

            list.add(tlv);
        }

        return list;
    }

    /**
     * EMV book 3  -> Annex B1
     */
    private static Tuple<String, Integer> getTagName(byte[] data, int i) {
        if (data == null || data.length <= i) return new Tuple<>("", 0);

        int tagNameLen = 1;
        if ((data[i] & 0x1F) == 0x1F) {
            tagNameLen = ((data[i + 1] & 0x80) == 0x80) ? 3 : 2;
        }

        return new Tuple<>(
                Helper.b2h(Arrays.copyOfRange(data, i, i + tagNameLen)),
                tagNameLen
        );
    }

    /**
     * EMV book 3  -> Annex B2
     */
    private static Tuple<Integer, Integer> getTagLen(byte[] data, int i) {
        if (data == null || data.length <= i) return new Tuple<>(0, 0);
        int dataLen = 0;
        int countByte;
        if ((data[i] & 0x80) == 0x80) {
            countByte = (data[i++] & 0x7F);
            byte[] lenByte = Arrays.copyOfRange(data, i, i + countByte++);
            for (byte b : lenByte) {
                dataLen = (dataLen << 8) + (b & 0xFF);
            }
        } else {
            countByte = 1;
            dataLen = data[i] & 0x7F;
        }

        return new Tuple<>(dataLen, countByte);
    }

    /**
     * is nested
     */
    private static boolean isConstructed(byte b) {
        return (b & 0x20) == 0x20;
    }
}
