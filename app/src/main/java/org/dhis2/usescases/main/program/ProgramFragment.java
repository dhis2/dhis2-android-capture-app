package org.dhis2.usescases.main.program;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DividerItemDecoration;

import org.dhis2.Components;
import org.dhis2.R;
import org.dhis2.databinding.FragmentProgramBinding;
import org.dhis2.usescases.general.FragmentGlobalAbstract;
import org.dhis2.usescases.main.MainActivity;
import org.dhis2.usescases.org_unit_selector.OUTreeActivity;
import org.dhis2.utils.HelpManager;
import org.dhis2.utils.filters.FilterManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.functions.Consumer;
import timber.log.Timber;

/**
 * Created by ppajuelo on 18/10/2017.f
 */

public class ProgramFragment extends FragmentGlobalAbstract implements ProgramContract.View {

    private FragmentProgramBinding binding;
    @Inject
    ProgramContract.Presenter presenter;
    @Inject
    ProgramModelAdapter adapter;
    private Context context;

    public FragmentProgramBinding getBinding() {
        return binding;
    }
    //-------------------------------------------
    //region LIFECYCLE


    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        this.context = context;
        if (getActivity() != null)
            ((Components) getActivity().getApplicationContext()).userComponent()
                    .plus(new ProgramModule()).inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_program, container, false);
        binding.setPresenter(presenter);
        binding.programRecycler.setAdapter(adapter);
        binding.programRecycler.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.init(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        presenter.dispose();
    }

    //endregion


    public boolean areFiltersApplied() {
        return presenter.areFiltersApplied();
    }

    @Override
    public Consumer<List<ProgramViewModel>> swapProgramModelData() {
        return programs -> {
            binding.programProgress.setVisibility(View.GONE);
            binding.emptyView.setVisibility(programs.isEmpty() ? View.VISIBLE : View.GONE);
            ((ProgramModelAdapter) binding.programRecycler.getAdapter()).setData(programs);
        };
    }

    @Override
    public void renderError(String message) {
        if (isAdded() && getActivity() != null)
            new AlertDialog.Builder(getActivity())
                    .setPositiveButton(android.R.string.ok, null)
                    .setTitle(getString(R.string.error))
                    .setMessage(message)
                    .show();
    }

    @Override
    public void openOrgUnitTreeSelector() {
        Intent ouTreeIntent = new Intent(context, OUTreeActivity.class);
        ((MainActivity) context).startActivityForResult(ouTreeIntent, FilterManager.OU_TREE);
    }

    @Override
    public void setTutorial() {
        try {
            if (getContext() != null && isAdded()) {
                new Handler().postDelayed(() -> {
                    if (getAbstractActivity() != null) {
                        SparseBooleanArray stepCondition = new SparseBooleanArray();
                        stepCondition.put(7, binding.programRecycler.getAdapter().getItemCount() > 0);
                        HelpManager.getInstance().show(getAbstractActivity(), HelpManager.TutorialName.PROGRAM_FRAGMENT, stepCondition);
                    }

                }, 500);
            }
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    public void openFilter(boolean open) {
        binding.filter.setVisibility(open ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showHideFilter() {
        ((MainActivity) getActivity()).showHideFilter();
    }

    @Override
    public void clearFilters() {
        ((MainActivity) getActivity()).getAdapter().notifyDataSetChanged();
    }
}