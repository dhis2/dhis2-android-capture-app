package org.dhis2.data.forms;

import com.google.android.gms.maps.model.LatLng;

import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.tuples.Pair;
import org.dhis2.data.tuples.Trio;
import org.hisp.dhis.android.core.category.CategoryComboModel;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.rules.RuleEngine;

import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

public interface FormRepository {

    @NonNull
    Flowable<String> title();

    @NonNull
    Flowable<Pair<ProgramModel, String>> reportDate();

    Flowable<Pair<ProgramModel, String>> incidentDate();

    Flowable<ProgramModel> getAllowDatesInFuture();

    @NonNull
    Flowable<RuleEngine> ruleEngine();

    @NonNull
    Consumer<String> storeReportDate();

    @NonNull
    Consumer<String> storeIncidentDate();

    @NonNull
    Consumer<LatLng> storeCoordinates();

    @NonNull
    Flowable<ReportStatus> reportStatus();

    @NonNull
    Flowable<List<FormSectionViewModel>> sections();

    @NonNull
    Consumer<ReportStatus> storeReportStatus();

    @NonNull
    Observable<Trio<String, String, String>> useFirstStageDuringRegistration();

    @NonNull
    Observable<String> autoGenerateEvents(String enrollmentUid);

    @NonNull
    Observable<List<FieldViewModel>> fieldValues();

    void deleteTrackedEntityAttributeValues(@NonNull String trackedEntityInstanceId);

    void deleteEnrollment(@NonNull String trackedEntityInstanceId);

    void deleteEvent();

    void deleteTrackedEntityInstance(@NonNull String trackedEntityInstanceId);

    @NonNull
    Observable<String> getTrackedEntityInstanceUid();

    Observable<Trio<Boolean, CategoryComboModel, List<CategoryOptionComboModel>>> getProgramCategoryCombo();

    void saveCategoryOption(CategoryOptionComboModel selectedOption);

    Observable<Boolean> captureCoodinates();

    Observable<OrganisationUnitModel> getOrgUnitDates();
}