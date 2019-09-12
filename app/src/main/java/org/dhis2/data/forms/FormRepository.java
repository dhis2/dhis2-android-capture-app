package org.dhis2.data.forms;

import com.mapbox.mapboxsdk.geometry.LatLng;

import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.tuples.Pair;
import org.dhis2.data.tuples.Trio;
import org.hisp.dhis.android.core.category.CategoryComboModel;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.common.Unit;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.program.ProgramStage;
import org.hisp.dhis.rules.RuleEngine;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

    Flowable<RuleEngine> restartRuleEngine();

    @NonNull
    Flowable<RuleEngine> ruleEngine();

    @NonNull
    Consumer<String> storeReportDate();

    @NonNull
    Observable<Long> saveReportDate(String date);

    @NonNull
    Consumer<String> storeIncidentDate();

    @NonNull
    Observable<Long> saveIncidentDate(String date);

    @NonNull
    Consumer<LatLng> storeCoordinates();

    @NonNull
    Flowable<ReportStatus> reportStatus();

    @NonNull
    Flowable<List<FormSectionViewModel>> sections();

    @NonNull
    Consumer<ReportStatus> storeReportStatus();

    @Nullable
    Observable<Trio<String, String, String>> useFirstStageDuringRegistration();

    @Nullable
    Observable<String> autoGenerateEvents(String enrollmentUid);

    @NonNull
    Observable<List<FieldViewModel>> fieldValues();

    void deleteTrackedEntityAttributeValues(@NonNull String trackedEntityInstanceId);

    void deleteEnrollment(@NonNull String trackedEntityInstanceId);

    void deleteEvent();

    void deleteTrackedEntityInstance(@NonNull String trackedEntityInstanceId);

    @NonNull
    Observable<String> getTrackedEntityInstanceUid();

    Observable<Trio<Boolean, CategoryComboModel, List<CategoryOptionComboModel>>> getProgramCategoryCombo(String eventUid);

    void saveCategoryOption(CategoryOptionComboModel selectedOption);

    Observable<Boolean> captureCoodinates();

    Observable<OrganisationUnit> getOrgUnitDates();

    Flowable<ProgramStage> getProgramStage(String eventUid);

    Consumer<Unit> clearCoordinates();
}