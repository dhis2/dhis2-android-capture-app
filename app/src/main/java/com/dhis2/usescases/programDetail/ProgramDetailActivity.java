package com.dhis2.usescases.programDetail;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.dhis2.App;
import com.dhis2.R;
import com.dhis2.databinding.ActivityProgramDetailBinding;
import com.dhis2.usescases.general.ActivityGlobalAbstract;
import com.dhis2.usescases.main.program.HomeViewModel;
import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;

import java.util.ArrayList;

import javax.inject.Inject;

/**
 * Created by ppajuelo on 31/10/2017.
 */

public class ProgramDetailActivity extends ActivityGlobalAbstract implements ProgramDetailContractModule.View {

    ActivityProgramDetailBinding binding;
    @Inject
    ProgramDetailContractModule.Presenter presenter;
    HomeViewModel homeViewModel;
    @Inject
    ProgramDetailAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
//        AndroidInjection.inject(this);
        ((App) getApplicationContext()).getUserComponent().plus(new ProgramDetailModule()).inject(this);

        super.onCreate(savedInstanceState);
        homeViewModel = (HomeViewModel) getIntent().getSerializableExtra("PROGRAM");
        binding = DataBindingUtil.setContentView(this, R.layout.activity_program_detail);
        binding.setPresenter(presenter);

        presenter.init(this, homeViewModel);

        binding.recycler.addOnScrollListener(new EndlessRecyclerViewScrollListener(binding.recycler.getLayoutManager()) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                presenter.nextPageForApi(page);
            }
        });

    }

    @Override
    public void swapData(TrackedEntityObject response) {
        if (binding.recycler.getAdapter() == null)
            binding.recycler.setAdapter(null); //TODO: NEW ADAPTER! SI QUIERES PUEDES INTENTAR METERLO POR DAGGER
        /*else
            binding.recycler.getAdapter().addItems(response.getTrackedEntityInstances());*/
    }

    @Override
    public void addTree(TreeNode treeNode) {


        binding.treeViewContainer.removeAllViews();

        AndroidTreeView treeView = new AndroidTreeView(getContext(), treeNode);

        treeView.setDefaultContainerStyle(R.style.TreeNodeStyle, false);
        treeView.setSelectionModeEnabled(true);

        binding.treeViewContainer.addView(treeView.getView());
        treeView.expandAll();

        treeView.setDefaultNodeLongClickListener((node, value) -> {
            node.setSelected(!node.isSelected());
            ArrayList<String> childIds = new ArrayList<String>();
            childIds.add(((OrganisationUnitModel) value).uid());
            for (TreeNode childNode : node.getChildren()) {
                childIds.add(((OrganisationUnitModel) childNode.getValue()).uid());
                for (TreeNode childNode2 : childNode.getChildren()) {
                    childIds.add(((OrganisationUnitModel) childNode2.getValue()).uid());
                    for (TreeNode childNode3 : childNode2.getChildren()) {
                        childIds.add(((OrganisationUnitModel) childNode3.getValue()).uid());
                    }
                }
            }
            binding.buttonOrgUnit.setText(((OrganisationUnitModel) value).displayShortName());
            binding.drawerLayout.closeDrawers();
            return true;
        });
    }
}
