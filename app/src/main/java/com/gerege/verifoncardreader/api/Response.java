package com.gerege.verifoncardreader.api;


import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Response implements Serializable {

    public static final String SUCCESS = "success";
    public static final String ERROR = "error";
    public static final String EXPIRED = "expired";
    public static final String LOADING = "";

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("code")
    private String code;

    public Response(Status status, String message) {
        this.message = message;
        switch (status) {
            case SUCCESS:
                this.status = SUCCESS;
                break;

            case LOADING:
                this.status = LOADING;
                break;

            case ERROR:
                this.status = ERROR;
                break;

            default:
                break;
        }
    }

    public Response(Status status, String message, String code) {
        this.message = message;
        this.code = code;

        switch (status) {
            case SUCCESS:
                this.status = SUCCESS;
                break;

            case LOADING:
                this.status = LOADING;
                break;

            case ERROR:
                this.status = ERROR;
                break;

            case EXPIRED:
                this.status = EXPIRED;
                break;

            default:
                break;
        }

        if (code != null && code.equals("401")) {
            this.status = EXPIRED;
        }
    }

    public Status getStatus() {
        if (status.equals(SUCCESS)) {
            return Status.SUCCESS;
        } else if (status.equals(LOADING)) {
            return Status.LOADING;
        } else {
            return Status.ERROR;
        }
    }

    public String getCodeId() {
        return code;
    }

    public String getMessage() {
        return message != null ? message : "";
    }

    /**
     * API -с ирсэн Response -н Log -г бичхэд энэ функц -г дуудна. /Log -д бичигдэх data/
     */
    public String getResponseLog() {
        return "";
    }

    public String getDefaultResponseLog() {
        return "status: " + status + "\n" +
                "message: " + message + "\n";
    }
}
