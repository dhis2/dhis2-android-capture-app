package org.dhis2.usescases.searchTrackEntity.tableHolder;

import org.dhis2.data.metadata.MetadataRepository;
import org.dhis2.databinding.ItemTableCellAttrBinding;
import org.dhis2.databinding.ItemTableCellProgramBinding;
import org.dhis2.usescases.searchTrackEntity.SearchTEContractsModule;
import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder;

import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by ppajuelo on 07/03/2018.
 */

public class CellHolder extends AbstractViewHolder {

    private ItemTableCellAttrBinding attrBinding;
    private ItemTableCellProgramBinding programbinding;
    private CompositeDisposable compositeDisposable;

    public CellHolder(ItemTableCellAttrBinding binding) {
        super(binding.getRoot());
        this.attrBinding = binding;
        compositeDisposable = new CompositeDisposable();

    }

    public CellHolder(ItemTableCellProgramBinding binding) {
        super(binding.getRoot());
        this.programbinding = binding;
        compositeDisposable = new CompositeDisposable();

    }

    public void bind(SearchTEContractsModule.Presenter presenter, TrackedEntityInstanceModel trackedEntityInstanceModel, MetadataRepository metadata, int p_nYPosition, int p_nXPosition) {
        attrBinding.setPosition(p_nYPosition);
        if (presenter.getProgramModel() == null)
            compositeDisposable.add(
                    metadata.getTEIAttributeValues(trackedEntityInstanceModel.uid())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(data -> setTEIData(data, p_nXPosition), Timber::d)

            );
        else
            compositeDisposable.add(
                    metadata.getTEIAttributeValues(presenter.getProgramModel().uid(), trackedEntityInstanceModel.uid())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(data -> setTEIData(data, p_nXPosition), Timber::d)
            );
    }


    private void setTEIData(List<TrackedEntityAttributeValueModel> trackedEntityAttributeValueModels, int position) {

        attrBinding.setAttr(trackedEntityAttributeValueModels.get(position).value());
        attrBinding.executePendingBindings();
    }


}
