package org.dhis2.commons.filters;

import androidx.annotation.Nullable;
import androidx.databinding.ObservableField;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.dhis2.commons.R;
import org.dhis2.commons.filters.cat_opt_comb.CatOptCombFilterAdapter;
import org.dhis2.commons.filters.data.EmptyWorkingList;
import org.dhis2.commons.filters.data.FilterStateExtensionsKt;
import org.dhis2.commons.filters.data.WorkingListScope;
import org.dhis2.commons.filters.sorting.SortingItem;
import org.dhis2.commons.filters.sorting.SortingStatus;
import org.dhis2.commons.filters.workingLists.WorkingListItem;
import org.dhis2.commons.resources.ResourceManager;
import org.hisp.dhis.android.core.arch.helpers.UidsHelper;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.period.DatePeriod;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import io.reactivex.Flowable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import kotlin.Pair;
import kotlin.collections.CollectionsKt;

public class FilterManager implements Serializable {

    public void publishData() {
        filterProcessor.onNext(this);
    }

    public void setCatComboAdapter(CatOptCombFilterAdapter adapter) {
        this.catComboAdapter = adapter;
    }

    public enum PeriodRequest {
        FROM_TO, OTHER
    }

    private ObservableField<Integer> periodIdSelected = new ObservableField<>(R.id.anytime);
    private ObservableField<Integer> enrollmentPeriodIdSelected = new ObservableField<>(R.id.anytime);

    private CatOptCombFilterAdapter catComboAdapter;

    private List<OrganisationUnit> ouFilters;
    private MutableLiveData<List<OrganisationUnit>> liveDataOUFilter = new MutableLiveData<>();
    private List<State> stateFilters;
    private MutableLiveData<List<State>> observableStates = new MutableLiveData<>();
    private List<DatePeriod> periodFilters;
    private ObservableField<List<DatePeriod>> observablePeriodFilters = new ObservableField<>();
    private ObservableField<InternalError> observablePeriodId = new ObservableField<>();
    private List<DatePeriod> enrollmentPeriodFilters;
    private List<CategoryOptionCombo> catOptComboFilters;
    private List<EventStatus> eventStatusFilters;
    private ObservableField<List<EventStatus>> observableEventStatus = new ObservableField<>();
    private List<EnrollmentStatus> enrollmentStatusFilters;
    private ObservableField<EnrollmentStatus> observableEnrollmentStatus = new ObservableField<>();
    private boolean assignedFilter;
    private ObservableField<Boolean> observableAssignedToMe = new ObservableField<>();
    private SortingItem sortingItem;
    private boolean followUpFilter;
    private ObservableField<Boolean> observableFollowUp = new ObservableField<>();

    private ArrayList<Filters> unsupportedFilters = new ArrayList<>();

    private ObservableField<Integer> ouFiltersApplied;
    private ObservableField<Integer> stateFiltersApplied;
    private ObservableField<Integer> periodFiltersApplied;
    private ObservableField<Integer> enrollmentPeriodFiltersApplied;
    private ObservableField<Integer> catOptCombFiltersApplied;
    private ObservableField<Integer> eventStatusFiltersApplied;
    private ObservableField<Integer> enrollmentStatusFiltersApplied;
    private ObservableField<Integer> assignedToMeApplied;
    private ObservableField<Integer> followUpFilterApplied;

    private List<String> stateValues = new ArrayList<>();

    private ObservableField<WorkingListScope> currentWorkingListScope = new ObservableField<>(
            new EmptyWorkingList()
    );

    private FlowableProcessor<FilterManager> filterProcessor;
    private FlowableProcessor<Boolean> ouTreeProcessor;
    private FlowableProcessor<Pair<PeriodRequest, Filters>> periodRequestProcessor;
    private FlowableProcessor<String> catOptComboRequestProcessor;

    private WorkingListItem currentWorkingList;

    private ResourceManager resourceManager;

    private static FilterManager instance;

    public static FilterManager getInstance() {
        if (instance == null)
            instance = new FilterManager();
        return instance;
    }

    public static FilterManager initWith(ResourceManager resourceManager) {
        if (instance == null)
            instance = new FilterManager(resourceManager);
        return instance;
    }

    private FilterManager(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
        reset();
    }

    private FilterManager() {
        reset();
    }

    public static void clearAll() {
        instance = null;
    }

