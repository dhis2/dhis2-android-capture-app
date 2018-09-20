package org.dhis2.data.forms.dataentry.fields;

import android.databinding.ViewDataBinding;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;

import org.dhis2.R;

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
        if (description != null)
            description.setOnClickListener(v -> new AlertDialog.Builder(itemView.getContext())
                    .setPositiveButton(android.R.string.ok, null)
                    .setTitle("Info")
                    .setMessage(label != null ? label : "No info for this field")
                    .show());
    }

    public abstract void dispose();
}
