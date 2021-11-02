package org.dhis2.usescases.eventsWithoutRegistration.eventInitial;

import android.app.DatePickerDialog;

import androidx.annotation.NonNull;

import org.dhis2.form.model.FieldUiModel;
import org.dhis2.usescases.general.AbstractActivityContracts;
import org.dhis2.utils.EventCreationType;
import org.hisp.dhis.android.core.category.CategoryCombo;
import org.hisp.dhis.android.core.category.CategoryOption;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;
import org.hisp.dhis.android.core.common.ObjectStyle;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramStage;

import java.util.List;
import java.util.Map;

public class EventInitialContract {

    public interface View extends AbstractActivityContracts.View {
        void checkActionButtonVisibility();

        void setProgram(@NonNull Program program);

        void setCatComboOptions(CategoryCombo catCombo, List<CategoryOptionCombo> categoryOptionCombos, Map<String, CategoryOption> stringCategoryOptionMap);

        void showDateDialog(DatePickerDialog.OnDateSetListener listener);

        void renderError(String message);

        void setEvent(Event event);

        void onEventCreated(String eventUid);

        void onEventUpdated(String eventUid);

        void setProgramStage(ProgramStage programStage);

        void updatePercentage(float primaryValue);

        void showProgramStageSelection();

        void setOrgUnit(String orgUnitId, String orgUnitName);

        void showNoOrgUnits();

        void setAccessDataWrite(Boolean canWrite);

        void showOrgUnitSelector(List<OrganisationUnit> orgUnits);

        void showQR();

        void showEventWasDeleted();

        void setHideSection(String sectionUid);

        void renderObjectStyle(ObjectStyle objectStyle);

        void setInitialOrgUnit(OrganisationUnit organisationUnit);

        EventCreationType eventcreateionType();

        void setGeometryModel(FieldUiModel geometryModel);

        void setNewGeometry(String value);

        void displayFeatureTypeError();

        void setEditionStatus(String reason);

        void hideEditionStatus();
    }
}
