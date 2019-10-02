package org.dhis2.utils.custom_views;

import android.app.AlertDialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import org.dhis2.R;
import org.dhis2.databinding.CustomDialogBinding;
import org.dhis2.databinding.TableFieldDialogBinding;
import org.dhis2.utils.DialogClickListener;

public class TableFieldDialog extends AlertDialog implements View.OnClickListener {

    private Context context;
    private AlertDialog dialog;
    private String title;
    private String subTitle;
    private View view;
    private DialogClickListener listener;
    private View.OnClickListener clearListener;

    public TableFieldDialog(@NonNull Context context,
                            @NonNull String title,
                            @NonNull String subTitle,
                            @NonNull View view,
                            @Nullable DialogClickListener listener,
                            @Nullable View.OnClickListener clearListener) {
        super(context);
        this.context = context;
        this.title = title;
        this.subTitle = subTitle;
        this.view = view;
        this.listener = listener;
        this.clearListener = clearListener;
    }


    @Override
    public void show() {
        Builder builder = new Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        TableFieldDialogBinding binding = DataBindingUtil.inflate(inflater, R.layout.table_field_dialog, null, false);

        binding.setTitle(title);
        binding.setSubTitle(subTitle);
        binding.setNegativeText(context.getString(R.string.cancel));
        binding.setPositiveText(context.getString(R.string.action_accept));

        binding.viewLayout.addView(view);
        builder.setView(binding.getRoot());

        dialog = builder.create();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        if (listener != null) {
            binding.negative.setOnClickListener(this);
            binding.possitive.setOnClickListener(this);
        }

        if(clearListener != null) {
            binding.clearSelection.setOnClickListener(this);
        }
        else{
            binding.clearSelection.setVisibility(View.GONE);
        }

        dialog.show();
    }

    @Override
    public void dismiss() {
        if (dialog != null)
            dialog.dismiss();
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
            case R.id.clearSelection:
                if (clearListener != null) {
                    clearListener.onClick(view);
                }
                break;
            default:
                break;
        }
    }
}
