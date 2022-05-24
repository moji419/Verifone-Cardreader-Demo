package com.gerege.verifoncardreader.pos;

public enum Banks {

    TDB_BANK (PosConstants.TDB_BANK, "Худалдаа хөгжлийн банк"),
    GOLOMT_BANK (PosConstants.GOLOMT_BANK, "Голомт банк"),
    STATE_BANK (PosConstants.STATE_BANK, "Төрийн банк");

    private final String bankCode;

    private final String name;

    Banks(String bankCode, String name) {
        this.bankCode = bankCode;
        this.name = name;
    }

    public String getBankCode() {
        return bankCode;
    }

    public String getName() {
        return name;
    }
}
