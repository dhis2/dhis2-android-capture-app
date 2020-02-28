package org.dhis2.utils.optionset

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import io.reactivex.Observable
import org.hisp.dhis.android.core.option.Option

interface OptionSetView {

    fun searchSource(): Observable<CharSequence>

    fun setLiveData(data: LiveData<PagedList<Option>>?)

    fun showDialog(): Boolean
}