    public void reset() {
        catComboAdapter = null;

        ouFilters = new ArrayList<>();
        stateFilters = new ArrayList<>();
        periodFilters = new ArrayList<>();
        enrollmentPeriodFilters = new ArrayList<>();
        catOptComboFilters = new ArrayList<>();
        eventStatusFilters = new ArrayList<>();
        enrollmentStatusFilters = new ArrayList<>();
        assignedFilter = false;
        followUpFilter =false;
        sortingItem = null;

        ouFiltersApplied = new ObservableField<>(0);
        stateFiltersApplied = new ObservableField<>(0);
        periodFiltersApplied = new ObservableField<>(0);
        enrollmentPeriodFiltersApplied = new ObservableField<>(0);
        catOptCombFiltersApplied = new ObservableField<>(0);
        eventStatusFiltersApplied = new ObservableField<>(0);
        enrollmentStatusFiltersApplied = new ObservableField<>(0);
        assignedToMeApplied = new ObservableField<>(0);
        followUpFilterApplied = new ObservableField<>(0);

        filterProcessor = PublishProcessor.create();
        ouTreeProcessor = PublishProcessor.create();
        periodRequestProcessor = PublishProcessor.create();
        catOptComboRequestProcessor = PublishProcessor.create();
    }

    public FilterManager copy() {
        FilterManager copy = new FilterManager();
        copy.ouFilters = new ArrayList<>(getOrgUnitFilters());
        copy.stateFilters = new ArrayList<>(getStateFilters());
        copy.periodFilters = new ArrayList<>(getPeriodFilters());
        copy.enrollmentPeriodFilters = new ArrayList<>(getEnrollmentPeriodFilters());
        copy.catOptComboFilters = new ArrayList<>(getCatOptComboFilters());
        copy.eventStatusFilters = new ArrayList<>(getEventStatusFilters());
        copy.enrollmentStatusFilters = new ArrayList<>(getEnrollmentStatusFilters());
        copy.assignedFilter = getAssignedFilter();
        copy.followUpFilter = getFollowUpFilter();
        copy.sortingItem = getSortingItem();
        return copy;
    }

    public boolean sameFilters(FilterManager filterManager) {
        return Objects.equals(filterManager.ouFilters, this.ouFilters) &&
                Objects.equals(filterManager.stateFilters, this.stateFilters) &&
                Objects.equals(filterManager.periodFilters, this.periodFilters) &&
                Objects.equals(filterManager.enrollmentPeriodFilters, this.enrollmentPeriodFilters) &&
                Objects.equals(filterManager.catOptComboFilters, this.catOptComboFilters) &&
                Objects.equals(filterManager.eventStatusFilters, this.eventStatusFilters) &&
                Objects.equals(filterManager.enrollmentStatusFilters, this.enrollmentStatusFilters) &&
                filterManager.assignedFilter == this.assignedFilter &&
                filterManager.followUpFilter == this.followUpFilter &&
                Objects.equals(filterManager.sortingItem, this.sortingItem);
    }

    public ObservableField<Integer> getPeriodIdSelected() {
        return this.periodIdSelected;
    }

    public ObservableField<Integer> getEnrollmentPeriodIdSelected() {
        return this.enrollmentPeriodIdSelected;
    }

//    region STATE FILTERS

    public void addState(boolean remove, State... states) {
        stateValues = new ArrayList<>();
        for (State stateToAdd : states) {
            String value = FilterStateExtensionsKt.toStringValue(stateToAdd, resourceManager);
            if (remove) {
                stateFilters.remove(stateToAdd);
                stateValues.remove(value);
            } else if (!stateFilters.contains(stateToAdd)) {
                stateFilters.add(stateToAdd);
                stateValues.add(value);
            }
        }
        observableStates.postValue(stateFilters);

        boolean hasNotSyncedState = stateFilters.contains(State.TO_POST) &&
                stateFilters.contains(State.TO_UPDATE) &&
                stateFilters.contains(State.UPLOADING);
        boolean hasErrorState = stateFilters.contains(State.ERROR) &&
                stateFilters.contains(State.WARNING);
        boolean hasSmsState = stateFilters.contains(State.SENT_VIA_SMS) &&
                stateFilters.contains(State.SYNCED_VIA_SMS);
        int stateFiltersCount = stateFilters.size();
        if (hasNotSyncedState) {
            stateFiltersCount = stateFiltersCount - 2;
        }
        if (hasErrorState) {
            stateFiltersCount = stateFiltersCount - 1;
        }
        if (hasSmsState) {
            stateFiltersCount = stateFiltersCount - 1;
        }

        stateFiltersApplied.set(stateFiltersCount);
        publishData();
    }

//    endregion

