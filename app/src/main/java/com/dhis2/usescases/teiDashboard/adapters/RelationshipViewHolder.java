package com.dhis2.usescases.teiDashboard.adapters;

import android.support.v7.widget.RecyclerView;

import com.dhis2.databinding.ItemRelationshipBinding;
import com.dhis2.usescases.teiDashboard.TeiDashboardContracts;

import org.hisp.dhis.android.core.relationship.Relationship;
import org.hisp.dhis.android.core.relationship.RelationshipModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel;

import java.util.List;

import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;

/**
 * Created by ppajuelo on 05/12/2017.
 */

public class RelationshipViewHolder extends RecyclerView.ViewHolder {

    private final ItemRelationshipBinding binding;
    private CompositeDisposable compositeDisposable;

    public RelationshipViewHolder(ItemRelationshipBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
        this.compositeDisposable = new CompositeDisposable();
    }

    public void bind(TeiDashboardContracts.Presenter presenter, Relationship relationship) {

        compositeDisposable.add(
                presenter.getTEIMainAttributes(presenter.getTeUid().equals(relationship.trackedEntityInstanceA()) ?
                        relationship.trackedEntityInstanceB() : relationship.trackedEntityInstanceA())
                        .subscribe(
                                this::setAttributes,
                                Timber::d
                        )
        );

        binding.teiRelationshipLink.setOnClickListener(view -> {
            String teiUid = presenter.getTeUid().equals(relationship.trackedEntityInstanceA()) ?
                    relationship.trackedEntityInstanceB() : relationship.trackedEntityInstanceA();
            presenter.openDashboard(teiUid);
        });

        binding.setPresenter(presenter);
        binding.setRelationship(relationship);
        binding.executePendingBindings();

//        presenter.subscribeToRelationshipLabel(relationship, binding.relationshipName);
    }

    private void setAttributes(List<TrackedEntityAttributeValueModel> trackedEntityAttributeValueModels) {
        if (trackedEntityAttributeValueModels.size() > 1)
            binding.setTeiName(String.format("%s %s", trackedEntityAttributeValueModels.get(0).value(), trackedEntityAttributeValueModels.get(1).value()));
        else
            binding.setTeiName(trackedEntityAttributeValueModels.get(0).value());
        binding.executePendingBindings();
    }
}
