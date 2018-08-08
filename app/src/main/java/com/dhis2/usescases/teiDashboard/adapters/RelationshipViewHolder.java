package com.dhis2.usescases.teiDashboard.adapters;

import android.support.v7.widget.RecyclerView;

import com.dhis2.data.tuples.Pair;
import com.dhis2.databinding.ItemRelationshipBinding;
import com.dhis2.usescases.teiDashboard.TeiDashboardContracts;

import org.hisp.dhis.android.core.relationship.Relationship;
import org.hisp.dhis.android.core.relationship.RelationshipType;
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

    public void bind(TeiDashboardContracts.Presenter presenter, Pair<Relationship, RelationshipType> relationships) {

        Relationship relationship = relationships.val0();
        String relationshipTEIUid;
        boolean from;

        if (!presenter.getTeUid().equals(relationship.from().trackedEntityInstance().trackedEntityInstance())) {
            relationshipTEIUid = relationship.from().trackedEntityInstance().trackedEntityInstance();
            from = true;
        }else {
            relationshipTEIUid = relationship.to().trackedEntityInstance().trackedEntityInstance();
            from = false;
        }
        compositeDisposable.add(
                presenter.getTEIMainAttributes(relationshipTEIUid)
                        .subscribe(
                                this::setAttributes,
                                Timber::d
                        )
        );

        binding.teiRelationshipLink.setOnClickListener(view -> {
            presenter.openDashboard(relationshipTEIUid);
        });

        binding.setPresenter(presenter);
        binding.setRelationship(relationship);
        binding.relationshipName.setText(from?relationships.val1().aIsToB():relationships.val1().bIsToA());
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
