package org.dhis2.usescases.teiDashboard.dashboardfragments.indicators;

import androidx.annotation.NonNull;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.data.dagger.PerActivity;
import org.dhis2.data.dagger.PerFragment;
import org.dhis2.data.forms.FormRepository;
import org.dhis2.data.forms.dataentry.EnrollmentRuleEngineRepository;
import org.dhis2.data.forms.dataentry.RuleEngineRepository;
import org.dhis2.usescases.teiDashboard.DashboardRepository;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.enrollment.EnrollmentCollectionRepository;

import dagger.Module;
import dagger.Provides;

import static android.text.TextUtils.isEmpty;

/**
 * QUADRAM. Created by ppajuelo on 09/04/2019.
 */
@PerFragment
@Module
public class IndicatorsModule {

    private final String programUid;
    private final String teiUid;

    public IndicatorsModule(String programUid, String teiUid) {
        this.programUid = programUid;
        this.teiUid = teiUid;
    }

    @Provides
    @PerFragment
    IndicatorsContracts.Presenter providesPresenter(D2 d2, DashboardRepository dashboardRepository, RuleEngineRepository ruleEngineRepository) {
        return new IndicatorsPresenterImpl(d2, programUid, teiUid, dashboardRepository, ruleEngineRepository);
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
