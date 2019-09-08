package org.dhis2.usescases.eventsWithoutRegistration.eventInitial;

import android.app.DatePickerDialog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.unnamed.b.atv.model.TreeNode;

import org.dhis2.data.forms.FormSectionViewModel;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.tuples.Pair;
import org.dhis2.usescases.general.AbstractActivityContracts;
import org.dhis2.utils.EventCreationType;
import org.hisp.dhis.android.core.category.CategoryCombo;
import org.hisp.dhis.android.core.category.CategoryOption;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;
import org.hisp.dhis.android.core.common.FeatureType;
import org.hisp.dhis.android.core.common.Geometry;
import org.hisp.dhis.android.core.common.ObjectStyle;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramStage;

import java.util.Date;
import java.util.List;
import java.util.Map;

import io.reactivex.functions.Consumer;

/**
 * QUADRAM. Created by Cristian on 01/03/2018.
 */

public class EventInitialContract {

    public interface View extends AbstractActivityContracts.View {
        void checkActionButtonVisibility();

        void setProgram(@NonNull Program program);

        void setCatComboOptions(CategoryCombo catCombo, Map<String, CategoryOption> stringCategoryOptionMap);

        void showDateDialog(DatePickerDialog.OnDateSetListener listener);

        void renderError(String message);

        Consumer<Pair<TreeNode, List<TreeNode>>> addNodeToTree();

        void setEvent(Event event);

//        void setLocation(Geometry geometry);

        void onEventCreated(String eventUid);

        void onEventUpdated(String eventUid);

        void setProgramStage(ProgramStage programStage);

        void onEventSections(List<FormSectionViewModel> formSectionViewModels);

        @NonNull
        Consumer<List<FieldViewModel>> showFields(String sectionUid);

        void showProgramStageSelection();

        void setOrgUnit(String orgUnitId, String orgUnitName);

        void showNoOrgUnits();

        void setAccessDataWrite(Boolean canWrite);

        void showOrgUnitSelector(List<OrganisationUnit> orgUnits);

        void showQR();

        void showEventWasDeleted();

        void setHideSection(String sectionUid);

        void renderObjectStyle(ObjectStyle objectStyle);

        void runSmsSubmission();

        EventCreationType eventcreateionType();

        void latitudeWarning(boolean showWarning);

        void longitudeWarning(boolean showWarning);
    }

    public interface Presenter extends AbstractActivityContracts.Presenter {
        void init(EventInitialContract.View view, String programId, String eventId, String orgUnitId, String programStageId);

        void getProgramStage(String programStageUid);

        void onBackClick();

        void createEvent(String enrollmentUid, String programStageModel, Date date, String orgUnitUid,
                         String catOption, String catOptionCombo,
                         Geometry geometry, String trackedEntityInstance);

        void scheduleEventPermanent(String enrollmentUid, String trackedEntityInstanceUid, String programStageModel, Date dueDate, String orgUnitUid,
                                    String categoryOptionComboUid, String categoryOptionsUid,
                                    Geometry geometry);

        void scheduleEvent(String enrollmentUid, String programStageModel, Date dueDate, String orgUnitUid,
                           String catOption, String catOptionCombo,
                           Geometry geometry);

        void editEvent(String trackedEntityInstance, String programStageModel, String eventUid, String date, String orgUnitUid,
                       String catOption, String catOptionCombo,
                       Geometry geometry);

        void onDateClick(@Nullable DatePickerDialog.OnDateSetListener listener);

        void onOrgUnitButtonClick();

        void onLocationClick();

        void onLocation2Click(FeatureType featureType);

        void onLatChanged(CharSequence s, int start, int before, int count);

        void onLonChanged(CharSequence s, int start, int before, int count);

        void onFieldChanged(CharSequence s, int start, int before, int count);

        void getSectionCompletion(@Nullable String sectionUid);

        void goToSummary();

        void getOrgUnits(String programId);

        void getEventSections(@NonNull String eventId);

        List<OrganisationUnit> getOrgUnits();

        void onShareClick(android.view.View mView);

        void deleteEvent(String trackedEntityInstance);

        boolean isEnrollmentOpen();

        void getStageObjectStyle(String uid);

        String getCatOptionCombo(List<CategoryOptionCombo> categoryOptionCombos, List<CategoryOption> values);

        Date getStageLastDate(String programStageUid, String enrollmentUid);

        void onExpandOrgUnitNode(TreeNode treeNode, String parentUid, String date);

        void getEventOrgUnit(String ouUid);
    }

}
