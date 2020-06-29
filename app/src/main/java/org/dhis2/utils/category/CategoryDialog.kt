package org.dhis2.utils.category

import android.app.Dialog
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
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import java.util.Date
import javax.inject.Inject
import org.dhis2.Bindings.app
import org.dhis2.R
import org.dhis2.databinding.DialogOptionSetBinding

class CategoryDialog(
    val type: Type,
    val uid: String,
    private val accessControl: Boolean,
    private val dateControl: Date?,
    val onItemSelected: (String) -> Unit
) : DialogFragment(), CategoryDialogView {

    private lateinit var binding: DialogOptionSetBinding

    enum class Type {
        CATEGORY_OPTIONS, CATEGORY_OPTION_COMBO
    }

    @Inject
    lateinit var presenter: CategoryDialogPresenter

    private var adapter: CategoryDialogAdapter? = null

    var clearListener: View.OnClickListener? = null
    var defaultSize: Int = 15

    var isDialogShown = false
        private set

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        create()
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_option_set, container, false)

        presenter.init()

        adapter = CategoryDialogAdapter {
            onItemSelected(it.uid)
            this.dismiss()
        }

        binding.recycler.adapter = adapter

        binding.clearButton.setOnClickListener { view ->
            clearListener?.onClick(view)
            this.dismiss()
        }
        binding.cancelButton.setOnClickListener { this.dismiss() }

        return binding.root
    }

    override fun setTitle(dialogTitle: String) {
        binding.title.text = dialogTitle
    }

    override fun onCancel(dialog: DialogInterface) {
        presenter.onDetach()
        super.onCancel(dialog)
    }

    override fun setLiveData(data: LiveData<PagedList<CategoryDialogItem>>) {
        data.observe(
            this,
            Observer {
                adapter?.submitList(it)
            }
        )
    }

    fun create() {
        app()
            .serverComponent()!!
            .plus(CategoryDialogModule(this, uid, type, accessControl, dateControl))
            .inject(this)
    }

    override fun show(manager: FragmentManager, tag: String?) {
        isDialogShown = true
        super.show(manager, tag)
    }

    override fun showDialog(): Boolean {
        return presenter.getCount() > defaultSize
    }

    override fun dismiss() {
        presenter.onDetach()
        if (isDialogShown) {
            isDialogShown = false
            super.dismiss()
        }
    }

    override fun searchSource(): Observable<String> {
        return RxTextView.textChanges(binding.txtSearch)
            .startWith("")
            .map { it.toString() }
    }

    companion object {
        val TAG: String = this::class.java.name
    }
}
