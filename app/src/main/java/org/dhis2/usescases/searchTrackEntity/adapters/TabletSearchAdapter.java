package org.dhis2.usescases.searchTrackEntity.adapters;


import android.annotation.SuppressLint;
import android.content.Context;
import androidx.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.dhis2.R;
import org.dhis2.data.metadata.MetadataRepository;
import org.dhis2.databinding.ItemTableCellAttrBinding;
import org.dhis2.usescases.searchTrackEntity.SearchTEContractsModule;
import org.dhis2.usescases.searchTrackEntity.tableHolder.CellHolder;
import org.dhis2.usescases.searchTrackEntity.tableHolder.HeaderHolder;
import org.dhis2.usescases.searchTrackEntity.tableHolder.RowHolder;
import com.evrencoskun.tableview.adapter.AbstractTableAdapter;
import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder;

import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * QUADRAM. Created by ppajuelo on 07/03/2018.
 */

public class TabletSearchAdapter extends AbstractTableAdapter<String, TrackedEntityInstanceModel, String> {

    private final MetadataRepository metadata;
    private List<TrackedEntityInstanceModel> teis;
    private List<ProgramModel> programs;
    private SearchTEContractsModule.Presenter presenter;

    public TabletSearchAdapter(Context p_jContext, SearchTEContractsModule.Presenter presenter, MetadataRepository metadataRepository) {
        super(p_jContext);

        this.metadata = metadataRepository;
        this.presenter = presenter;
    }


    @SuppressLint("CheckResult")
    public void setItems(List<TrackedEntityInstanceModel> teis, List<ProgramModel> programs, List<TrackedEntityAttributeModel> formData) {

        this.teis = teis;
        this.programs = programs;

        List<String> headers = new ArrayList<>();
        for (TrackedEntityAttributeModel trackedEntityAttributeModel : formData) {
            headers.add(trackedEntityAttributeModel.displayName());
        }

        List<List<String>> cellItems = new ArrayList<>();
        Observable.fromIterable(teis)
                .map(tei -> {
                    cellItems.add(headers);
                    return cellItems;
                })
                .subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.io())
                .subscribe(data -> setCellItems(cellItems), Timber::d);

        setRowHeaderItems(teis);
        setColumnHeaderItems(headers);
    }

    @Override
    public int getColumnHeaderItemViewType(int position) {
        return 0;
    }

    @Override
    public int getRowHeaderItemViewType(int position) {
        return 0;
    }

    @Override
    public int getCellItemViewType(int position) {
        return 0;
    }

    @Override
    public AbstractViewHolder onCreateCellViewHolder(ViewGroup parent, int viewType) {
        ItemTableCellAttrBinding attrBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_table_cell_attr, parent, false);
        return new CellHolder(attrBinding);

    }

    @Override
    public AbstractViewHolder onCreateColumnHeaderViewHolder(ViewGroup parent, int viewType) {
        return new HeaderHolder(
                DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_table_header, parent, false)
        );
    }

    @Override
    public AbstractViewHolder onCreateRowHeaderViewHolder(ViewGroup parent, int viewType) {
        return new RowHolder(
                DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_table_row, parent, false)
        );
    }

    @Override
    public void onBindCellViewHolder(AbstractViewHolder holder, Object p_jValue, int p_nXPosition, int p_nYPosition) {
        ((CellHolder) holder).bind(presenter, teis.get(p_nYPosition), metadata, p_nYPosition, p_nXPosition);
    }

    @Override
    public void onBindColumnHeaderViewHolder(AbstractViewHolder holder, Object p_jValue, int p_nXPosition) {
        ((HeaderHolder) holder).bind((String) p_jValue);
    }

    @Override
    public void onBindRowHeaderViewHolder(AbstractViewHolder holder, Object p_jValue, int p_nYPosition) {
        ((RowHolder) holder).bind(teis.get(p_nYPosition), p_nYPosition);
    }

    @Override
    public View onCreateCornerView() {
        return null;
    }

    public TrackedEntityInstanceModel getTEI(int position) {
        return teis.get(position);
    }

}
