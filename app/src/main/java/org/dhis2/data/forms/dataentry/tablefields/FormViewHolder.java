package org.dhis2.data.forms.dataentry.tablefields;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder;

import org.dhis2.R;
import org.dhis2.utils.Constants;
import org.dhis2.utils.custom_views.CustomDialog;

import androidx.databinding.ViewDataBinding;

/**
 * QUADRAM. Created by ppajuelo on 06/11/2017.
 */

public abstract class FormViewHolder extends AbstractViewHolder {

    public ViewDataBinding binding;
    public ImageView description;
    public StringBuilder label;
    public String descriptionText;

    public FormViewHolder(ViewDataBinding binding) {
        super(binding.getRoot());
        label = new StringBuilder();
        this.binding = binding;
        this.description = binding.getRoot().findViewById(R.id.descriptionLabel);
        if (description != null) {
            description.setOnClickListener(v ->
                    new CustomDialog(
                            itemView.getContext(),
                            label.toString(),
                            descriptionText != null ? descriptionText : itemView.getContext().getString(R.string.empty_description),
                            itemView.getContext().getString(R.string.action_close),
                            null,
                            Constants.DESCRIPTION_DIALOG,
                            null
                    ).show());
        }
    }

    public abstract void dispose();

    public void openKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) itemView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
    }

    public void closeKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }
}
