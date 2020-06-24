package org.dhis2.utils.filters;

import androidx.databinding.ObservableField;

import org.hisp.dhis.android.core.arch.helpers.UidsHelper;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.period.DatePeriod;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import kotlin.Pair;

public class FilterManager {

    public static final int OU_TREE = 1986;

    public void publishData() {
        filterProcessor.onNext(this);
    }

    public enum PeriodRequest {
        FROM_TO, OTHER
    }

    private int periodIdSelected;
    private int enrollmentPeriodIdSelected;
    private int totalSearchTeiFilter = 0;

    private List<OrganisationUnit> ouFilters;
    private List<State> stateFilters;
    private List<DatePeriod> periodFilters;
    private List<DatePeriod> enrollmentPeriodFilters;
    private List<CategoryOptionCombo> catOptComboFilters;
    private List<EventStatus> eventStatusFilters;
    private List<EnrollmentStatus> enrollmentStatusFilters;
    private boolean assignedFilter;

    private ObservableField<Integer> ouFiltersApplied;
    private ObservableField<Integer> stateFiltersApplied;
    private ObservableField<Integer> periodFiltersApplied;
    private ObservableField<Integer> enrollmentPeriodFiltersApplied;
    private ObservableField<Integer> catOptCombFiltersApplied;
    private ObservableField<Integer> eventStatusFiltersApplied;
    private ObservableField<Integer> enrollmentStatusFiltersApplied;
    private ObservableField<Integer> assignedToMeApplied;

    private FlowableProcessor<FilterManager> filterProcessor;
    private FlowableProcessor<Boolean> ouTreeProcessor;
    private FlowableProcessor<Pair<PeriodRequest, Filters>> periodRequestProcessor;

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
        periodFilters = new ArrayList<>();
        enrollmentPeriodFilters = new ArrayList<>();
        catOptComboFilters = new ArrayList<>();
        eventStatusFilters = new ArrayList<>();
        enrollmentStatusFilters = new ArrayList<>();
        assignedFilter = false;

        ouFiltersApplied = new ObservableField<>(0);
        stateFiltersApplied = new ObservableField<>(0);
        periodFiltersApplied = new ObservableField<>(0);
        enrollmentPeriodFiltersApplied = new ObservableField<>(0);
        catOptCombFiltersApplied = new ObservableField<>(0);
        eventStatusFiltersApplied = new ObservableField<>(0);
        enrollmentStatusFiltersApplied = new ObservableField<>(0);
        assignedToMeApplied = new ObservableField<>(0);

        filterProcessor = PublishProcessor.create();
        ouTreeProcessor = PublishProcessor.create();
        periodRequestProcessor = PublishProcessor.create();
    }

    public void setPeriodIdSelected(int selected) {
        this.periodIdSelected = selected;
    }

    public void setEnrollmentPeriodIdSelected(int selected) {
        this.enrollmentPeriodIdSelected = selected;
    }

    public int getPeriodIdSelected() {
        return this.periodIdSelected;
    }

    public int getEnrollmentPeriodIdSelected() {
        return this.enrollmentPeriodIdSelected;
    }

//    region STATE FILTERS

    public void addState(boolean remove, State... states) {
        for (State stateToAdd : states) {
            if (remove)
                stateFilters.remove(stateToAdd);
            else if (!stateFilters.contains(stateToAdd))
                stateFilters.add(stateToAdd);
        }
        if (stateFilters.contains(State.TO_POST) &&
                stateFilters.contains(State.TO_UPDATE) &&
                stateFilters.contains(State.UPLOADING)) {
            stateFiltersApplied.set(stateFilters.size() - 2);
        } else {
            stateFiltersApplied.set(stateFilters.size());
        }
        filterProcessor.onNext(this);
    }

