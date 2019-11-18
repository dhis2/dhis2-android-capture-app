package org.dhis2.usescases.eventsWithoutRegistration.eventSummary;

import androidx.annotation.NonNull;

import org.dhis2.data.forms.FormSectionViewModel;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.utils.Result;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.rules.models.RuleEffect;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;

/**
 * QUADRAM. Created by Cristian E. on 02/11/2017.
 */

public interface EventSummaryRepository {

    @NonNull
    Flowable<List<FormSectionViewModel>> programStageSections(String eventUid);

    @NonNull
    Flowable<List<FieldViewModel>> list(String sectionUid, String eventUid);

    @NonNull
    Flowable<Result<RuleEffect>> calculate();

    Observable<Event> changeStatus(String eventUid);

    Flowable<Event> getEvent(String eventId);

    Observable<Boolean> accessDataWrite(String eventId);

    boolean isEnrollmentOpen();

    Observable<Program> getProgramWithId(String programUid);
}
