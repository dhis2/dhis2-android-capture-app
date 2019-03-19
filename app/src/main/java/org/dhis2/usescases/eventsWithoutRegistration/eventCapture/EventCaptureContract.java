package org.dhis2.usescases.eventsWithoutRegistration.eventCapture;

import org.dhis2.data.forms.FormSectionViewModel;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.usescases.general.AbstractActivityContracts;
import org.dhis2.utils.Result;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.rules.models.RuleEffect;

import java.util.Date;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.processors.FlowableProcessor;

/**
 * QUADRAM. Created by ppajuelo on 19/11/2018.
 */
public class EventCaptureContract {

    public interface View extends AbstractActivityContracts.View {

        void renderInitialInfo(String stageName, String eventDate, String orgUnit, String catOption);

        EventCaptureContract.Presenter getPresenter();

        void setUp();

        Consumer<Float> updatePercentage();

        void setMandatoryWarning(Map<String, FieldViewModel> emptyMandatoryFields);

        void attemptToFinish(boolean canComplete);

        void showCompleteActions(boolean canComplete);

        void restartDataEntry();

        void finishDataEntry();

        void setShowError(Map<String, String> errors);

        void showMessageOnComplete(boolean canComplete, String completeMessage);

        void attemptToReopen();

        void showSnackBar(int messageId);

        android.view.View getSnackbarAnchor();

        void clearFocus();

        void attemptToSkip();

        void attemptToReschedule();

        void setProgramStage(String programStageUid);
    }

    public interface Presenter extends AbstractActivityContracts.Presenter {
        void init(EventCaptureContract.View view);

        void onBackClick();

        void subscribeToSection();

        void onNextSection();

        void onPreviousSection();

        Observable<List<OrganisationUnitModel>> getOrgUnits();

        ObservableField<String> getCurrentSection();

        boolean isEnrollmentOpen();

        void onSectionSelectorClick(boolean isCurrentSection, int position, String sectionUid);

        void initCompletionPercentage(FlowableProcessor<Float> integerFlowableProcessor);

        void goToSection(String sectionUid);

        void completeEvent(boolean addNew);

        void reopenEvent();

        void deleteEvent();

        void skipEvent();

        void rescheduleEvent(Date time);

        boolean canWrite();

        boolean hasExpired();
    }

    public interface EventCaptureRepository {

        Flowable<String> programStageName();

        Flowable<String> eventDate();

        Flowable<String> orgUnit();

        Flowable<String> catOption();

        Flowable<List<FormSectionViewModel>> eventSections();

        @NonNull
        Flowable<List<FieldViewModel>> list(String sectionUid);

        @NonNull
        Flowable<List<FieldViewModel>> list();

        @NonNull
        Flowable<Result<RuleEffect>> calculate();

        @NonNull
        Flowable<Result<RuleEffect>> calculate(String lastUpdatedElement);

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
    }

}
