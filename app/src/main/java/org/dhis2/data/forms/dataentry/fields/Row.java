package org.dhis2.data.forms.dataentry.fields;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

public interface Row<V extends ViewHolder, M extends FieldViewModel> {

    @NonNull
    V onCreate(@NonNull ViewGroup parent);

    void onBind(@NonNull V viewHolder, @NonNull M viewModel);

    void deAttach(@NonNull V viewHolder);
}