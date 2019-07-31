/*
 * Copyright (c) 2018. Evren Coşkun
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.evrencoskun.tableview.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.evrencoskun.tableview.ITableView;
import com.evrencoskun.tableview.TableView;
import com.evrencoskun.tableview.adapter.recyclerview.CellRecyclerViewAdapter;
import com.evrencoskun.tableview.adapter.recyclerview.ColumnHeaderRecyclerViewAdapter;
import com.evrencoskun.tableview.adapter.recyclerview.RowHeaderRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by evrencoskun on 10/06/2017.
 */

public abstract class AbstractTableAdapter<CH, RH, C> implements ITableAdapter {

    private int mRowHeaderWidth;
    private int mColumnHeaderHeight;

    protected Context mContext;
    private List<ColumnHeaderRecyclerViewAdapter> mColumnsHeaderRecyclerViewAdapters = new ArrayList<>();
    private RowHeaderRecyclerViewAdapter mRowHeaderRecyclerViewAdapter;
    private CellRecyclerViewAdapter mCellRecyclerViewAdapter;
    private View mCornerView;

    protected List<List<CH>> mColumnsHeaderItems = new ArrayList<>();
    protected List<RH> mRowHeaderItems;
    protected List<List<C>> mCellItems;

    private ITableView mTableView;
    private List<AdapterDataSetChangedListener> dataSetChangedListeners;
    private boolean mHasTotal;
    private int headerHeight;
    public OnScale onScaleListener;

    public AbstractTableAdapter(Context context) {
        mContext = context;
    }

    public void setTableView(TableView tableView) {
        mTableView = tableView;
        initialize();
    }

    private void initialize() {
        // Create Column header RecyclerView Adapter
        for (int i = 0; i < mTableView.getHeaderCount(); i++) {
            mColumnsHeaderItems.add(null);
            mColumnsHeaderRecyclerViewAdapters.add(new ColumnHeaderRecyclerViewAdapter(mContext,
                    mColumnsHeaderItems.get(i), this));
        }

        // Create Row Header RecyclerView Adapter
        mRowHeaderRecyclerViewAdapter = new RowHeaderRecyclerViewAdapter(mContext,
                mRowHeaderItems, this);

        // Create Cell RecyclerView Adapter
        mCellRecyclerViewAdapter = new CellRecyclerViewAdapter(mContext, mCellItems, mTableView);
    }

    public void setColumnHeaderItems(List<CH> columnHeaderItems, int header) {
        if (columnHeaderItems == null) {
            return;
        }

        mColumnsHeaderItems.set(header, columnHeaderItems);
        // Invalidate the cached widths for letting the view measure the cells width
        // from scratch.
        mTableView.getColumnHeaderLayoutManager(header).clearCachedWidths();
        // Set the items to the adapter
        mColumnsHeaderRecyclerViewAdapters.get(header).setItems(columnHeaderItems);
        dispatchColumnHeaderDataSetChangesToListeners(columnHeaderItems);
    }

    public void setRowHeaderItems(List<RH> rowHeaderItems) {
        if (rowHeaderItems == null) {
            return;
        }

        mRowHeaderItems = rowHeaderItems;

        // Set the items to the adapter
        mRowHeaderRecyclerViewAdapter.setItems(mRowHeaderItems);
        dispatchRowHeaderDataSetChangesToListeners(mRowHeaderItems);
    }

    public void setCellItems(List<List<C>> cellItems) {
        if (cellItems == null) {
            return;
        }

        mCellItems = cellItems;
        // Invalidate the cached widths for letting the view measure the cells width
        // from scratch.
        mTableView.getCellLayoutManager().clearCachedWidths();
        // Set the items to the adapter
        mCellRecyclerViewAdapter.setItems(mCellItems);
        dispatchCellDataSetChangesToListeners(mCellItems);
    }

    public void setAllItems(List<List<CH>> columnHeaderItems, List<RH> rowHeaderItems, List<List<C>>
            cellItems, boolean hasTotal) {
        // Set all items
        for (int i = 0; i < columnHeaderItems.size(); i++) {
            setColumnHeaderItems(columnHeaderItems.get(i), i);
        }
        setRowHeaderItems(rowHeaderItems);
        setCellItems(cellItems);

        // Control corner view
        if ((columnHeaderItems != null && !columnHeaderItems.isEmpty()) && (rowHeaderItems !=
                null && !rowHeaderItems.isEmpty()) && (cellItems != null && !cellItems.isEmpty())
                && mTableView != null && mCornerView == null) {

            // Create corner view
            mCornerView = onCreateCornerView();
            if (mCornerView != null)
                mTableView.addView(mCornerView, new FrameLayout.LayoutParams(mRowHeaderWidth,
                        headerHeight * columnHeaderItems.size()));
        } else if (mCornerView != null) {

            // Change corner view visibility
            if (rowHeaderItems != null && !rowHeaderItems.isEmpty()) {
                mCornerView.setVisibility(View.VISIBLE);
            } else {
                mCornerView.setVisibility(View.GONE);
            }
        }

        //Handle totalColumn
        mHasTotal = hasTotal;
    }