    public void addEventStatus(boolean remove, EventStatus... status) {
        boolean changed = true;
        for (EventStatus eventStatus : status) {
            if (remove)
                eventStatusFilters.remove(eventStatus);
            else if (!eventStatusFilters.contains(eventStatus))
                eventStatusFilters.add(eventStatus);
            else {
                changed = false;
            }
        }
        if (changed) {
            observableEventStatus.set(eventStatusFilters);
            if (eventStatusFilters.contains(EventStatus.ACTIVE)) {
                eventStatusFiltersApplied.set(eventStatusFilters.size() - 1);
            } else {
                eventStatusFiltersApplied.set(eventStatusFilters.size());
            }
            publishData();
        }
    }

    public void addEnrollmentStatus(boolean remove, EnrollmentStatus enrollmentStatus) {
        boolean changed = true;
        if (remove) {
            enrollmentStatusFilters.remove(enrollmentStatus);
        } else if (!enrollmentStatusFilters.contains(enrollmentStatus)){
            enrollmentStatusFilters.clear();
            enrollmentStatusFilters.add(enrollmentStatus);
            observableEnrollmentStatus.set(enrollmentStatus);
        } else {
            changed = false;
        }
        enrollmentStatusFiltersApplied.set(enrollmentStatusFilters.size());
        if (!workingListActive() && changed)
            publishData();
    }

    public void addPeriod(List<DatePeriod> datePeriod) {
        this.periodFilters = datePeriod;
        observablePeriodFilters.set(datePeriod);
        periodFiltersApplied.set(datePeriod != null && !datePeriod.isEmpty() ? 1 : 0);
        publishData();
    }

    public void addEnrollmentPeriod(List<DatePeriod> datePeriod) {
        this.enrollmentPeriodFilters = datePeriod;

        enrollmentPeriodFiltersApplied.set(datePeriod != null && !datePeriod.isEmpty() ? 1 : 0);
        publishData();
    }

    public void addOrgUnit(OrganisationUnit ou) {

        if (ouFilters.contains(ou))
            ouFilters.remove(ou);
        else
            ouFilters.add(ou);

        liveDataOUFilter.setValue(ouFilters);
        ouFiltersApplied.set(ouFilters.size());
        publishData();
    }

    public void addOrgUnits(List<OrganisationUnit> ouList){
        ouFilters.clear();
        ouFilters.addAll(ouList);
        liveDataOUFilter.setValue(ouFilters);
        ouFiltersApplied.set(ouFilters.size());
        publishData();
    }

    public void addCatOptCombo(CategoryOptionCombo catOptCombo) {
        if (catOptComboFilters.contains(catOptCombo))
            catOptComboFilters.remove(catOptCombo);
        else
            catOptComboFilters.add(catOptCombo);

        if (catComboAdapter != null) {
            catComboAdapter.notifyDataSetChanged();
        }
        catOptCombFiltersApplied.set(catOptComboFilters.size());
        publishData();
    }


    public ObservableField<Integer> observeField(Filters filter) {
        switch (filter) {
            case ORG_UNIT:
                return ouFiltersApplied;
            case SYNC_STATE:
                return stateFiltersApplied;
            case PERIOD:
                return periodFiltersApplied;
            case ENROLLMENT_DATE:
                return enrollmentPeriodFiltersApplied;
            case CAT_OPT_COMB:
                return catOptCombFiltersApplied;
            case EVENT_STATUS:
                return eventStatusFiltersApplied;
            case ENROLLMENT_STATUS:
                return enrollmentStatusFiltersApplied;
            case ASSIGNED_TO_ME:
                return assignedToMeApplied;
            case FOLLOW_UP:
                return followUpFilterApplied;
            default:
                return new ObservableField<>(0);
        }
    }

    public FlowableProcessor<Boolean> getOuTreeProcessor() {
        return ouTreeProcessor;
    }

    public FlowableProcessor<FilterManager> asFlowableProcessor() {
        return filterProcessor;
    }

    public Flowable<FilterManager> asFlowable() {
        return filterProcessor;
    }

    public FlowableProcessor<Pair<PeriodRequest, Filters>> getPeriodRequest() {
        return periodRequestProcessor;
    }

    public FlowableProcessor<String> getCatComboRequest() {
        return catOptComboRequestProcessor;
    }

    public Flowable<Boolean> ouTreeFlowable() {
        return ouTreeProcessor;
    }

