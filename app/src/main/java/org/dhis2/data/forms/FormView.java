package org.dhis2.data.forms;

import androidx.annotation.NonNull;

import com.mapbox.mapboxsdk.geometry.LatLng;

import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.tuples.Pair;
import org.dhis2.data.tuples.Trio;
import org.hisp.dhis.android.core.category.CategoryCombo;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;
import org.hisp.dhis.android.core.common.FeatureType;
import org.hisp.dhis.android.core.common.Geometry;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityType;
import org.hisp.dhis.rules.models.RuleActionErrorOnCompletion;
import org.hisp.dhis.rules.models.RuleActionShowError;
import org.hisp.dhis.rules.models.RuleActionWarningOnCompletion;
import org.hisp.dhis.android.core.common.Unit;

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
    Observable<Geometry> teiCoordinatesChanged();

    @NonNull
    Observable<Unit> reportCoordinatesCleared();

    @NonNull
    Observable<String> incidentDateChanged();

    @NonNull
    Observable<Geometry> reportCoordinatesChanged();

    @NonNull
    Consumer<List<FormSectionViewModel>> renderSectionViewModels();

    @NonNull
    Consumer<Pair<Program, String>> renderReportDate();

    @NonNull
    Consumer<String> renderTitle();

    @NonNull
    Consumer<ReportStatus> renderStatus();

    @NonNull
    Consumer<Trio<String, String, String>> finishEnrollment();

    void renderStatusChangeSnackBar(@NonNull ReportStatus eventStatus);

    @NonNull
    Consumer<Pair<Program, String>> renderIncidentDate();

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

    void showCatComboDialog(CategoryCombo categoryCombo, List<CategoryOptionCombo> categoryOptionCombo);

    Consumer<FeatureType> renderCaptureCoordinates();

    void setMinMaxDates(Date openingDate, Date closingDate);

    Observable<EnrollmentStatus> onObservableBackPressed();

    void setNeedInitial(boolean need, String programStage);

    Consumer<TrackedEntityType> renderTeiCoordinates();
}