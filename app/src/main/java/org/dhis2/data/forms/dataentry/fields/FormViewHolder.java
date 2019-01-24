package org.dhis2.data.forms.dataentry.fields;

import android.app.Activity;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import org.dhis2.R;
import org.dhis2.utils.Constants;
import org.dhis2.utils.custom_views.CustomDialog;

/**
 * QUADRAM. Created by ppajuelo on 06/11/2017.
 */

public abstract class FormViewHolder extends RecyclerView.ViewHolder {

    public ViewDataBinding binding;
    public ImageView description;
    public StringBuilder label;
    public String descriptionText;

    public FormViewHolder(ViewDataBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
        this.description = binding.getRoot().findViewById(R.id.descriptionLabel);
        if (description != null) {
            description.setOnClickListener(v ->
                    new CustomDialog(
                            itemView.getContext(),
                            label.toString(),
                            descriptionText != null ? descriptionText : "No info for this field",
                            itemView.getContext().getString(R.string.action_accept),
                            null,
                            Constants.DESCRIPTION_DIALOG,
                            null
                    ).show());
        }
    }

    public abstract void dispose();

    public void closeKeyboard(View v){
        InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }
}
