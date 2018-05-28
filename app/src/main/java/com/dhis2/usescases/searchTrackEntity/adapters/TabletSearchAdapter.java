package com.dhis2.usescases.searchTrackEntity.adapters;


import android.annotation.SuppressLint;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dhis2.R;
import com.dhis2.data.metadata.MetadataRepository;
import com.dhis2.databinding.ItemTableCellAttrBinding;
import com.dhis2.databinding.ItemTableCellProgramBinding;
import com.dhis2.usescases.searchTrackEntity.SearchTEContractsModule;
import com.dhis2.usescases.searchTrackEntity.tableHolder.CellHolder;
import com.dhis2.usescases.searchTrackEntity.tableHolder.HeaderHolder;
import com.dhis2.usescases.searchTrackEntity.tableHolder.RowHolder;
import com.evrencoskun.tableview.adapter.AbstractTableAdapter;
import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder;

import org.hisp.dhis.android.core.program.ProgramModel;
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
    private final int ATTR_COLUMN_NUMBER = 0;
    private List<TrackedEntityInstanceModel> teis;
    private List<ProgramModel> programs;
    private SearchTEContractsModule.Presenter presenter;

    public TabletSearchAdapter(Context p_jContext, SearchTEContractsModule.Presenter presenter, MetadataRepository metadataRepository) {
        super(p_jContext);

        this.metadata = metadataRepository;
        this.presenter = presenter;
    }


    @SuppressLint("CheckResult")
    public void setItems(List<TrackedEntityInstanceModel> teis, List<ProgramModel> programs) {

        this.teis = teis;
        this.programs = programs;

        List<String> headers = new ArrayList<>();
        headers.add("Attr1");
        /*for (ProgramModel programModel : programs) {
            headers.add(programModel.displayShortName());
        }*/

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
        if (position == ATTR_COLUMN_NUMBER)
            return 0;
        else
            return 1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateCellViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0) {
            ItemTableCellAttrBinding attrBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_table_cell_attr, parent, false);
            return new CellHolder(attrBinding);

        } else {
            ItemTableCellProgramBinding programBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_table_cell_program, parent, false);
            return new CellHolder(programBinding);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateColumnHeaderViewHolder(ViewGroup parent, int viewType) {
        return new HeaderHolder(
                DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_table_header, parent, false)
        );
    }

    @Override
    public RecyclerView.ViewHolder onCreateRowHeaderViewHolder(ViewGroup parent, int viewType) {
        return new RowHolder(
                DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_table_row, parent, false)
        );
    }

    @Override
    public void onBindCellViewHolder(AbstractViewHolder holder, Object p_jValue, int p_nXPosition, int p_nYPosition) {
        if (p_nXPosition == ATTR_COLUMN_NUMBER) {
            ((CellHolder) holder).bind(presenter, teis.get(p_nYPosition), metadata, p_nYPosition);
        } else {
            ((CellHolder) holder).bind(teis.get(p_nYPosition), programs.get(p_nXPosition - 1), metadata, p_nYPosition);
        }
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

    public TrackedEntityInstanceModel getTEI(int position){
        return teis.get(position);
    }

}
