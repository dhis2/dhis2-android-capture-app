package org.dhis2.usescases.teiDashboard.dashboardsfragments.feedback

import dagger.Module
import dagger.Provides
import org.dhis2.data.dagger.PerFragment
import org.hisp.dhis.android.core.D2

@PerFragment
@Module
class FeedbackModule() {

    @Provides
    @PerFragment
    fun providesFeedbackPresenter(
        feedbackProgramRepository: FeedbackProgramRepository
    ): FeedbackPresenter {
        return FeedbackPresenter(feedbackProgramRepository)
    }

    @Provides
    @PerFragment
    fun providesFeedbackContentPresenter(): FeedbackContentPresenter {
        return FeedbackContentPresenter()
    }

    @Provides
    @PerFragment
    fun providesFeedbackProgramRepository(d2: D2): FeedbackProgramRepository {
        return D2FeedbackProgramRepository(d2)
    }
}
