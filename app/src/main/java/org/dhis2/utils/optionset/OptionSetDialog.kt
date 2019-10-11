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

import com.jakewharton.rxbinding2.widget.RxTextView

import org.dhis2.App
import org.dhis2.R
import org.dhis2.data.forms.dataentry.fields.spinner.SpinnerViewModel
import org.dhis2.databinding.DialogOptionSetBinding
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.helpers.UidsHelper
import org.hisp.dhis.android.core.option.Option

import java.util.ArrayList
import java.util.concurrent.TimeUnit

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

import android.text.TextUtils.isEmpty
import android.widget.TextView
import androidx.core.util.Consumer
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import org.dhis2.utils.custom_views.OptionSetOnClickListener
import org.dhis2.utils.granular_sync.GranularSyncContracts
import org.dhis2.utils.granular_sync.GranularSyncModule
import org.hisp.dhis.android.core.common.ObjectWithUid
import javax.inject.Inject

class OptionSetDialog(private val optionSet: SpinnerViewModel, private val listener: OptionSetOnClickListener,
                      private val clearListener: View.OnClickListener) : DialogFragment(), OptionSetContracts.View {

    @Inject
    lateinit var presenter: OptionSetContracts.Presenter

    private var adapter: OptionSetAdapter? = null

    var isDialogShown = false
        private set


    override fun onAttach(context: Context) {
        super.onAttach(context)
        (context.applicationContext as App).userComponent()!!.plus(OptionSetModule()).inject(this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)

        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val binding = DataBindingUtil.inflate<DialogOptionSetBinding>(inflater, R.layout.dialog_option_set, container, false)
        binding.title.text = optionSet.label()

        presenter.init(this, optionSet, binding.txtSearch)

        adapter = OptionSetAdapter { option ->
            listener.onSelectOption(option)
            this.dismiss()
        }
        binding.recycler.adapter = adapter

        binding.clearButton.setOnClickListener { view ->
            clearListener.onClick(view)
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

    override fun show(manager: FragmentManager, tag: String?) {
        isDialogShown = true
        super.show(manager, tag)
    }

    override fun dismiss() {
        presenter.onDettach()
        isDialogShown = false
        super.dismiss()
    }

    companion object {

        val TAG = OptionSetDialog::class.java.name
    }
}
