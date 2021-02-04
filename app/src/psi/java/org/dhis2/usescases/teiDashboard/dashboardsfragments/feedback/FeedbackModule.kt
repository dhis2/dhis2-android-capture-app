package org.dhis2.usescases.teiDashboard.dashboardsfragments.feedback

import android.content.Context
import dagger.Module
import dagger.Provides
import org.dhis2.data.dagger.PerFragment
import org.dhis2.data.dhislogic.DhisEventUtils
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.TeiDataRepository
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.TeiDataRepositoryImpl
import org.dhis2.usescases.teiDashboard.dashboardsfragments.systemInfo.GetSystemInfo
import org.dhis2.usescases.teiDashboard.dashboardsfragments.systemInfo.SystemInfoD2Repository
import org.dhis2.usescases.teiDashboard.dashboardsfragments.systemInfo.SystemInfoRepository
import org.hisp.dhis.android.core.D2

@PerFragment
@Module
class FeedbackModule(
    private val programUid: String,
    private val teiUid: String,
    private val enrollmentUid: String,
    private val context: Context
) {
    @Provides
    @PerFragment
    fun provideFeedbackPresenter(
        feedbackProgramRepository: FeedbackProgramRepository
    ): FeedbackPresenter {
        return FeedbackPresenter(feedbackProgramRepository)
    }

    @Provides
    @PerFragment
    fun provideFeedbackContentPresenter(
        getFeedback: GetFeedback,
        getSystemInfo: GetSystemInfo
    ): FeedbackContentPresenter {
        return FeedbackContentPresenter(getFeedback, getSystemInfo)
    }

    @Provides
    @PerFragment
    fun provideGetFeedback(
        teiDataRepository: TeiDataRepository,
        valuesRepository: ValuesRepository,
        dataElementRepository: DataElementRepository
    ): GetFeedback {
        return GetFeedback(teiDataRepository, dataElementRepository, valuesRepository)
    }

    @Provides
    @PerFragment
    fun provideGetSystemInfo(systemInfoRepository: SystemInfoRepository): GetSystemInfo {
        return GetSystemInfo(systemInfoRepository)
    }

    @Provides
    @PerFragment
    fun providesFeedbackProgramRepository(d2: D2): FeedbackProgramRepository {
        return D2FeedbackProgramRepository(d2)
    }

    @Provides
    @PerFragment
    fun provideTeiDataRepository(d2: D2, dhisEventUtils: DhisEventUtils): TeiDataRepository {
        return TeiDataRepositoryImpl(d2, programUid, teiUid, enrollmentUid, dhisEventUtils)
    }

    @Provides
    @PerFragment
    fun provideValuesRepository(d2: D2): ValuesRepository {
        return ValuesD2Repository(d2, context)
    }

    @Provides
    @PerFragment
    fun provideDataElementRepository(d2: D2): DataElementRepository {
        return DataElementD2Repository(d2)
    }

    @Provides
    @PerFragment
    fun provideSystemInfoRepository(d2: D2): SystemInfoRepository {
        return SystemInfoD2Repository(d2)
    }
}
