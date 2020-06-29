package org.dhis2.utils.filters;

import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.databinding.ObservableField;

import org.dhis2.R;
import org.dhis2.databinding.FilterPeriodBinding;
import org.dhis2.databinding.ItemFilterPeriodBinding;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.Period;
import org.dhis2.utils.filters.sorting.SortingItem;
import org.hisp.dhis.android.core.period.DatePeriod;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

class PeriodFilterHolder extends FilterHolder implements CompoundButton.OnCheckedChangeListener {

    private ItemFilterPeriodBinding localBinding;

    PeriodFilterHolder(@NonNull ItemFilterPeriodBinding binding, ObservableField<Filters> openedFilter, ObservableField<SortingItem> sortingItem, FiltersAdapter.ProgramType programType) {
        super(binding, openedFilter, sortingItem);
        localBinding = binding;
        filterType = Filters.PERIOD;
        this.programType = programType;
    }

    @Override
    public void bind() {
        super.bind();
        filterIcon.setImageDrawable(AppCompatResources.getDrawable(itemView.getContext(), R.drawable.ic_calendar_positive));
        filterTitle.setText(R.string.filters_title_period);

        setListeners(localBinding.periodLayout);

        switch (FilterManager.getInstance().getPeriodIdSelected()) {
            case R.id.today:
                localBinding.periodLayout.today.setChecked(true);
                break;
            case R.id.yesterday:
                localBinding.periodLayout.yesterday.setChecked(true);
                break;
            case R.id.tomorrow:
                localBinding.periodLayout.tomorrow.setChecked(true);
                break;
            case R.id.this_week:
                localBinding.periodLayout.thisWeek.setChecked(true);
                break;
            case R.id.last_week:
                localBinding.periodLayout.lastWeek.setChecked(true);
                break;
            case R.id.next_week:
                localBinding.periodLayout.nextWeek.setChecked(true);
                break;
            case R.id.this_month:
                localBinding.periodLayout.thisMonth.setChecked(true);
                break;
            case R.id.last_month:
                localBinding.periodLayout.lastMonth.setChecked(true);
                break;
            case R.id.next_month:
                localBinding.periodLayout.nextMonth.setChecked(true);
                break;
            case R.id.fromTo:
                localBinding.periodLayout.fromTo.setChecked(true);
                localBinding.periodLayout.other.setChecked(false);
                break;
            case R.id.other:
                localBinding.periodLayout.fromTo.setChecked(false);
                localBinding.periodLayout.other.setChecked(true);
                break;
            default:
                localBinding.periodLayout.anytime.setChecked(true);
        }
    }

