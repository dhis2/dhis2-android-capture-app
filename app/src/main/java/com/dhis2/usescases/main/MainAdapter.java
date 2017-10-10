package com.dhis2.usescases.main;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dhis2.R;

import org.hisp.dhis.android.core.program.Program;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ppajuelo on 10/10/2017.
 */

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.ProgramHolder> {

    List<HomeViewModel> homeViewModels;

    public MainAdapter( List<HomeViewModel> homeViewModels) {
        this.homeViewModels = new ArrayList<>();
        this.homeViewModels.addAll(homeViewModels);
    }

    public class ProgramHolder extends RecyclerView.ViewHolder {
        TextView name;

        public ProgramHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.program_name);
        }

        public void bind(HomeViewModel program){
            name.setText(program.title());
        }
    }

    @Override
    public ProgramHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.item_program, parent, false);
        return new ProgramHolder(v);
    }

    @Override
    public void onBindViewHolder(ProgramHolder holder, int position) {
        holder.bind(getItem(position));
    }

    @Override
    public int getItemCount() {
        return homeViewModels.size();
    }

    public HomeViewModel getItem(int position){
        return homeViewModels.get(position);
    }

    void swapData(@Nullable List<HomeViewModel> homeViewModels) {
        this.homeViewModels.clear();

        if (homeViewModels != null) {
            this.homeViewModels.addAll(homeViewModels);
        }

        notifyDataSetChanged();
    }
}
