package org.dhis2.usescases.syncManager;

import android.app.Dialog;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.DividerItemDecoration;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import org.dhis2.R;
import org.dhis2.databinding.ErrorDialogBinding;
import org.dhis2.utils.ErrorMessageModel;

import java.util.List;

/**
 * QUADRAM. Created by frodriguez on 5/4/2018.
 */

public class ErrorDialog extends DialogFragment {

    private static ErrorDialog instace;
    private String title;
    private List<ErrorMessageModel> data;
    private DividerItemDecoration divider;
    public static String TAG = "FullScreenDialog";

    public static ErrorDialog newInstace() {
        if (instace == null) {
            instace = new ErrorDialog();
        }
        return instace;
    }

    public ErrorDialog setData(List<ErrorMessageModel> data) {
        this.data = data;
        return this;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.title = context.getString(R.string.error_dialog_title);
        this.divider = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setStyle(DialogFragment.STYLE_NORMAL, R.style.FulLScreenDialogStyle);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        ErrorDialogBinding binding = DataBindingUtil.inflate(inflater, R.layout.error_dialog, container, false);

        binding.titleDialog.setText(title);
        binding.errorRecycler.setAdapter(new ErrorAdapter(data));
        binding.errorRecycler.addItemDecoration(divider);
        binding.possitive.setOnClickListener(view -> dismiss());

        return binding.getRoot();
    }
}