    public void setUnsupportedFilters(Filters... unsupported) {
        this.unsupportedFilters.addAll(Arrays.asList(unsupported));
    }

    public void clearUnsupportedFilters() {
        this.unsupportedFilters.clear();
    }

    public int getTotalFilters() {
        int ouIsApplying = ouFilters.isEmpty() ? 0 : 1;
        int stateIsApplying = stateFilters.isEmpty() ? 0 : 1;
        int periodIsApplying = periodFilters == null || periodFilters.isEmpty() ? 0 : 1;
        int enrollmentPeriodIsApplying = unsupportedFilters.contains(Filters.ENROLLMENT_DATE) || enrollmentPeriodFilters == null || enrollmentPeriodFilters.isEmpty() ? 0 : 1;
        int eventStatusApplying = eventStatusFilters.isEmpty() ? 0 : 1;
        int enrollmentStatusApplying = unsupportedFilters.contains(Filters.ENROLLMENT_STATUS) || enrollmentStatusFilters.isEmpty() ? 0 : 1;
        int catComboApplying = catOptComboFilters.isEmpty() ? 0 : 1;
        int assignedApplying = assignedFilter ? 1 : 0;
        int sortingIsActive = sortingItem != null ? 1 : 0;
        int workingListFilters = getTotalFilterCounterForWorkingList(currentWorkingListScope.get());
        int followUpApplying = followUpFilter ? 1 : 0;
        return ouIsApplying + stateIsApplying + periodIsApplying +
                eventStatusApplying + catComboApplying +
                assignedApplying + enrollmentPeriodIsApplying + enrollmentStatusApplying +
                sortingIsActive + workingListFilters + followUpApplying;
    }

    public List<DatePeriod> getPeriodFilters() {
        return periodFilters != null ? periodFilters : new ArrayList<>();
    }

    public List<DatePeriod> getEnrollmentPeriodFilters() {
        return enrollmentPeriodFilters != null ? enrollmentPeriodFilters : new ArrayList<>();
    }

    public List<OrganisationUnit> getOrgUnitFilters() {
        return ouFilters;
    }

    public LiveData<List<OrganisationUnit>> observeOrgUnitFilters() {
        return liveDataOUFilter;
    }

    public List<CategoryOptionCombo> getCatOptComboFilters() {
        return catOptComboFilters;
    }

    public List<String> getOrgUnitUidsFilters() {
        return UidsHelper.getUidsList(ouFilters);
    }

    public List<State> getStateFilters() {
        return stateFilters;
    }

    public LiveData<List<State>> observeSyncState() {
        return observableStates;
    }

    public List<EventStatus> getEventStatusFilters() {
        return eventStatusFilters;
    }

    public ObservableField<List<EventStatus>> observeEventStatus() {
        return observableEventStatus;
    }

    public List<EnrollmentStatus> getEnrollmentStatusFilters() {
        return enrollmentStatusFilters;
    }

    public ObservableField<EnrollmentStatus> observeEnrollmentStatus() {
        return observableEnrollmentStatus;
    }

    public void addPeriodRequest(PeriodRequest periodRequest, Filters filter) {
        periodRequestProcessor.onNext(new Pair<>(periodRequest, filter));
    }

    public void addCatOptComboRequest(String catOptComboUid) {
        catOptComboRequestProcessor.onNext(catOptComboUid);
    }

    public void removeAll() {
        ouFilters = new ArrayList<>();
        liveDataOUFilter.setValue(ouFilters);
        ouFiltersApplied.set(ouFilters.size());
        publishData();
    }

    public void addIfCan(OrganisationUnit content, boolean b) {
        if (!b) {
            if (ouFilters.contains(content)) {
                ouFilters.remove(content);
            }
        } else {
            if (ouFilters.contains(content)) {
                ouFilters.remove(content);
            }
            ouFilters.add(content);
        }
        liveDataOUFilter.setValue(ouFilters);
        ouFiltersApplied.set(ouFilters.size());
        publishData();
    }

    public boolean exist(OrganisationUnit content) {
        return ouFilters.contains(content);
    }

    public void clearCatOptCombo() {
        catOptComboFilters.clear();
        catOptCombFiltersApplied.set(catOptComboFilters.size());
    }

    public void clearEventStatus() {
        eventStatusFilters.clear();
        eventStatusFiltersApplied.set(eventStatusFilters.size());
        observableEventStatus.set(eventStatusFilters);
    }

