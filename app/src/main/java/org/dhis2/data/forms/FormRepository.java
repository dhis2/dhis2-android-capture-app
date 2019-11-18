package org.dhis2.data.forms;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.tuples.Pair;
import org.dhis2.data.tuples.Trio;
import org.hisp.dhis.android.core.category.CategoryCombo;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;
import org.hisp.dhis.android.core.common.FeatureType;
import org.hisp.dhis.android.core.common.Geometry;
import org.hisp.dhis.android.core.common.Unit;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramStage;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityType;
import org.hisp.dhis.rules.RuleEngine;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;

public interface FormRepository {

    @NonNull
    Flowable<String> title();

    @NonNull
    Flowable<Pair<Program, String>> reportDate();

    Flowable<Pair<Program, String>> incidentDate();

    Flowable<Program> getAllowDatesInFuture();

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
    Consumer<Geometry> storeCoordinates();

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

    Observable<Trio<Boolean, CategoryCombo, List<CategoryOptionCombo>>> getProgramCategoryCombo(String eventUid);

    void saveCategoryOption(CategoryOptionCombo selectedOption);

    Observable<FeatureType> captureCoodinates();

    Observable<OrganisationUnit> getOrgUnitDates();

    Flowable<ProgramStage> getProgramStage(String eventUid);

    Single<TrackedEntityType> captureTeiCoordinates();

    Consumer<Geometry> storeTeiCoordinates();

    Consumer<Unit> clearCoordinates();

}