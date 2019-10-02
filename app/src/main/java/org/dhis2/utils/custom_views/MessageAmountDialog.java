package org.dhis2.utils.custom_views;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import org.dhis2.R;
import org.dhis2.usescases.sms.SmsSubmitActivity;
import org.jetbrains.annotations.NotNull;

public class MessageAmountDialog extends DialogFragment {
    public static final String ARG_AMOUNT = "amount";
    private final OnMessageCountAccepted messageCountAcceptedListener;

    public MessageAmountDialog(OnMessageCountAccepted messageCountAcceptedListener) {
        this.messageCountAcceptedListener = messageCountAcceptedListener;
    }

    public interface OnMessageCountAccepted {
        void acceptSMSCount(boolean accepted);
    }

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int amount = getArguments().getInt(ARG_AMOUNT);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getString(R.string.sms_amount_question, amount));

        builder.setPositiveButton(android.R.string.yes, (dialog, which) ->
                messageCountAcceptedListener.acceptSMSCount(true)
        );
        builder.setNegativeButton(android.R.string.no, (dialog, which) ->
                messageCountAcceptedListener.acceptSMSCount(false)
        );
        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        AlertDialog dialog = (AlertDialog) getDialog();
        int color = ContextCompat.getColor(getActivity(), R.color.colorPrimary);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(color);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(color);
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        FragmentActivity activity = getActivity();
        if (activity instanceof SmsSubmitActivity) {
            messageCountAcceptedListener.acceptSMSCount(false);
        }
    }

    @Override
    public void onPause() {
        dismiss();
        super.onPause();
    }
}
