package org.dhis2.utils.customviews;

import android.app.AlertDialog;
import android.content.Context;
import androidx.databinding.DataBindingUtil;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;

import org.dhis2.R;
import org.dhis2.databinding.CustomDialogBinding;
import org.dhis2.utils.DialogClickListener;

/**
 * QUADRAM. Created by frodriguez on 5/4/2018.
 */

public class CustomDialog extends AlertDialog implements View.OnClickListener {

    private Context context;
    private AlertDialog dialog;
    private String title;
    private String message;
    private String positiveText;
    private String negativeText;
    private int requestCode;
    private DialogClickListener listener;

    public CustomDialog(@NonNull Context context,
                        @NonNull String title,
                        @NonNull String message,
                        @NonNull String positiveText,
                        @Nullable String negativeText,
                        int requestCode,
                        @Nullable DialogClickListener listener) {
        super(context);
        this.context = context;
        this.title = title;
        this.message = message;
        this.positiveText = positiveText;
        this.negativeText = negativeText;
        this.requestCode = requestCode;
        this.listener = listener;
    }


    @Override
    public void show() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        CustomDialogBinding binding = DataBindingUtil.inflate(inflater, R.layout.custom_dialog, null, false);

        binding.setTitle(title);
        binding.setMessage(message);
        binding.setNegativeText(negativeText);
        binding.setPositiveText(positiveText);

        builder.setView(binding.getRoot());

        dialog = builder.create();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        if (!TextUtils.isEmpty(negativeText))
            binding.negative.setOnClickListener(this);
        if (!TextUtils.isEmpty(positiveText))
            binding.possitive.setOnClickListener(this);
        dialog.show();
    }

    @Override
    public void dismiss() {
        if (dialog != null)
            dialog.dismiss();
    }

    public int getRequestCode() {
        return requestCode;
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.negative:
                if (listener != null) {
                    listener.onNegative();
                }
                dismiss();
                break;
            case R.id.possitive:
                if (listener != null) {
                    listener.onPositive();
                }
                dismiss();
                break;
            default:
                break;
        }
    }
}
