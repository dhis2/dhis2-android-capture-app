package org.dhis2.data.forms.dataentry.fields;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

public interface Row<H extends ViewHolder, M extends FieldViewModel> {

    @NonNull
    H onCreate(@NonNull ViewGroup parent);

    void onBind(@NonNull H viewHolder, @NonNull M viewModel);

    void deAttach(@NonNull H viewHolder);
}