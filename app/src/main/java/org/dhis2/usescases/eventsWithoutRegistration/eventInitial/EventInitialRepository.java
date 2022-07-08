package org.dhis2.usescases.eventsWithoutRegistration.eventInitial;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.dhis2.data.forms.FormSectionViewModel;
import org.dhis2.form.model.FieldUiModel;
import org.dhis2.utils.Result;
import org.hisp.dhis.android.core.common.Geometry;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.event.EventEditableStatus;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramStage;
import org.hisp.dhis.rules.models.RuleEffect;

import java.util.Date;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observable;

public interface EventInitialRepository {

    @NonNull
    Observable<Event> event(String eventId);

    Observable<String> createEvent(String enrollmentUid, @Nullable String trackedEntityInstanceUid,
                                   @NonNull String program,
                                   @NonNull String programStage, @NonNull Date date,
                                   @NonNull String orgUnitUid, @NonNull String catComboUid,
                                   @NonNull String catOptionUid, @NonNull Geometry coordinates);

    Observable<String> scheduleEvent(String enrollmentUid, @Nullable String trackedEntityInstanceUid,
                                     @NonNull String program,
                                     @NonNull String programStage, @NonNull Date dueDate,
                                     @NonNull String orgUnitUid, @NonNull String catComboUid,
                                     @NonNull String catOptionUid, @NonNull Geometry coordinates);

    @NonNull
    Observable<ProgramStage> programStage(String programUid);

    @NonNull
    Observable<ProgramStage> programStageWithId(String programStageUid);

    Observable<Boolean> accessDataWrite(String programId);

    void deleteEvent(String eventId, String trackedEntityInstance);

    boolean isEnrollmentOpen();

    Observable<Program> getProgramWithId(String programUid);

    Flowable<ProgramStage> programStageForEvent(String eventId);

    boolean showCompletionPercentage();

    Flowable<List<FormSectionViewModel>> eventSections();

    Flowable<List<FieldUiModel>> list();

    Flowable<Result<RuleEffect>> calculate();

    Flowable<EventEditableStatus> getEditableStatus();

    Observable<String> permanentReferral(
            String enrollmentUid,
            @NonNull String teiUid,
            @NonNull String programUid,
            @NonNull String programStage,
            @NonNull Date dueDate,
            @NonNull String orgUnitUid,
            @Nullable String categoryOptionsUid,
            @Nullable String categoryOptionComboUid,
            @NonNull Geometry geometry
    );
}
