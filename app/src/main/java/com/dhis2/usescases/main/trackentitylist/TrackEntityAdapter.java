package com.dhis2.usescases.main.trackentitylist;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.dhis2.R;
import com.dhis2.databinding.CellLayoutBinding;
import com.dhis2.databinding.ColumnLayoutBinding;
import com.dhis2.databinding.RowLayoutBinding;
import com.evrencoskun.tableview.adapter.AbstractTableAdapter;

/**
 * Created by frodriguez on 10/19/2017.
 *
 */

public class TrackEntityAdapter  extends AbstractTableAdapter<ColumnHeaderModel, RowHeaderModel, CellModel> {

    private TrackEntityListPresenter presenter;

    public TrackEntityAdapter(Context p_jContext, TrackEntityListPresenter presenter) {
        super(p_jContext);
        this.presenter = presenter;
    }

    /*public TrackEntityAdapter(Context context, TrackEntityListPresenter presenter) {
        super(context);
        this.presenter = presenter;
    }*/

    @Override
    public RecyclerView.ViewHolder onCreateCellViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(m_jContext);
        CellLayoutBinding cellBinding = DataBindingUtil.inflate(inflater, R.layout.cell_layout, parent, false);
        return new CellViewHolder(cellBinding);
    }

    @Override
    public void onBindCellViewHolder(RecyclerView.ViewHolder holder, int p_nXPosition, int p_nYPosition) {
        CellModel cell = getCellItem(p_nXPosition, p_nYPosition);
        CellViewHolder cellViewHolder = (CellViewHolder) holder;
        cellViewHolder.itemView.getLayoutParams().width = LinearLayout.LayoutParams.WRAP_CONTENT;
        cellViewHolder.bind(presenter, cell);
    }

    @Override
    public RecyclerView.ViewHolder onCreateColumnHeaderViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(m_jContext);
        ColumnLayoutBinding columnBinding = DataBindingUtil.inflate(inflater, R.layout.column_layout, parent, false);
        return new ColumnViewHolder(columnBinding);
    }

    @Override
    public void onBindColumnHeaderViewHolder(RecyclerView.ViewHolder holder, int position) {
        ColumnHeaderModel column = getColumnHeaderItem(position);
        ColumnViewHolder columnViewHolder = (ColumnViewHolder) holder;
        columnViewHolder.itemView.getLayoutParams().width = LinearLayout.LayoutParams.WRAP_CONTENT;
        columnViewHolder.bind(presenter, column);
    }

    @Override
    public RecyclerView.ViewHolder onCreateRowHeaderViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(m_jContext);
        RowLayoutBinding rowBinding = DataBindingUtil.inflate(inflater, R.layout.row_layout, parent, false);
        return new RowViewHolder(rowBinding);
    }

    @Override
    public void onBindRowHeaderViewHolder(RecyclerView.ViewHolder holder, int position) {
        RowHeaderModel row = getRowHeaderItem(position);
        RowViewHolder rowViewHolder = (RowViewHolder) holder;
        rowViewHolder.bind(presenter, row);
    }

    @Override
    public View onCreateCornerView() {
        return LayoutInflater.from(m_jContext).inflate(R.layout.table_view_corner_layout, null);
    }

    @Override
    public int getColumnHeaderItemViewType(int position) {
        return 0;
    }

    @Override
    public int getRowHeaderItemViewType(int position) {
        return 0;
    }
}
