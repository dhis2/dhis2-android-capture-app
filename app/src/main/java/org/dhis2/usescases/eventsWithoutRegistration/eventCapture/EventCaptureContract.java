package org.dhis2.usescases.eventsWithoutRegistration.eventCapture;

import androidx.lifecycle.LiveData;

import org.dhis2.ui.dialogs.bottomsheet.FieldWithIssue;
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.model.EventCompletionDialog;
import org.dhis2.usescases.general.AbstractActivityContracts;
import org.hisp.dhis.android.core.common.ValidationStrategy;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;
import java.util.List;
import java.util.Map;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;

public class EventCaptureContract {

    public interface View extends AbstractActivityContracts.View {

        void renderInitialInfo(String stageName, String eventDate, String orgUnit, String catOption);

        EventCaptureContract.Presenter getPresenter();

        void updatePercentage(float primaryValue);

        void showCompleteActions(
                boolean canComplete,
                Map<String, String> emptyMandatoryFields,
                EventCompletionDialog eventCompletionDialog);

        void updateProgramStageName(String stageName);

        void restartDataEntry();

        void finishDataEntry();

        void SaveAndFinish();

        void showSnackBar(int messageId);

        void attemptToSkip();

        void attemptToReschedule();

        void showEventIntegrityAlert();

        void updateNoteBadge(int numberOfNotes);

        void goBack();

        void showProgress();

        void hideProgress();

        void showNavigationBar();

        void hideNavigationBar();
    }

    public interface Presenter extends AbstractActivityContracts.Presenter {

        LiveData<EventCaptureAction> observeActions();

        void init();

        void onBackClick();

        void attemptFinish(boolean canComplete,
                           @Nullable String onCompleteMessage,
                           List<FieldWithIssue> errorFields,
                           Map<String, String> emptyMandatoryFields,
                           List<FieldWithIssue> warningFields);

        boolean isEnrollmentOpen();

        void completeEvent(boolean addNew);

        void deleteEvent();

        void skipEvent();

        void rescheduleEvent(Date time);

        boolean canWrite();

        boolean hasExpired();

        void initNoteCounter();

        void refreshTabCounters();

        void refreshProgramStage();

        void hideProgress();

        void showProgress();

        boolean getCompletionPercentageVisibility();

        void emitAction(@NotNull EventCaptureAction onBack);
    }

    public interface EventCaptureRepository {

        Flowable<Boolean> eventIntegrityCheck();

        Flowable<String> programStageName();

        Flowable<String> eventDate();

        Flowable<OrganisationUnit> orgUnit();

        Flowable<String> catOption();

        Observable<Boolean> completeEvent();

        Flowable<EventStatus> eventStatus();

        boolean isEnrollmentOpen();

        Observable<Boolean> deleteEvent();

        Observable<Boolean> updateEventStatus(EventStatus skipped);

        Observable<Boolean> rescheduleEvent(Date time);

        Observable<String> programStage();

        boolean getAccessDataWrite();

        boolean isEnrollmentCancelled();

        boolean isEventEditable(String eventUid);

        Single<Boolean> canReOpenEvent();

        Observable<Boolean> isCompletedEventExpired(String eventUid);

        Single<Integer> getNoteCount();

        boolean showCompletionPercentage();

        boolean hasAnalytics();

        boolean hasRelationships();

        ValidationStrategy validationStrategy();
    }

}
