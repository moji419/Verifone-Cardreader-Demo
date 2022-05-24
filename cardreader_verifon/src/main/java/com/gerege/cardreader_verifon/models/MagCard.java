package com.gerege.cardreader_verifon.models;

public class MagCard {

    private final String owner;
    private final String cardNo;
    private final String track1;
    private final String track2;
    private final String track3;
    private final String expireDate;

    public MagCard(String owner, String cardNo, String track1, String track2, String track3, String expireDate) {
        this.owner = owner;
        this.cardNo = cardNo;
        this.track1 = track1;
        this.track2 = track2;
        this.track3 = track3;
        this.expireDate = expireDate;
    }

    public String getOwner() {
        return owner != null ? owner : "";
    }

    public String getCardNo() {
        return cardNo != null ? cardNo : "";
    }

    public String getTrack1() {
        return track1 != null ? track1 : "";
    }

    public String getTrack2() {
        return track2 != null ? track2 : "";
    }

    public String getTrack3() {
        return track3 != null ? track3 : "";
    }

    public String getExpireDate() {
        return expireDate != null ? expireDate : "";
    }
}
