package org.dhis2.data.forms;

import androidx.annotation.NonNull;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.data.schedulers.SchedulerProvider;
import org.dhis2.utils.CodeGenerator;
import org.hisp.dhis.android.core.D2;
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
                                @NonNull FormRepository formRepository,
                                @NonNull D2 d2) {
        return new FormPresenterImpl(formViewArguments, schedulerProvider, briteDatabase, formRepository, d2);
    }

    @Provides
    @PerForm
    RulesRepository rulesRepository(@NonNull BriteDatabase briteDatabase, @NonNull D2 d2) {
        return new RulesRepository(d2);
    }

    @Provides
    @PerForm
    FormRepository formRepository(@NonNull D2 d2,
                                  @NonNull BriteDatabase briteDatabase,
                                  @NonNull RuleExpressionEvaluator evaluator,
                                  @NonNull RulesRepository rulesRepository,
                                  @NonNull CodeGenerator codeGenerator
            /*@NonNull CurrentDateProvider currentDateProvider*/) {
        if (formViewArguments.type().equals(FormViewArguments.Type.ENROLLMENT)) {
            return new EnrollmentFormRepository(briteDatabase, evaluator, rulesRepository,
                    codeGenerator, formViewArguments.uid(), d2);
        } else if (formViewArguments.type().equals(FormViewArguments.Type.EVENT)) {
            return new EventRepository(briteDatabase, evaluator,
                    rulesRepository, formViewArguments.uid(), d2);
        } else {
            throw new IllegalArgumentException("FormViewArguments of " +
                    "unexpected type: " + formViewArguments.type());
        }
    }
}