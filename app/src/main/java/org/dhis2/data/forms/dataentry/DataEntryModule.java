package org.dhis2.data.forms.dataentry;

import android.content.Context;

import androidx.annotation.NonNull;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.R;
import org.dhis2.data.dagger.PerFragment;
import org.dhis2.data.forms.FormRepository;
import org.dhis2.data.forms.dataentry.fields.FieldViewModelFactory;
import org.dhis2.data.forms.dataentry.fields.FieldViewModelFactoryImpl;
import org.dhis2.data.schedulers.SchedulerProvider;
import org.dhis2.utils.RulesUtilsProvider;
import org.hisp.dhis.android.core.D2;

import dagger.Module;
import dagger.Provides;

import static android.text.TextUtils.isEmpty;


@PerFragment
@Module(includes = DataEntryStoreModule.class)
public class DataEntryModule {

    @NonNull
    private final Context context;

    @NonNull
    private final FieldViewModelFactory modelFactory;

    @NonNull
    private final DataEntryArguments arguments;

    DataEntryModule(@NonNull Context context, @NonNull DataEntryArguments arguments) {
        this.arguments = arguments;
        this.context = context;
        this.modelFactory = new FieldViewModelFactoryImpl(
                context.getString(R.string.enter_text),
                context.getString(R.string.enter_long_text),
                context.getString(R.string.enter_number),
                context.getString(R.string.enter_integer),
                context.getString(R.string.enter_positive_integer),
                context.getString(R.string.enter_negative_integer),
                context.getString(R.string.enter_positive_integer_or_zero),
                context.getString(R.string.filter_options),
                context.getString(R.string.choose_date));
    }

    @Provides
    @PerFragment
    RuleEngineRepository ruleEngineRepository(@NonNull BriteDatabase briteDatabase,
                                              @NonNull FormRepository formRepository,
                                              @NonNull D2 d2) {
        if (!isEmpty(arguments.event())) { // NOPMD
            return new EventsRuleEngineRepository(briteDatabase,
                    formRepository, arguments.event());
        } else if (!isEmpty(arguments.enrollment())) { //NOPMD
            return new EnrollmentRuleEngineRepository(briteDatabase,
                    formRepository, arguments.enrollment(), d2);
        } else {
            throw new IllegalArgumentException("Unsupported entity type");
        }
    }

    @Provides
    @PerFragment
    DataEntryPresenter dataEntryPresenter(
            @NonNull SchedulerProvider schedulerProvider,
            @NonNull DataEntryStore dataEntryStore,
            @NonNull DataEntryRepository dataEntryRepository,
            @NonNull RuleEngineRepository ruleEngineRepository,
            @NonNull RulesUtilsProvider ruleUtils) {
        return new DataEntryPresenterImpl(dataEntryStore,
                dataEntryRepository, ruleEngineRepository, schedulerProvider, ruleUtils);
    }

    @Provides
    @PerFragment
    DataEntryRepository dataEntryRepository(@NonNull BriteDatabase briteDatabase, @NonNull D2 d2) {
        if (!isEmpty(arguments.event())) { // NOPMD
            return new ProgramStageRepository(briteDatabase, modelFactory,
                    arguments.event(), arguments.section(), d2);
        } else if (!isEmpty(arguments.enrollment())) { //NOPMD
            return new EnrollmentRepository(context, modelFactory, arguments.enrollment(), d2);
        } else {
            throw new IllegalArgumentException("Unsupported entity type");
        }
    }
}
