package org.dhis2.data.forms.dataentry.fields;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.ViewGroup;

public interface Row<VH extends ViewHolder, VM extends FieldViewModel> {

    @NonNull
    VH onCreate(@NonNull ViewGroup parent);

    void onBind(@NonNull VH viewHolder, @NonNull VM viewModel);

    void deAttach(@NonNull VH viewHolder);
}