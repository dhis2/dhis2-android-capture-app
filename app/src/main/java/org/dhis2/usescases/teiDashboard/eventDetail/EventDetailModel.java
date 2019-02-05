package org.dhis2.usescases.teiDashboard.eventDetail;

import org.dhis2.data.tuples.Pair;
import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.program.ProgramStageDataElementModel;
import org.hisp.dhis.android.core.program.ProgramStageModel;
import org.hisp.dhis.android.core.program.ProgramStageSectionModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueModel;

import java.util.Date;
import java.util.List;

import androidx.databinding.BaseObservable;

/**
 * QUADRAM. Created by Cristian on 08/02/2018.
 */

public class EventDetailModel extends BaseObservable {

    private final ProgramStageModel programStage;
    private final List<CategoryOptionComboModel> optionComboList;
    private final ProgramModel programModel;
    private final String catComboName;
    private final boolean isEnrollmentActive;
    private EventModel eventModel;
    private final OrganisationUnitModel orgUnit;

    EventDetailModel(EventModel eventModel,
                     ProgramStageModel programStage,
                     OrganisationUnitModel orgUnit,
                     Pair<String, List<CategoryOptionComboModel>> optionComboList,
                     ProgramModel programModel,
                     boolean isEnrollmentActive) {
        this.eventModel = eventModel;
        this.programStage = programStage;
        this.orgUnit = orgUnit;
        this.catComboName = optionComboList.val0();
        this.optionComboList = optionComboList.val1();
        this.programModel = programModel;
        this.isEnrollmentActive = isEnrollmentActive;

    }

    EventModel getEventModel() {
        return eventModel;
    }

    public ProgramStageModel getProgramStage() {
        return programStage;
    }

    public String getCatComboName() {
        return catComboName;
    }

    public Date orgUnitOpeningDate() {
        return orgUnit.openingDate();
    }

    public Date orgUnitClosingDate() {
        return orgUnit.closedDate();
    }

    public String getOrgUnitName() {
        return orgUnit.displayName();
    }

    public List<CategoryOptionComboModel> getOptionComboList() {
        return optionComboList;
    }

    public boolean hasExpired() {
        return eventModel.completedDate() != null && DateUtils.getInstance().hasExpired(eventModel, programModel.expiryDays(), programModel.completeEventsExpiryDays(), programModel.expiryPeriodType());
    }

    public String getEventCatComboOptionName() {
        String eventCatComboOptionName = null;

        for (CategoryOptionComboModel option : optionComboList) {
            if (option.uid().equals(eventModel.attributeOptionCombo()))
                eventCatComboOptionName = option.name();
        }

        return eventCatComboOptionName;
    }

    public boolean isEnrollmentActive() {
        return isEnrollmentActive;
    }

    public ProgramModel getProgram() {
        return programModel;
    }
}
