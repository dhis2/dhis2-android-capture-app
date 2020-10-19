package org.dhis2.usescases.jira

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Browser
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.gson.Gson
import okhttp3.ResponseBody
import org.dhis2.R
import org.dhis2.data.prefs.PreferenceProviderImpl
import org.dhis2.databinding.FragmentJiraBinding
import org.dhis2.usescases.general.FragmentGlobalAbstract
import org.dhis2.utils.NetworkUtils
import org.dhis2.utils.jira.JiraIssue
import org.dhis2.utils.jira.JiraIssueListResponse
import org.dhis2.utils.jira.OnJiraIssueClick
import retrofit2.Response
import java.io.IOException
import java.util.ArrayList

class JiraFragment : FragmentGlobalAbstract(), OnJiraIssueClick {
    private lateinit var mContext: Context
    private val jiraViewModel: JiraViewModel by viewModels()
    private val adapter = JiraIssueAdapter(this)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentJiraBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_jira, container, false)

        jiraViewModel.init(PreferenceProviderImpl(mContext.applicationContext))

        jiraViewModel.issueListResponse().observe(viewLifecycleOwner, Observer { result ->
            if (result.isSuccess) {
                adapter.submitList(result.getOrDefault(arrayListOf()))
            } else {
                displayMessage(result.exceptionOrNull()?.message)
            }
        })

        jiraViewModel.issueMessage().observe(
            viewLifecycleOwner,
            Observer { result ->
                if(result.isSuccess){
                    displayMessage("Issue Sent")
                }else{
                    displayMessage(result.exceptionOrNull()?.message)
                }
            }
        )
        binding.jiraViewModel = jiraViewModel
        binding.sendReportButton.isEnabled = NetworkUtils.isOnline(context)
        binding.issueRecycler.adapter = adapter
        binding.issueRecycler.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        )
        return binding.root
    }

    override fun onJiraIssueClick(issueKey: String) {
        val browserIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://jira.dhis2.org/browse/$issueKey")
        )
        val bundle = Bundle()
        bundle.putString("Authorization", String.format("Basic %s", jiraViewModel!!.getAuth()))
        browserIntent.putExtra(Browser.EXTRA_HEADERS, bundle)
        startActivity(browserIntent)
    }
}