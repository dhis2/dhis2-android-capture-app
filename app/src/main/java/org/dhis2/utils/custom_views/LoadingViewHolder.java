package org.dhis2.utils.custom_views;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.dhis2.databinding.ItemLoadingBinding;

/**
 * Created by frodriguez on 5/20/2019.
 */
public class LoadingViewHolder extends RecyclerView.ViewHolder {

    public LoadingViewHolder(@NonNull ItemLoadingBinding itemView) {
        super(itemView.getRoot());
    }

    public void bind() {
        // unused
    }
}
