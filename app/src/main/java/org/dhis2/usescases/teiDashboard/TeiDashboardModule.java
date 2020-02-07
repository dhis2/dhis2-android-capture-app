package org.dhis2.usescases.teiDashboard;

import androidx.annotation.NonNull;

import org.dhis2.data.dagger.PerActivity;
import org.dhis2.data.forms.EnrollmentFormRepository;
import org.dhis2.data.forms.FormRepository;
import org.dhis2.data.forms.RulesRepository;
import org.dhis2.data.schedulers.SchedulerProvider;
import org.dhis2.utils.analytics.AnalyticsHelper;
import org.dhis2.utils.resources.ResourceManager;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.enrollment.EnrollmentCollectionRepository;
import org.hisp.dhis.rules.RuleExpressionEvaluator;

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
    private final TeiDashboardContracts.View view;

    public TeiDashboardModule(TeiDashboardContracts.View view, String teiUid, String programUid) {
        this.view = view;
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
    TeiDashboardContracts.Presenter providePresenter(DashboardRepository dashboardRepository, SchedulerProvider schedulerProvider, AnalyticsHelper analyticsHelper) {
        return new TeiDashboardPresenter(view, teiUid, programUid, dashboardRepository, schedulerProvider, analyticsHelper);
    }

    @Provides
    @PerActivity
    DashboardRepository dashboardRepository(D2 d2, ResourceManager resources) {
        return new DashboardRepositoryImpl(d2, teiUid, programUid, resources);
    }

    @Provides
    @PerActivity
    RulesRepository rulesRepository(@NonNull D2 d2) {
        return new RulesRepository(d2);
    }

    @Provides
    @PerActivity
    FormRepository formRepository(
            @NonNull RuleExpressionEvaluator evaluator,
            @NonNull RulesRepository rulesRepository,
            D2 d2) {
        EnrollmentCollectionRepository enrollmentRepository = d2.enrollmentModule().enrollments()
                .byTrackedEntityInstance().eq(teiUid);
        if (!isEmpty(programUid))
            enrollmentRepository = enrollmentRepository.byProgram().eq(programUid);

        String uid = enrollmentRepository.one().blockingGet().uid();

        return new EnrollmentFormRepository(evaluator, rulesRepository, uid, d2);
    }
}