    public View getCornerView() {
        return mCornerView;
    }

    public ColumnHeaderRecyclerViewAdapter getColumnHeaderRecyclerViewAdapter(int index) {
        return mColumnsHeaderRecyclerViewAdapters.get(index);
    }

    public RowHeaderRecyclerViewAdapter getRowHeaderRecyclerViewAdapter() {
        return mRowHeaderRecyclerViewAdapter;
    }

    public CellRecyclerViewAdapter getCellRecyclerViewAdapter() {
        return mCellRecyclerViewAdapter;
    }

    public void setRowHeaderWidth(int rowHeaderWidth) {
        this.mRowHeaderWidth = rowHeaderWidth;

        if (mCornerView != null) {
            ViewGroup.LayoutParams layoutParams = mCornerView.getLayoutParams();
            layoutParams.width = rowHeaderWidth;
        }
    }

    public int getRowHeaderWidth() {
        return mRowHeaderWidth;
    }

    public void setColumnHeaderHeight(int columnHeaderHeight) {
        this.mColumnHeaderHeight = columnHeaderHeight;
    }

    public void setHeaderHeight(int headerHeight) {
        this.headerHeight = headerHeight;
    }

    public CH getColumnHeaderItem(int position, int header) {
        if ((mColumnsHeaderItems.get(header) == null || mColumnsHeaderItems.get(header).isEmpty()) || position < 0 ||
                position >= mColumnsHeaderItems.get(header).size()) {
            return null;
        }
        return mColumnsHeaderItems.get(header).get(position);
    }

    public RH getRowHeaderItem(int position) {
        if ((mRowHeaderItems == null || mRowHeaderItems.isEmpty()) || position < 0 || position >=
                mRowHeaderItems.size()) {
            return null;
        }
        return mRowHeaderItems.get(position);
    }

    public C getCellItem(int columnPosition, int rowPosition) {
        if ((mCellItems == null || mCellItems.isEmpty()) || columnPosition < 0 || rowPosition >=
                mCellItems.size() || mCellItems.get(rowPosition) == null || rowPosition < 0 ||
                columnPosition >= mCellItems.get(rowPosition).size()) {
            return null;
        }

        return mCellItems.get(rowPosition).get(columnPosition);
    }

    public List<C> getCellRowItems(int rowPosition) {
        return (List<C>) mCellRecyclerViewAdapter.getItem(rowPosition);
    }

    public void removeRow(int rowPosition) {
        mCellRecyclerViewAdapter.deleteItem(rowPosition);
        mRowHeaderRecyclerViewAdapter.deleteItem(rowPosition);
    }

    public void removeRow(int rowPosition, boolean updateRowHeader) {
        mCellRecyclerViewAdapter.deleteItem(rowPosition);

        // To be able update the row header data
        if (updateRowHeader) {
            rowPosition = mRowHeaderRecyclerViewAdapter.getItemCount() - 1;

            // Cell RecyclerView items should be notified.
            // Because, other items stores the old row position.
            mCellRecyclerViewAdapter.notifyDataSetChanged();
        }

        mRowHeaderRecyclerViewAdapter.deleteItem(rowPosition);

    }

    public void removeRowRange(int rowPositionStart, int itemCount) {
        mCellRecyclerViewAdapter.deleteItemRange(rowPositionStart, itemCount);
        mRowHeaderRecyclerViewAdapter.deleteItemRange(rowPositionStart, itemCount);
    }

    public void removeRowRange(int rowPositionStart, int itemCount, boolean updateRowHeader) {
        mCellRecyclerViewAdapter.deleteItemRange(rowPositionStart, itemCount);

        // To be able update the row header data sets
        if (updateRowHeader) {
            rowPositionStart = mRowHeaderRecyclerViewAdapter.getItemCount() - 1 - itemCount;

            // Cell RecyclerView items should be notified.
            // Because, other items stores the old row position.
            mCellRecyclerViewAdapter.notifyDataSetChanged();
        }

        mRowHeaderRecyclerViewAdapter.deleteItemRange(rowPositionStart, itemCount);
    }

    public void addRow(int rowPosition, RH rowHeaderItem, List<C> cellItems) {
        mCellRecyclerViewAdapter.addItem(rowPosition, cellItems);
        mRowHeaderRecyclerViewAdapter.addItem(rowPosition, rowHeaderItem);
    }

    public void addRowRange(int rowPositionStart, List<RH> rowHeaderItem, List<List<C>> cellItems) {
        mRowHeaderRecyclerViewAdapter.addItemRange(rowPositionStart, rowHeaderItem);
        mCellRecyclerViewAdapter.addItemRange(rowPositionStart, cellItems);
    }

    public void changeRowHeaderItem(int rowPosition, RH rowHeaderModel) {
        mRowHeaderRecyclerViewAdapter.changeItem(rowPosition, rowHeaderModel);
    }