    public void clearEnrollmentStatus() {
        enrollmentStatusFilters.clear();
        observableEnrollmentStatus.set(null);
        enrollmentStatusFiltersApplied.set(enrollmentStatusFilters.size());
    }

    public void clearAssignToMe() {
        if (assignedFilter) {
            assignedFilter = false;
            observableAssignedToMe.set(false);
            assignedToMeApplied.set(0);
        }
    }

    public void clearEnrollmentDate() {
        if (enrollmentPeriodFilters != null) {
            enrollmentPeriodFilters.clear();
        }
        enrollmentPeriodIdSelected.set(R.id.anytime);
        enrollmentPeriodFiltersApplied.set(enrollmentPeriodFilters == null ? 0 : enrollmentPeriodFilters.size());
    }

    public void clearWorkingList(boolean silently) {
        if (currentWorkingList != null) {
            currentWorkingList = null;
            setWorkingListScope(new EmptyWorkingList());
        }
        if (!silently) {
            publishData();
        }
    }

    public void clearFollowUp() {
        if (followUpFilter) {
            followUpFilter = false;
            observableFollowUp.set(false);
            followUpFilterApplied.set(0);
        }
    }

    public void clearSorting() {
        sortingItem = null;
    }

    public void clearPeriodFilter() {
        periodFilters = new ArrayList<>();
        observablePeriodFilters.set(periodFilters);
        periodIdSelected.set(R.id.anytime);
        periodFiltersApplied.set(0);
    }

    public void clearSyncFilter() {
        stateFilters.clear();
        observableStates.postValue(stateFilters);
        stateFiltersApplied.set(stateFilters.size());
    }

    public void clearOuFilter() {
        ouFilters.clear();
        liveDataOUFilter.setValue(ouFilters);
        ouFiltersApplied.set(ouFilters.size());
    }

    public void clearAllFilters() {
        eventStatusFilters.clear();
        observableEventStatus.set(null);
        enrollmentStatusFilters.clear();
        observableEnrollmentStatus.set(null);
        catOptComboFilters.clear();
        stateFilters.clear();
        observableStates.postValue(stateFilters);
        ouFilters.clear();
        liveDataOUFilter.setValue(ouFilters);
        periodFilters = new ArrayList<>();
        observablePeriodFilters.set(periodFilters);
        enrollmentPeriodFilters = new ArrayList<>();
        enrollmentPeriodIdSelected.set(R.id.anytime);
        periodIdSelected.set(R.id.anytime);
        assignedFilter = false;
        observableAssignedToMe.set(false);
        sortingItem = null;
        followUpFilter = false;
        observableFollowUp.set(false);

        eventStatusFiltersApplied.set(eventStatusFilters.size());
        enrollmentPeriodFiltersApplied.set(enrollmentPeriodFilters.size());
        enrollmentStatusFiltersApplied.set(enrollmentStatusFilters.size());
        catOptCombFiltersApplied.set(catOptComboFilters.size());
        stateFiltersApplied.set(stateFilters.size());
        ouFiltersApplied.set(ouFilters.size());
        periodFiltersApplied.set(0);
        assignedToMeApplied.set(0);
        this.currentWorkingList = null;
        setWorkingListScope(new EmptyWorkingList());
        followUpFilterApplied.set(0);

        if (!workingListActive())
            publishData();
    }

    public boolean getAssignedFilter() {
        return assignedFilter;
    }

    public ObservableField<Boolean> observeAssignedToMe() {
        return observableAssignedToMe;
    }

    public void setAssignedToMe(boolean isChecked) {
        this.assignedFilter = isChecked;
        observableAssignedToMe.set(isChecked);
        assignedToMeApplied.set(isChecked ? 1 : 0);
        if (!workingListActive()) {
            publishData();
        }
    }

    public void setSortingItem(SortingItem sortingItem) {
        if (sortingItem.getSortingStatus() != SortingStatus.NONE) {
            this.sortingItem = sortingItem;
        } else {
            this.sortingItem = null;
        }
        publishData();
    }

    public boolean getFollowUpFilter() {
        return followUpFilter;
    }

    public ObservableField<Boolean> observeFollowUp() {
        return observableFollowUp;
    }

    public void setFollowUp(boolean isChecked) {
        this.followUpFilter = isChecked;
        observableFollowUp.set(isChecked);
        followUpFilterApplied.set(isChecked ? 1 : 0);
        publishData();

    }

    public SortingItem getSortingItem() {
        return sortingItem;
    }

