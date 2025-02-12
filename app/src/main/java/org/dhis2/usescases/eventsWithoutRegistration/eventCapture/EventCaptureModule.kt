package org.dhis2.usescases.eventsWithoutRegistration.eventCapture

import android.content.Context
import dagger.Module
import dagger.Provides
import io.reactivex.processors.FlowableProcessor
import io.reactivex.processors.PublishProcessor
import org.dhis2.R
import org.dhis2.commons.data.EntryMode
import org.dhis2.commons.di.dagger.PerActivity
import org.dhis2.commons.network.NetworkUtils
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.reporting.CrashReportController
import org.dhis2.commons.reporting.CrashReportControllerImpl
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.data.dhislogic.DhisEnrollmentUtils
import org.dhis2.data.forms.dataentry.SearchTEIRepository
import org.dhis2.data.forms.dataentry.SearchTEIRepositoryImpl
import org.dhis2.form.data.FileController
import org.dhis2.form.data.FormValueStore
import org.dhis2.form.data.UniqueAttributeController
import org.dhis2.form.model.RowAction
import org.dhis2.form.ui.FieldViewModelFactory
import org.dhis2.mobileProgramRules.EvaluationType
import org.dhis2.mobileProgramRules.RuleEngineHelper
import org.dhis2.mobileProgramRules.RulesRepository
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureContract.EventCaptureRepository
import org.dhis2.utils.customviews.navigationbar.NavigationPageConfigurator
import org.hisp.dhis.android.core.D2

@Module
class EventCaptureModule(
    private val view: EventCaptureContract.View,
    private val eventUid: String,
    private val isPortrait: Boolean,
) {
    @Provides
    @PerActivity
    fun providePresenter(
        eventCaptureRepository: EventCaptureRepository,
        schedulerProvider: SchedulerProvider,
        preferences: PreferenceProvider,
        pageConfigurator: NavigationPageConfigurator,
        resourceManager: ResourceManager,
    ): EventCaptureContract.Presenter {
        return EventCapturePresenterImpl(
            view,
            eventUid,
            eventCaptureRepository,
            schedulerProvider,
            preferences,
            pageConfigurator,
            resourceManager,
        )
    }

    @Provides
    @PerActivity
    fun provideFieldMapper(
        context: Context,
        fieldFactory: FieldViewModelFactory,
    ): EventFieldMapper {
        return EventFieldMapper(fieldFactory, context.getString(R.string.field_is_mandatory))
    }

    @Provides
    @PerActivity
    fun provideRepository(d2: D2?): EventCaptureRepository {
        return EventCaptureRepositoryImpl(eventUid, d2)
    }

    @Provides
    @PerActivity
    fun ruleEngineRepository(d2: D2): RuleEngineHelper {
        return RuleEngineHelper(
            EvaluationType.Event(eventUid),
            RulesRepository(d2),
        )
    }

    @Provides
    @PerActivity
    fun valueStore(
        d2: D2,
        crashReportController: CrashReportController,
        networkUtils: NetworkUtils,
        resourceManager: ResourceManager,
        fileController: FileController,
        uniqueAttributeController: UniqueAttributeController,
    ): FormValueStore {
        return FormValueStore(
            d2,
            eventUid,
            EntryMode.DE,
            null,
            null,
            crashReportController,
            networkUtils,
            resourceManager,
            fileController,
            uniqueAttributeController,
        )
    }

    @Provides
    @PerActivity
    fun searchTEIRepository(d2: D2): SearchTEIRepository {
        return SearchTEIRepositoryImpl(d2, DhisEnrollmentUtils(d2), CrashReportControllerImpl())
    }

    @get:PerActivity
    @get:Provides
    val processor: FlowableProcessor<RowAction>
        get() = PublishProcessor.create()

    @Provides
    @PerActivity
    fun pageConfigurator(
        repository: EventCaptureRepository,
    ): NavigationPageConfigurator {
        return EventPageConfigurator(repository, isPortrait)
    }
}
