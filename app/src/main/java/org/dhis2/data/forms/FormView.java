package org.dhis2.data.forms;

import com.mapbox.mapboxsdk.geometry.LatLng;

import androidx.annotation.NonNull;

import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.tuples.Pair;
import org.dhis2.data.tuples.Trio;

import org.hisp.dhis.android.core.category.CategoryComboModel;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.common.Unit;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.rules.models.RuleActionErrorOnCompletion;
import org.hisp.dhis.rules.models.RuleActionShowError;
import org.hisp.dhis.rules.models.RuleActionWarningOnCompletion;

import java.util.Date;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

interface FormView {

    @NonNull
    Observable<ReportStatus> eventStatusChanged();

    @NonNull
    Observable<String> reportDateChanged();

    @NonNull
    Observable<Unit> reportCoordinatesCleared();

    @NonNull
    Observable<String> incidentDateChanged();

    @NonNull
    Observable<LatLng> reportCoordinatesChanged();

    @NonNull
    Consumer<List<FormSectionViewModel>> renderSectionViewModels();

    @NonNull
    Consumer<Pair<ProgramModel, String>> renderReportDate();

    @NonNull
    Consumer<String> renderTitle();

    @NonNull
    Consumer<ReportStatus> renderStatus();

    @NonNull
    Consumer<Trio<String, String, String>> finishEnrollment();

    void renderStatusChangeSnackBar(@NonNull ReportStatus eventStatus);

    @NonNull
    Consumer<Pair<ProgramModel, String>> renderIncidentDate();

    void initReportDatePicker(boolean reportAllowFutureDates, boolean incidentAllowFutureDates);

    void onNext(ReportStatus reportStatus);

    void isMandatoryFieldsRequired(List<FieldViewModel> viewModels);

    void showMandatoryFieldsDialog();

    void onAllSavedDataDeleted();

    void onBackPressed();

    void messageOnComplete(String content, boolean b);

    void hideDates();

    void setErrorOnCompletion(RuleActionErrorOnCompletion errorOnCompletion);

    void setWarningOnCompletion(RuleActionWarningOnCompletion errorOnCompletion);

    void setShowError(RuleActionShowError showError);

    void showCatComboDialog(CategoryComboModel categoryComboModel, List<CategoryOptionComboModel> categoryOptionComboModels);

    Consumer<Boolean> renderCaptureCoordinates();

    void setMinMaxDates(Date openingDate, Date closingDate);

    Observable<EnrollmentStatus> onObservableBackPressed();

    void setNeedInitial(boolean need, String programStage);
}