    private void setListeners(FilterPeriodBinding periodLayout) {
        periodLayout.today.setOnCheckedChangeListener(this);
        periodLayout.yesterday.setOnCheckedChangeListener(this);
        periodLayout.tomorrow.setOnCheckedChangeListener(this);
        periodLayout.thisWeek.setOnCheckedChangeListener(this);
        periodLayout.lastWeek.setOnCheckedChangeListener(this);
        periodLayout.nextWeek.setOnCheckedChangeListener(this);
        periodLayout.thisMonth.setOnCheckedChangeListener(this);
        periodLayout.lastMonth.setOnCheckedChangeListener(this);
        periodLayout.nextMonth.setOnCheckedChangeListener(this);
        periodLayout.fromTo.setOnClickListener(view -> {
            if (periodLayout.fromTo.isChecked()) {
                int id = R.id.fromTo;
                updateSelection(id);
                FilterManager.getInstance().addPeriodRequest(FilterManager.PeriodRequest.FROM_TO, Filters.PERIOD);
                if (id != FilterManager.getInstance().getPeriodIdSelected()) {
                    FilterManager.getInstance().setPeriodIdSelected(id);
                }
            }
        });
        periodLayout.other.setOnClickListener(view -> {
            if (periodLayout.other.isChecked()) {
                int id = R.id.other;
                updateSelection(id);
                FilterManager.getInstance().addPeriodRequest(FilterManager.PeriodRequest.OTHER, Filters.PERIOD);
                if (id != FilterManager.getInstance().getPeriodIdSelected()) {
                    FilterManager.getInstance().setPeriodIdSelected(id);
                }
            }
        });
        periodLayout.anytime.setOnCheckedChangeListener(this);
    }


    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (b) {
            int id = compoundButton.getId();

            updateSelection(id);

            if (id != R.id.other && id != R.id.fromTo) {
                Date[] dates = null;
                Calendar calendar = Calendar.getInstance();
                switch (id) {
                    case R.id.today:
                        dates = DateUtils.getInstance().getDateFromDateAndPeriod(calendar.getTime(), Period.DAILY);
                        break;
                    case R.id.yesterday:
                        calendar.add(Calendar.DAY_OF_YEAR, -1);
                        dates = DateUtils.getInstance().getDateFromDateAndPeriod(calendar.getTime(), Period.DAILY);
                        break;
                    case R.id.tomorrow:
                        calendar.add(Calendar.DAY_OF_YEAR, 1);
                        dates = DateUtils.getInstance().getDateFromDateAndPeriod(calendar.getTime(), Period.DAILY);
                        break;
                    case R.id.this_week:
                        dates = DateUtils.getInstance().getDateFromDateAndPeriod(calendar.getTime(), Period.WEEKLY);
                        break;
                    case R.id.last_week:
                        calendar.add(Calendar.WEEK_OF_YEAR, -1);
                        dates = DateUtils.getInstance().getDateFromDateAndPeriod(calendar.getTime(), Period.WEEKLY);
                        break;
                    case R.id.next_week:
                        calendar.add(Calendar.WEEK_OF_YEAR, 1);
                        dates = DateUtils.getInstance().getDateFromDateAndPeriod(calendar.getTime(), Period.WEEKLY);
                        break;
                    case R.id.this_month:
                        dates = DateUtils.getInstance().getDateFromDateAndPeriod(calendar.getTime(), Period.MONTHLY);
                        break;
                    case R.id.last_month:
                        calendar.add(Calendar.MONTH, -1);
                        dates = DateUtils.getInstance().getDateFromDateAndPeriod(calendar.getTime(), Period.MONTHLY);
                        break;
                    case R.id.next_month:
                        calendar.add(Calendar.MONTH, 1);
                        dates = DateUtils.getInstance().getDateFromDateAndPeriod(calendar.getTime(), Period.MONTHLY);
                        break;

                }
                if (dates != null)
                    FilterManager.getInstance().addPeriod(Collections.singletonList(DatePeriod.builder().startDate(dates[0]).endDate(dates[1]).build()));
                else
                    FilterManager.getInstance().addPeriod(null);
            }

            if (id != FilterManager.getInstance().getPeriodIdSelected()) {
                FilterManager.getInstance().setPeriodIdSelected(id);
            }
        }
    }

    private void updateSelection(int id) {

        localBinding.periodLayout.today.setChecked(id == R.id.today);
        localBinding.periodLayout.yesterday.setChecked(id == R.id.yesterday);
        localBinding.periodLayout.tomorrow.setChecked(id == R.id.tomorrow);
        localBinding.periodLayout.thisWeek.setChecked(id == R.id.this_week);
        localBinding.periodLayout.lastWeek.setChecked(id == R.id.last_week);
        localBinding.periodLayout.nextWeek.setChecked(id == R.id.next_week);
        localBinding.periodLayout.thisMonth.setChecked(id == R.id.this_month);
        localBinding.periodLayout.lastMonth.setChecked(id == R.id.last_month);
        localBinding.periodLayout.nextMonth.setChecked(id == R.id.next_month);
        localBinding.periodLayout.fromTo.setChecked(id == R.id.fromTo);
        localBinding.periodLayout.other.setChecked(id == R.id.other);
        localBinding.periodLayout.anytime.setChecked(id == R.id.anytime);
    }
}
