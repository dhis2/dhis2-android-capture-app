package com.dhis2.usescases.searchTrackEntity.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by frodriguez on 4/17/2018.
 */

public abstract class BaseSearchAdapter<T extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<T> {

    private List items = new ArrayList<>();

    @Override
    public abstract T onCreateViewHolder(ViewGroup parent, int viewType);

    @Override
    public abstract void onBindViewHolder(T holder, int position);

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    public void setItems(List items){
        this.items.clear();
        this.items.addAll(items);
        notifyDataSetChanged();
    }

    public void clear() {
        items.clear();
        notifyDataSetChanged();
    }
}
