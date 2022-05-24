package com.gerege.cardreader_verifon.dialogs;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.gerege.cardreader_verifon.R;


public class TransactionResponseDialog extends DialogFragment implements View.OnClickListener {

    private static final String PARAM_IS_SUCCESS = "TransactionResponseDialog.param_is_success";
    private static final String PARAM_MSG = "TransactionResponseDialog.param_msg";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_transaction_response, null);

        setCancelable(false);

        if (getArguments() != null) {
            if (getArguments().getBoolean(PARAM_IS_SUCCESS)) {
                view.findViewById(R.id.iv_icon).setBackgroundResource(R.drawable.success_64);
                ((TextView) view.findViewById(R.id.tv_status_msg)).setText(getString(R.string.transaction_success));
                if (getActivity() != null) {
                    ((TextView) view.findViewById(R.id.tv_status_msg)).setTextColor(ContextCompat.getColor(getActivity(), R.color.posSuccessTrans));
                }
            } else {
                view.findViewById(R.id.iv_icon).setBackgroundResource(R.drawable.error_64);
                ((TextView) view.findViewById(R.id.tv_status_msg)).setText(getString(R.string.transaction_failed));
                if (getActivity() != null) {
                    ((TextView) view.findViewById(R.id.tv_status_msg)).setTextColor(ContextCompat.getColor(getActivity(), R.color.posErrorTrans));
                }
            }

            ((TextView) view.findViewById(R.id.tv_label)).setText(getArguments().getString(PARAM_MSG));
        } else {
            dismiss();
        }

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

        view.findViewById(R.id.btn_close).setOnClickListener(TransactionResponseDialog.this);

        return view;
    }

    public static TransactionResponseDialog newInstance(boolean success, String body) {
        TransactionResponseDialog dialog = new TransactionResponseDialog();
        Bundle bundle = new Bundle();
        bundle.putBoolean(PARAM_IS_SUCCESS, success);
        bundle.putString(PARAM_MSG, body);
        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_close) {
            dismiss();
        }
    }
}