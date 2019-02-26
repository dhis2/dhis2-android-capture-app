package org.dhis2.utils.custom_views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.dhis2.R;
import org.dhis2.databinding.ItemDateBinding;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.Period;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import static org.dhis2.utils.DateUtils.DATE_FORMAT_YEAR;

/**
 * QUADRAM. Created by ppajuelo on 05/12/2017.
 */

public class DateAdapter extends RecyclerView.Adapter<DateViewHolder> {

    private List<String> datesNames = new ArrayList<>();
    private List<Date> dates = new ArrayList<>();
    private List<Date> selectedDates = new ArrayList<>();


    public DateAdapter(Period period, Context context) {
        Calendar calendar = DateUtils.getInstance().getCalendar();
        calendar.add(Calendar.YEAR, 1); //let's the user select dates in the next year
        int year = calendar.get(Calendar.YEAR);

        do {
            String date = null;

            SimpleDateFormat dayFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            String week = context.getString(R.string.week);
            SimpleDateFormat weeklyFormat = new SimpleDateFormat("'" + week + "' w", Locale.getDefault());
            String weeklyFormatWithDates = "%s, %s / %s";
            SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
            SimpleDateFormat yearFormat = new SimpleDateFormat(DATE_FORMAT_YEAR, Locale.getDefault());
            switch (period) {
                case WEEKLY:
                    date = weeklyFormat.format(calendar.getTime()); //Get current week

                    calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek()); //set calendar to firstday of the week
                    String firstDay = dayFormat.format(calendar.getTime());

                    calendar.add(Calendar.WEEK_OF_YEAR, 1); //Move to next week
                    calendar.add(Calendar.DAY_OF_MONTH, -1);//Substract one day to get last day of current week
                    String lastDay = dayFormat.format(calendar.getTime());

                    calendar.add(Calendar.DAY_OF_MONTH, 1); //Move back to current date
                    calendar.add(Calendar.WEEK_OF_YEAR, -1);

                    datesNames.add(String.format(weeklyFormatWithDates, date, firstDay, lastDay));
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
                default:
                    break;
            }

        } while (calendar.get(Calendar.YEAR) > year - 11); //show last 10 years

    }

    @NonNull
    @Override
    public DateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDateBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_date, parent, false);
        return new DateViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DateViewHolder holder, int position) {
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

    public List<Date> clearFilters() {
        selectedDates.clear();
        return selectedDates;
    }

    public List<Date> getSelectedDates() {
        return selectedDates;
    }
}
