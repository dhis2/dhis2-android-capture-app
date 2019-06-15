package org.dhis2.usescases.eventsWithoutRegistration.eventInitial;

import android.app.DatePickerDialog;

import com.unnamed.b.atv.model.TreeNode;

import org.dhis2.data.forms.FormSectionViewModel;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.usescases.general.AbstractActivityContracts;
import org.hisp.dhis.android.core.category.CategoryCombo;
import org.hisp.dhis.android.core.category.CategoryOption;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;
import org.hisp.dhis.android.core.common.ObjectStyleModel;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.program.ProgramStageModel;

import java.util.Date;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.reactivex.functions.Consumer;

/**
 * QUADRAM. Created by Cristian on 01/03/2018.
 */

public class EventInitialContract {

    public interface View extends AbstractActivityContracts.View {
        void checkActionButtonVisibility();

        void setProgram(@NonNull ProgramModel program);

        void setCatComboOptions(CategoryCombo catCombo, Map<String, CategoryOption> stringCategoryOptionMap);

        void showDateDialog(DatePickerDialog.OnDateSetListener listener);

        void openDrawer();

        void renderError(String message);

        void addTree(TreeNode treeNode);

        void setEvent(EventModel event);

        void setLocation(double latitude, double longitude);

        void onEventCreated(String eventUid);

        void onEventUpdated(String eventUid);

        void setProgramStage(ProgramStageModel programStage);

        void onEventSections(List<FormSectionViewModel> formSectionViewModels);

        @NonNull
        Consumer<List<FieldViewModel>> showFields(String sectionUid);

        void showProgramStageSelection();

        void setOrgUnit(String orgUnitId, String orgUnitName);

        void showNoOrgUnits();

        void setAccessDataWrite(Boolean canWrite);

        void showOrgUnitSelector(List<OrganisationUnitModel> orgUnits);

        void showQR();

        void showEventWasDeleted();

        void setHideSection(String sectionUid);

        void renderObjectStyle(ObjectStyleModel objectStyleModel);

        void runSmsSubmission();
    }

    public interface Presenter extends AbstractActivityContracts.Presenter {
        void init(EventInitialContract.View view, String programId, String eventId, String orgUnitId, String programStageId);

        void getProgramStage(String programStageUid);

        void onBackClick();

        void createEvent(String enrollmentUid, String programStageModel, Date date, String orgUnitUid,
                         String catOption, String catOptionCombo,
                         String latitude, String longitude, String trackedEntityInstance);

        void createEventPermanent(String enrollmentUid, String trackedEntityInstanceUid, String programStageModel,
                                  Date date, String orgUnitUid,
                                  String catOption, String catOptionCombo,
                                  String latitude, String longitude);

        void scheduleEventPermanent(String enrollmentUid, String trackedEntityInstanceUid, String programStageModel, Date dueDate, String orgUnitUid,
                                    String categoryOptionComboUid, String categoryOptionsUid,
                                    String latitude, String longitude);

        void scheduleEvent(String enrollmentUid, String programStageModel, Date dueDate, String orgUnitUid,
                           String catOption, String catOptionCombo,
                           String latitude, String longitude);

        void editEvent(String trackedEntityInstance, String programStageModel, String eventUid, String date, String orgUnitUid,
                       String catOption, String catOptionCombo,
                       String latitude, String longitude);

        void onDateClick(@Nullable DatePickerDialog.OnDateSetListener listener);

        void onOrgUnitButtonClick();

        void onLocationClick();

        void onLocation2Click();

        void onFieldChanged(CharSequence s, int start, int before, int count);

        void filterOrgUnits(String date);

        void getSectionCompletion(@Nullable String sectionUid);

        void goToSummary();

        void getOrgUnits(String programId);

        void getEventSections(@NonNull String eventId);

        List<OrganisationUnitModel> getOrgUnits();

        void onShareClick(android.view.View mView);

        void deleteEvent(String trackedEntityInstance);

        boolean isEnrollmentOpen();

        void getStageObjectStyle(String uid);

        String getCatOptionCombo(List<CategoryOptionCombo> categoryOptionCombos, List<CategoryOption> values);
    }

}