    public void changeRowHeaderItemRange(int rowPositionStart, List<RH> rowHeaderModelList) {
        mRowHeaderRecyclerViewAdapter.changeItemRange(rowPositionStart, rowHeaderModelList);
    }

    public void changeCellItem(int columnPosition, int rowPosition, C cellModel, boolean isTotal) {
        List<C> cellItems = (List<C>) mCellRecyclerViewAdapter.getItem(rowPosition);
        if (cellItems != null && cellItems.size() > columnPosition) {
            // Update cell row items.
            cellItems.set(columnPosition, cellModel);

            mCellRecyclerViewAdapter.changeItem(rowPosition, cellItems);
        }
    }

    public void changeColumnHeader(int columnPosition, CH columnHeaderModel) {
        mColumnsHeaderRecyclerViewAdapters.get(0).changeItem(columnPosition, columnHeaderModel);
    }

    public void changeColumnHeaderRange(int columnPositionStart, List<CH> columnHeaderModelList) {
        mColumnsHeaderRecyclerViewAdapters.get(0).changeItemRange(columnPositionStart,
                columnHeaderModelList);
    }


    public List<C> getCellColumnItems(int columnPosition) {
        return mCellRecyclerViewAdapter.getColumnItems(columnPosition);
    }

    public void removeColumn(int columnPosition) {
        mColumnsHeaderRecyclerViewAdapters.get(0).deleteItem(columnPosition);
        mCellRecyclerViewAdapter.removeColumnItems(columnPosition);
    }

    public void addColumn(int columnPosition, CH columnHeaderItem, List<C> cellItems) {
        mColumnsHeaderRecyclerViewAdapters.get(0).addItem(columnPosition, columnHeaderItem);
        mCellRecyclerViewAdapter.addColumnItems(columnPosition, cellItems);
    }


    public final void notifyDataSetChanged() {
        for (int i = 0; i < mColumnsHeaderRecyclerViewAdapters.size(); i++)
            mColumnsHeaderRecyclerViewAdapters.get(i).notifyDataSetChanged();
        mRowHeaderRecyclerViewAdapter.notifyDataSetChanged();
        mCellRecyclerViewAdapter.notifyCellDataSetChanged();
    }

    @Override
    public ITableView getTableView() {
        return mTableView;
    }

    @SuppressWarnings("unchecked")
    private void dispatchColumnHeaderDataSetChangesToListeners(List<CH> newColumnHeaderItems) {
        if (dataSetChangedListeners != null) {
            for (AdapterDataSetChangedListener listener : dataSetChangedListeners) {
                listener.onColumnHeaderItemsChanged(newColumnHeaderItems);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void dispatchRowHeaderDataSetChangesToListeners(final List<RH> newRowHeaderItems) {
        if (dataSetChangedListeners != null) {
            for (AdapterDataSetChangedListener listener : dataSetChangedListeners) {
                listener.onRowHeaderItemsChanged(newRowHeaderItems);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void dispatchCellDataSetChangesToListeners(List<List<C>> newCellItems) {
        if (dataSetChangedListeners != null) {
            for (AdapterDataSetChangedListener listener : dataSetChangedListeners) {
                listener.onCellItemsChanged(newCellItems);
            }
        }
    }

    /**
     * Sets the listener for changes of data set on the TableView.
     *
     * @param listener The AdapterDataSetChangedListener listener.
     */
    public void addAdapterDataSetChangedListener(AdapterDataSetChangedListener listener) {
        if (dataSetChangedListeners == null) {
            dataSetChangedListeners = new ArrayList<>();
        }

        dataSetChangedListeners.add(listener);
    }

    protected int getHeaderRecyclerPositionFor(Object object) {
        for (int i = 0; i < mTableView.getHeaderCount(); i++) {
            if (mColumnsHeaderRecyclerViewAdapters.get(i).getItems().contains(object)) {
                if (i != mTableView.getHeaderCount() - 1) {
                    int p = mColumnsHeaderRecyclerViewAdapters.get(i + 1).getItems().size() / mColumnsHeaderRecyclerViewAdapters.get(i).getItems().size();
                    if (mHasTotal)
                        p = (mColumnsHeaderRecyclerViewAdapters.get(i + 1).getItems().size() - 1) / (mColumnsHeaderRecyclerViewAdapters.get(i).getItems().size() - 1);

                    return p * (mTableView.getHeaderCount() - (i + 1));
                }
                return mTableView.getHeaderCount() - i;
            }
        }
        return 1;
    }


    public void clear() {
        mColumnsHeaderRecyclerViewAdapters.clear();
        mColumnsHeaderItems.clear();
    }

    public interface OnScale {
        void scaleTo(int width, int height);
    }


    @Override
    public void setOnScaleListener(AbstractTableAdapter.OnScale listener) {
        this.onScaleListener = listener;
    }

    public boolean hasTotal() {
        return mHasTotal;
    }
}
