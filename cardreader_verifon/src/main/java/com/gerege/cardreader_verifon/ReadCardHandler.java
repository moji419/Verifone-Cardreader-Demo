package com.gerege.cardreader_verifon;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import com.gerege.cardreader_verifon.interfaces.CardReaderListener;
import com.gerege.cardreader_verifon.models.ICCard;
import com.gerege.cardreader_verifon.models.MagCard;

public class ReadCardHandler extends Handler {

    public static final int STARTED = 1;
    public static final int MAG_CARD = 2;
    public static final int IC_CARD = 3;
    public static final int ERROR = 4;
    public static final int CANCELLED = 5;

    private CardReaderListener listener;
    private String pin;         // TODO: here ?

    public ReadCardHandler(@NonNull Looper looper) {
        super(looper);
    }

    public void setListener(CardReaderListener listener) {
        this.listener = listener;
    }

    public void msgStarted() {
        sendMessage(obtainMessage(STARTED));
    }

    public void msgMagCard(MagCard magCard, String pin) {
        this.pin = pin;
        sendMessage(obtainMessage(MAG_CARD, magCard));
    }

    public void msgIcCard(ICCard icCard, String pin) {
        this.pin = pin;
        sendMessage(obtainMessage(IC_CARD, icCard));
    }

    public void msgError() {
        sendMessage(obtainMessage(ERROR));
    }

    public void msgCancelled() {
        sendMessage(obtainMessage(CANCELLED));
    }

    public void msgError(Object o) {
        sendMessage(obtainMessage(ERROR, o));
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        super.handleMessage(msg);

        if (listener == null) return;

        switch (msg.what) {
            case STARTED:
                listener.onStart();
                break;

            case MAG_CARD:
                listener.onMagCard((MagCard) msg.obj, pin);
                break;

            case IC_CARD:
                listener.onIcCard((ICCard) msg.obj, pin);
                break;

            case ERROR:
                listener.onError();
                break;

            case CANCELLED:
                listener.onCancelled();
                break;
        }

        pin = "";
    }
}
