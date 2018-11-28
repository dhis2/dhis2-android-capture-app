package org.dhis2.utils;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.dhis2.R;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;

import java.util.List;

import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

/**
 * QUADRAM. Created by ppajuelo on 28/11/2018.
 */
public class OrgUnitSelectorAdapter extends RecyclerView.Adapter<OrgUnitSelectorHolder> {

    private final List<ParentChildModel<OrganisationUnitModel>> topLevelList;
    private FlowableProcessor<List<ParentChildModel<OrganisationUnitModel>>> processor;

    public OrgUnitSelectorAdapter(List<ParentChildModel<OrganisationUnitModel>> topLevelOrgUnitList) {
        this.topLevelList = topLevelOrgUnitList;
        this.processor = PublishProcessor.create();

    }

    @NonNull
    @Override
    public OrgUnitSelectorHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        return new OrgUnitSelectorHolder(DataBindingUtil.inflate(inflater, R.layout.org_unit_menu_selector_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull OrgUnitSelectorHolder orgUnitSelectorHolder, int i) {
        orgUnitSelectorHolder.bind(topLevelList.get(i).parent());
    }

    @Override
    public int getItemCount() {
        return topLevelList != null ? topLevelList.size() : 0;
    }
}
