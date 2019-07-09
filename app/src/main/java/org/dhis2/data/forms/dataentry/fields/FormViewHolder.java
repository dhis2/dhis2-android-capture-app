package org.dhis2.data.forms.dataentry.fields;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;

import org.dhis2.R;
import org.dhis2.utils.Constants;
import org.dhis2.utils.custom_views.CustomDialog;

/**
 * QUADRAM. Created by ppajuelo on 06/11/2017.
 */

public abstract class FormViewHolder extends RecyclerView.ViewHolder {

    protected ViewDataBinding binding;
    protected ImageView description;
    protected StringBuilder label;
    protected String descriptionText;
    protected MutableLiveData<String> currentUid;
    protected String fieldUid;

    public FormViewHolder(ViewDataBinding binding) {
        super(binding.getRoot());
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

    public void closeKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    public void openKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (!imm.showSoftInput(v, 0))
            v.postDelayed(() -> openKeyboard(v), 500);
    }

    public void clearBackground(boolean isSarchMode) {
        if (!isSarchMode)
            itemView.setBackgroundColor(Color.WHITE);
    }

    public void setSelectedBackground(boolean isSarchMode) {
        if (!isSarchMode)
            currentUid.setValue(fieldUid);
    }
}
