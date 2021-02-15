package org.dhis2.usescases.teiDashboard.dashboardsfragments.feedback

import android.content.Context
import dagger.Module
import dagger.Provides
import org.dhis2.Bindings.valueTypeHintMap
import org.dhis2.data.dagger.PerActivity
import org.dhis2.data.dagger.PerFragment
import org.dhis2.data.dhislogic.DhisEventUtils
import org.dhis2.data.forms.EventRepository
import org.dhis2.data.forms.FormRepository
import org.dhis2.data.forms.RulesRepository
import org.dhis2.data.forms.dataentry.fields.FieldViewModelFactory
import org.dhis2.data.forms.dataentry.fields.FieldViewModelFactoryImpl
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureContract
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureContract.EventCaptureRepository
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureRepositoryImpl
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.TeiDataRepository
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.TeiDataRepositoryImpl
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.rules.RuleExpressionEvaluator

@PerFragment
@Module
class FeedbackModule(
    private val programUid: String,
    private val teiUid: String,
    private val enrollmentUid: String
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
    fun provideFeedbackContentPresenter(getFeedback: GetFeedback): FeedbackContentPresenter {
        return FeedbackContentPresenter(getFeedback)
    }

    @Provides
    @PerFragment
    fun provideGetFeedback(
        teiDataRepository: TeiDataRepository,
        valuesRepository: ValuesRepository
    ): GetFeedback {
        return GetFeedback(teiDataRepository, valuesRepository)
    }

    @Provides
    @PerFragment
    fun providesFeedbackProgramRepository(d2: D2): FeedbackProgramRepository {
        return D2FeedbackProgramRepository(d2)
    }

    @Provides
    @PerFragment
    fun provideTeiDataRepository(d2: D2,dhisEventUtils: DhisEventUtils): TeiDataRepository {
        return TeiDataRepositoryImpl(d2, programUid, teiUid, enrollmentUid,dhisEventUtils)
    }

    @Provides
    @PerFragment
    fun provideValuesRepository(d2: D2): ValuesRepository {
        return ValuesD2Repository( d2)
    }
}
