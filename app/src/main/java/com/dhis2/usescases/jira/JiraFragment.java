package com.dhis2.usescases.jira;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dhis2.Components;
import com.dhis2.R;
import com.dhis2.databinding.FragmentJiraBinding;
import com.dhis2.usescases.general.FragmentGlobalAbstract;

import javax.inject.Inject;

/**
 * QUADRAM. Created by ppajuelo on 24/05/2018.
 */

public class JiraFragment extends FragmentGlobalAbstract {

    @Inject
    JiraPresenter presenter;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((Components) context.getApplicationContext()).userComponent()
                .plus(new JiraModule()).inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentJiraBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_jira, container, false);
        binding.setPresenter(presenter);
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.init(getAbstracContext());
    }
}
