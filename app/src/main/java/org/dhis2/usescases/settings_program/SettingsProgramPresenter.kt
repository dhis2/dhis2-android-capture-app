package org.dhis2.usescases.settings_program

import io.reactivex.disposables.CompositeDisposable
import org.dhis2.data.schedulers.SchedulerProvider
import org.hisp.dhis.android.core.D2
import timber.log.Timber

class SettingsProgramPresenter(
   private val d2: D2,
   private val view: ProgramSettingsView,
   private val schedulerProvider: SchedulerProvider
) {

    val disposable = CompositeDisposable()

    fun init() {
        disposable.add(
            d2.settingModule().programSetting().get()
                .map { it.specificSettings().values }
                .toFlowable().flatMapIterable { list -> list }
                .map { programSetting ->
                    val style = d2.programModule().programs()
                        .uid(programSetting.uid()).blockingGet()
                        .style()
                    ProgramSettingsViewModel(
                        programSetting,
                        style.icon(),
                        style.color()
                    )
                }
                .toList()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { data -> view.setData(data) },
                    { error -> Timber.e(error) }
                )
        )
    }

    fun dispose() {
        disposable.clear()
    }
}