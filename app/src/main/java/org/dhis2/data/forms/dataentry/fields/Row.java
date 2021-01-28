package org.dhis2.data.forms.dataentry.fields;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import android.view.ViewGroup;

public interface Row<VH extends ViewHolder, VM extends FieldViewModel> {

    @NonNull
    VH onCreate(@NonNull ViewGroup parent);

    void onBind(@NonNull VH viewHolder, @NonNull VM viewModel);
}