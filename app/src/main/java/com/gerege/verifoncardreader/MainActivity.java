package com.gerege.verifoncardreader;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.gerege.cardreader_verifon.CardReader;
import com.gerege.cardreader_verifon.dialogs.ReadCardDialog;
import com.gerege.cardreader_verifon.dialogs.TransactionResponseDialog;
import com.gerege.cardreader_verifon.interfaces.CardReaderListener;
import com.gerege.cardreader_verifon.models.ICCard;
import com.gerege.cardreader_verifon.models.MagCard;
import com.gerege.verifoncardreader.api.PosRepository;
import com.gerege.verifoncardreader.api.PurchaseResponse;
import com.gerege.verifoncardreader.pos.BankMode;
import com.gerege.verifoncardreader.pos.Banks;
import com.gerege.verifoncardreader.pos.PosConstants;
import com.verifone.androidtmslib.AndroidTMSLib;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final int PERMISSION_REQUEST_STORAGE = 16699;

    private static final String TAG = "qweqweMain";
    private ProgressDialog progressDialog;
    private CardReader cardReader;
    private Button button;
    private ReadCardDialog readCardDialog;
    private TmsLibCallback tmsLibCallback;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (hasPermission(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE})) {
            copyAssets();
            AndroidTMSLib.getInstance().initialize(getApplicationContext());

                tmsLibCallback = new TmsLibCallback(this);
                AndroidTMSLib.getInstance().registerApplication(
                        this.getPackageName(),0xffffffff, tmsLibCallback, this,
                        "com.gerege.verifoncardreader.provider"); //BuildConfig.APPLICATION_ID
        } else {
            requestPermission(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_STORAGE);
        }


        PosConstants.setMasterKey("FB89C1086BBC3E1A4F8AEC1C94C41929");
        BankMode.setBank(Banks.TDB_BANK);


        readCardDialog = ReadCardDialog.newInstance("10");
        readCardDialog.setListener(() -> cardReader.stop());

        findViewById(R.id.btn_get_key).setOnClickListener(this);
        button = findViewById(R.id.btn_mag);

        button.setOnClickListener(this);

        findViewById(R.id.btn_printer).setOnClickListener(this);
        findViewById(R.id.btn_connect).setOnClickListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_STORAGE) {
            if (isGranted(grantResults)) {
                if (hasPermission(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE})) {
                    copyAssets();
                    AndroidTMSLib.getInstance().initialize(getApplicationContext());

                    tmsLibCallback = new TmsLibCallback(this);
                    AndroidTMSLib.getInstance().registerApplication(
                            this.getPackageName(),0xffffffff, tmsLibCallback, this,
                            "com.gerege.verifoncardreader.provider"); //BuildConfig.APPLICATION_ID
                } else {
                    requestPermission(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_STORAGE);
                }
            } else {
                Toast.makeText(this, "Permission not granted", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void copyAssets() {
        AssetManager assetManager = getAssets();
        String[] files = null;
        try {
            files = assetManager.list("");
        } catch (IOException e) {
            Log.e("tag", "Failed to get asset file list.", e);
        }
        for (String filename : files) {
            Log.d("dirlog", "" + filename);
            InputStream in = null;
            OutputStream out = null;
            try {
                in = assetManager.open(filename);
                String outDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/vhq/config/";
                Log.d("dirlog", "" + outDir);
                File outFile = new File(outDir, filename);

                out = new FileOutputStream(outFile);
                copyFile(in, out);
                in.close();
                in = null;
                out.flush();
                out.close();
                out = null;
            } catch (IOException e) {
                Log.e("tag", "Failed to copy asset file: " + filename, e);
            }
        }
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }



    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_get_key) {
            getKey();
        } else if (view.getId() == R.id.btn_mag) {
            read("1000");
        } else if(view.getId() == R.id.btn_printer) {
            startActivity(new Intent(this, PrinterActivity.class));
        } else if(view.getId() == R.id.btn_connect){
            connectToVHQ();
        }
    }

    //        String rootUrl = "https://qa.mumbai.verifonehq.net/MessagingServer/MessageHandler.asmx";
