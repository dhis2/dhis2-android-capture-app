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

package com.evrencoskun.tableview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.evrencoskun.tableview.adapter.AbstractTableAdapter;
import com.evrencoskun.tableview.adapter.recyclerview.CellRecyclerView;
import com.evrencoskun.tableview.adapter.recyclerview.ColumnHeaderRecyclerViewAdapter;
import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder;
import com.evrencoskun.tableview.filter.Filter;
import com.evrencoskun.tableview.handler.ColumnSortHandler;
import com.evrencoskun.tableview.handler.ColumnWidthHandler;
import com.evrencoskun.tableview.handler.FilterHandler;
import com.evrencoskun.tableview.handler.PreferencesHandler;
import com.evrencoskun.tableview.handler.ScrollHandler;
import com.evrencoskun.tableview.handler.SelectionHandler;
import com.evrencoskun.tableview.handler.VisibilityHandler;
import com.evrencoskun.tableview.layoutmanager.CellLayoutManager;
import com.evrencoskun.tableview.layoutmanager.ColumnHeaderLayoutManager;
import com.evrencoskun.tableview.listener.ITableViewListener;
import com.evrencoskun.tableview.listener.TableViewLayoutChangeListener;
import com.evrencoskun.tableview.listener.itemclick.ColumnHeaderRecyclerViewItemClickListener;
import com.evrencoskun.tableview.listener.itemclick.RowHeaderRecyclerViewItemClickListener;
import com.evrencoskun.tableview.listener.scroll.HorizontalRecyclerViewListener;
import com.evrencoskun.tableview.listener.scroll.VerticalRecyclerViewListener;
import com.evrencoskun.tableview.preference.SavedState;
import com.evrencoskun.tableview.sort.SortState;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by evrencoskun on 11/06/2017.
 */

public class TableView extends FrameLayout implements ITableView {

    private static final String LOG_TAG = TableView.class.getSimpleName();

    protected CellRecyclerView mCellRecyclerView;
    protected CellRecyclerView mColumnHeaderRecyclerView;
    protected List<CellRecyclerView> mColumnHeaderRecyclerViews = new ArrayList<>();
    protected CellRecyclerView mRowHeaderRecyclerView;

    protected AbstractTableAdapter mTableAdapter;
    private ITableViewListener mTableViewListener;

    private VerticalRecyclerViewListener mVerticalRecyclerListener;
    private HorizontalRecyclerViewListener mHorizontalRecyclerViewListener;

    private ColumnHeaderRecyclerViewItemClickListener mColumnHeaderRecyclerViewItemClickListener;
    private RowHeaderRecyclerViewItemClickListener mRowHeaderRecyclerViewItemClickListener;

    private List<ColumnHeaderLayoutManager> mColumnHeaderLayoutManagers = new ArrayList<>();
    private LinearLayoutManager mRowHeaderLayoutManager;
    private CellLayoutManager mCellLayoutManager;

    private DividerItemDecoration mVerticalItemDecoration;
    private DividerItemDecoration mHorizontalItemDecoration;

    private SelectionHandler mSelectionHandler;
    private ColumnSortHandler mColumnSortHandler;
    private VisibilityHandler mVisibilityHandler;
    private ScrollHandler mScrollHandler;
    private FilterHandler mFilterHandler;
    private PreferencesHandler mPreferencesHandler;
    private ColumnWidthHandler mColumnWidthHandler;

    private int mRowHeaderWidth;
    private int mColumnHeaderHeight;
    private int mHeaderHeight;
    private int mHeaderWidth;

    private int mSelectedColor;
    private int mUnSelectedColor;
    private int mShadowColor;
    private int mSeparatorColor = -1;
    private int mHeadersColor;

    private boolean mHasFixedWidth;
    private boolean mIgnoreSelectionColors;
    private boolean mShowHorizontalSeparators = true;
    private boolean mShowVerticalSeparators = true;
    private boolean mIsSortable;
    private int mHeaderCount = 1;
    private List<CellRecyclerView> mBackupHeaders = new ArrayList<>();
    private AbstractTableAdapter.OnScale scaleListener;

    public TableView(@NonNull Context context) {
        super(context);
        initialDefaultValues(null);
        initialize();
    }

