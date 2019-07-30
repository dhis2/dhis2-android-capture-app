package org.dhis2.utils.filters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableField;
import androidx.recyclerview.widget.RecyclerView;

import org.dhis2.R;
import org.dhis2.databinding.ItemFilterBinding;
import org.dhis2.databinding.ItemFilterOrgUnitBinding;
import org.dhis2.databinding.ItemFilterPeriodBinding;
import org.dhis2.databinding.ItemFilterStateBinding;

import java.util.ArrayList;
import java.util.List;

public class FiltersAdapter extends RecyclerView.Adapter<FilterHolder> {

    private List<Filters> filtersList;
    private ObservableField<Filters> openedFilter;

    public FiltersAdapter() {
        this.filtersList = new ArrayList<>();
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
            case PERIOD:
                return new PeriodFilterHolder(ItemFilterPeriodBinding.inflate(inflater, parent, false), openedFilter);
            case ORG_UNIT:
                return new OrgUnitFilterHolder(ItemFilterOrgUnitBinding.inflate(inflater, parent, false), openedFilter);
            case SYNC_STATE:
            default:
                return new SyncStateFilterHolder(ItemFilterStateBinding.inflate(inflater, parent, false), openedFilter);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull FilterHolder holder, int position) {
        holder.bind();
    }

    @Override
    public int getItemCount() {
        return filtersList.size(); //TODO: Should change depending on the screen
    }

    @Override
    public int getItemViewType(int position) {
        return filtersList.get(position).ordinal();
    }
}
