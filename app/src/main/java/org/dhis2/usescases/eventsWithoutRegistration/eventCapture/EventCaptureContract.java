package org.dhis2.usescases.eventsWithoutRegistration.eventCapture;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.dhis2.data.forms.FormSectionViewModel;
import org.dhis2.form.model.FieldUiModel;
import org.dhis2.form.model.RowAction;
import org.dhis2.usescases.general.AbstractActivityContracts;
import org.dhis2.utils.Result;
import org.dhis2.utils.RulesUtilsProviderConfigurationError;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.rules.models.RuleEffect;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.List;
import java.util.Map;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.subjects.BehaviorSubject;

/**
 * QUADRAM. Created by ppajuelo on 19/11/2018.
 */
public class EventCaptureContract {

    public interface View extends AbstractActivityContracts.View {

        void renderInitialInfo(String stageName, String eventDate, String orgUnit, String catOption);

        EventCaptureContract.Presenter getPresenter();

        void updatePercentage(float primaryValue);

        void showCompleteActions(boolean canComplete, String completeMessage, Map<String, String> errors, Map<String, FieldUiModel> emptyMandatoryFields);

        void updateProgramStageName(String stageName);

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

        void showLoopWarning();

        void goBack();

        void showProgress();

        void hideProgress();

        void showNavigationBar();

        void hideNavigationBar();

        void displayConfigurationErrors(List<RulesUtilsProviderConfigurationError> configurationError);
    }

    public interface Presenter extends AbstractActivityContracts.Presenter {

        void init();

        BehaviorSubject<List<FieldUiModel>> formFieldsFlowable();

        void onBackClick();

        void nextCalculation(boolean doNextCalculation);

        void attempFinish();

        boolean isEnrollmentOpen();

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

        void hideProgress();

        void showProgress();

        boolean getCompletionPercentageVisibility();

        void setValueChanged(@NotNull String uid);

        void disableConfErrorMessage();
    }

    public interface EventCaptureRepository {

        Flowable<Boolean> eventIntegrityCheck();

        Flowable<String> programStageName();

        Flowable<String> eventDate();

        Flowable<OrganisationUnit> orgUnit();

        Flowable<String> catOption();

        Flowable<List<FormSectionViewModel>> eventSections();

        @NonNull
        Flowable<List<FieldUiModel>> list(FlowableProcessor<RowAction> processor);

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

        boolean isEnrollmentCancelled();

        boolean isEventEditable(String eventUid);

        String getSectionFor(String field);

        Single<Boolean> canReOpenEvent();

        Observable<Boolean> isCompletedEventExpired(String eventUid);

        Single<Integer> getNoteCount();

        List<String> getOptionsFromGroups(List<String> optionGroupUids);

        boolean showCompletionPercentage();

        void updateFieldValue(String uid);
    }

}
