package org.dhis2.utils.optionset

import android.text.TextUtils
import android.widget.EditText
import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.disposables.CompositeDisposable
import org.dhis2.data.forms.dataentry.fields.spinner.SpinnerViewModel
import org.dhis2.data.schedulers.SchedulerProvider
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.helpers.UidsHelper
import org.hisp.dhis.android.core.common.ObjectWithUid
import org.hisp.dhis.android.core.option.Option
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit

class OptionSetPresenter(val d2: D2, val schedulerProvider: SchedulerProvider) : OptionSetContracts.Presenter {

    private var disposable: CompositeDisposable = CompositeDisposable()

    override fun init(view : OptionSetContracts.View, optionSet : SpinnerViewModel, textSearch : EditText){

        val optionsToHide = if (optionSet.optionsToHide != null) optionSet.optionsToHide else ArrayList()
        val optionGroupsToHide = if (optionSet.optionGroupsToHide != null) optionSet.optionGroupsToHide else ArrayList()
        val optionGroupsToShow = if (optionSet.optionGroupsToShow != null) optionSet.optionGroupsToShow else ArrayList()

        disposable.add(RxTextView.textChanges(textSearch)
                .startWith("")
                .debounce(500, TimeUnit.MILLISECONDS, schedulerProvider.io())
                .map<LiveData<PagedList<Option>>> { textToSearch ->
                    var optionRepository = d2.optionModule().options.byOptionSetUid().eq(optionSet.optionSet())

                    val finalOptionsToHide = ArrayList<String>()
                    val finalOptionsToShow = ArrayList<String>()

                    if (optionsToHide.isNotEmpty())
                        finalOptionsToHide.addAll(optionsToHide)

                    if (optionGroupsToShow.isNotEmpty()) {
                        for (groupUid in optionGroupsToShow) {
                            finalOptionsToShow.addAll(
                                    UidsHelper.getUidsList<ObjectWithUid>(
                                            d2.optionModule().optionGroups.withOptions().uid(groupUid).blockingGet()!!.options()!!)
                            )
                        }
                    }

                    if (optionGroupsToHide.isNotEmpty()) {
                        for (groupUid in optionGroupsToHide) {
                            finalOptionsToHide.addAll(
                                    UidsHelper.getUidsList<ObjectWithUid>(
                                            d2.optionModule().optionGroups.withOptions().uid(groupUid).blockingGet()!!.options()!!)
                            )
                        }
                    }

                    if (finalOptionsToShow.isNotEmpty())
                        optionRepository = optionRepository.byUid().`in`(finalOptionsToShow)

                    if (finalOptionsToHide.isNotEmpty())
                        optionRepository = optionRepository.byUid().notIn(finalOptionsToHide)

                    if (!TextUtils.isEmpty(textToSearch))
                        optionRepository = optionRepository.byDisplayName().like("%$textToSearch%")

                    optionRepository.getPaged(20)
                }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                        { view.setLiveData(it) },
                        { Timber.e(it) }
                ))
    }

    override fun onDettach() {
        disposable.clear()
    }

    override fun displayMessage(message: String?) {

    }

}

