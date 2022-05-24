package com.gerege.verifoncardreader;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "qweqweMain";
    private ProgressDialog progressDialog;
    private CardReader cardReader;
    private Button button;
    private ReadCardDialog readCardDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BankMode.setBank(Banks.TDB_BANK);

        readCardDialog = ReadCardDialog.newInstance("10");
        readCardDialog.setListener(() -> cardReader.stop());

        findViewById(R.id.btn_get_key).setOnClickListener(this);
        button = findViewById(R.id.btn_mag);

        button.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_get_key) {
            getKey();
        } else if (view.getId() == R.id.btn_mag) {
            read("1000");
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
}