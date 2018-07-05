package com.dhis2.usescases.about;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dhis2.Components;
import com.dhis2.R;
import com.dhis2.databinding.FragmentAboutBinding;

import javax.inject.Inject;

/**
 * QUADRAM. Created by ppajuelo on 05/07/2018.
 */

public class AboutFragment extends Fragment {

    @Inject
    AboutPresenter presenter;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((Components) context.getApplicationContext()).userComponent()
                .plus(new AboutModule()).inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentAboutBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_about, container, false);
        binding.setPresenter(presenter);
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.init(this);
    }

    @Override
    public void onPause() {
        presenter.dispose();
        super.onPause();
    }
}
