package com.gerege.verifoncardreader.api;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class KeyResponse extends Response {


    public KeyResponse(Status status, String message) {
        super(status, message);
    }

    @SerializedName("result")
    @Expose
    private Result result;


    public class Result {

        public Result(String bank_code, String bank_terminal_id, String pin_key, String created_date) {
            this.bank_code = bank_code;
            this.bank_terminal_id = bank_terminal_id;
            this.pin_key = pin_key;
            this.created_date = created_date;
        }

        @SerializedName("bank_code")
        private String bank_code;

        @SerializedName("bank_terminal_id")
        private String bank_terminal_id;

        @SerializedName("pin_key")
        private String pin_key;

        @SerializedName("created_date")
        private String created_date;

        public String getBank_code() {
            return bank_code;
        }

        public String getBank_terminal_id() {
            return bank_terminal_id;
        }

        public String getPin_key() {
            return pin_key != null ? pin_key : "";
        }

        public String getCreated_date() {
            return created_date;
        }
    }


    public void setResult(Result result) {
        this.result = result;
    }

    public Result getResult() {
        return result;
    }


    @Override
    public String getResponseLog() {
        return null;
    }
}
