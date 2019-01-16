package org.dhis2.utils.custom_views;

import android.app.AlertDialog;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;

import org.dhis2.R;
import org.dhis2.databinding.CatComboDialogBinding;
import org.dhis2.utils.CatComboAdapter2;
import org.hisp.dhis.android.core.category.CategoryComboModel;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;

import java.util.List;

/**
 * QUADRAM. Created by frodriguez on 5/4/2018.
 */

public class CategoryComboDialog extends AlertDialog {

    private final List<CategoryOptionComboModel> options;
    private final String catComboName;
    private Context context;
    private AlertDialog dialog;
    private int requestCode;
    private OnCatOptionSelected listener;

    public CategoryComboDialog(@NonNull Context context,
                               @NonNull CategoryComboModel categoryComboModel,
                               List<CategoryOptionComboModel> options,
                               int requestCode,
                               @Nullable OnCatOptionSelected listener) {
        super(context);
        this.context = context;
        this.catComboName = categoryComboModel.displayName();
        this.options = options;
        this.requestCode = requestCode;
        this.listener = listener;

        setCancelable(false);
    }

    public CategoryComboDialog(Context context, String catComboName, List<CategoryOptionComboModel> options, int requestCode, OnCatOptionSelected listener) {
        super(context);
        this.context = context;
        this.catComboName = catComboName;
        this.options = options;
        this.requestCode = requestCode;
        this.listener = listener;

        setCancelable(true);
    }


    @Override
    public void show() {
        Builder builder = new Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        CatComboDialogBinding binding = DataBindingUtil.inflate(inflater, R.layout.cat_combo_dialog, null, false);

        builder.setView(binding.getRoot());

        dialog = builder.create();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        binding.titleDialog.setText(catComboName);

        CatComboAdapter2 adapter = new CatComboAdapter2(context,
                R.layout.spinner_layout,
                R.id.spinner_text,
                options,
                catComboName != null ? catComboName : context.getString(R.string.category_option));

        binding.catCombo.setAdapter(adapter);

        binding.catCombo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (options.size() > position - 1 && position > 0) {
                    listener.onCatOptionSelected(options.get(position - 1));
                    dismiss();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

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

    public interface OnCatOptionSelected {
        void onCatOptionSelected(CategoryOptionComboModel selectedOption);
    }
}
