package com.dhis2.usescases.programDetailTablet;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dhis2.R;
import org.hisp.dhis.android.core.program.ProgramModel;
import com.evrencoskun.tableview.adapter.AbstractTableAdapter;
import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder;

import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttributeModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;

import java.util.List;

/**
 * Created by ppajuelo on 12/12/2017.
 */

public class ProgramDetailTabletAdapter extends AbstractTableAdapter<TrackedEntityInstance, ProgramTrackedEntityAttributeModel, Integer> {

    private List<ProgramTrackedEntityAttributeModel> attributesToShow;
    private List<OrganisationUnitModel> orgUnits;
    private ProgramModel program;

    public ProgramDetailTabletAdapter(Context p_jContext) {
        super(p_jContext);
    }

    @Override
    public int getColumnHeaderItemViewType(int position) {
        // The unique ID for this type of column header item
        // If you have different items for Cell View by X (Column) position,
        // then you should fill this method to be able create different
        // type of CellViewHolder on "onCreateCellViewHolder"
        return 0;
    }

    @Override
    public int getRowHeaderItemViewType(int position) {
        // The unique ID for this type of row header item
        // If you have different items for Row Header View by Y (Row) position,
        // then you should fill this method to be able create different
        // type of RowHeaderViewHolder on "onCreateRowHeaderViewHolder"
        return 0;
    }

    @Override
    public int getCellItemViewType(int position) {
        return 0;
    }

    public void setAttributesToShow(List<ProgramTrackedEntityAttributeModel> attributesToShow) {
        this.attributesToShow = attributesToShow;
    }

    public void setOrgUnits(List<OrganisationUnitModel> orgUnits) {
        this.orgUnits = orgUnits;
    }

    public void setProgram(ProgramModel program) {
        this.program = program;
    }

    /*
    *  CELL VIEW
    * */

    class CellViewHolder extends AbstractViewHolder {

        public TextView textView;

        public CellViewHolder(View view) {
            super(view);
            textView = view.findViewById(android.R.id.text1);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateCellViewHolder(ViewGroup parent, int viewType) {
        // Get cell xml layout
        View layout = LayoutInflater.from(m_jContext).inflate(R.layout.support_simple_spinner_dropdown_item,
                parent, false);
        // Create a Custom ViewHolder for a Cell item.
        return new CellViewHolder(layout);
    }

    @Override
    public void onBindCellViewHolder(AbstractViewHolder holder, Object p_jValue, int p_nXPosition, int p_nYPosition) {
        CellViewHolder cellViewHolder = (CellViewHolder) holder;
        cellViewHolder.textView.setText(getRowHeaderItem(p_nYPosition).uid());
    }

    /*
    *  COLUMN HEADER VIEW
    * */

    class HeaderViewHolder extends AbstractViewHolder {

        public TextView textView;

        public HeaderViewHolder(View view) {
            super(view);
            textView = view.findViewById(android.R.id.text1);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateColumnHeaderViewHolder(ViewGroup parent, int viewType) {
        View layout = LayoutInflater.from(m_jContext).inflate(R.layout.support_simple_spinner_dropdown_item,
                parent, false);
        return new HeaderViewHolder(layout);
    }

    @Override
    public void onBindColumnHeaderViewHolder(AbstractViewHolder holder, Object p_jValue, int p_nXPosition) {
        HeaderViewHolder cellViewHolder = (HeaderViewHolder) holder;
        cellViewHolder.textView.setText(getRowHeaderItem(p_nXPosition).displayShortName());
    }

    /*
    *  ROW HEADER VIEW
    * */
    class RowViewHolder extends AbstractViewHolder {
        public TextView textView;

        public RowViewHolder(View view) {
            super(view);
            textView = view.findViewById(android.R.id.text1);

        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateRowHeaderViewHolder(ViewGroup parent, int viewType) {
        View layout = LayoutInflater.from(m_jContext).inflate(R.layout.support_simple_spinner_dropdown_item,
                parent, false);
        return new RowViewHolder(layout);
    }

    @Override
    public void onBindRowHeaderViewHolder(AbstractViewHolder holder, Object p_jValue, int p_nYPosition) {
        RowViewHolder cellViewHolder = (RowViewHolder) holder;
        cellViewHolder.textView.setText(""+p_nYPosition);
    }

    @Override
    public View onCreateCornerView() {
        return LayoutInflater.from(m_jContext).inflate(R.layout.table_view_corner_layout, null);
    }
}
