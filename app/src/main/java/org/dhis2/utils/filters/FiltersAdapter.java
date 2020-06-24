package org.dhis2.utils.filters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;
import androidx.recyclerview.widget.RecyclerView;

import org.dhis2.data.tuples.Pair;
import org.dhis2.databinding.ItemFilterAssignedBinding;
import org.dhis2.databinding.ItemFilterCatOptCombBinding;
import org.dhis2.databinding.ItemFilterOrgUnitBinding;
import org.dhis2.databinding.ItemFilterPeriodBinding;
import org.dhis2.databinding.ItemFilterSortingBinding;
import org.dhis2.databinding.ItemFilterStateBinding;
import org.dhis2.databinding.ItemFilterStatusBinding;
import org.hisp.dhis.android.core.category.CategoryCombo;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;

import java.util.ArrayList;
import java.util.List;

public class FiltersAdapter extends RecyclerView.Adapter<FilterHolder> {

    private final ProgramType programType;

    public enum ProgramType {ALL, EVENT, TRACKER, DATASET, DASHBOARD}

    private List<Filters> filtersList;
    private ObservableField<Filters> openedFilter;
    private Pair<CategoryCombo, List<CategoryOptionCombo>> catCombData;

    public FiltersAdapter(ProgramType programType) {
        this.filtersList = new ArrayList<>();
        this.programType = programType;
        filtersList.add(Filters.SORTING);
        filtersList.add(Filters.PERIOD);
        filtersList.add(Filters.ORG_UNIT);
        filtersList.add(Filters.SYNC_STATE);
        openedFilter = new ObservableField<>();
    }

    @NonNull
    @Override
    public FilterHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (Filters.values()[viewType]) {
            case SORTING:
                return new SortingFilterHolder(ItemFilterSortingBinding.inflate(inflater, parent, false), programType, openedFilter);
            case PERIOD:
                return new PeriodFilterHolder(ItemFilterPeriodBinding.inflate(inflater, parent, false), openedFilter);
            case ORG_UNIT:
                return new OrgUnitFilterHolder(ItemFilterOrgUnitBinding.inflate(inflater, parent, false), openedFilter);
            case SYNC_STATE:
                return new SyncStateFilterHolder(ItemFilterStateBinding.inflate(inflater, parent, false), openedFilter);
            case CAT_OPT_COMB:
                return new CatOptCombFilterHolder(ItemFilterCatOptCombBinding.inflate(inflater, parent, false), openedFilter, catCombData);
            case EVENT_STATUS:
                return new StatusEventFilterHolder(ItemFilterStatusBinding.inflate(inflater, parent, false), openedFilter, programType);
            case ASSIGNED_TO_ME:
                return new AssignToMeFilterHolder(ItemFilterAssignedBinding.inflate(inflater, parent, false), openedFilter);
            default:
                throw new IllegalArgumentException("Unsupported filter value");
        }
    }

    @Override
    public void onBindViewHolder(@NonNull FilterHolder holder, int position) {
        holder.bind();
    }

    @Override
    public int getItemCount() {
        return filtersList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return filtersList.get(position).ordinal();
    }

    public void addCatOptCombFilter(Pair<CategoryCombo, List<CategoryOptionCombo>> categoryOptionCombos) {
        if (!filtersList.contains(Filters.CAT_OPT_COMB)) {
            filtersList.add(Filters.CAT_OPT_COMB);
            this.catCombData = categoryOptionCombos;
            notifyDataSetChanged();
        }
    }

    public void addEventStatus() {
        if (!filtersList.contains(Filters.EVENT_STATUS)) {
            filtersList.add(Filters.EVENT_STATUS);
            notifyDataSetChanged();
        }
    }

    public void addAssignedToMe() {
        if (!filtersList.contains(Filters.ASSIGNED_TO_ME)) {
            filtersList.add(Filters.ASSIGNED_TO_ME);
            notifyDataSetChanged();
        }
    }

    public void removeAssignedToMe() {
        if (filtersList.contains(Filters.ASSIGNED_TO_ME)) {
            filtersList.remove(Filters.ASSIGNED_TO_ME);
        }
        FilterManager.getInstance().clearAssignToMe();
        notifyDataSetChanged();
    }

}
