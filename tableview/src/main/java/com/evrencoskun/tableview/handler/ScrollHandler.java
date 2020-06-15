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

package com.evrencoskun.tableview.handler;

import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.evrencoskun.tableview.ITableView;
import com.evrencoskun.tableview.adapter.recyclerview.CellRecyclerView;
import com.evrencoskun.tableview.layoutmanager.CellLayoutManager;
import com.evrencoskun.tableview.layoutmanager.ColumnHeaderLayoutManager;
import com.evrencoskun.tableview.layoutmanager.ColumnLayoutManager;

import java.util.List;

/**
 * Created by evrencoskun on 13.01.2018.
 */

public class ScrollHandler {
    private ITableView mTableView;
    private CellLayoutManager mCellLayoutManager;
    private LinearLayoutManager mRowHeaderLayoutManager;
    private ColumnHeaderLayoutManager mColumnHeaderLayoutManager;
    private final List<CellRecyclerView> mBackupHeaders;
    private List<CellRecyclerView> mColumnHeaderRecyclerViews;
    private RecyclerView mLastTouchedRecyclerView;

    public ScrollHandler(ITableView tableView) {
        this.mTableView = tableView;
        this.mCellLayoutManager = tableView.getCellLayoutManager();
        this.mRowHeaderLayoutManager = tableView.getRowHeaderLayoutManager();
        this.mColumnHeaderLayoutManager = tableView.getColumnHeaderLayoutManager(mTableView.getHeaderCount() - 1);

        this.mColumnHeaderRecyclerViews = tableView.getColumnHeaderRecyclerView();
        this.mBackupHeaders = tableView.getBackupHeaders();
    }

    public void scrollToColumnPosition(int columnPosition) {
        // TableView is not on screen yet.
        if (!((View) mTableView).isShown()) {
            // Change default value of the listener
            mTableView.getHorizontalRecyclerViewListener().setScrollPosition(columnPosition);
        }

        // Column Header should be scrolled firstly because of fitting column width process.
        scrollColumnHeader(columnPosition, 0);
        scrollCellHorizontally(columnPosition, 0);
    }

    public void scrollToColumnPosition(int columnPosition, int offset) {
        // TableView is not on screen yet.
        if (!((View) mTableView).isShown()) {
            // Change default value of the listener
            mTableView.getHorizontalRecyclerViewListener().setScrollPosition(columnPosition);
            mTableView.getHorizontalRecyclerViewListener().setScrollPositionOffset(offset);
        }

        // Column Header should be scrolled firstly because of fitting column width process.
        scrollColumnHeader(columnPosition, offset);
        scrollCellHorizontally(columnPosition, offset);
    }

    public void scrollToRowPosition(int rowPosition) {
        mRowHeaderLayoutManager.scrollToPosition(rowPosition);
        mCellLayoutManager.scrollToPosition(rowPosition);
    }

    public void scrollToRowPosition(int rowPosition, int offset) {
        mRowHeaderLayoutManager.scrollToPositionWithOffset(rowPosition, offset);
        mCellLayoutManager.scrollToPositionWithOffset(rowPosition, offset);
    }

    private void scrollCellHorizontally(int columnPosition, int offset) {
        CellLayoutManager cellLayoutManager = mTableView.getCellLayoutManager();

        for (int i = cellLayoutManager.findFirstVisibleItemPosition(); i < cellLayoutManager
                .findLastVisibleItemPosition() + 1; i++) {

            RecyclerView cellRowRecyclerView = (RecyclerView) cellLayoutManager
                    .findViewByPosition(i);

            if (cellRowRecyclerView != null) {
                ColumnLayoutManager columnLayoutManager = (ColumnLayoutManager)
                        cellRowRecyclerView.getLayoutManager();

                columnLayoutManager.scrollToPositionWithOffset(columnPosition, offset);
            }
        }
    }

    public void scrollToNextField() {
        final CellRecyclerView child = (CellRecyclerView) mCellLayoutManager.getChildAt(mTableView.getSelectedRow());
        child.post(new Runnable() {
            @Override
            public void run() {
                child.scrollBy(300, 0);
            }
        });
    }

    private void scrollColumnHeader(int columnPosition, int offset) {

        int totalCorrection = mTableView.getAdapter().hasTotal() ? 1 : 0;

        for (int i = 0; i < mTableView.getHeaderCount(); i++) {

            int step = (mTableView.getBackupHeaders().get(mTableView.getHeaderCount() - 1).getAdapter().getItemCount() - totalCorrection) /
                    ((mTableView.getBackupHeaders().get(i).getAdapter().getItemCount() - totalCorrection));

            int correctedColumn = columnPosition / step;

            int correctedOffset = (300+offset) * (columnPosition % step) - offset;

            mTableView.getColumnHeaderLayoutManager(i)
                    .scrollToPositionWithOffset(correctedColumn, -correctedOffset);

            ((ColumnHeaderLayoutManager) mTableView.getBackupHeaders().get(i).getLayoutManager())
                    .scrollToPositionWithOffset(correctedColumn, -correctedOffset);
        }
    }

    public int getColumnPosition() {
        return mColumnHeaderLayoutManager.findFirstVisibleItemPosition();
    }

    public int getColumnPositionOffset() {
        View child = mColumnHeaderLayoutManager.findViewByPosition(mColumnHeaderLayoutManager
                .findFirstVisibleItemPosition());
        if (child != null) {
            return child.getLeft();
        }
        return 0;
    }

    public int getRowPosition() {
        return mRowHeaderLayoutManager.findFirstVisibleItemPosition();
    }

    public int getRowPositionOffset() {
        View child = mRowHeaderLayoutManager.findViewByPosition(mRowHeaderLayoutManager
                .findFirstVisibleItemPosition());
        if (child != null) {
            return child.getLeft();
        }
        return 0;
    }
}
