package org.dhis2.data.forms.dataentry.fields;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

/**
 * QUADRAM. Created by Administrador on 15/06/2018.
 */

public abstract class FieldViewHolder extends RecyclerView.ViewHolder {

    public FieldViewHolder(View itemView) {
        super(itemView);
    }

    public abstract void update(@NonNull FieldViewModel viewModel);

    public abstract void dispose();

}
