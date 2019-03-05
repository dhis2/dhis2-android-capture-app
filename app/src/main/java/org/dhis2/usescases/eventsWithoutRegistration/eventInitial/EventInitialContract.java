package org.dhis2.usescases.eventsWithoutRegistration.eventInitial;

import android.app.DatePickerDialog;

import com.unnamed.b.atv.model.TreeNode;

import org.dhis2.data.forms.FormSectionViewModel;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.usescases.general.AbstractActivityContracts;
import org.hisp.dhis.android.core.category.CategoryComboModel;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.common.ObjectStyleModel;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.period.PeriodType;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.program.ProgramStage;

import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.reactivex.functions.Consumer;

/**
 * QUADRAM. Created by Cristian on 01/03/2018.
 */

public class EventInitialContract {

    public interface EventInitialView extends AbstractActivityContracts.View {
        void setProgram(@NonNull ProgramModel program);

        void setCatComboOptions(CategoryComboModel catCombo, List<CategoryOptionComboModel> catComboList);

        void showDateDialog(DatePickerDialog.OnDateSetListener listener);

        void openDrawer();

        void renderError(String message);

        void addTree(TreeNode treeNode);

        void setEvent(EventModel event);

        void setCatOption(CategoryOptionComboModel categoryOptionComboModel);

        void setLocation(double latitude, double longitude);

        void onEventCreated(String eventUid);

        void onEventUpdated(String eventUid);

        void setProgramStage(ProgramStage programStage);

        void onEventSections(List<FormSectionViewModel> formSectionViewModels);

        @NonNull
        Consumer<List<FieldViewModel>> showFields(String sectionUid);

        void showProgramStageSelection();

        void setReportDate(Date date);

        void setOrgUnit(String orgUnitId, String orgUnitName);

        void showNoOrgUnits();

        void setAccessDataWrite(Boolean canWrite);

        void showOrgUnitSelector(List<OrganisationUnitModel> orgUnits);

        void showQR();

        void showEventWasDeleted();

        void setHideSection(String sectionUid);

        void renderObjectStyle(ObjectStyleModel objectStyleModel);
    }

    public interface EventInitialPresenter extends AbstractActivityContracts.Presenter {
        void init(EventInitialView view, String programId, String eventId, String orgUnitId, String programStageId);

        void getProgramStage(String programStageUid);

        void onBackClick();

        @SuppressWarnings("squid:S00107")
        void createEvent(String enrollmentUid, String programStage, Date date, String orgUnitUid,
                         String catOption, String catOptionCombo,
                         String latitude, String longitude, String trackedEntityInstance);

        @SuppressWarnings("squid:S00107")
        void createEventPermanent(String enrollmentUid, String trackedEntityInstanceUid, String programStage,
                                  Date date, String orgUnitUid,
                                  String catOption, String catOptionCombo,
                                  String latitude, String longitude);

        @SuppressWarnings("squid:S00107")
        void scheduleEvent(String enrollmentUid, String programStage, Date dueDate, String orgUnitUid,
                           String catOption, String catOptionCombo,
                           String latitude, String longitude);

        @SuppressWarnings("squid:S00107")
        void editEvent(String trackedEntityInstance, String programStage, String eventUid, String date, String orgUnitUid,
                       String catOption, String catOptionCombo,
                       String latitude, String longitude);

        void onDateClick(@Nullable DatePickerDialog.OnDateSetListener listener);

        void onOrgUnitButtonClick();

        void onLocationClick();

        void onLocation2Click();

        void getCatOption(String categoryOptionComboId);

        void filterOrgUnits(String date);

        void getSectionCompletion(@Nullable String sectionUid);

        void goToSummary();

        void getEvents(String programUid, String enrollmentUid, String programStageUid, PeriodType periodType);

        void getOrgUnits(String programId);

        void getEventSections(@NonNull String eventId);

        List<OrganisationUnitModel> getOrgUnits();

        void onShareClick(android.view.View mView);

        void deleteEvent(String trackedEntityInstance);

        boolean isEnrollmentOpen();

        void getStageObjectStyle(String uid);
    }

}
