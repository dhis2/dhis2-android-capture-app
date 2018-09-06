package org.dhis2.usescases.teiDashboard.dashboardfragments;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.dhis2.R;
import org.dhis2.data.tuples.Pair;
import org.dhis2.databinding.FragmentRelationshipsBinding;
import org.dhis2.usescases.general.FragmentGlobalAbstract;
import org.dhis2.usescases.teiDashboard.DashboardProgramModel;
import org.dhis2.usescases.teiDashboard.TeiDashboardContracts;
import org.dhis2.usescases.teiDashboard.adapters.RelationshipAdapter;
import org.dhis2.usescases.teiDashboard.mobile.TeiDashboardMobileActivity;
import org.dhis2.utils.Constants;

import org.hisp.dhis.android.core.relationship.Relationship;
import org.hisp.dhis.android.core.relationship.RelationshipType;

import java.util.List;

import io.reactivex.functions.Consumer;

import static android.app.Activity.RESULT_OK;

/**
 * Created by ppajuelo on 29/11/2017.
 */

public class RelationshipFragment extends FragmentGlobalAbstract {

    FragmentRelationshipsBinding binding;
    TeiDashboardContracts.Presenter presenter;

    private DashboardProgramModel dashboardProgramModel;
    static RelationshipFragment instance;
    static RelationshipAdapter relationshipAdapter;

    static public RelationshipFragment getInstance() {
        if (instance == null) {
            instance = new RelationshipFragment();
        }
        return instance;
    }

    public static RelationshipFragment createInstance() {
        return instance = new RelationshipFragment();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        presenter = ((TeiDashboardMobileActivity) context).getPresenter();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_relationships, container, false);
        binding.setPresenter(presenter);
        relationshipAdapter = new RelationshipAdapter(presenter);
        binding.relationshipRecycler.setAdapter(relationshipAdapter);
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.subscribeToRelationships(this);
        if (dashboardProgramModel != null)
            setData(dashboardProgramModel);
    }

    public void setData(DashboardProgramModel dashboardProgramModel) {
        this.dashboardProgramModel = dashboardProgramModel;
        if (dashboardProgramModel.getCurrentProgram().relationshipText() == null)
            binding.setRelationshipType(getString(R.string.default_relationship_label));
        else
            binding.setRelationshipType(dashboardProgramModel.getCurrentProgram().relationshipText());
        binding.executePendingBindings();
    }

    public Consumer<List<Pair<Relationship,RelationshipType>>> setRelationships() {
        return relationships -> relationshipAdapter.addItems(relationships);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQ_ADD_RELATIONSHIP) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    String tei_a = data.getStringExtra("TEI_A_UID");
                    String relationshipType = data.getStringExtra("RELATIONSHIP_TYPE_UID");
                    presenter.addRelationship(tei_a, relationshipType);
                }
            }
        }
    }
}
