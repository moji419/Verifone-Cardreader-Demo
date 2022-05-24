package com.gerege.verifoncardreader.api;

import com.google.gson.annotations.SerializedName;

public class TerminalResponse extends Response {

    @SerializedName("result")
    private Result result;

    public TerminalResponse(Status status, String message) {
        super(status, message);
    }

    public TerminalResponse(Status status, String message, String code) {
        super(status, message, code);
    }

    public Result getResult() {
        return result;
    }

    public static class Result {

        @SerializedName("id")
        private String id;

        @SerializedName("name")
        private String name;

        @SerializedName("description")
        private String description;

        @SerializedName("brand")
        private String brand;

        @SerializedName("model")
        private String model;

        @SerializedName("location")
        private String location;

        @SerializedName("status")
        private String status;

        @SerializedName("serial_no")
        private String serialNo;

        @SerializedName("terminal_id")
        private String terminalId;

        @SerializedName("merchant_id")
        private String merchantId;

        @SerializedName("contract_id")
        private String contractId;

        @SerializedName("created_date")
        private String createdDate;

        public String getId() {
            return id != null ? id : "";
        }

        public String getName() {
            return name != null ? name : "";
        }

        public String getDescription() {
            return description != null ? description : "";
        }

        public String getBrand() {
            return brand != null ? brand : "";
        }

        public String getModel() {
            return model != null ? model : "";
        }

        public String getLocation() {
            return location != null ? location : "";
        }

        public String getStatus() {
            return status != null ? status : "";
        }

        public String getSerialNo() {
            return serialNo != null ? serialNo : "";
        }

        public String getTerminalId() {
            return terminalId != null ? terminalId : "";
        }

        public String getMerchantId() {
            return merchantId != null ? merchantId : "";
        }

        public String getContractId() {
            return contractId != null ? contractId : "";
        }

        public String getCreatedDate() {
            return createdDate != null ? createdDate : "";
        }
    }

}
