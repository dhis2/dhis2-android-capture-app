package org.dhis2.usescases.eventswithoutregistration.eventcapture;

import androidx.annotation.NonNull;

import org.dhis2.data.forms.FormRepository;
import org.dhis2.data.forms.dataentry.RuleEngineRepository;
import org.dhis2.form.bindings.RuleExtensionsKt;
import org.dhis2.utils.Result;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.program.ProgramStage;
import org.hisp.dhis.rules.RuleEngine;
import org.hisp.dhis.rules.models.RuleDataValue;
import org.hisp.dhis.rules.models.RuleEffect;
import org.hisp.dhis.rules.models.RuleEvent;

import java.util.List;

import io.reactivex.Flowable;

public final class EventRuleEngineRepository implements RuleEngineRepository {

    D2 d2;
    FormRepository formRepository;
    String eventUid;
    private RuleEvent.Builder eventBuilder;

    public EventRuleEngineRepository(D2 d2, FormRepository formRepository, String eventUid) {
        this.d2 = d2;
        this.formRepository = formRepository;
        this.eventUid = eventUid;

        initData();
    }

    public void initData() {
        eventBuilder = RuleEvent.builder();
        if (eventUid != null) {
            Event currentEvent = d2.eventModule().events().withTrackedEntityDataValues().uid(eventUid).blockingGet();
            ProgramStage currentStage = d2.programModule().programStages().uid(currentEvent.programStage()).blockingGet();
            OrganisationUnit ou = d2.organisationUnitModule().organisationUnits().uid(currentEvent.organisationUnit()).blockingGet();

            eventBuilder
                    .event(currentEvent.uid())
                    .programStage(currentEvent.programStage())
                    .programStageName(currentStage.displayName())
                    .status(RuleEvent.Status.valueOf(currentEvent.status().name()))
                    .eventDate(currentEvent.eventDate())
                    .dueDate(currentEvent.dueDate() != null ? currentEvent.dueDate() : currentEvent.eventDate())
                    .organisationUnit(currentEvent.organisationUnit())
                    .organisationUnitCode(ou.code());
        }
    }

    @Override
    public Flowable<RuleEngine> updateRuleEngine() {
        return this.formRepository.restartRuleEngine();
    }

    @NonNull
    @Override
    public Flowable<Result<RuleEffect>> calculate() {
        return queryDataValues(eventUid)
                .switchMap(dataValues ->
                        formRepository.ruleEngine()
                                .flatMap(ruleEngine ->
                                        Flowable.fromCallable(
                                                ruleEngine.evaluate(
                                                        eventBuilder.dataValues(dataValues).build()
                                                ))
                                                .map(Result::success)
                                                .onErrorReturn(error->Result.failure(new Exception(error)))

                                )
                );
    }

    @NonNull
    @Override
    public Flowable<Result<RuleEffect>> reCalculate() {
        initData();
        return calculate();
    }

    @NonNull
    private Flowable<List<RuleDataValue>> queryDataValues(String eventUid) {
        return d2.eventModule().events().uid(eventUid).get()
                .flatMap(event -> d2.trackedEntityModule().trackedEntityDataValues().byEvent().eq(eventUid).byValue().isNotNull().get()
                        .map(values -> RuleExtensionsKt.toRuleDataValue(values, event, d2.dataElementModule().dataElements(), d2.programModule().programRuleVariables(), d2.optionModule().options()))).toFlowable();
    }
}