    public TableView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialDefaultValues(attrs);
        initialize();
    }

    public TableView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int
            defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialDefaultValues(null);
        initialize();
    }

    private void initialDefaultValues(AttributeSet attrs) {
        // Dimensions
        mRowHeaderWidth = (int) getResources().getDimension(R.dimen.default_row_header_width);
        mColumnHeaderHeight = (int) getResources().getDimension(R.dimen.default_column_header_height);
        mHeaderHeight = (int) getResources().getDimension(R.dimen.small_column_header_height);
        mHeaderWidth = (int) getResources().getDimension(R.dimen.small_row_header_width);
        // Colors
        mSelectedColor = ContextCompat.getColor(getContext(), R.color
                .table_view_default_selected_background_color);
        mUnSelectedColor = ContextCompat.getColor(getContext(), R.color
                .table_view_default_unselected_background_color);
        mShadowColor = ContextCompat.getColor(getContext(), R.color
                .table_view_default_shadow_background_color);
        mHeadersColor = ContextCompat.getColor(getContext(), R.color
                .table_view_default_header_background_color);

        if (attrs == null) {
            // That means TableView is created programmatically.
            return;
        }

        // Get values from xml attributes
        TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable
                .TableView, 0, 0);
        try {
            // Dimensions
            mRowHeaderWidth = (int) a.getDimension(R.styleable.TableView_row_header_width,
                    mRowHeaderWidth);
            mColumnHeaderHeight = (int) a.getDimension(R.styleable
                    .TableView_column_header_height, mColumnHeaderHeight);

            // Colors
            mSelectedColor = a.getColor(R.styleable.TableView_selected_color, mSelectedColor);
            mUnSelectedColor = a.getColor(R.styleable.TableView_unselected_color, mUnSelectedColor);
            mShadowColor = a.getColor(R.styleable.TableView_shadow_color, mShadowColor);
            mSeparatorColor = a.getColor(R.styleable.TableView_separator_color, ContextCompat
                    .getColor(getContext(), R.color.table_view_default_separator_color));

            // Booleans
            mShowVerticalSeparators = a.getBoolean(R.styleable.TableView_show_vertical_separator,
                    mShowVerticalSeparators);
            mShowHorizontalSeparators = a.getBoolean(R.styleable
                    .TableView_show_horizontal_separator, mShowHorizontalSeparators);

        } finally {
            a.recycle();
        }
    }

    private void initialize() {

        // Create Views
        for (int i = 0; i < mHeaderCount; i++) {
            mColumnHeaderRecyclerViews.add(createColumnHeaderRecyclerView(i));
        }
        mRowHeaderRecyclerView = createRowHeaderRecyclerView();
        mCellRecyclerView = createCellRecyclerView();

        // Add Views
        for (int i = 0; i < mHeaderCount; i++) {
            addView(mColumnHeaderRecyclerViews.get(i));
        }
        addView(mRowHeaderRecyclerView);
        addView(mCellRecyclerView);

        // Create Handlers
        mSelectionHandler = new SelectionHandler(this);
        mVisibilityHandler = new VisibilityHandler(this);
        mScrollHandler = new ScrollHandler(this);
        mPreferencesHandler = new PreferencesHandler(this);
        mColumnWidthHandler = new ColumnWidthHandler(this);

        mColumnHeaderRecyclerView = mColumnHeaderRecyclerViews.get(0);
        mBackupHeaders = createHeaderBackUps();
        initializeListeners();
    }

    protected void initializeListeners() {

        // --- Listeners to help Scroll synchronously ---
        // It handles Vertical scroll listener
        mVerticalRecyclerListener = new VerticalRecyclerViewListener(this);

        // Set this listener both of Cell RecyclerView and RowHeader RecyclerView
        mRowHeaderRecyclerView.addOnItemTouchListener(mVerticalRecyclerListener);
        mCellRecyclerView.addOnItemTouchListener(mVerticalRecyclerListener);


        // It handles Horizontal scroll listener
        mHorizontalRecyclerViewListener = new HorizontalRecyclerViewListener(this);
        // Set scroll listener to be able to scroll all rows synchrony.
        for (int i = 0; i < mHeaderCount; i++) {
            mColumnHeaderRecyclerViews.get(i).addOnItemTouchListener(mHorizontalRecyclerViewListener);
            mBackupHeaders.get(i).addOnItemTouchListener(mHorizontalRecyclerViewListener);
        }


        /*// --- Listeners to help item clicks ---
        // Create item click listeners
        mColumnHeaderRecyclerViewItemClickListener = new
                ColumnHeaderRecyclerViewItemClickListener(mColumnHeaderRecyclerViews.get(mHeaderCount-1), this);
        mRowHeaderRecyclerViewItemClickListener = new RowHeaderRecyclerViewItemClickListener
                (mRowHeaderRecyclerView, this);

        // Add item click listeners for both column header & row header recyclerView
        mColumnHeaderRecyclerViews.get(mHeaderCount-1).addOnItemTouchListener
                (mColumnHeaderRecyclerViewItemClickListener);
        mRowHeaderRecyclerView.addOnItemTouchListener(mRowHeaderRecyclerViewItemClickListener);*/


        // Add Layout change listener both of Column Header  & Cell recyclerView to detect
        // changing size
        // For some case, it is pretty necessary.
        TableViewLayoutChangeListener layoutChangeListener = new TableViewLayoutChangeListener
                (this);
        for (int i = 0; i < mHeaderCount; i++) {
            mColumnHeaderRecyclerViews.get(i).addOnLayoutChangeListener(layoutChangeListener);
            mBackupHeaders.get(i).addOnLayoutChangeListener(layoutChangeListener);
        }
        mCellRecyclerView.addOnLayoutChangeListener(layoutChangeListener);
    }

    protected CellRecyclerView createColumnHeaderRecyclerView(int header) {
        CellRecyclerView recyclerView = new CellRecyclerView(getContext());

        // Set layout manager
        recyclerView.setLayoutManager(getColumnHeaderLayoutManager(header));

        // Set layout params
        LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, mHeaderHeight);
        layoutParams.leftMargin = mRowHeaderWidth;
        layoutParams.topMargin = mHeaderHeight * header;
        recyclerView.setLayoutParams(layoutParams);

        if (isShowHorizontalSeparators()) {
            // Add vertical item decoration to display column line
            recyclerView.addItemDecoration(getHorizontalItemDecoration());
            recyclerView.addItemDecoration(getVerticalItemDecoration());
        }

        return recyclerView;
    }

    public List<CellRecyclerView> createHeaderBackUps() {
        List<CellRecyclerView> headerBackups = new ArrayList<>();

        for (int i = 0; i < mColumnHeaderLayoutManagers.size(); i++) {
            CellRecyclerView recyclerView = new CellRecyclerView(getContext());

            // Set layout manager
            recyclerView.setLayoutManager(new ColumnHeaderLayoutManager(getContext(), this));

            // Set layout params
            LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, mHeaderHeight);
            layoutParams.leftMargin = mRowHeaderWidth;
            layoutParams.topMargin = mHeaderHeight * i;
            recyclerView.setLayoutParams(layoutParams);

            if (isShowHorizontalSeparators()) {
                // Add vertical item decoration to display column line
                recyclerView.addItemDecoration(getHorizontalItemDecoration());
                recyclerView.addItemDecoration(getVerticalItemDecoration());
            }

            ColumnHeaderRecyclerViewAdapter adapter = (ColumnHeaderRecyclerViewAdapter) mColumnHeaderRecyclerViews.get(i).getAdapter();
            recyclerView.setAdapter(adapter);
            headerBackups.add(recyclerView);
        }
        return headerBackups;
    }

    protected CellRecyclerView createRowHeaderRecyclerView() {
        if (mRowHeaderRecyclerView == null) {
            mRowHeaderRecyclerView = new CellRecyclerView(getContext());

            // Set layout manager
            mRowHeaderRecyclerView.setLayoutManager(getRowHeaderLayoutManager());

        } else {

            // Set layout params
            LayoutParams layoutParams = new LayoutParams(mRowHeaderWidth, LayoutParams.WRAP_CONTENT);
            layoutParams.topMargin = mHeaderHeight * mHeaderCount;
            mRowHeaderRecyclerView.setLayoutParams(layoutParams);


            if (isShowVerticalSeparators()) {
                // Add vertical item decoration to display row line
                mRowHeaderRecyclerView.addItemDecoration(getVerticalItemDecoration());
                mRowHeaderRecyclerView.addItemDecoration(getHorizontalItemDecoration());
            }

        }
        return mRowHeaderRecyclerView;
    }

    protected CellRecyclerView createCellRecyclerView() {
        if (mCellRecyclerView == null) {
            mCellRecyclerView = new CellRecyclerView(getContext());

            // Disable multitouch
            mCellRecyclerView.setMotionEventSplittingEnabled(false);

            // Set layout manager
            mCellRecyclerView.setLayoutManager(getCellLayoutManager());
        } else {
            // Set layout params
            LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams
                    .WRAP_CONTENT);
            layoutParams.leftMargin = mRowHeaderWidth;
            layoutParams.topMargin = mHeaderHeight * mHeaderCount;
            mCellRecyclerView.setLayoutParams(layoutParams);

            if (isShowVerticalSeparators()) {
                // Add vertical item decoration to display row line on center recycler view
                mCellRecyclerView.addItemDecoration(getVerticalItemDecoration());
            }
        }
        return mCellRecyclerView;
    }


    public void setAdapter(AbstractTableAdapter tableAdapter) {
        if (tableAdapter != null) {
            this.mTableAdapter = tableAdapter;
            this.mTableAdapter.setRowHeaderWidth(mRowHeaderWidth);
            this.mTableAdapter.setColumnHeaderHeight(mColumnHeaderHeight);
            this.mTableAdapter.setHeaderHeight(mHeaderHeight);
            this.mTableAdapter.setTableView(this);

            // set adapters
            for (int i = 0; i < mHeaderCount; i++) {
                if (mColumnHeaderRecyclerViews.get(i) != null) {
                    mColumnHeaderRecyclerViews.get(i).setAdapter(mTableAdapter
                            .getColumnHeaderRecyclerViewAdapter(i));
                }

                if (mBackupHeaders.get(i) != null) {
                    mBackupHeaders.get(i).setAdapter(mTableAdapter
                            .getColumnHeaderRecyclerViewAdapter(i));
                }
            }
            if (mRowHeaderRecyclerView != null) {
                mRowHeaderRecyclerView.setAdapter(mTableAdapter.getRowHeaderRecyclerViewAdapter());
            }
            if (mCellRecyclerView != null) {
                mCellRecyclerView.setAdapter(mTableAdapter.getCellRecyclerViewAdapter());

                // Create Sort Handler
                mColumnSortHandler = new ColumnSortHandler(this);

                // Create Filter Handler
                mFilterHandler = new FilterHandler(this);
            }


            tableAdapter.setOnScaleListener(new AbstractTableAdapter.OnScale() {
                @Override
                public void scaleTo(int width, int height) {
                    //ROW HEADERS
                    FrameLayout.LayoutParams mRowHeaderParams = ((FrameLayout.LayoutParams) mRowHeaderRecyclerView.getLayoutParams());
                    mRowHeaderParams.topMargin = height * mHeaderCount;

                    //COLUMN HEADERS
                    for (int i = 0; i < mColumnHeaderRecyclerViews.size(); i++) {
                        FrameLayout.LayoutParams mColumnHeaderParams = ((FrameLayout.LayoutParams) mColumnHeaderRecyclerViews.get(i).getLayoutParams());
                        FrameLayout.LayoutParams mBuckupHeaderParams = ((FrameLayout.LayoutParams) mBackupHeaders.get(i).getLayoutParams());
                        mColumnHeaderParams.height = height;
                        mColumnHeaderParams.topMargin = height * i;
                        mBuckupHeaderParams.height = height;
                        mBuckupHeaderParams.topMargin = height * i;
                        mBackupHeaders.get(i).scrollToPosition(0);
                    }

                    //CELL HEADERS
                    FrameLayout.LayoutParams mCellHeaderParams = ((FrameLayout.LayoutParams) mCellRecyclerView.getLayoutParams());
                    mCellHeaderParams.topMargin = height * mHeaderCount;
                    scrollToColumnPosition(0);

                    //Corner
                    if(getAdapter().getCornerView()!=null) {
                        FrameLayout.LayoutParams mCornerHeaderParams = ((FrameLayout.LayoutParams) getAdapter().getCornerView().getLayoutParams());
                        mCornerHeaderParams.height = height * mHeaderCount;
                    }

                }
            });

        }

    }

    @Override
    public boolean hasFixedWidth() {
        return mHasFixedWidth;
    }

    public void setHasFixedWidth(boolean hasFixedWidth) {
        this.mHasFixedWidth = hasFixedWidth;

        // RecyclerView has also the same control to provide better performance.
        mColumnHeaderRecyclerView.setHasFixedSize(hasFixedWidth);
    }

    @Override
    public boolean isIgnoreSelectionColors() {
        return mIgnoreSelectionColors;
    }

    public void setIgnoreSelectionColors(boolean ignoreSelectionColor) {
        this.mIgnoreSelectionColors = ignoreSelectionColor;
    }

    @Override
    public boolean isShowHorizontalSeparators() {
        return mShowHorizontalSeparators;
    }

    @Override
    public boolean isSortable() {
        return mIsSortable;
    }

    public void setShowHorizontalSeparators(boolean showSeparators) {
        this.mShowHorizontalSeparators = showSeparators;
    }

    @Override
    public boolean isShowVerticalSeparators() {
        return mShowVerticalSeparators;
    }

    public void setShowVerticalSeparators(boolean showSeparators) {
        this.mShowVerticalSeparators = showSeparators;
    }

    @Override
    public CellRecyclerView getCellRecyclerView() {
        return mCellRecyclerView;
    }

    @Override
    public CellRecyclerView getColumnHeaderRecyclerView(int header) {
        return mColumnHeaderRecyclerViews.get(header);
    }

    @Override
    public List<CellRecyclerView> getColumnHeaderRecyclerView() {
        return mColumnHeaderRecyclerViews;
    }

    @Override
    public CellRecyclerView getRowHeaderRecyclerView() {
        return mRowHeaderRecyclerView;
    }

    @Override
    public ColumnHeaderLayoutManager getColumnHeaderLayoutManager(int index) {
        if (index >= mColumnHeaderLayoutManagers.size()) {
            mColumnHeaderLayoutManagers.add(new ColumnHeaderLayoutManager(getContext(), this));
        }
        return mColumnHeaderLayoutManagers.get(index);
    }

    @Override
    public CellLayoutManager getCellLayoutManager() {
        if (mCellLayoutManager == null) {
            mCellLayoutManager = new CellLayoutManager(getContext(), this);
        }
        return mCellLayoutManager;
    }

    @Override
    public LinearLayoutManager getRowHeaderLayoutManager() {
        if (mRowHeaderLayoutManager == null) {
            mRowHeaderLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        }
        return mRowHeaderLayoutManager;
    }

    @Override
    public HorizontalRecyclerViewListener getHorizontalRecyclerViewListener() {
        return mHorizontalRecyclerViewListener;
    }

    @Override
    public VerticalRecyclerViewListener getVerticalRecyclerViewListener() {
        return mVerticalRecyclerListener;
    }

    @Override
    public ITableViewListener getTableViewListener() {
        return mTableViewListener;
    }


    public void setTableViewListener(ITableViewListener tableViewListener) {
        this.mTableViewListener = tableViewListener;
    }

    @Override
    public void sortColumn(int columnPosition, SortState sortState) {
        mIsSortable = true;
        mColumnSortHandler.sort(columnPosition, sortState);
    }

    @Override
    public void sortRowHeader(SortState sortState) {
        mIsSortable = true;
        mColumnSortHandler.sortByRowHeader(sortState);
    }


    @Override
    public void remeasureColumnWidth(int column) {
        // Remove calculated width value to be ready for recalculation.
        mColumnHeaderLayoutManagers.get(0).removeCachedWidth(column);
        // Recalculate of the width values of the columns
        getCellLayoutManager().fitWidthSize(column, false);
    }

    @Override
    public AbstractTableAdapter getAdapter() {
        return mTableAdapter;
    }

    @Override
    public void filter(Filter filter) {
        mFilterHandler.filter(filter);
    }

    @Override
    public FilterHandler getFilterHandler() {
        return mFilterHandler;
    }

    @Override
    public SortState getSortingStatus(int column) {
        return mColumnSortHandler.getSortingStatus(column);
    }

    @Override
    public SortState getRowHeaderSortingStatus() {
        return mColumnSortHandler.getRowHeaderSortingStatus();
    }

    @Override
    public void scrollToColumnPosition(int column) {
        mScrollHandler.scrollToColumnPosition(column);
    }

    @Override
    public void scrollToColumnPosition(int column, int offset) {
        mScrollHandler.scrollToColumnPosition(column, offset);
    }

    @Override
    public void scrollToRowPosition(int row) {
        mScrollHandler.scrollToRowPosition(row);
    }

    @Override
    public void scrollToRowPosition(int row, int offset) {
        mScrollHandler.scrollToRowPosition(row, offset);
    }

    public ScrollHandler getScrollHandler() {
        return mScrollHandler;
    }

    @Override
    public int getHeaderCount() {
        return mHeaderCount;
    }

    @Override
    public List<CellRecyclerView> getBackupHeaders() {
        return this.mBackupHeaders;
    }

    @Override
    public void showRow(int row) {
        mVisibilityHandler.showRow(row);
    }

    @Override
    public void hideRow(int row) {
        mVisibilityHandler.hideRow(row);
    }

    @Override
    public void showAllHiddenRows() {
        mVisibilityHandler.showAllHiddenRows();
    }

    @Override
    public void clearHiddenRowList() {
        mVisibilityHandler.clearHideRowList();
    }

    @Override
    public void showColumn(int column) {
        mVisibilityHandler.showColumn(column);
    }

    @Override
    public void hideColumn(int column) {
        mVisibilityHandler.hideColumn(column);
    }

    @Override
    public boolean isColumnVisible(int column) {
        return mVisibilityHandler.isColumnVisible(column);
    }

    @Override
    public void showAllHiddenColumns() {
        mVisibilityHandler.showAllHiddenColumns();
    }

    @Override
    public void clearHiddenColumnList() {
        mVisibilityHandler.clearHideColumnList();
    }

    @Override
    public boolean isRowVisible(int row) {
        return mVisibilityHandler.isRowVisible(row);
    }

    /**
     * Returns the index of the selected row, -1 if no row is selected.
     */
    public int getSelectedRow() {
        return mSelectionHandler.getSelectedRowPosition();
    }

    public void setSelectedRow(int row) {
        // Find the row header view holder which is located on row position.
        AbstractViewHolder rowViewHolder = (AbstractViewHolder) getRowHeaderRecyclerView()
                .findViewHolderForAdapterPosition(row);


        mSelectionHandler.setSelectedRowPosition(rowViewHolder, row);
    }

    /**
     * Returns the index of the selected column, -1 if no column is selected.
     */
    public int getSelectedColumn() {
        return mSelectionHandler.getSelectedColumnPosition();
    }

    public void setSelectedColumn(int column) {
        // Find the column view holder which is located on column position .
        AbstractViewHolder columnViewHolder = (AbstractViewHolder) getColumnHeaderRecyclerView(0)
                .findViewHolderForAdapterPosition(column);

        mSelectionHandler.setSelectedColumnPosition(columnViewHolder, column);
    }

    public void setSelectedCell(int column, int row) {
        // Find the cell view holder which is located on x,y (column,row) position.
        AbstractViewHolder cellViewHolder = getCellLayoutManager().getCellViewHolder(column, row);

        mSelectionHandler.setSelectedCellPositions(cellViewHolder, column, row);
    }

    @Override
    public SelectionHandler getSelectionHandler() {
        return mSelectionHandler;
    }

    @Override
    public ColumnSortHandler getColumnSortHandler() {
        return mColumnSortHandler;
    }

    @Override
    public DividerItemDecoration getHorizontalItemDecoration() {
        if (mHorizontalItemDecoration == null) {
            mHorizontalItemDecoration = createItemDecoration(DividerItemDecoration.HORIZONTAL);
        }
        return mHorizontalItemDecoration;
    }

    @Override
    public DividerItemDecoration getVerticalItemDecoration() {
        if (mVerticalItemDecoration == null) {
            mVerticalItemDecoration = createItemDecoration(DividerItemDecoration.VERTICAL);
        }
        return mVerticalItemDecoration;
    }

    protected DividerItemDecoration createItemDecoration(int orientation) {
        Drawable divider = ContextCompat.getDrawable(getContext(), R.drawable.cell_line_divider);

        // That means; There is a custom separator color from user.
        if (mSeparatorColor != -1) {
            // Change its color
            divider.setColorFilter(mSeparatorColor, PorterDuff.Mode.SRC_ATOP);
        }

        DividerItemDecoration itemDecoration = new DividerItemDecoration(getContext(), orientation);
        itemDecoration.setDrawable(divider);
        return itemDecoration;
    }

    public void setHeaderCount(int headerCount) {
        this.mHeaderCount = headerCount;
        clear();
        initialize();
        mTableAdapter.setTableView(this);
        setAdapter(mTableAdapter);
    }

    private void clear() {
        mColumnHeaderRecyclerViews.clear();
        mColumnHeaderLayoutManagers.clear();
        mTableAdapter.clear();
        removeAllViews();
    }


    /**
     * This method helps to change default selected color programmatically.
     *
     * @param selectedColor It must be Color int.
     */
    public void setSelectedColor(@ColorInt int selectedColor) {
        this.mSelectedColor = selectedColor;
    }

    @Override
    public @ColorInt
    int getSelectedColor() {
        return mSelectedColor;
    }

    public void setSeparatorColor(@ColorInt int mSeparatorColor) {
        this.mSeparatorColor = mSeparatorColor;
    }

    @Override
    public @ColorInt
    int getSeparatorColor() {
        return mSeparatorColor;
    }

    /**
     * This method helps to change default unselected color programmatically.
     *
     * @param unSelectedColor It must be Color int.
     */
    public void setUnSelectedColor(@ColorInt int unSelectedColor) {
        this.mUnSelectedColor = unSelectedColor;
    }

    @Override
    public @ColorInt
    int getUnSelectedColor() {
        return mUnSelectedColor;
    }

    public void setHeadersColor(@ColorInt int mHeadersColor) {
        this.mHeadersColor = mHeadersColor;
    }

    @Override
    public @ColorInt
    int getHeadersColor() {
        return mHeadersColor;
    }


    public void setShadowColor(@ColorInt int shadowColor) {
        this.mShadowColor = shadowColor;
    }

    @Override
    public @ColorInt
    int getShadowColor() {
        return mShadowColor;
    }

    /**
     * get header width
     *
     * @return size in pixel
     */
    @Override
    public int getHeaderWidth() {
        return mHeaderWidth;
    }

    /**
     * set RowHeaderWidth
     *
     * @param rowHeaderWidth in pixel
     */
    @Override
    public void setRowHeaderWidth(int rowHeaderWidth) {
        this.mRowHeaderWidth = rowHeaderWidth;
        if (mRowHeaderRecyclerView != null) {
            // Update RowHeader layout width
            ViewGroup.LayoutParams layoutParams = mRowHeaderRecyclerView.getLayoutParams();
            layoutParams.width = rowHeaderWidth;
            mRowHeaderRecyclerView.setLayoutParams(layoutParams);
            mRowHeaderRecyclerView.requestLayout();
        }

        if (mColumnHeaderRecyclerView != null) {
            // Update ColumnHeader left margin
            LayoutParams layoutParams = (LayoutParams) mColumnHeaderRecyclerView.getLayoutParams();
            layoutParams.leftMargin = rowHeaderWidth;
            mColumnHeaderRecyclerView.setLayoutParams(layoutParams);
            mColumnHeaderRecyclerView.requestLayout();
        }

        if (mCellRecyclerView != null) {
            // Update Cells left margin
            LayoutParams layoutParams = (LayoutParams) mCellRecyclerView.getLayoutParams();
            layoutParams.leftMargin = rowHeaderWidth;
            mCellRecyclerView.setLayoutParams(layoutParams);
            mCellRecyclerView.requestLayout();
        }

        if (getAdapter() != null) {
            // update CornerView size
            getAdapter().setRowHeaderWidth(rowHeaderWidth);
        }
    }

    public void setColumnWidth(int columnPosition, int width) {
        mColumnWidthHandler.setColumnWidth(columnPosition, width);
    }


    @Override
    protected Parcelable onSaveInstanceState() {
        SavedState state = new SavedState(super.onSaveInstanceState());
        // Save all preferences of The TableView before the configuration changed.
        state.preferences = mPreferencesHandler.savePreferences();
        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {

        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());

        // Reload the preferences
        mPreferencesHandler.loadPreferences(savedState.preferences);
    }

    public View getCornerView() {
        return getAdapter().getCornerView();
    }
}
