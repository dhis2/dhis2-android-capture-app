package org.dhis2.usescases.eventsWithoutRegistration.eventCapture;

import android.support.annotation.NonNull;

import org.dhis2.data.forms.FormSectionViewModel;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.usescases.general.AbstractActivityContracts;
import org.dhis2.utils.Result;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.rules.models.RuleEffect;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        void attemptToFinish();
    }

    public interface Presenter extends AbstractActivityContracts.Presenter {
        void init(EventCaptureContract.View view);

        void subscribeToSection();

        void onNextSection();

        void onPreviousSection();

        Observable<List<OrganisationUnitModel>> getOrgUnits();

        void onSectionSelectorClick(boolean isCurrentSection, int position);

        void initCompletionPercentage(FlowableProcessor<Float> integerFlowableProcessor);
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
        Flowable <List<FieldViewModel>> list();
        @NonNull
        Flowable<Result<RuleEffect>> calculate();
    }

}
