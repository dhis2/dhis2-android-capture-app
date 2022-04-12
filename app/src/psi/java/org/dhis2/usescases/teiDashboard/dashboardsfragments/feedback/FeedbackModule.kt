package org.dhis2.usescases.teiDashboard.dashboardsfragments.feedback

import android.content.Context
import dagger.Module
import dagger.Provides
import org.dhis2.commons.di.dagger.PerActivity
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.data.dhislogic.DhisPeriodUtils
import org.dhis2.data.forms.RulesRepository
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.TeiDataRepository
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.TeiDataRepositoryImpl
import org.dhis2.usescases.teiDashboard.dashboardsfragments.enrollment.EnrollmentInfoD2Repository
import org.dhis2.usescases.teiDashboard.dashboardsfragments.enrollment.EnrollmentInfoRepository
import org.dhis2.usescases.teiDashboard.dashboardsfragments.enrollment.GetEnrollmentInfo
import org.dhis2.usescases.teiDashboard.dashboardsfragments.systemInfo.GetSystemInfo
import org.dhis2.usescases.teiDashboard.dashboardsfragments.systemInfo.SystemInfoD2Repository
import org.dhis2.usescases.teiDashboard.dashboardsfragments.systemInfo.SystemInfoRepository
import org.hisp.dhis.android.core.D2

@PerActivity
@Module
class FeedbackModule(
    private val programUid: String,
    private val teiUid: String,
    private val enrollmentUid: String,
    private val context: Context
) {
    @Provides
    @PerActivity
    fun provideFeedbackPresenter(
        feedbackProgramRepository: FeedbackProgramRepository,
        preferenceProvider: PreferenceProvider
    ): FeedbackPresenter {
        return FeedbackPresenter(feedbackProgramRepository, preferenceProvider)
    }

    @Provides
    @PerActivity
    fun provideFeedbackContentPresenter(
        getFeedback: GetFeedback,
        getSystemInfo: GetSystemInfo,
        getEnrollmentInfo: GetEnrollmentInfo
    ): FeedbackContentPresenter {
        return FeedbackContentPresenter(getFeedback, getSystemInfo, getEnrollmentInfo)
    }

    @Provides
    @PerActivity
    fun provideGetFeedback(
        teiDataRepository: TeiDataRepository,
        valuesRepository: ValuesRepository,
        dataElementRepository: DataElementRepository
    ): GetFeedback {
        return GetFeedback(teiDataRepository, dataElementRepository, valuesRepository)
    }

    @Provides
    @PerActivity
    fun provideGetSystemInfo(systemInfoRepository: SystemInfoRepository): GetSystemInfo {
        return GetSystemInfo(systemInfoRepository)
    }

    @Provides
    @PerActivity
    fun provideGetEnrollmentInfo(enrollmentInfoRepository: EnrollmentInfoRepository): GetEnrollmentInfo {
        return GetEnrollmentInfo(enrollmentInfoRepository)
    }

    @Provides
    @PerActivity
    fun providesFeedbackProgramRepository(d2: D2): FeedbackProgramRepository {
        return D2FeedbackProgramRepository(d2)
    }

    @Provides
    @PerActivity
    fun provideTeiDataRepository(d2: D2, dhisPeriodUtils: DhisPeriodUtils): TeiDataRepository {
        return TeiDataRepositoryImpl(d2, programUid, teiUid, enrollmentUid, dhisPeriodUtils)
    }

    @Provides
    @PerActivity
    fun provideValuesRepository(d2: D2): ValuesRepository {
        return ValuesD2Repository(d2, context)
    }

    @Provides
    @PerActivity
    fun provideDataElementRepository(d2: D2): DataElementRepository {
        return DataElementD2Repository(d2)
    }

    @Provides
    @PerActivity
    fun provideSystemInfoRepository(d2: D2): SystemInfoRepository {
        return SystemInfoD2Repository(d2)
    }

    @Provides
    @PerActivity
    fun provideEnrollmentInfoRepository(d2: D2): EnrollmentInfoRepository {
        return EnrollmentInfoD2Repository(d2)
    }
}
