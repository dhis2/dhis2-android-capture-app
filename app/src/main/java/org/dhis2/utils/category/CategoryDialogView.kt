package org.dhis2.utils.category

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import io.reactivex.Observable

interface CategoryDialogView {

    fun setTitle(dialogTitle: String)

    fun searchSource(): Observable<CharSequence>

    fun setLiveData(data: LiveData<PagedList<CategoryDialogItem>>)

    fun showDialog(): Boolean
}
