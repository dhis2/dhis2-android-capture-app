package com.dhis2.usescases.main.program;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.dhis2.R;
import com.dhis2.databinding.ItemProgramBinding;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

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
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        Collections.sort(this.itemList, (ob1, ob2) -> {
            Date date1 = calendar.getTime();
            Date date2 = calendar.getTime();
            try {
                calendar.setTime(format.parse(ob1.lastUpdated()));
                date1 = calendar.getTime();
                calendar.setTime(format.parse(ob2.lastUpdated()));
                date2 = calendar.getTime();
            } catch (ParseException e) {
                Timber.e(e);
            }

            return date2.compareTo(date1);
        });
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
