package org.dhis2.data.forms.dataentry.tablefields;

import androidx.annotation.NonNull;
import android.view.ViewGroup;

import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder;

public interface Row<VH extends AbstractViewHolder, VM extends FieldViewModel> {

    @NonNull
    VH onCreate(@NonNull ViewGroup parent);

    void onBind(@NonNull VH viewHolder, @NonNull VM viewModel, String value);

    void deAttach(@NonNull VH viewHolder);
}