package com.gerege.cardreader_verifon.models;

public class ICCard {

    private final String cardNo;
    private final String track2;
    private final String track3;
    private final String cardSerialNo;
    private final String expireDate;

    public ICCard(String cardNo, String track2, String track3, String cardSerialNo, String expireDate) {
        this.cardNo = cardNo;
        this.track2 = track2;
        this.track3 = track3;
        this.cardSerialNo = cardSerialNo;
        this.expireDate = expireDate;
    }

    public String getCardNo() {
        return cardNo != null ? cardNo : "";
    }

    public String getTrack2() {
        return track2 != null ? track2 : "";
    }

    public String getTrack3() {
        return track3 != null ? track3 : "";
    }

    public String getCardSerialNo() {
        return cardSerialNo != null ? cardSerialNo : "";
    }

    public String getExpireDate() {
        return expireDate != null ? expireDate : "";
    }

}
