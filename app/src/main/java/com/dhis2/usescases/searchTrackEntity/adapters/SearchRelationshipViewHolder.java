package com.dhis2.usescases.searchTrackEntity.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;

import com.dhis2.data.metadata.MetadataRepository;
import com.dhis2.databinding.ItemSearchRelationshipTrackedEntityBinding;
import com.dhis2.usescases.searchTrackEntity.SearchTEContractsModule;

import org.hisp.dhis.android.core.relationship.RelationshipTypeModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by frodriguez on 11/7/2017.
 */

public class SearchRelationshipViewHolder extends RecyclerView.ViewHolder {

    private ItemSearchRelationshipTrackedEntityBinding binding;
    private CompositeDisposable compositeDisposable;
    private SearchTEContractsModule.Presenter presenter;
    private MetadataRepository metadataRepository;
    private TrackedEntityInstanceModel trackedEntityInstanceModel;

    private RelationshipTypeModel relationshipType;
    private List<RelationshipTypeModel> relationshipTypes = new ArrayList<>();

    SearchRelationshipViewHolder(ItemSearchRelationshipTrackedEntityBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
        compositeDisposable = new CompositeDisposable();
    }


    public void bind(SearchTEContractsModule.Presenter presenter, TrackedEntityInstanceModel trackedEntityInstanceModel, MetadataRepository metadataRepository) {
        this.presenter = presenter;
        this.metadataRepository = metadataRepository;
        this.trackedEntityInstanceModel = trackedEntityInstanceModel;
        binding.setPresenter(presenter);

        //--------------------------
        //region ATTRI
        if (presenter.getProgramModel() == null)
            compositeDisposable.add(
                    metadataRepository.getTEIAttributeValues(trackedEntityInstanceModel.uid())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(this::setTEIData, Timber::d)

            );
        else
            compositeDisposable.add(
                    metadataRepository.getTEIAttributeValues(presenter.getProgramModel().uid(), trackedEntityInstanceModel.uid())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(this::setTEIData, Timber::d)

            );
        //endregion

        compositeDisposable.add(
                metadataRepository.getRelationshipType(presenter.getProgramModel().uid())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::setRelationshipType, Timber::d)
        );

        binding.executePendingBindings();

        binding.buttonAdd.setOnClickListener(
                view -> {
                    if (relationshipType.uid() != null)
                        presenter.addRelationship(trackedEntityInstanceModel.uid(), null);
                    else {
                        binding.relationshipSpinner.performClick();
                    }
                });

    }

    private void getRelationshipTypeList() {
        compositeDisposable.add(
                metadataRepository.getRelationshipTypeList()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::setRelationshipTypeList, Timber::d)
        );
    }

    private void setRelationshipTypeList(List<RelationshipTypeModel> relationshipTypesModels) {
        RelationshipTypeModel fake = RelationshipTypeModel.builder()
                .aIsToB(" ")
                .build();
        this.relationshipTypes.add(fake);
        this.relationshipTypes.addAll(relationshipTypesModels);
        RelationshipSpinnerAdapter adapter = new RelationshipSpinnerAdapter(binding.getRoot().getContext(), this.relationshipTypes);
        binding.relationshipSpinner.setAdapter(adapter);
        binding.relationshipSpinner.setSelection(adapter.NO_SELECTION, false);
        binding.relationshipSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                presenter.addRelationship(trackedEntityInstanceModel.uid(), relationshipTypes.get(position).uid());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setRelationshipType(RelationshipTypeModel relationshipTypeModel) {
        relationshipType = relationshipTypeModel;
        binding.setRelationship(relationshipType.aIsToB());
        if (relationshipType.uid() == null)
            getRelationshipTypeList();
        binding.executePendingBindings();
    }

    private void setTEIData(List<TrackedEntityAttributeValueModel> trackedEntityAttributeValueModels) {
        binding.setAttribute(trackedEntityAttributeValueModels);
        binding.executePendingBindings();
    }


}
