package org.dhis2.usescases.general;

import android.support.annotation.NonNull;
import android.support.v7.recyclerview.extensions.ListAdapter;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;

/**
 * QUADRAM. Created by ppajuelo on 17/10/2018.
 */

public abstract class AbstractRecyclerAdapter<T,VH extends RecyclerView.ViewHolder> extends ListAdapter<T, VH> {

    protected AbstractRecyclerAdapter(@NonNull DiffUtil.ItemCallback<T> diffCallback) {
        super(diffCallback);
    }
}
