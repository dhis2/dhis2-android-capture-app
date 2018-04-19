package com.dhis2.usescases.searchTrackEntity.adapters;

import android.support.v7.widget.RecyclerView;

import com.dhis2.data.metadata.MetadataRepository;
import com.dhis2.databinding.ItemSearchRelationshipTrackedEntityBinding;
import com.dhis2.usescases.searchTrackEntity.SearchTEContractsModule;
import com.dhis2.utils.OnErrorHandler;

import org.hisp.dhis.android.core.relationship.RelationshipTypeModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by frodriguez on 11/7/2017.
 */

public class SearchRelationshipViewHolder extends RecyclerView.ViewHolder {

    private ItemSearchRelationshipTrackedEntityBinding binding;
    private CompositeDisposable compositeDisposable;
    private SearchTEContractsModule.Presenter presenter;

    SearchRelationshipViewHolder(ItemSearchRelationshipTrackedEntityBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
        compositeDisposable = new CompositeDisposable();
    }


    public void bind(SearchTEContractsModule.Presenter presenter, TrackedEntityInstanceModel trackedEntityInstanceModel, MetadataRepository metadataRepository) {
        this.presenter = presenter;
        binding.setPresenter(presenter);

        //--------------------------
        //region ATTRI
        if (presenter.getProgramModel() == null)
            compositeDisposable.add(
                    metadataRepository.getTEIAttributeValues(trackedEntityInstanceModel.uid())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(this::setTEIData, OnErrorHandler.create())

            );
        else
            compositeDisposable.add(
                    metadataRepository.getTEIAttributeValues(presenter.getProgramModel().uid(), trackedEntityInstanceModel.uid())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(this::setTEIData, OnErrorHandler.create())

            );
        //endregion

        compositeDisposable.add(
                metadataRepository.getRelationshipType(presenter.getProgramModel().uid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::setRelationshipType, OnErrorHandler.create())
        );

        binding.executePendingBindings();

        binding.buttonAdd.setOnClickListener(view -> presenter.addRelationship(trackedEntityInstanceModel.uid()));
    }

    private void setRelationshipType(RelationshipTypeModel relationshipTypeModel) {
        binding.setRelationship(relationshipTypeModel.aIsToB());
        binding.executePendingBindings();
    }

    private void setTEIData(List<TrackedEntityAttributeValueModel> trackedEntityAttributeValueModels) {
        binding.setAttribute(trackedEntityAttributeValueModels);
        binding.executePendingBindings();
    }


}
