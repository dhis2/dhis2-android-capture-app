package org.dhis2.utils.customviews;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import org.dhis2.R;
import org.dhis2.data.tuples.Pair;
import org.dhis2.databinding.ItemDateBinding;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.Period;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * QUADRAM. Created by ppajuelo on 05/12/2017.
 */

public class DateAdapter extends RecyclerView.Adapter<DateViewHolder> {

    private Period currentPeriod = null;
    private List<String> datesNames = new ArrayList<>();
    private List<String> seletedDatesName = new ArrayList<>();
    private List<Date> dates = new ArrayList<>();
    private Pair<Period, List<Date>> selectedDates;
    private SimpleDateFormat dayFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
    private SimpleDateFormat weeklyFormat = new SimpleDateFormat("'Week' w", Locale.getDefault());
    private String weeklyFormatWithDates = "%s, %s / %s";
    private SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
    private SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());
    private Map<String, String> mapPeriod = new HashMap<>();

    public DateAdapter(Period period) {
        //if (period != Period.DAILY)
            initData(period);
    }

    private void initData(Period period) {
        this.currentPeriod = period;
        selectedDates = Pair.create(currentPeriod, new ArrayList<>());
        Calendar calendar = DateUtils.getInstance().getCalendar();
        calendar.add(Calendar.YEAR, 1); //let's the user select dates in the next year
        int year = calendar.get(Calendar.YEAR);
        datesNames.clear();
        dates.clear();
        do {
            String date = null;

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
            }

        } while (calendar.get(Calendar.YEAR) > year - 11); //show last 10 years

        notifyDataSetChanged();
    }

    public DateAdapter() {
        currentPeriod = Period.WEEKLY;
    }

    public void swapMapPeriod(Map<String, String> mapPeriods) {
        this.mapPeriod = mapPeriods;
        for (Map.Entry<String, String> entry : mapPeriods.entrySet())
            datesNames.add(entry.getValue());
    }

    @Override
    public DateViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemDateBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_date, parent, false);
        return new DateViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(DateViewHolder holder, int position) {
        holder.bind(datesNames.get(position));

        if ((dates.size() > 0 && selectedDates.val1().contains(dates.get(position))) || (datesNames.size() > 0 && seletedDatesName.contains(datesNames.get(position)))) {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.colorPrimaryLight));
        } else {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.white));
        }

        holder.itemView.setOnClickListener(view -> {
            if (mapPeriod == null || mapPeriod.size() == 0) {
                if (!selectedDates.val1().contains(dates.get(position))) {
                    selectedDates.val1().add(dates.get(position));
                    holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.colorPrimaryLight));
                } else {
                    selectedDates.val1().remove(dates.get(position));
                    holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.white));
                }
            } else {
                if (!seletedDatesName.contains(datesNames.get(position))) {
                    seletedDatesName.add(datesNames.get(position));
                    holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.colorPrimaryLight));
                } else {
                    seletedDatesName.remove(datesNames.get(position));
                    holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.white));
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        if (mapPeriod != null && mapPeriod.size() > 0)
            return mapPeriod.size();
        return datesNames != null ? datesNames.size() : 0;
    }

    public Pair<Period, List<Date>> clearFilters() {
        selectedDates.val1().clear();
        return selectedDates;
    }

    public List<String> clearFiltersPeriod() {
        seletedDatesName.clear();
        return seletedDatesName;
    }

    public Pair<Period, List<Date>> getSelectedDates() {
        return selectedDates;
    }

    public List<String> getSeletedDatesName() {
        return seletedDatesName;
    }


    public Period swapPeriod(boolean next) {
        Period period = currentPeriod;
        switch (currentPeriod) {
            case DAILY:
                if(next) {
                    period = Period.WEEKLY;
                    initData(period);
                }
                else {
                    period = Period.YEARLY;
                    initData(period);
                }
                break;
            case WEEKLY:
                if(next) {
                    period = Period.MONTHLY;
                    initData(period);
                }
                else{
                    this.currentPeriod = Period.DAILY;
                    return currentPeriod;
                }
                break;
            case MONTHLY:
                if(next) {
                    period = Period.YEARLY;
                    initData(period);
                }
                else {
                    period = Period.WEEKLY;
                    initData(period);
                }
                break;
            case YEARLY:
                if(next) {
                    this.currentPeriod = Period.DAILY;
                    return currentPeriod;
                }
                else {
                    period = Period.MONTHLY;
                    initData(period);
                }
                break;
        }
        return period;
    }
}
