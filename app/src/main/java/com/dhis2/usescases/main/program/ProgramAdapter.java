package com.dhis2.usescases.main.program;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.dhis2.R;
import com.dhis2.databinding.ItemProgramBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ppajuelo on 18/10/2017.
 */

public class ProgramAdapter extends RecyclerView.Adapter<ProgramViewHolder> {

    List<HomeViewModel> itemList;
    ProgramPresenter presenter;

    public ProgramAdapter(ProgramPresenter presenter) {
        this.presenter = presenter;
        this.itemList = new ArrayList<>();
    }

    @Override
    public ProgramViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemProgramBinding itemBinding = DataBindingUtil.inflate(inflater, R.layout.item_program, parent, false);
        return new ProgramViewHolder(itemBinding);
    }

    @Override
    public void onBindViewHolder(ProgramViewHolder holder, int position) {
        holder.bind(presenter, getItemAt(position));

    }

    public void setData(List<HomeViewModel> program) {
        this.itemList.clear();
        this.itemList.addAll(program);
        notifyDataSetChanged();
    }

    private HomeViewModel getItemAt(int position) {
        return itemList.get(position);
    }

    @Override
    public int getItemCount() {
        return itemList != null ? itemList.size() : 0;
    }
}
