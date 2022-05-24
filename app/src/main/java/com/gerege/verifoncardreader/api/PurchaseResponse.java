package com.gerege.verifoncardreader.api;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class PurchaseResponse extends Response implements Serializable {

    @SerializedName("amount")
    private String amount;

    @SerializedName("card_info")
    private CardInfo card_info;

    public PurchaseResponse(Status status, String message) {
        super(status, message);
    }

    public PurchaseResponse(Status status, String s, String message) {
        super(status, message);
    }

    public static class CardInfo implements Serializable {

        @SerializedName("cardholder")
        private String cardholder;

        @SerializedName("currency")
        private String currency;

        @SerializedName("date_time")
        private String date_time;

        @SerializedName("pan")
        private String pan;

        @SerializedName("rrn")
        private String rrn;

        @SerializedName("invoice_number")
        private String invoice_number;

        @SerializedName("approval")
        private String approval;

        @SerializedName("trace_number")
        private String trace_number;

        @SerializedName("bank_terminal_id")
        private String bank_terminal_id;

        @SerializedName("bank_merchant_id")
        private String bank_merchant_id;

        @SerializedName("terminal_id")
        private String terminal_id;

        public String getCardholder() {
            return cardholder;
        }

        public String getCurrency() {
            return currency;
        }

        public String getDate_time() {
            return date_time;
        }

        public String getPan() {
            return pan;
        }

        public String getRrn() {
            return rrn;
        }

        public String getInvoice_number() {
            return invoice_number;
        }

        public String getApproval() {
            return approval;
        }

        public String getTrace_number() {
            return trace_number;
        }

        public String getBank_terminal_id() {
            return bank_terminal_id;
        }

        public String getBank_merchant_id() {
            return bank_merchant_id;
        }

        public String getTerminal_id() {
            return terminal_id;
        }
    }

    public String getCode() {
        return super.getCodeId();
    }

    public String getAmount() {
        return amount != null ? amount : "";
    }

    public CardInfo getCardInfo() {
        return card_info;
    }

    /**
     * Log -д бичигдэх data
     */
    @Override
    public String getResponseLog() {
        return "amount: " + amount;
    }
}
