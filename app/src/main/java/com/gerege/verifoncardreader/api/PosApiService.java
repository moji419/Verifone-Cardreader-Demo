package com.gerege.verifoncardreader.api;


import java.util.HashMap;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface PosApiService {

//    @POST("api/processing/key")
//    Call<KeyResponse> getKeyTDB(@Body HashMap<String, String> fields);
//
//    @POST("api/processing/key")
//    Call<KeyResponse> getKeyGolomt(@Body HashMap<String, String> fields);

//    @POST("api/processing/purchase") //@POST("/processing/purchase")
//    Call<PurchaseResponse> purchaseTDB(@Body HashMap<String, String> body);
//
//    @POST("api/processing/purchase") //@POST("/processing/purchase")
//    Call<PurchaseResponse> purchaseGolomt(@Body HashMap<String, String> body);

    @POST("api/processing/key")
    Call<KeyResponse> getBanksKey(@Body HashMap<String, String> fields);

    @POST("api/processing/purchase") //@POST("/processing/purchase")
    Call<PurchaseResponse> purchase(@Body HashMap<String, String> body);
    
    @POST("api/processing/payment") //@POST("/processing/purchase")
    Call<PurchaseResponse> payment(@Body HashMap<String, String> body);

    @POST("tdb/api/processing/reversal") //@POST("/processing/purchase")
    Call<PurchaseResponse> reverseTDB(@Body HashMap<String, String> body);

    @POST("/golomt/api/processing/reversal") //@POST("/processing/purchase")
    Call<PurchaseResponse> reverseGolomt(@Body HashMap<String, String> body);

    @POST("papi/terminal")
    Call<TerminalResponse> getTerminalId(@Body HashMap<String, String> fields);
}
