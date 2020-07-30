package org.dhis2.usescases.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import org.dhis2.R
import org.dhis2.usescases.general.FragmentGlobalAbstract

class PolicyFragment : FragmentGlobalAbstract() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        DataBindingUtil.inflate<>(inflater, R.layout.fragment_about, container, false);
     //   val fragmentPolicyBinding :   = DataBindingUtil.inflate(inflater, R.layout.fragment_about_policy, container, false)


     //   return fragmentPolicyBinding.getRoot()
    }

}