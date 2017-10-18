package com.dhis2.usescases.main.program;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dhis2.R;
import com.dhis2.databinding.FragmentProgramBinding;
import com.dhis2.usescases.general.FragmentGlobalAbstract;

import java.util.List;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;
import io.reactivex.functions.Consumer;

/**
 * Created by ppajuelo on 18/10/2017.
 */

public class ProgramFragment extends FragmentGlobalAbstract implements ProgramContractModule.View {


    FragmentProgramBinding binding;
    @Inject
    ProgramPresenter presenter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        AndroidSupportInjection.inject(this);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_program, container, false);
        binding.setPresenter(presenter);
        return binding.getRoot();
    }

    @Override
    public void setUpRecycler() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2, LinearLayoutManager.VERTICAL, false);
        binding.programRecycler.setLayoutManager(gridLayoutManager);
        binding.programRecycler.setAdapter(new ProgramAdapter(presenter));
        presenter.init();
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpRecycler();
    }

    @Override
    public Consumer<List<HomeViewModel>> swapData() {

        return homeEntities -> ((ProgramAdapter) binding.programRecycler.getAdapter()).setData(homeEntities);
    }

    @Override
    public void renderError(String message) {
        new AlertDialog.Builder(getActivity())
                .setPositiveButton(android.R.string.ok, null)
                .setTitle(getString(R.string.error))
                .setMessage(message)
                .show();
    }
}
