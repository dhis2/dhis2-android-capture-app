package com.dhis2.usescases.searchTrackEntity.adapters;

import android.support.v7.widget.RecyclerView;

import com.dhis2.data.metadata.MetadataRepository;
import com.dhis2.databinding.ItemSearchTrackedEntityOnlineBinding;
import com.dhis2.usescases.searchTrackEntity.SearchTEContractsModule;

import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;

/**
 * Created by frodriguez on 11/7/2017.
 */

public class SearchTEViewHolderOnline extends RecyclerView.ViewHolder {

    private ItemSearchTrackedEntityOnlineBinding binding;


    public SearchTEViewHolderOnline(ItemSearchTrackedEntityOnlineBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }


    public void bind(SearchTEContractsModule.Presenter presenter, TrackedEntityInstanceModel trackedEntityInstanceModel, MetadataRepository metadataRepository) {

    }


}
