package com.gerege.cardreader_verifon.dialogs;


import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.gerege.cardreader_verifon.R;


public class PinDialog extends DialogFragment {

    private PinDialogListener listener;

    public interface PinDialogListener {
        void onInputDone(String input);

        void onCancel();
    }

    private PinDialog() {
    }

    public void setListener(PinDialogListener listener) {
        this.listener = listener;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof PinDialogListener && listener == null) {
            listener = (PinDialogListener) context;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_pin, container, false);

        setCancelable(false);

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

        EditText editText = view.findViewById(R.id.et_input);

        view.findViewById(R.id.btn_done).setOnClickListener(view1 -> {
            if (listener != null) {
                listener.onInputDone(editText.getText().toString());
                dismiss();
            }
        });

        showKeyboard();

        return view;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        closeKeyboard();
    }

    public static PinDialog newInstance() {
        return new PinDialog();
    }

    public void showKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public void closeKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }
}