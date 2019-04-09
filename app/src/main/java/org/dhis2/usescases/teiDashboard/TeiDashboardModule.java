package org.dhis2.usescases.teiDashboard;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.data.dagger.PerActivity;
import org.dhis2.data.forms.EnrollmentFormRepository;
import org.dhis2.data.forms.FormRepository;
import org.dhis2.data.forms.RulesRepository;
import org.dhis2.data.forms.dataentry.EnrollmentRuleEngineRepository;
import org.dhis2.data.forms.dataentry.RuleEngineRepository;
import org.dhis2.data.metadata.MetadataRepository;
import org.dhis2.usescases.teiDashboard.mobile.TeiDashboardMobileActivity;
import org.dhis2.utils.CodeGenerator;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.enrollment.EnrollmentCollectionRepository;
import org.hisp.dhis.rules.RuleExpressionEvaluator;

import androidx.annotation.NonNull;
import dagger.Module;
import dagger.Provides;

import static android.text.TextUtils.isEmpty;

/**
 * QUADRAM. Created by ppajuelo on 30/11/2017.
 */
@PerActivity
@Module
public class TeiDashboardModule {

    public final String programUid;
    public final String teiUid;

    public TeiDashboardModule(String teiUid, String programUid) {
        this.teiUid = teiUid;
        this.programUid = programUid;
    }

    @Provides
    @PerActivity
    TeiDashboardContracts.View provideView(TeiDashboardMobileActivity mobileActivity) {
        return mobileActivity;
    }

    @Provides
    @PerActivity
    TeiDashboardContracts.Presenter providePresenter(D2 d2, DashboardRepository dashboardRepository,
                                                     MetadataRepository metadataRepository,
                                                     RuleEngineRepository ruleRepository) {
        return new TeiDashboardPresenter(d2, dashboardRepository, metadataRepository, ruleRepository);
    }

    @Provides
    @PerActivity
    DashboardRepository dashboardRepository(CodeGenerator codeGenerator, BriteDatabase briteDatabase, D2 d2) {
        return new DashboardRepositoryImpl(codeGenerator, briteDatabase, d2);
    }

    @Provides
    @PerActivity
    RulesRepository rulesRepository(@NonNull BriteDatabase briteDatabase) {
        return new RulesRepository(briteDatabase);
    }

    @Provides
    @PerActivity
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
    @PerActivity
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