//        HashMap<String, String> map = new HashMap<>();
//        map.put("server,url root", rootUrl);
//        map.put("vhq,TerminalID", "Pos TEST 1");
//
//        AndroidTMSLib.getInstance().setVHQConfig(this, map);

    private void connectToVHQ(){
        if (hasPermission(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE})) {
            copyAssets();
            AndroidTMSLib.getInstance().initialize(getApplicationContext());

            tmsLibCallback = new TmsLibCallback(this);
            AndroidTMSLib.getInstance().registerApplication(
                    this.getPackageName(),0xffffffff, tmsLibCallback, this,
                    "com.gerege.verifoncardreader.provider"); //BuildConfig.APPLICATION_ID
        } else {
            requestPermission(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_STORAGE);
        }

    }

    // region
    private void getKey() {
        Log.d(TAG, "getKey");
        PosRepository.getKey().observe(this, response -> {
            if (response == null) {
                setShowLoading(false);
                Toast.makeText(this, getString(R.string.err_get_key), Toast.LENGTH_SHORT).show();
                return;
            }
            Log.d(TAG, "status: " + response.getStatus()); // FB89C1086BBC3E1A4F8AEC1C94C41929
            switch (response.getStatus()) {
                case SUCCESS:
                    setShowLoading(false);
                    Log.d(TAG, "PKey: " + PosConstants.getPinKey());    // B7D0540813DED532C0F19D6FACB916EE
                    Log.d(TAG, "MKey: " + PosConstants.getMasterKey()); // FB89C1086BBC3E1A4F8AEC1C94C41929

                    button.setEnabled(true);
                    cardReader = new CardReader(
                            PosConstants.getPinKey(),
                            PosConstants.getMasterKey() //"D9E95170F125BA9BC42C075873914370"
                    );
                    cardReader.connect(this);
                    break;

                case ERROR:
                    setShowLoading(false);
                    Toast.makeText(this, !response.getMessage().isEmpty() ? response.getMessage() : getString(R.string.err_get_key), Toast.LENGTH_SHORT).show();
                    break;

                case LOADING:
                    setShowLoading(true);
                    break;
            }
        });
    }

    private void read(String amount) {
        Log.d(TAG, "ThreadId (read): " + Thread.currentThread().getId());

        cardReader.readCard(this, Long.parseLong(amount), new CardReaderListener() {
            @Override
            public void onStart() {
                readCardDialog.show(getSupportFragmentManager(), "");
                Log.d(TAG, "onStart");
            }

            @Override
            public void onMagCard(MagCard magCard, String pin) {
                readCardDialog.dismiss();
                Log.d(TAG, "onMagCard");
                PosRepository.purchaseMag(magCard, amount, pin, PosConstants.getTerminalId(), "", "", false).observe(MainActivity.this, res -> onPurchaseResponse(amount, res));
            }

            @Override
            public void onIcCard(ICCard icCard, String pin) {
                readCardDialog.dismiss();
                Log.d(TAG, "onIcCard: " + Thread.currentThread().getId());
                PosRepository.purchaseIC(icCard, amount, pin, PosConstants.getTerminalId(), "", "", "IC", false)
                        .observe(MainActivity.this, res -> onPurchaseResponse(amount, res));
            }

            @Override
            public void onCancelled() {
                readCardDialog.dismiss();
                Log.d(TAG, "onCancelled");
            }

            @Override
            public void onError() {
                readCardDialog.dismiss();
                Toast.makeText(MainActivity.this, "Карт уншихад алдаа гарлаа !", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onError");
            }
        });
    }

    public void onPurchaseResponse(String amount, PurchaseResponse response) {
        if (response == null) {
            Toast.makeText(this, R.string.purchase_failed, Toast.LENGTH_SHORT).show();
            setShowLoading(false);
            return;
        }

        switch (response.getStatus()) {
            case SUCCESS:
                setShowLoading(false);
                showResponseDialog(true, getString(R.string.success));
                break;

            case ERROR:
                setShowLoading(false);
                String errMessage = !response.getMessage().isEmpty() ? response.getMessage() : getString(R.string.purchase_failed);
                showResponseDialog(false, errMessage);
                break;

            case LOADING:
                setShowLoading(true);
                break;
        }
    }

    void showResponseDialog(boolean success, String message) {
        TransactionResponseDialog.newInstance(success, message).show(
                getSupportFragmentManager(),
                "TransactionResponseDialog"
        );
    }

    /**
     * Loading
     */
    private void setShowLoading(Boolean show) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage(getString(R.string.loading));
            progressDialog.setCancelable(false);
        }

        if (show) {
            progressDialog.show();
        } else {
            progressDialog.dismiss();
        }
    }
    // endregion

    /**
     * Хэрэглэгчээс permission -үүд асуух, хүсэх
     *
     * @param permissions Хэрэглэгчээс асуу /хүсэх/ permission array
     * @param requestCode requestCode (Permission -үүдийн хүсэлтийн үр дүнг уг кодтой хамт буцаах бөгөөд тухайн хүсэлтийн үр дүнг авахын тулд requestCode -р ялгаж авна)
     */
    protected void requestPermission(String[] permissions, int requestCode) {
        if (permissions == null) {
            return;
        }

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, permissions, requestCode);
                break;
            }
        }
    }

    /**
     * App -д Permission байгаа эсэхийг шалгах
     *
     * @param permissions Шалгах permission array (Бүх permission олгогдсон үед true)
     */
    protected boolean hasPermission(String[] permissions) {
        if (permissions == null) {
            return false;
        }

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }

    /**
     * Хэрэглэгч Permission -г зөвшөөрсөн эсэхийг шалгах
     *
     * @param grantResults Permission -үүдийг зөвшөөрсөн эсэхийг заасан array (Бүх permission зөвшөөрөгдсөн үед true)
     */
    protected boolean isGranted(int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }
}