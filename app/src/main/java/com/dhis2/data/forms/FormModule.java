package com.dhis2.data.forms;

import android.support.annotation.NonNull;

import com.dhis2.data.schedulers.SchedulerProvider;
import com.dhis2.usescases.general.AbstractActivityContracts;
import com.dhis2.utils.CodeGenerator;
import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.rules.RuleExpressionEvaluator;

import dagger.Module;
import dagger.Provides;

@Module
@PerForm
public class FormModule {

    @NonNull
    private final FormViewArguments formViewArguments;

    public FormModule(@NonNull FormViewArguments formViewArguments) {
        this.formViewArguments = formViewArguments;
    }


    @Provides
    @PerForm
    FormPresenter formPresenter(@NonNull SchedulerProvider schedulerProvider,
                                @NonNull BriteDatabase briteDatabase,
                                @NonNull FormRepository formRepository) {
        return new FormPresenterImpl(formViewArguments, schedulerProvider, briteDatabase, formRepository);
    }

    @Provides
    @PerForm
    RulesRepository rulesRepository(@NonNull BriteDatabase briteDatabase) {
        return new RulesRepository(briteDatabase);
    }

    @Provides
    @PerForm
    FormRepository formRepository(@NonNull BriteDatabase briteDatabase,
                                  @NonNull RuleExpressionEvaluator evaluator,
                                  @NonNull RulesRepository rulesRepository,
                                  @NonNull CodeGenerator codeGenerator
            /*@NonNull CurrentDateProvider currentDateProvider*/) {
        if (formViewArguments.type().equals(FormViewArguments.Type.ENROLLMENT)) {
            return new EnrollmentFormRepository(briteDatabase, evaluator, rulesRepository,
                    codeGenerator, formViewArguments.uid());
        } else if (formViewArguments.type().equals(FormViewArguments.Type.EVENT)) {
            return new EventRepository(briteDatabase, evaluator,
                    rulesRepository, formViewArguments.uid());
        } else {
            throw new IllegalArgumentException("FormViewArguments of " +
                    "unexpected type: " + formViewArguments.type());
        }
    }
}