package org.dhis2.usescases.jira

import android.content.Context
import android.content.Intent
import android.net.Uri
import okhttp3.ResponseBody
import android.os.Bundle
import android.provider.Browser
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import retrofit2.Response


import com.google.gson.Gson

import org.dhis2.R
import org.dhis2.databinding.FragmentJiraBinding
import org.dhis2.usescases.general.FragmentGlobalAbstract
import org.dhis2.utils.NetworkUtils
import org.dhis2.utils.jira.JiraIssue
import org.dhis2.utils.jira.JiraIssueListResponse
import org.dhis2.utils.jira.OnJiraIssueClick

import java.io.IOException
import java.util.ArrayList
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration

import org.hisp.dhis.android.core.utils.support.StringUtils.isEmpty

/**
 * QUADRAM. Created by ppajuelo on 24/05/2018.
 */

class JiraFragment : FragmentGlobalAbstract(), OnJiraIssueClick {

    private var jiraViewModel: JiraViewModel? = null
    private val adapter = JiraIssueAdapter(this)

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<FragmentJiraBinding>(inflater,
                R.layout.fragment_jira, container, false)
        jiraViewModel = ViewModelProviders.of(this).get(JiraViewModel::class.java)
        jiraViewModel!!.init()

        jiraViewModel!!.issueListResponse().observe(this, Observer<Response<ResponseBody>> {
            response ->
            if (response.isSuccessful && response.body() != null) {
                var issueList: List<JiraIssue>? = ArrayList()
                try {
                    val jiraIssueListRes = Gson()
                            .fromJson<JiraIssueListResponse>(response.body()!!.string(),
                                    JiraIssueListResponse::class.java)
                    issueList = jiraIssueListRes.issues
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                adapter.addItems(issueList!!)
            } else
                displayMessage(response.message())
        })

        jiraViewModel!!.issueMessage().observe(this, Observer<String> { t ->
            if (!isEmpty(t))
                displayMessage(t)
        })

        binding.jiraViewModel = jiraViewModel
        binding.sendReportButton.isEnabled = NetworkUtils.isOnline(context!!)
        binding.issueRecycler.adapter = adapter
        binding.issueRecycler.addItemDecoration(DividerItemDecoration(context!!,
                DividerItemDecoration.VERTICAL))

        return binding.root
    }

    override fun onJiraIssueClick(issueKey: String) {
        val browserIntent = Intent(Intent.ACTION_VIEW,
                Uri.parse("https://jira.dhis2.org/browse/$issueKey"))
        val bundle = Bundle()
        bundle.putString("Authorization", "Basic ${jiraViewModel!!.getAuth()}")
        browserIntent.putExtra(Browser.EXTRA_HEADERS, bundle)
        startActivity(browserIntent)
    }


}
