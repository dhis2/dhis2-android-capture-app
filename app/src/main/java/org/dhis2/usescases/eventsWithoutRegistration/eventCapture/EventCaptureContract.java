package org.dhis2.usescases.eventsWithoutRegistration.eventCapture;

import androidx.annotation.NonNull;

import org.dhis2.data.forms.FormSectionViewModel;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.tuples.Pair;
import org.dhis2.data.tuples.Trio;
import org.dhis2.usescases.general.AbstractActivityContracts;
import org.dhis2.utils.Result;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitLevel;
import org.hisp.dhis.android.core.program.ProgramIndicator;
import org.hisp.dhis.rules.models.RuleEffect;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.List;
import java.util.Map;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.subjects.BehaviorSubject;

/**
 * QUADRAM. Created by ppajuelo on 19/11/2018.
 */
public class EventCaptureContract {

    public interface View extends AbstractActivityContracts.View {

        void renderInitialInfo(String stageName, String eventDate, String orgUnit, String catOption);

        EventCaptureContract.Presenter getPresenter();

        void updateProgramStageName(String stageName);

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

        void showIndicatorsIcon();

        void hideIndicatorsIcon();

        void showLoopWarning();

        void goBack();

        void showProgress();

        void hideProgress();
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

        void hideProgress();

        void showProgress();
    }

    public interface EventCaptureRepository {

        Flowable<Boolean> eventIntegrityCheck();

        Flowable<String> programStageName();

        Flowable<String> eventDate();

        Flowable<OrganisationUnit> orgUnit();

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

        boolean isEventEditable(String eventUid);

        String getSectionFor(String field);

        Single<Boolean> canReOpenEvent();

        Observable<Boolean> isCompletedEventExpired(String eventUid);

        Single<Integer> getNoteCount();

        List<String> getOptionsFromGroups(List<String> optionGroupUids);

        Flowable<List<ProgramIndicator>> getIndicators( String programUid);

        Observable<Trio<ProgramIndicator, String, String>> getLegendColorForIndicator(ProgramIndicator programIndicator, String value);
    }

}