//    endregion

    public void addEventStatus(boolean remove, EventStatus... status) {
        for (EventStatus eventStatus : status) {
            if (remove)
                eventStatusFilters.remove(eventStatus);
            else if (!eventStatusFilters.contains(eventStatus))
                eventStatusFilters.add(eventStatus);
        }
        if (eventStatusFilters.contains(EventStatus.ACTIVE)) {
            eventStatusFiltersApplied.set(eventStatusFilters.size() - 1);
        } else {
            eventStatusFiltersApplied.set(eventStatusFilters.size());
        }
        filterProcessor.onNext(this);
    }

    public void addEnrollmentStatus(boolean remove, EnrollmentStatus enrollmentStatus) {
        if (remove) {
            enrollmentStatusFilters.remove(enrollmentStatus);
        } else if (!enrollmentStatusFilters.contains(enrollmentStatus)) {
            enrollmentStatusFilters.add(enrollmentStatus);
        }
        enrollmentStatusFiltersApplied.set(enrollmentStatusFilters.size());
        filterProcessor.onNext(this);
    }

    public void addPeriod(List<DatePeriod> datePeriod) {
        this.periodFilters = datePeriod;

        periodFiltersApplied.set(datePeriod != null ? 1 : 0);
        filterProcessor.onNext(this);
    }

    public void addEnrollmentPeriod(List<DatePeriod> datePeriod) {
        this.enrollmentPeriodFilters = datePeriod;

        enrollmentPeriodFiltersApplied.set(datePeriod != null ? 1 : 0);
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

    public void addCatOptCombo(CategoryOptionCombo catOptCombo) {
        if (catOptComboFilters.contains(catOptCombo))
            catOptComboFilters.remove(catOptCombo);
        else
            catOptComboFilters.add(catOptCombo);

        catOptCombFiltersApplied.set(catOptComboFilters.size());
        filterProcessor.onNext(this);
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
            default:
                return new ObservableField<>(0);
        }
    }

    public FlowableProcessor<Boolean> getOuTreeProcessor() {
        return ouTreeProcessor;
    }

    public Flowable<FilterManager> asFlowable() {
        return filterProcessor;
    }

    public FlowableProcessor<Pair<PeriodRequest, Filters>> getPeriodRequest() {
        return periodRequestProcessor;
    }

    public Flowable<Boolean> ouTreeFlowable() {
        return ouTreeProcessor;
    }

    public int getTotalFilters() {
        int ouIsApplying = ouFilters.isEmpty() ? 0 : 1;
        int stateIsApplying = stateFilters.isEmpty() ? 0 : 1;
        int periodIsApplying = periodFilters == null ? 0 : 1;
        int enrollmentPeriodIsApplying = enrollmentPeriodFilters == null ? 0 : 1;
        int eventStatusApplying = eventStatusFilters.isEmpty() ? 0 : 1;
        int enrollmentStatusApplying = enrollmentStatusFilters.isEmpty() ? 0 : 1;
        int catComboApplying = catOptComboFilters.isEmpty() ? 0 : 1;
        int assignedApplying = assignedFilter ? 1 : 0;
        return ouIsApplying + stateIsApplying + periodIsApplying +
                eventStatusApplying + catComboApplying +
                assignedApplying + enrollmentPeriodIsApplying+ enrollmentStatusApplying;
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

    public List<CategoryOptionCombo> getCatOptComboFilters() {
        return catOptComboFilters;
    }

    public List<String> getOrgUnitUidsFilters() {
        return UidsHelper.getUidsList(ouFilters);
    }

    public List<State> getStateFilters() {
        return stateFilters;
    }

    public List<EventStatus> getEventStatusFilters() {
        return eventStatusFilters;
    }

    public List<EnrollmentStatus> getEnrollmentStatusFilters() {
        return enrollmentStatusFilters;
    }

    public void addPeriodRequest(PeriodRequest periodRequest, Filters filter) {
        periodRequestProcessor.onNext(new Pair<>(periodRequest, filter));
    }

    public void removeAll() {
        ouFilters = new ArrayList<>();
        ouFiltersApplied.set(ouFilters.size());
        filterProcessor.onNext(this);
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
        ouFiltersApplied.set(ouFilters.size());
        filterProcessor.onNext(this);
    }

    public boolean exist(OrganisationUnit content) {
        return ouFilters.contains(content);
    }

    public void clearCatOptCombo() {
        catOptComboFilters.clear();
        catOptCombFiltersApplied.set(catOptComboFilters.size());
        filterProcessor.onNext(this);
    }

    public void clearEventStatus() {
        eventStatusFilters.clear();
        eventStatusFiltersApplied.set(eventStatusFilters.size());
        filterProcessor.onNext(this);
    }

    public void clearEnrollmentStatus(){
        enrollmentStatusFilters.clear();
        enrollmentStatusFiltersApplied.set(enrollmentStatusFilters.size());
        filterProcessor.onNext(this);
    }

    public void clearAssignToMe() {
        assignedFilter = false;
        assignedToMeApplied.set(0);
        filterProcessor.onNext(this);
    }

    public void clearEnrollmentDate() {
        if(enrollmentPeriodFilters!=null) {
            enrollmentPeriodFilters.clear();
        }
        enrollmentPeriodFiltersApplied.set(catOptComboFilters.size());
        filterProcessor.onNext(this);
    }

    public void clearAllFilters() {
        eventStatusFilters.clear();
        enrollmentStatusFilters.clear();
        catOptComboFilters.clear();
        stateFilters.clear();
        ouFilters.clear();
        periodFilters = null;
        enrollmentPeriodFilters = null;
        periodIdSelected = 0;
        assignedFilter = false;

        eventStatusFiltersApplied.set(eventStatusFilters.size());
        enrollmentStatusFiltersApplied.set(enrollmentStatusFilters.size());
        catOptCombFiltersApplied.set(catOptComboFilters.size());
        stateFiltersApplied.set(stateFilters.size());
        ouFiltersApplied.set(ouFilters.size());
        periodFiltersApplied.set(0);
        assignedToMeApplied.set(0);

        filterProcessor.onNext(this);
    }

    public int getTotalSearchTeiFilter() {
        return totalSearchTeiFilter;
    }

    public void setTotalSearchTeiFilter(int totalSearchTeiFilter) {
        this.totalSearchTeiFilter = totalSearchTeiFilter;
    }

    public boolean getAssignedFilter() {
        return assignedFilter;
    }

    public void setAssignedToMe(boolean isChecked) {
        this.assignedFilter = isChecked;
        assignedToMeApplied.set(isChecked ? 1 : 0);
        filterProcessor.onNext(this);
    }
}
