package com.gerege.cardreader_verifon.dialogs;


import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.gerege.cardreader_verifon.R;


public class ReadCardDialog extends DialogFragment implements View.OnClickListener {

    private ReadCardDialogListener listener;
    private String amount;

    public interface ReadCardDialogListener {
        void onClose();
    }

    public ReadCardDialog(String amount) {
        this.amount = amount;
    }

    public ReadCardDialog() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ReadCardDialogListener && listener == null) {
            listener = (ReadCardDialogListener) context;
        }
    }

    public void setListener(ReadCardDialogListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.cr_dialog_read_card, null);

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }
        setCancelable(false);

        view.findViewById(R.id.btn_close).setOnClickListener(ReadCardDialog.this);
        ((TextView) view.findViewById(R.id.tv_amount)).setText(this.amount + "₮");

        return view;
    }

    public static ReadCardDialog newInstance(String amount) {
        return new ReadCardDialog(amount);
    }

    public static ReadCardDialog newInstance() {
        return new ReadCardDialog();
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_close) {
            dismiss();
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (listener != null) {
            listener.onClose();
        }
    }
}