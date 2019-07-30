package org.dhis2.usescases.programEventDetail;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.transition.ChangeBounds;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.SparseBooleanArray;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.DividerItemDecoration;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.data.tuples.Pair;
import org.dhis2.databinding.ActivityProgramEventDetailBinding;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.usescases.org_unit_selector.OUTreeActivity;
import org.dhis2.utils.HelpManager;
import org.dhis2.utils.filters.FilterManager;
import org.dhis2.utils.filters.FiltersAdapter;
import org.hisp.dhis.android.core.category.CategoryCombo;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;
import org.hisp.dhis.android.core.program.Program;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;

import static org.dhis2.R.layout.activity_program_event_detail;

/**
 * QUADRAM. Created by Cristian on 13/02/2018.
 */

public class ProgramEventDetailActivity extends ActivityGlobalAbstract implements ProgramEventDetailContract.View {

    private ActivityProgramEventDetailBinding binding;

    @Inject
    ProgramEventDetailContract.Presenter presenter;

    private ProgramEventDetailLiveAdapter liveAdapter;
    private boolean backDropActive;
    private FiltersAdapter filtersAdapter;
    private String programUid;

    public static Bundle getBundle(String programUid, String period, List<Date> dates) {
        Bundle bundle = new Bundle();
        bundle.putString("PROGRAM_UID", programUid);
        bundle.putString("CURRENT_PERIOD", period);
        bundle.putSerializable("DATES", (ArrayList) dates);
        return bundle;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        ((App) getApplicationContext()).userComponent().plus(new ProgramEventDetailModule(getIntent().getStringExtra("PROGRAM_UID"))).inject(this);
        super.onCreate(savedInstanceState);

        FilterManager.getInstance().clearCatOptCombo();

        this.programUid =getIntent().getStringExtra("PROGRAM_UID");
        binding = DataBindingUtil.setContentView(this, activity_program_event_detail);

        binding.setPresenter(presenter);
        binding.setTotalFilters(FilterManager.getInstance().getTotalFilters());

        liveAdapter = new ProgramEventDetailLiveAdapter(presenter);
        binding.recycler.setAdapter(liveAdapter);
        binding.recycler.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        filtersAdapter = new FiltersAdapter();
        try {
            binding.filterLayout.setAdapter(filtersAdapter);

        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.init(this);
    }

    @Override
    protected void onPause() {
        presenter.onDettach();
        super.onPause();
    }

    @Override
    public void setProgram(Program program) {
        binding.setName(program.displayName());
    }

    @Override
    public void setLiveData(LiveData<PagedList<ProgramEventViewModel>> pagedListLiveData) {
        pagedListLiveData.observe(this, pagedList -> {
            binding.programProgress.setVisibility(View.GONE);
            liveAdapter.submitList(pagedList, () -> {
                if (binding.recycler.getAdapter() != null && binding.recycler.getAdapter().getItemCount() == 0) {
                    binding.emptyTeis.setVisibility(View.VISIBLE);
                } else {
                    binding.emptyTeis.setVisibility(View.GONE);
                }
            });

        });

    }

    @Override
    public void setOptionComboAccess(Boolean canCreateEvent) {
        switch (binding.addEventButton.getVisibility()) {
            case View.VISIBLE:
                binding.addEventButton.setVisibility(canCreateEvent ? View.VISIBLE : View.GONE);
                break;
            case View.GONE:
                binding.addEventButton.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public void renderError(String message) {
        if (getActivity() != null)
            new AlertDialog.Builder(getActivity())
                    .setPositiveButton(android.R.string.ok, null)
                    .setTitle(getString(R.string.error))
                    .setMessage(message)
                    .show();
    }

    @Override
    public void showHideFilter() {
        Transition transition = new ChangeBounds();
        transition.setDuration(200);
        TransitionManager.beginDelayedTransition(binding.backdropLayout, transition);
        backDropActive = !backDropActive;
        ConstraintSet initSet = new ConstraintSet();
        initSet.clone(binding.backdropLayout);
        if (backDropActive)
            initSet.connect(R.id.recycler, ConstraintSet.TOP, R.id.backdropGuide, ConstraintSet.BOTTOM, 0);
        else
            initSet.connect(R.id.recycler, ConstraintSet.TOP, R.id.toolbar, ConstraintSet.BOTTOM, 0);
        initSet.applyTo(binding.backdropLayout);
    }

    @Override
    public void setWritePermission(Boolean canWrite) {
        switch (binding.addEventButton.getVisibility()) {
            case View.VISIBLE:
                binding.addEventButton.setVisibility(canWrite ? View.VISIBLE : View.GONE);
                break;
            case View.GONE:
                binding.addEventButton.setVisibility(View.GONE);
                break;
        }
        if (binding.addEventButton.getVisibility() == View.VISIBLE) {
            binding.emptyTeis.setText(R.string.empty_tei_add);
        } else {
            binding.emptyTeis.setText(R.string.empty_tei_no_add);
        }
    }

    @Override
    public void setTutorial() {
        new Handler().postDelayed(() -> {
            SparseBooleanArray stepConditions = new SparseBooleanArray();
            stepConditions.put(2, findViewById(R.id.addEventButton).getVisibility() == View.VISIBLE);
            HelpManager.getInstance().show(getActivity(), HelpManager.TutorialName.PROGRAM_EVENT_LIST,
                    stepConditions);

        }, 500);
    }

    @Override
    public void updateFilters(int totalFilters) {
        binding.setTotalFilters(totalFilters);
    }

    @Override
    public void setCatOptionComboFilter(Pair<CategoryCombo, List<CategoryOptionCombo>> categoryOptionCombos) {
        filtersAdapter.addCatOptCombFilter(categoryOptionCombos);
    }

    @Override
    public void openOrgUnitTreeSelector() {
        Intent ouTreeIntent = new Intent(this, OUTreeActivity.class);
        Bundle bundle = OUTreeActivity.getBundle(programUid);
        ouTreeIntent.putExtras(bundle);
        startActivityForResult(ouTreeIntent, FilterManager.OU_TREE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == FilterManager.OU_TREE && resultCode == Activity.RESULT_OK) {
            filtersAdapter.notifyDataSetChanged();
            updateFilters(FilterManager.getInstance().getTotalFilters());
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void showTutorial(boolean shaked) {
        setTutorial();
    }
}