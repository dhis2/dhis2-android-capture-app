package org.dhis2.usescases.datasets.datasetInitial;

import android.app.DatePickerDialog;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.dhis2.data.forms.FormSectionViewModel;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialContract;
import org.dhis2.usescases.general.AbstractActivityContracts;
import com.unnamed.b.atv.model.TreeNode;

import org.hisp.dhis.android.core.category.CategoryComboModel;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.dataset.DataSetModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.period.PeriodType;
import org.hisp.dhis.android.core.program.ProgramStageModel;

import java.util.Date;
import java.util.List;

import io.reactivex.functions.Consumer;

public class DataSetInitialContract {

    public interface View extends AbstractActivityContracts.View {

        void setCatComboOptions(CategoryComboModel catCombo, List<CategoryOptionComboModel> catComboList);

        void showDateDialog(DatePickerDialog.OnDateSetListener listener);

        void openDrawer();

        void renderError(String message);

        void addTree(TreeNode treeNode);

        void setDataSet(DataSetModel event);

        void setCatOption(CategoryOptionComboModel categoryOptionComboModel);

        void setLocation(double latitude, double longitude);

        void onDataSetCreated(String eventUid);

        void onDataSetUpdated(String eventUid);
        //Esto para que es?
        void onEventSections(List<FormSectionViewModel> formSectionViewModels);

        @NonNull
        Consumer<List<FieldViewModel>> showFields(String sectionUid);

        void showProgramStageSelection();

        void setReportDate(String format);

        void setOrgUnit(String orgUnitId, String orgUnitName);

        void showNoOrgUnits();

        void setAccessDataWrite(Boolean canWrite);

        void showOrgUnitSelector(List<OrganisationUnitModel> orgUnits);
    }

    public interface Presenter extends AbstractActivityContracts.Presenter {
        void init(View view, String datasetId, String orgUnitId);

        void onBackClick();

        void createDataSet(String enrollmentUid, String programStageModel, Date date, String orgUnitUid,
                           String catOption, String catOptionCombo,
                           String latitude, String longitude);

        void createDataSetPermanent(String enrollmentUid, String trackedEntityInstanceUid, String programStageModel,
                                    Date date, String orgUnitUid,
                                    String catOption, String catOptionCombo,
                                    String latitude, String longitude);

        void scheduleDataSet(String enrollmentUid, String programStageModel, Date dueDate, String orgUnitUid,
                             String catOption, String catOptionCombo,
                             String latitude, String longitude);

        void editDataSet(String programStageModel, String eventUid, String date, String orgUnitUid,
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

        void getDataSet(String programUid, String enrollmentUid, String programStageUid, PeriodType periodType);

        void getOrgUnits(String programId);
        //Para que es?
        void getDataSetSections(@NonNull String eventId);

        List<OrganisationUnitModel> getOrgUnits();
    }


}
