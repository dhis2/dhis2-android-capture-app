package org.dhis2.usescases.main.program

import android.content.Context
import android.os.Bundle
import android.text.TextUtils.isEmpty
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Consumer
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers
import org.dhis2.data.tuples.Pair
import org.dhis2.usescases.datasets.datasetDetail.DataSetDetailActivity
import org.dhis2.usescases.programEventDetail.ProgramEventDetailActivity
import org.dhis2.usescases.searchTrackEntity.SearchTEActivity
import org.dhis2.utils.ColorUtils
import org.dhis2.utils.Constants
import org.dhis2.utils.analytics.SELECT_PROGRAM
import org.dhis2.utils.analytics.TYPE_PROGRAM_SELECTED
import org.dhis2.utils.filters.FilterManager
import org.dhis2.utils.granular_sync.GranularSyncContracts
import org.dhis2.utils.granular_sync.SyncStatusDialog
import org.hisp.dhis.android.core.period.DatePeriod
import org.hisp.dhis.android.core.program.ProgramType
import timber.log.Timber

/**
 * Created by ppajuelo on 18/10/2017.f
 */

class ProgramPresenter internal constructor(private val homeRepository: HomeRepository) : ProgramContract.Presenter {

    private var view: ProgramContract.View? = null
    private var compositeDisposable: CompositeDisposable? = null
    private val programQueries = PublishProcessor.create<Pair<List<DatePeriod>, List<String>>>()

    override fun init(view: ProgramContract.View) {
        this.view = view
        var currentOrgUnitFilter: List<String> = ArrayList()
        var currentDateFilter: List<DatePeriod> = ArrayList()
        this.compositeDisposable = CompositeDisposable()


        if (FilterManager.getInstance().periodFilters.size != 0)
            currentDateFilter = FilterManager.getInstance().periodFilters
        if (FilterManager.getInstance().orgUnitFilters.size != 0)
            currentOrgUnitFilter = FilterManager.getInstance().orgUnitUidsFilters

        compositeDisposable!!.add(
                programQueries
                        .startWith(Pair.create(currentDateFilter, currentOrgUnitFilter))
                        .flatMap { datePeriodOrgs ->
                            Flowable.zip(
                                    homeRepository.programModels(datePeriodOrgs.val0(), datePeriodOrgs.val1(), FilterManager.getInstance().stateFilters).subscribeOn(Schedulers.io()),
                                    homeRepository.aggregatesModels(datePeriodOrgs.val0(), datePeriodOrgs.val1(), FilterManager.getInstance().stateFilters).subscribeOn(Schedulers.io()),
                                    BiFunction<List<ProgramViewModel>, List<ProgramViewModel>, List<ProgramViewModel>> { programs, dataSets ->
                                        val finalList = ArrayList<ProgramViewModel>()
                                        finalList.addAll(programs)
                                        finalList.addAll(dataSets)
                                        finalList.sortWith(Comparator { program1, program2 -> program1.title().compareTo(program2.title(), ignoreCase = true) })
                                        finalList
                                    })
                        }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                view.swapProgramModelData(),
                                Consumer { throwable -> view.renderError(throwable.message ?: "") }
                        ))

        compositeDisposable!!.add(
                FilterManager.getInstance().asFlowable()
                        .subscribeOn(Schedulers.io())
                        .flatMap { filterManager ->
                            homeRepository.programModels(filterManager.periodFilters, filterManager.orgUnitUidsFilters, filterManager.stateFilters).flatMapIterable { data -> data }
                                    .mergeWith(homeRepository.aggregatesModels(filterManager.periodFilters, filterManager.orgUnitUidsFilters, filterManager.stateFilters).flatMapIterable { data -> data })
                                    .sorted { p1, p2 -> p1.title().compareTo(p2.title(), ignoreCase = true) }.toList().toFlowable()
                                    .subscribeOn(Schedulers.io())
                        }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                view.swapProgramModelData(),
                                Consumer { throwable -> view.renderError(throwable.message ?: "") }
                        )
        )

        compositeDisposable!!.add(
                FilterManager.getInstance().ouTreeFlowable()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                { view.openOrgUnitTreeSelector() },
                                { Timber.e(it) }
                        )
        )
    }

    override fun dispose() {
        compositeDisposable!!.clear()
    }

    override fun onSyncStatusClick(program: ProgramViewModel) {
        view!!.showSyncDialog(
                SyncStatusDialog.Builder()
                        .setConflictType(
                                if (program.typeName() != "DataSets")
                                    SyncStatusDialog.ConflictType.PROGRAM
                                else
                                    SyncStatusDialog.ConflictType.DATA_SET
                        )
                        .setUid(program.id())
                        .onDismissListener(
                                object : GranularSyncContracts.OnDismissListener {
                                    override fun onDismiss(hasChanged: Boolean) {
                                        if (hasChanged)
                                            programQueries.onNext(
                                                    Pair.create(FilterManager.getInstance().periodFilters, FilterManager.getInstance().orgUnitUidsFilters)
                                            )
                                    }
                                })
                        .build()
        )
    }

    override fun onItemClick(programModel: ProgramViewModel) {

        val bundle = Bundle()
        val idTag = if (programModel.typeName() == "DataSets")
            "DATASET_UID"
        else
            "PROGRAM_UID"

        if (!isEmpty(programModel.type()))
            bundle.putString("TRACKED_ENTITY_UID", programModel.type())

        view!!.analyticsHelper().setEvent(TYPE_PROGRAM_SELECTED, if (programModel.programType().isNotEmpty()) programModel.programType() else programModel.typeName(), SELECT_PROGRAM)
        bundle.putString(idTag, programModel.id())
        bundle.putString(Constants.DATA_SET_NAME, programModel.title())
        bundle.putString(Constants.ACCESS_DATA, java.lang.Boolean.toString(programModel.accessDataWrite()))
        val programTheme = ColorUtils.getThemeFromColor(programModel.color())
        val prefs = view!!.abstracContext.getSharedPreferences(
                Constants.SHARE_PREFS, Context.MODE_PRIVATE)
        if (programTheme != -1) {
            prefs.edit().putInt(Constants.PROGRAM_THEME, programTheme).apply()
        } else
            prefs.edit().remove(Constants.PROGRAM_THEME).apply()

        when {
            programModel.programType() == ProgramType.WITH_REGISTRATION.name -> view!!.startActivity(SearchTEActivity::class.java, bundle, false, false, null)
            programModel.programType() == ProgramType.WITHOUT_REGISTRATION.name -> view!!.startActivity(ProgramEventDetailActivity::class.java,
                    ProgramEventDetailActivity.getBundle(programModel.id()),
                    false, false, null)
            else -> view!!.startActivity(DataSetDetailActivity::class.java, bundle, false, false, null)
        }
    }

    override fun showDescription(description: String) {
        if (!isEmpty(description))
            view!!.showDescription(description)
    }

    override fun showHideFilterClick() {
        view!!.showHideFilter()
    }

    override fun clearFilterClick() {
        FilterManager.getInstance().clearAllFilters()
        view!!.clearFilters()
    }
}
