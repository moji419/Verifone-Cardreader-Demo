package com.gerege.cardreader_verifon.models;

public class EmvApplication {

    private byte[] AID;                 // Application Identifier (ADF Name)   (AID)
    private String label;               // Application Label   (VISA DEBIT, VISA CREDIT... etc.)
    private int priority;               // Application Priority Indicator
    private String preferredName;       // Application Preferred Name

    public EmvApplication(byte[] AID, String label, int priority, String preferredName) {
        this.AID = AID;
        this.label = label;
        this.priority = priority;
        this.preferredName = preferredName;
    }

    public byte[] getAID() {
        return AID;
    }

    public String getLabel() {
        return label != null ? label : "";
    }

    public int getPriority() {
        return priority;
    }

    public String getPreferredName() {
        return preferredName != null ? preferredName : "";
    }
}
