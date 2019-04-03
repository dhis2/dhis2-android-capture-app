package org.dhis2.usescases.teiDashboard.dashboardfragments.indicators;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.data.dagger.PerFragment;
import org.dhis2.data.forms.EnrollmentFormRepository;
import org.dhis2.data.forms.FormRepository;
import org.dhis2.data.forms.RulesRepository;
import org.dhis2.data.forms.dataentry.EnrollmentRuleEngineRepository;
import org.dhis2.data.forms.dataentry.RuleEngineRepository;
import org.dhis2.data.metadata.MetadataRepository;
import org.dhis2.usescases.teiDashboard.DashboardRepository;
import org.dhis2.usescases.teiDashboard.DashboardRepositoryImpl;
import org.dhis2.utils.CodeGenerator;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.enrollment.EnrollmentCollectionRepository;
import org.hisp.dhis.rules.RuleExpressionEvaluator;

import androidx.annotation.NonNull;
import dagger.Module;
import dagger.Provides;

import static android.text.TextUtils.isEmpty;

@Module
public class IndicatorsModule {

    public final String programUid;
    public final String teiUid;

    public IndicatorsModule(String teiUid, String programUid) {
        this.teiUid = teiUid;
        this.programUid = programUid;
    }

    @Provides
    @PerFragment
    IndicatorsPresenter providePresenter(D2 d2, DashboardRepository dashboardRepository,
                                         MetadataRepository metadataRepository,
                                         RuleEngineRepository ruleRepository) {
        return new IndicatorsPresenterImpl(d2, dashboardRepository, metadataRepository, ruleRepository,
                programUid, teiUid);
    }

    @Provides
    @PerFragment
    DashboardRepository dashboardRepository(CodeGenerator codeGenerator, BriteDatabase briteDatabase, D2 d2) {
        return new DashboardRepositoryImpl(codeGenerator, briteDatabase, d2);
    }

    @Provides
    @PerFragment
    RulesRepository rulesRepository(@NonNull BriteDatabase briteDatabase) {
        return new RulesRepository(briteDatabase);
    }

    @Provides
    @PerFragment
    FormRepository formRepository(@NonNull BriteDatabase briteDatabase,
                                  @NonNull RuleExpressionEvaluator evaluator,
                                  @NonNull RulesRepository rulesRepository,
                                  @NonNull CodeGenerator codeGenerator,
                                  D2 d2) {
        EnrollmentCollectionRepository enrollmentRepository = d2.enrollmentModule().enrollments
                .byTrackedEntityInstance().eq(teiUid);
        if (!isEmpty(programUid))
            enrollmentRepository = enrollmentRepository.byProgram().eq(programUid);

        String uid = enrollmentRepository.one().get().uid();

        return new EnrollmentFormRepository(briteDatabase, evaluator, rulesRepository, codeGenerator, uid, d2);
    }

    @Provides
    @PerFragment
    RuleEngineRepository ruleEngineRepository(@NonNull BriteDatabase briteDatabase,
                                              @NonNull FormRepository formRepository,
                                              D2 d2) {
        EnrollmentCollectionRepository enrollmentRepository = d2.enrollmentModule().enrollments
                .byTrackedEntityInstance().eq(teiUid);
        if (!isEmpty(programUid))
            enrollmentRepository = enrollmentRepository.byProgram().eq(programUid);

        String uid = enrollmentRepository.one().get().uid();
        return new EnrollmentRuleEngineRepository(briteDatabase, formRepository, uid, d2);

    }
}
