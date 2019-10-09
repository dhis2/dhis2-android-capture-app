package org.dhis2.utils.custom_views;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import org.dhis2.R;
import org.dhis2.databinding.ItemDateBinding;
import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.period.PeriodType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PeriodAdapter extends RecyclerView.Adapter<DateViewHolder> {

    private PeriodType periodType;
    private Integer openFuturePeriods;
    private List<Date> datePeriods;
    private PeriodDialog.OnDateSet onDateSetListener;
    private final int DEFAULT_PERIODS_SIZE = 10;
    private Date lastDate;

    public PeriodAdapter(PeriodType periodType, Integer openFuturePeriods) {
        this.periodType = periodType;
        this.openFuturePeriods = openFuturePeriods;
        datePeriods = new ArrayList<>();
        lastDate = DateUtils.getInstance().getToday();
        setDates();
    }

    @Override
    public DateViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemDateBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_date, parent, false);
        return new DateViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(DateViewHolder holder, int position) {
        if(position == datePeriods.size()){
            holder.bind(holder.itemView.getContext().getString(R.string.view_more));
            holder.itemView.setOnClickListener(v -> {
                setDates();
                notifyDataSetChanged();
            });
        }else{
            holder.bind(DateUtils.getInstance().getPeriodUIString(periodType, datePeriods.get(position), Locale.getDefault()));
            holder.itemView.setOnClickListener(v -> {
                if (onDateSetListener != null)
                    onDateSetListener.onDateSet(datePeriods.get(holder.getAdapterPosition()));
            });
        }
    }

    private void setDates() {
        lastDate = DateUtils.getInstance().getNextPeriod(periodType, lastDate, openFuturePeriods - 1);
        datePeriods.add(lastDate);
        for (int i = 1; i < DEFAULT_PERIODS_SIZE; i++) {
            lastDate = DateUtils.getInstance().getNextPeriod(periodType, lastDate, -1);
            datePeriods.add(lastDate);
        }
    }

    @Override
    public int getItemCount() {
        return datePeriods.size() +1;
    }

    public void setOnDateSetListener(PeriodDialog.OnDateSet onDateSetListener) {
        this.onDateSetListener = onDateSetListener;
    }
}

