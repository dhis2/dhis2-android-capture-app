package org.dhis2.utils.filters;

import androidx.databinding.ObservableField;

import org.hisp.dhis.android.core.arch.helpers.UidsHelper;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.period.DatePeriod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

public class FilterManager {

    public static final int OU_TREE = 1986;

    private List<OrganisationUnit> ouFilters;
    private List<State> stateFilters;
    private DatePeriod periodFilters;

    private ObservableField<Integer> ouFiltersApplied;
    private ObservableField<Integer> stateFiltersApplyed;
    private ObservableField<Integer> periodFiltersApplyed;

    private FlowableProcessor<FilterManager> filterProcessor;
    private FlowableProcessor<Boolean> ouTreeProcessor;

    private static FilterManager instance;

    public static FilterManager getInstance() {
        if (instance == null)
            instance = new FilterManager();
        return instance;
    }

    private FilterManager() {
        reset();
    }

    public static void clearAll() {
        instance = null;
    }

    public void reset() {
        ouFilters = new ArrayList<>();
        stateFilters = new ArrayList<>();
        ouFiltersApplied = new ObservableField<>(0);
        stateFiltersApplyed = new ObservableField<>(0);
        periodFiltersApplyed = new ObservableField<>(0);
        filterProcessor = PublishProcessor.create();
        ouTreeProcessor = PublishProcessor.create();
    }

    public void addState(State... states) {
        for (State stateToAdd : states) {
            if (stateFilters.contains(stateToAdd))
                stateFilters.remove(stateToAdd);
            else
                stateFilters.add(stateToAdd);
        }
        stateFiltersApplyed.set(stateFilters.size());
        filterProcessor.onNext(this);
    }


    public void addPeriod(DatePeriod datePeriod) {
        this.periodFilters = datePeriod;

        periodFiltersApplyed.set(datePeriod != null ? 1 : 0);
        filterProcessor.onNext(this);
    }

    public void addOrgUnit(OrganisationUnit ou) {

        if (ouFilters.contains(ou))
            ouFilters.remove(ou);
        else
            ouFilters.add(ou);

        ouFiltersApplied.set(ouFilters.size());
        filterProcessor.onNext(this);
    }


    public ObservableField<Integer> observeField(Filters filter) {
        switch (filter) {
            case ORG_UNIT:
                return ouFiltersApplied;
            case SYNC_STATE:
                return stateFiltersApplyed;
            case PERIOD:
                return periodFiltersApplyed;
            default:
                return new ObservableField<>(0);
        }
    }

    public FlowableProcessor<Boolean> getOuTreeProcessor(){
        return ouTreeProcessor;
    }

    public Flowable<FilterManager> asFlowable() {
        return filterProcessor;
    }

    public Flowable<Boolean> ouTreeFlowable() {
        return ouTreeProcessor;
    }

    public int getTotalFilters() {
        int ouIsApplying = ouFilters.isEmpty() ? 0 : 1;
        int stateIsApplying = stateFilters.isEmpty() ? 0 : 1;
        int periodIsApplying = periodFilters == null ? 0 : 1;
        return ouIsApplying + stateIsApplying + periodIsApplying;
    }

    public List<DatePeriod> getPeriodFilters() {
        return periodFilters != null ? Collections.singletonList(periodFilters) : new ArrayList<>();
    }

    public List<OrganisationUnit> getOrgUnitFilters() {
        return ouFilters;
    }

    public List<String> getOrgUnitUidsFilters() {
        return UidsHelper.getUidsList(ouFilters);
    }

    public List<State> getStateFilters() {
        return stateFilters;
    }

    public void removeAll() {
        ouFilters = new ArrayList<>();
        ouFiltersApplied.set(ouFilters.size());
        filterProcessor.onNext(this);
    }
}
