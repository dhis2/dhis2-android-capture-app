package org.dhis2.usescases.orgunitselector

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.databinding.DataBindingUtil

import org.dhis2.App
import org.dhis2.R
import org.dhis2.data.tuples.Pair
import org.dhis2.databinding.OuTreeActivityBinding
import org.dhis2.usescases.general.ActivityGlobalAbstract
import org.dhis2.utils.filters.FilterManager
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit

import java.util.ArrayList
import java.util.concurrent.TimeUnit

import javax.inject.Inject

import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.FlowableProcessor
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class OUTreeActivity : ActivityGlobalAbstract(), OUTreeView, OrgUnitSelectorAdapter.OnOrgUnitClick {



    @Inject
    lateinit var presenter: OUTreePresenter

    private lateinit var binding: OuTreeActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.ou_tree_activity)

        binding.search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                //Not used
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.toString().isEmpty()) {
                    presenter.onStartSearch.onNext(true)
                } else {
                    presenter.onSearchListener.onNext(s.toString())
                }
            }

            override fun afterTextChanged(s: Editable) {
                //Not used
            }
        })
        presenter.onStartSearch.onNext(true)
        binding.clearAll.setOnClickListener {
            if (binding.orgUnitRecycler.adapter != null) {
                (binding.orgUnitRecycler.adapter as OrgUnitSelectorAdapter).clearAll()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        presenter.init()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
    }

    fun onBackClick(view: View) {
        setResult(Activity.RESULT_OK)
        finish()
    }

    override fun onOrgUnitClick(node: TreeNode, position: Int) {
        presenter.ouChildListener.onNext(Pair(position, node.content))
    }

    override fun setOrgUnits(organisationUnits: List<TreeNode>) {
        binding.orgUnitRecycler.adapter = OrgUnitSelectorAdapter(organisationUnits, this)
    }

    override fun addOrgUnits(location: Int, organisationUnits: List<TreeNode>) {
        (binding.orgUnitRecycler.adapter as OrgUnitSelectorAdapter)
            .addOrgUnits(location, organisationUnits)
    }

    fun addToArray(list: MutableList<String>, uuid: String): MutableList<String> {
        if (!list.contains(uuid))
            list.add(uuid)
        return list
    }

    companion object {

        fun getBundle(
            programUid: String?
        ): Bundle {
            val bundle = Bundle()

            if (programUid != null)
                bundle.putString("PROGRAM", programUid)
            return bundle
        }
    }
}
