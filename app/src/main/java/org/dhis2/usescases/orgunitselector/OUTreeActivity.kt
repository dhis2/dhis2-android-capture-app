package org.dhis2.usescases.orgunitselector

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.databinding.DataBindingUtil
import javax.inject.Inject
import org.dhis2.App
import org.dhis2.R
import org.dhis2.data.tuples.Pair
import org.dhis2.databinding.OuTreeActivityBinding
import org.dhis2.usescases.general.ActivityGlobalAbstract

class OUTreeActivity : ActivityGlobalAbstract(), OUTreeView, OrgUnitSelectorAdapter.OnOrgUnitClick {

    @Inject
    lateinit var presenter: OUTreePresenter

    private lateinit var binding: OuTreeActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.ou_tree_activity)

        (applicationContext as App).userComponent()!!
            .plus(OUTreeModule(this))
            .inject(this)

        binding.search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // Not used
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.toString().isEmpty()) {
                    presenter.onStartSearch.onNext(true)
                } else {
                    presenter.onSearchListener.onNext(s.toString())
                }
            }

            override fun afterTextChanged(s: Editable) {
                // Not used
            }
        })
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