    public void currentWorkingList(WorkingListItem workingListItem) {
        if (workingListItem != null) {
            this.currentWorkingList = workingListItem;
        } else {
            this.currentWorkingList = null;
            setWorkingListScope(new EmptyWorkingList());
        }
        publishData();
    }

    @Nullable
    public WorkingListItem currentWorkingList() {
        return currentWorkingList;
    }

    public boolean workingListActive() {
        return currentWorkingList != null;
    }

    public void setWorkingListScope(WorkingListScope scope) {
        if (!currentWorkingListScope.get().equals(scope)) {
            currentWorkingListScope.set(scope);
            setFilterCountersForWorkingList(scope);
        }
    }

    private void setFilterCountersForWorkingList(WorkingListScope scope) {

        periodFilters = new ArrayList<>();
        periodFiltersApplied.set(0);
        periodIdSelected.set(R.id.anytime);
        enrollmentPeriodFilters.clear();
        enrollmentPeriodFiltersApplied.set(0);
        enrollmentPeriodIdSelected.set(R.id.anytime);
        ouFilters.clear();
        liveDataOUFilter.postValue(ouFilters);
        ouFiltersApplied.set(ouFilters.size());
        stateFilters.clear();
        observableStates.postValue(stateFilters);
        stateFiltersApplied.set(stateFilters.size());
        enrollmentStatusFilters.clear();
        enrollmentStatusFiltersApplied.set(0);
        observableEnrollmentStatus.set(null);
        eventStatusFilters.clear();
        observableEventStatus.set(null);
        eventStatusFiltersApplied.set(0);
        clearAssignToMe();
        assignedFilter = false;
        assignedToMeApplied.set(0);
        catOptComboFilters.clear();
        if (catComboAdapter != null) {
            catComboAdapter.notifyDataSetChanged();
        }
        catOptCombFiltersApplied.set(0);

        periodFiltersApplied.set(scope.eventDateCount());
        enrollmentPeriodFiltersApplied.set(scope.enrollmentDateCount());
        enrollmentStatusFiltersApplied.set(scope.enrollmentStatusCount());
        eventStatusFiltersApplied.set(scope.eventStatusCount());
        assignedToMeApplied.set(scope.assignCount());

        publishData();
    }

    private int getTotalFilterCounterForWorkingList(WorkingListScope scope) {
        int eventDateCount = scope.eventDateCount() != 0 ? 1 : 0;
        int enrollmentDateCount = scope.enrollmentDateCount() != 0 ? 1 : 0;
        int enrollmentStatusCount = scope.enrollmentStatusCount() != 0 ? 1 : 0;
        int eventStatusCount = scope.eventStatusCount() != 0 ? 1 : 0;
        int eventAssignedToMeCount = scope.assignCount() != 0 ? 1 : 0;
        return eventDateCount + enrollmentDateCount + enrollmentStatusCount + eventStatusCount + eventAssignedToMeCount;
    }

    public ObservableField<WorkingListScope> observeWorkingListScope() {
        return currentWorkingListScope;
    }

    public boolean isFilterActiveForWorkingList(Filters filterType) {
        switch (filterType) {
            case ENROLLMENT_DATE:
                return currentWorkingListScope.get().isPeriodActive(Filters.ENROLLMENT_DATE);
            case PERIOD:
                return currentWorkingListScope.get().isPeriodActive(Filters.PERIOD);
            case ENROLLMENT_STATUS:
                return currentWorkingListScope.get().isEnrollmentStatusActive();
            case EVENT_STATUS:
                return currentWorkingListScope.get().isEventStatusActive();
            case ASSIGNED_TO_ME:
                return currentWorkingListScope.get().isAssignedActive();
            default:
                return false;
        }
    }

    public String getFilterStringValue(Filters filterType, String defaultValue) {
        if (isFilterActiveForWorkingList(filterType)) {
            return currentWorkingListScope.get().value(filterType);
        } else {
            switch (filterType) {
                case SYNC_STATE:
                    return CollectionsKt.joinToString(stateValues, ", ", "", "", -1, "", null);
                case PERIOD:
                case ORG_UNIT:
                case CAT_OPT_COMB:
                case EVENT_STATUS:
                case ASSIGNED_TO_ME:
                case ENROLLMENT_DATE:
                case ENROLLMENT_STATUS:
                case WORKING_LIST:
                case FOLLOW_UP:
                default:
                    return defaultValue;
            }
        }
    }
}
