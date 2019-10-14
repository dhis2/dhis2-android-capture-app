package org.dhis2.utils.optionset

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import org.dhis2.App
import org.dhis2.R
import org.dhis2.data.forms.dataentry.fields.spinner.SpinnerViewModel
import org.dhis2.databinding.DialogOptionSetBinding
import org.dhis2.utils.Constants
import org.dhis2.utils.custom_views.OptionSetOnClickListener
import org.hisp.dhis.android.core.option.Option
import javax.inject.Inject

class OptionSetDialog : DialogFragment(), OptionSetContracts.View {

    @Inject
    lateinit var presenter: OptionSetContracts.Presenter

    private var adapter: OptionSetAdapter? = null

    var optionSet: SpinnerViewModel? = null
    var optionSetTable: org.dhis2.data.forms.dataentry.tablefields.spinner.SpinnerViewModel? = null
    var listener: OptionSetOnClickListener? = null
    var clearListener: View.OnClickListener? = null
    var defalutSize: Int = 0

    var isDialogShown = false
        private set

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)

        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val binding = DataBindingUtil.inflate<DialogOptionSetBinding>(inflater, R.layout.dialog_option_set, container, false)

        binding.title.text = if (optionSet != null) optionSet?.label() else optionSetTable?.label()

        if (optionSet != null)
            presenter.init(this, optionSet!!, binding.txtSearch)
        else if (optionSetTable != null)
            presenter.init(this, optionSetTable!!, binding.txtSearch)

        adapter = OptionSetAdapter (OptionSetOnClickListener {
                listener?.onSelectOption(it)
                this.dismiss()
            })

        binding.recycler.adapter = adapter

        binding.clearButton.setOnClickListener { view ->
            clearListener?.onClick(view)
            this.dismiss()
        }
        binding.cancelButton.setOnClickListener { this.dismiss() }

        return binding.root

    }

    override fun onCancel(dialog: DialogInterface) {
        presenter.onDettach()
        super.onCancel(dialog)
    }

    override fun setLiveData(data: LiveData<PagedList<Option>>?) {
        data?.observe(this, Observer {
            adapter?.submitList(it)
        })
    }

    fun create(context: Context) {
        (context.applicationContext as App).userComponent()!!.plus(OptionSetModule()).inject(this)
        defalutSize = context.getSharedPreferences(Constants.SHARE_PREFS, Context.MODE_PRIVATE)
                .getInt(Constants.OPTION_SET_DIALOG_THRESHOLD, 15)
    }

    override fun show(manager: FragmentManager, tag: String?) {
        isDialogShown = true
        super.show(manager, tag)
    }

    override fun showDialog(): Boolean {
        return presenter.getCount(if (optionSet != null) optionSet!!.optionSet() else optionSetTable!!.optionSet())!! > defalutSize
    }

    override fun dismiss() {
        presenter.onDettach()
        if (isDialogShown) {
            isDialogShown = false
            super.dismiss()
        }
    }

    companion object {
        val TAG = OptionSetDialog::class.java.name
    }
}
