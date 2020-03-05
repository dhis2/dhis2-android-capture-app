package org.dhis2.usescases.eventsWithoutRegistration.eventCapture;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import org.dhis2.data.forms.FormSectionViewModel;
import org.dhis2.data.forms.dataentry.DataEntryStore;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.tuples.Pair;
import org.dhis2.usescases.general.AbstractActivityContracts;
import org.dhis2.utils.Result;
import org.dhis2.utils.RulesActionCallbacks;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitLevel;
import org.hisp.dhis.rules.models.RuleEffect;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.List;
import java.util.Map;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Consumer;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.subjects.BehaviorSubject;

/**
 * QUADRAM. Created by ppajuelo on 19/11/2018.
 */
public class EventCaptureContract {

    public interface View extends AbstractActivityContracts.View {

        void renderInitialInfo(String stageName, String eventDate, String orgUnit, String catOption);

        EventCaptureContract.Presenter getPresenter();

        void updatePercentage(float primaryValue, float secondaryValue);

        void showCompleteActions(boolean canComplete, String completeMessage, Map<String, String> errors, Map<String, FieldViewModel> emptyMandatoryFields);

        void restartDataEntry();

        void finishDataEntry();

        void attemptToReopen();

        void showSnackBar(int messageId);

        void clearFocus();

        void attemptToSkip();

        void attemptToReschedule();

        void setProgramStage(String programStageUid);

        void showErrorSnackBar();

        void showEventIntegrityAlert();

        void updateNoteBadge(int numberOfNotes);
    }

    public interface Presenter extends AbstractActivityContracts.Presenter {

        void init();

        BehaviorSubject<List<FieldViewModel>> formFieldsFlowable();

        void onBackClick();

        void nextCalculation(boolean doNextCalculation);

        void onNextSection();

        void attempFinish();

        void onPreviousSection();

        boolean isEnrollmentOpen();

        void goToSection(String sectionUid);

        void goToSection();

        void completeEvent(boolean addNew);

        void reopenEvent();

        void deleteEvent();

        void skipEvent();

        void rescheduleEvent(Date time);

        boolean canWrite();

        boolean hasExpired();

        void saveImage(String uuid, String filePath);

        void initNoteCounter();

        void refreshTabCounters();

        void setLastUpdatedUid(@NotNull String lastUpdatedUid);
    }

    public interface EventCaptureRepository {

        Flowable<Boolean> eventIntegrityCheck();

        Flowable<String> programStageName();

        Flowable<String> eventDate();

        Flowable<String> orgUnit();

        Flowable<String> catOption();

        Flowable<List<FormSectionViewModel>> eventSections();

        @NonNull
        Flowable<List<FieldViewModel>> list();

        @NonNull
        Flowable<Result<RuleEffect>> calculate();

        Observable<Boolean> completeEvent();

        Flowable<EventStatus> eventStatus();

        boolean reopenEvent();

        boolean isEnrollmentOpen();

        Observable<Boolean> deleteEvent();

        Observable<Boolean> updateEventStatus(EventStatus skipped);

        Observable<Boolean> rescheduleEvent(Date time);

        Observable<String> programStage();

        boolean getAccessDataWrite();

        void setLastUpdated(String lastUpdatedUid);

        boolean isEnrollmentCancelled();

        boolean isEventExpired(String eventUid);

        boolean optionIsInOptionGroup(String optionUid, String optionGroupToHide);

        String getSectionFor(String field);

        Single<Boolean> canReOpenEvent();

        Observable<Boolean> isCompletedEventExpired(String eventUid);

        Single<Integer> getNoteCount();

        List<String> getOptionsFromGroups(List<String> optionGroupUids);

        List<String> getOptionCodesFrom(List<String> optionsToHide, List<String> optionsGroupsToHide);
    }

}
