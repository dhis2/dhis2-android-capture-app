package org.dhis2.usescases.main.program

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import io.reactivex.functions.Consumer
import javax.inject.Inject
import org.dhis2.Components
import org.dhis2.R
import org.dhis2.databinding.FragmentProgramBinding
import org.dhis2.usescases.general.FragmentGlobalAbstract
import org.dhis2.usescases.main.MainActivity
import org.dhis2.usescases.orgunitselector.OUTreeActivity
import org.dhis2.utils.HelpManager
import org.dhis2.utils.filters.FilterManager
import timber.log.Timber

/**
 * Created by ppajuelo on 18/10/2017.f
 */

class ProgramFragment : FragmentGlobalAbstract(), ProgramContract.View {

    var binding: FragmentProgramBinding? = null

    @Inject
    lateinit var presenter: ProgramContract.Presenter
    @Inject
    lateinit var adapter: ProgramModelAdapter

    // -------------------------------------------
    //region LIFECYCLE

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (activity != null) {
            (activity!!.applicationContext as Components).userComponent()!!
                .plus(ProgramModule()).inject(this)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_program, container, false)
        binding!!.presenter = presenter
        binding!!.programRecycler.adapter = adapter
        binding!!.programRecycler.addItemDecoration(
            DividerItemDecoration(
                context!!,
                DividerItemDecoration.VERTICAL
            )
        )
        return binding!!.root
    }

    override fun onResume() {
        super.onResume()
        presenter.init(this)
    }

    override fun onPause() {
        super.onPause()
        presenter.dispose()
    }

    //endregion

    override fun swapProgramModelData(): Consumer<List<ProgramViewModel>> {
        return Consumer { programs ->
            binding!!.progressLayout.visibility = View.GONE
            binding!!.emptyView.visibility = if (programs.isEmpty()) View.VISIBLE else View.GONE
            (binding!!.programRecycler.adapter as ProgramModelAdapter).setData(programs)
        }
    }

    override fun showFilterProgress() {
        binding!!.progressLayout.visibility = View.VISIBLE
    }

    override fun renderError(message: String) {
        if (isAdded && activity != null) {
            AlertDialog.Builder(activity!!)
                .setPositiveButton(android.R.string.ok, null)
                .setTitle(getString(R.string.error))
                .setMessage(message)
                .show()
        }
    }

    override fun openOrgUnitTreeSelector() {
        val ouTreeIntent = Intent(context, OUTreeActivity::class.java)
        (context as MainActivity).startActivityForResult(ouTreeIntent, FilterManager.OU_TREE)
    }

    override fun setTutorial() {
        try {
            if (context != null && isAdded) {
                Handler().postDelayed(
                    {
                        if (abstractActivity != null) {
                            val stepCondition = SparseBooleanArray()
                            stepCondition.put(
                                7,
                                binding!!.programRecycler.adapter!!.itemCount > 0
                            )
                            HelpManager.getInstance().show(
                                abstractActivity,
                                HelpManager.TutorialName.PROGRAM_FRAGMENT,
                                stepCondition
                            )
                        }
                    },
                    500
                )
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    fun openFilter(open: Boolean) {
        binding!!.filter.visibility = if (open) View.VISIBLE else View.GONE
    }

    override fun showHideFilter() {
        (activity as MainActivity).showHideFilter()
    }

    override fun clearFilters() {
        (activity as MainActivity).adapter?.notifyDataSetChanged()
    }
}
