package com.gerege.verifoncardreader.api;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.lifecycle.MutableLiveData;

import com.gerege.cardreader_verifon.models.ICCard;
import com.gerege.cardreader_verifon.models.MagCard;
import com.gerege.verifoncardreader.helpers.Helper;
import com.gerege.verifoncardreader.pos.BankMode;
import com.gerege.verifoncardreader.pos.Banks;
import com.gerege.verifoncardreader.pos.PosConstants;
import com.gerege.verifoncardreader.pos.RsaUtils;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.RequestBody;
import okio.Buffer;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PosRepository {

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static MutableLiveData<TerminalResponse> getTerminalId() {
        MutableLiveData<TerminalResponse> data = new MutableLiveData<>();
        data.setValue(new TerminalResponse(Status.LOADING, ""));
        PosApiService apiService = PosRetrofitFactory.getInstanceTDBTPTP().create(PosApiService.class);

        HashMap<String, String> map = new HashMap<>();
        map.put("serialNo", Helper.encrypt("PL01189E00615"));       // TODO: Build.SERIAL

        Call<TerminalResponse> call = apiService.getTerminalId(map);
        call.enqueue(new Callback<TerminalResponse>() {
            @Override
            public void onResponse(Call<TerminalResponse> call, Response<TerminalResponse> response) {
                if (response.body() != null) {
                    if (response.body().getResult() != null) {
                        PosConstants.setTerminalId(response.body().getResult().getTerminalId());
                        data.setValue(response.body());
                    } else {
                        data.setValue(new TerminalResponse(Status.ERROR, response.body().getMessage()));
                    }
                } else {
                    data.setValue(new TerminalResponse(Status.ERROR, ""));
                }
            }

            @Override
            public void onFailure(Call<TerminalResponse> call, Throwable t) {
                data.setValue(new TerminalResponse(Status.ERROR, "Error: Terminal ID"));
            }
        });
        return data;
    }

    public static MutableLiveData<KeyResponse> getKey() {
        MutableLiveData<KeyResponse> data = new MutableLiveData<>();
        PosApiService posApiService = PosRetrofitFactory.getInstanceTDB().create(PosApiService.class);

        HashMap<String, String> map = new HashMap<>();

        map.put("bank_merchant_id", PosConstants.getMerchantId());
        map.put("bank_terminal_id", BankMode.getBankTerminalCode());
        //map.put("bank_terminal_id", "91200010"); // DEV
        Call<KeyResponse> call = posApiService.getBanksKey(map);

        Log.d("purchase_Req", "get_key: " + call.request().url());
        Log.d("purchase_Req", "get_key:" + bodyToString(call.request().body()));

        data.setValue(new KeyResponse(Status.LOADING, ""));

        call.enqueue(new Callback<KeyResponse>() {
            @Override
            public void onResponse(Call<KeyResponse> call, retrofit2.Response<KeyResponse> response) {
                if (response.body() != null && response.body().getResult() != null) {
                    PosConstants.setPinKey(response.body().getResult().getPin_key());
                    Log.d("purchase_Req", " pinkey " + PosConstants.getPinKey());
                }
                data.setValue(response.body());
            }

            @Override
            public void onFailure(Call<KeyResponse> call, Throwable t) {
                Log.d("Throwable", "onFailure: ", t);
                data.setValue(new KeyResponse(Status.ERROR, "Error: User info"));
            }
        });
        return data;
    }

    /**
     * Magnetic картаар худалдан авалт хийх
     */
    public static MutableLiveData<PurchaseResponse> purchaseMag(MagCard magCard, String amount, String pin, String terminalId, String orgId, String userId, boolean isPayment) {
        MutableLiveData<PurchaseResponse> data = new MutableLiveData<>();
        data.setValue(new PurchaseResponse(Status.LOADING, "", ""));
        PosApiService apiService = PosRetrofitFactory.getInstanceTDB().create(PosApiService.class);

        HashMap<String, String> hashMap = new HashMap<>();

        hashMap.put("bank_terminal_id", BankMode.getBankTerminalCode());
        hashMap.put("track1", magCard.getTrack1());
        hashMap.put("track2", magCard.getTrack2());
        hashMap.put("bank_merchant_id", PosConstants.getMerchantId());
//      hashMap.put("track3", magCard.getTrack3());
        hashMap.put("pin", pin);
        hashMap.put("card_type", "MAGNETIC");
        //hashMap.put("tags", BankMode.isTDB() ? null : "");
        hashMap.put("owner_id", "454545");
        hashMap.put("org_id", orgId);   // TransactionInfo.getOrg_id()
        hashMap.put("user_id", userId); // GeregeAuth.getUserId()
        hashMap.put("terminal_id", terminalId);   // PosConstants.getTerminalId()
        hashMap.put("amount", amount);
        try {
            hashMap.put("pin", RsaUtils.encrypt(pin));
            hashMap.put("track2", RsaUtils.encrypt(magCard.getTrack2()));
            hashMap.put("adata", RsaUtils.sign(amount));
        } catch (Exception e) {
            e.printStackTrace();
            hashMap.remove("adata");
            hashMap.put("pin", pin);
            hashMap.put("track2", magCard.getTrack2());
        }

        Call<PurchaseResponse> call;
//
//        if (BankMode.isTDB()) {
//            call = apiService.purchaseTDB(hashMap);
//        } else {
//            call = apiService.purchaseGolomt(hashMap);
//        }

        if (isPayment) {
            call = apiService.payment(hashMap);
        } else {
            call = apiService.purchase(hashMap);
        }

        Log.d("purchase_Req", "purchaseMag: " + call.request().url());
        Log.d("purchase_Req", "purchaseMag: " + call.request().body());
        Log.d("purchase_Req", "purchaseMag: " + PosConstants.getMasterKey());
        Log.d("purchase_Req", "purchaseMag: " + pin);
        Log.d("purchase_Req", "purchaseMag:" + bodyToString(call.request().body()));

        call.enqueue(new Callback<PurchaseResponse>() {
            @Override
            public void onResponse(Call<PurchaseResponse> call, retrofit2.Response<PurchaseResponse> response) {
                data.setValue(response.body());
            }

            @Override
            public void onFailure(Call<PurchaseResponse> call, Throwable t) {
                data.setValue(new PurchaseResponse(Status.ERROR, "", ""));
            }
        });
        return data;
    }

    /**
     * IC болон NFC картаар худалдан авалт хийх
     */
    public static MutableLiveData<PurchaseResponse> purchaseIC(ICCard icCard, String amount, String pin, String terminalId, String orgId, String userId, String cardType, boolean isPayment) {
        Log.d("qweqwe", "purchaseIC: " + Thread.currentThread().getId());
        MutableLiveData<PurchaseResponse> data = new MutableLiveData<>();
        data.setValue(new PurchaseResponse(Status.LOADING, "", ""));
        PosApiService apiService = PosRetrofitFactory.getInstanceTDB().create(PosApiService.class);

        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("bank_terminal_id", BankMode.getBankTerminalCode());    // "91200010"
        hashMap.put("bank_merchant_id", PosConstants.getMerchantId());
        hashMap.put("card_type", cardType);
        hashMap.put("mag", "false");
        hashMap.put("tags", icCard.getTrack3());
        hashMap.put("pin", pin);
        hashMap.put("owner_id", "454545");
        hashMap.put("terminal_id", terminalId);   // PosConstants.getTerminalId()
        hashMap.put("org_id", orgId);    // TransactionInfo.getOrg_id()
        hashMap.put("user_id", userId);   // GeregeAuth.getUserId()
        hashMap.put("amount", amount);
        try {
            hashMap.put("pin", RsaUtils.encrypt(pin));
            hashMap.put("track2", RsaUtils.encrypt(icCard.getTrack2()));
            Log.d("qweqwe", "track3: " + icCard.getTrack3());
            // 9F7C009F1A0204969F360205049F370440AB79905F3401019F34034203009F3501229F100706010A03A0A8009F3303E0F8C85F2A020496950508800480009F0B009F6E009F4C009F2701809A032203259F0607A00000000310109B02E8009C01009F03060000000000009F26081AFB61EA1DDF728E9F080200A09F4104000000029F63009F02060000000010005F2016454E4B4854555220424F4C442020202020202020202082023C009F1E0830303030303930358407A0000000031010
            hashMap.put("etags", RsaUtils.encrypt(icCard.getTrack3()));
            hashMap.put("adata", RsaUtils.sign(amount));
        } catch (Exception e) {
            e.printStackTrace();
            hashMap.remove("adata");
            hashMap.put("pin", pin);
            hashMap.put("track2", icCard.getTrack2());
        }

        Call<PurchaseResponse> call;

//        if (BankMode.isTDB()) {
//            call = apiService.purchaseTDB(hashMap);
//        } else {
//            call = apiService.purchaseGolomt(hashMap);
//        }

        if (isPayment) {
            call = apiService.payment(hashMap);
        } else {
            call = apiService.purchase(hashMap);
        }

        Log.d("purchase_Req", "purchaseIC: " + call.request().url());
        Log.d("purchase_Req", "purchaseIC: " + call.request().body());
        Log.d("purchase_Req", "purchaseIC: " + PosConstants.getMasterKey());
        Log.d("purchase_Req", "purchaseIC: " + pin);
        Log.d("purchase_Req", "purchaseIC:" + bodyToString(call.request().body()));

        call.enqueue(new Callback<PurchaseResponse>() {

            @Override
            public void onResponse(Call<PurchaseResponse> call, retrofit2.Response<PurchaseResponse> response) {
                data.setValue(response.body());
            }

            @Override
            public void onFailure(Call<PurchaseResponse> call, Throwable t) {
                data.setValue(new PurchaseResponse(Status.ERROR, "", ""));
            }
        });
//        data.setValue(new PurchaseResponse(Status.ERROR, "", ""));
        return data;
    }

    /**
     * Magnetic картаар худалдан авалт хийх
     */

    public static MutableLiveData<PurchaseResponse> reverseMag(MagCard magCard, String amount, String pin, String terminalId, String orgId, String userId, String invoiceNumber) {
        MutableLiveData<PurchaseResponse> data = new MutableLiveData<>();
        data.setValue(new PurchaseResponse(Status.LOADING, "", ""));
        PosApiService apiService = PosRetrofitFactory.getInstanceTDB().create(PosApiService.class);

        HashMap<String, String> hashMap = new HashMap<>();

        hashMap.put("bank_terminal_id", BankMode.getBankTerminalCode());
        hashMap.put("track1", magCard.getTrack1());
        hashMap.put("track2", magCard.getTrack2());
//      hashMap.put("track3", magCard.getTrack3());
        hashMap.put("pin", pin);
        hashMap.put("card_type", "MAGNETIC");
        //hashMap.put("tags", BankMode.isTDB() ? null : "");
        hashMap.put("owner_id", "454545");
        hashMap.put("org_id", orgId);   // TransactionInfo.getOrg_id()
        hashMap.put("user_id", userId); // GeregeAuth.getUserId()
        hashMap.put("terminal_id", terminalId);   // PosConstants.getTerminalId()
        hashMap.put("amount", amount);
        try {
            hashMap.put("pin", RsaUtils.encrypt(pin));
            hashMap.put("invoice_number", RsaUtils.encrypt(invoiceNumber));
            hashMap.put("track2", RsaUtils.encrypt(magCard.getTrack2()));
            hashMap.put("adata", RsaUtils.sign(amount));
        } catch (Exception e) {
            e.printStackTrace();
            data.setValue(new PurchaseResponse(Status.ERROR, "", "Retry error"));
            return data;
        }

        Call<PurchaseResponse> call;

        if (BankMode.getBank().equals(Banks.TDB_BANK)) {
            call = apiService.reverseTDB(hashMap);
        } else {
            call = apiService.reverseGolomt(hashMap);
        }
        Log.d("purchase_Req", "purchaseMag: " + call.request().url());
        Log.d("purchase_Req", "purchaseMag: " + call.request().body());
        Log.d("purchase_Req", "purchaseMag:" + bodyToString(call.request().body()));

        call.enqueue(new Callback<PurchaseResponse>() {
            @Override
            public void onResponse(Call<PurchaseResponse> call, retrofit2.Response<PurchaseResponse> response) {
                data.setValue(response.body());
            }

            @Override
            public void onFailure(Call<PurchaseResponse> call, Throwable t) {
                data.setValue(new PurchaseResponse(Status.ERROR, "", ""));
            }
        });
        return data;
    }

    /**
     * IC болон NFC картаар худалдан авалт хийх
     */
    public static MutableLiveData<PurchaseResponse> reverseIC(ICCard icCard, String amount, String pin, String terminalId, String orgId, String userId, String invoiceNumber) {
        MutableLiveData<PurchaseResponse> data = new MutableLiveData<>();
        data.setValue(new PurchaseResponse(Status.LOADING, "", ""));
        PosApiService apiService = PosRetrofitFactory.getInstanceTDB().create(PosApiService.class);

        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("bank_terminal_id", BankMode.getBankTerminalCode());
        hashMap.put("card_type", "IC");
        hashMap.put("mag", "false");
        hashMap.put("tags", icCard.getTrack3());
        hashMap.put("pin", pin);
        hashMap.put("owner_id", "454545");
        hashMap.put("terminal_id", terminalId);   // PosConstants.getTerminalId()
        hashMap.put("org_id", orgId);    // TransactionInfo.getOrg_id()
        hashMap.put("user_id", userId);   // GeregeAuth.getUserId()
        hashMap.put("amount", amount);
        try {
            hashMap.put("pin", RsaUtils.encrypt(pin));
            hashMap.put("invoice_number", RsaUtils.encrypt(invoiceNumber));
            hashMap.put("track2", RsaUtils.encrypt(icCard.getTrack2()));
            hashMap.put("adata", RsaUtils.sign(amount));
        } catch (Exception e) {
            e.printStackTrace();
            data.setValue(new PurchaseResponse(Status.ERROR, "", "Retry error"));
            return data;
        }

        Call<PurchaseResponse> call;

        if (BankMode.getBank().equals(Banks.TDB_BANK)) {
            call = apiService.reverseTDB(hashMap);
        } else {
            call = apiService.reverseGolomt(hashMap);
        }

        Log.d("purchase_Req", "purchaseIC: " + call.request().url());
        Log.d("purchase_Req", "purchaseIC: " + call.request().body());
        Log.d("purchase_Req", "purchaseIC:" + bodyToString(call.request().body()));

        call.enqueue(new Callback<PurchaseResponse>() {

            @Override
            public void onResponse(Call<PurchaseResponse> call, retrofit2.Response<PurchaseResponse> response) {
                data.setValue(response.body());
            }

            @Override
            public void onFailure(Call<PurchaseResponse> call, Throwable t) {
                data.setValue(new PurchaseResponse(Status.ERROR, "", ""));
            }
        });
        return data;
    }

    private static String bodyToString(final RequestBody request) {
        try {
            final RequestBody copy = request;
            final Buffer buffer = new Buffer();
            if (copy != null)
                copy.writeTo(buffer);
            else
                return "";
            return buffer.readUtf8();
        } catch (final IOException e) {
            return "did not work";
        }
    }
}
