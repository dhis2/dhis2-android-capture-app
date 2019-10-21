package org.dhis2.utils.optionset

import android.widget.EditText
import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import org.dhis2.data.forms.dataentry.fields.spinner.SpinnerViewModel
import org.dhis2.data.forms.dataentry.tablefields.spinner.SpinnerViewModel as TableSpinnerViewModel

import org.dhis2.usescases.general.AbstractActivityContracts
import org.hisp.dhis.android.core.option.Option

class OptionSetContracts {

    interface View {

        fun setLiveData(data: LiveData<PagedList<Option>>?)

        fun showDialog(): Boolean
    }

    interface Presenter : AbstractActivityContracts.Presenter {

        fun init(view: View, optionSet : SpinnerViewModel, textSearch : EditText)

        fun init(view: View, optionSetTable: TableSpinnerViewModel, textSearch: EditText)

        fun getCount(optionSetUid : String) : Int?
    }
}
