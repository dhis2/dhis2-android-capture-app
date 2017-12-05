package com.dhis2.utils.CustomViews;

import android.databinding.DataBindingUtil;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.dhis2.R;
import com.dhis2.databinding.ItemDateBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by ppajuelo on 05/12/2017.
 */

public class DateAdapter extends RecyclerView.Adapter<DateViewHolder> {

    private List<String> datesNames = new ArrayList<>();
    private List<Date> dates = new ArrayList<>();
    private List<Date> selectedDates = new ArrayList<>();
    private Period currentPeriod = Period.WEEKLY;
    private SimpleDateFormat weeklyFormat = new SimpleDateFormat("'Week' w 'of' yyyy", Locale.getDefault());
    private SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
    private SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());

    public enum Period {
        DAILY, WEEKLY, MONTHLY, YEARLY
    }

    public DateAdapter(Period period) {
        currentPeriod = period;
        Calendar calendar = Calendar.getInstance();

        do {
            String date = null;

            switch (period) {
                case WEEKLY:
                    date = weeklyFormat.format(calendar.getTime());
                    datesNames.add(date);
                    dates.add(calendar.getTime());
                    calendar.add(Calendar.WEEK_OF_YEAR, -1);
                    break;
                case MONTHLY:
                    date = monthFormat.format(calendar.getTime());
                    datesNames.add(date);
                    dates.add(calendar.getTime());
                    calendar.add(Calendar.MONTH, -1);
                    break;
                case YEARLY:
                    date = yearFormat.format(calendar.getTime());
                    datesNames.add(date);
                    dates.add(calendar.getTime());
                    calendar.add(Calendar.YEAR, -1);
                    break;
            }

        } while (calendar.get(Calendar.YEAR) > 2000);

    }

    @Override
    public DateViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemDateBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_date, parent, false);
        return new DateViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(DateViewHolder holder, int position) {
        holder.bind(datesNames.get(position));

        if (selectedDates.contains(dates.get(position))) {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.white_dfd));
        } else {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.transparent));
        }

        holder.itemView.setOnClickListener(view -> {
            if (!selectedDates.contains(dates.get(position))) {
                selectedDates.add(dates.get(position));
                holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.white_dfd));
            } else {
                selectedDates.remove(dates.get(position));
                holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.transparent));
            }
        });
    }

    @Override
    public int getItemCount() {
        return datesNames != null ? datesNames.size() : 0;
    }
}
