package com.gerege.cardreader_verifon;

import androidx.annotation.NonNull;

public class TLV {

    private final String tag;
    private final int len;
    private final byte[] data;

    public TLV(String tag, int len, byte[] data) {
        this.tag = tag;
        this.len = len;
        this.data = data;
    }

    public String getTag() {
        return tag;
    }

    public int getLen() {
        return len;
    }

    public byte[] getData() {
        return data;
    }

    @NonNull
    @Override
    public String toString() {
        return "[0x" + tag + "], len=" + len + ", " + TLVUtils.b2h(data) + ", data(str)=" + new String(data);
    }
}
