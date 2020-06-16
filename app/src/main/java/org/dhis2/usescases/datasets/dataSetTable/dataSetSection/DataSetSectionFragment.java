package org.dhis2.usescases.datasets.dataSetTable.dataSetSection;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;

import com.evrencoskun.tableview.TableView;
import com.evrencoskun.tableview.adapter.recyclerview.CellRecyclerView;
import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder;
import com.google.android.material.snackbar.Snackbar;

import org.dhis2.App;
import org.dhis2.Bindings.MeasureExtensionsKt;
import org.dhis2.R;
import org.dhis2.data.forms.dataentry.tablefields.FieldViewModel;
import org.dhis2.data.forms.dataentry.tablefields.RowAction;
import org.dhis2.data.tuples.Trio;
import org.dhis2.databinding.FragmentDatasetSectionBinding;
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableActivity;
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableContract;
import org.dhis2.usescases.general.FragmentGlobalAbstract;
import org.dhis2.utils.ColorUtils;
import org.dhis2.utils.Constants;
import org.dhis2.utils.OrientationUtilsKt;
import org.hisp.dhis.android.core.category.CategoryOption;
import org.hisp.dhis.android.core.dataset.DataSet;
import org.hisp.dhis.android.core.dataset.Section;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Flowable;
import io.reactivex.processors.FlowableProcessor;
import kotlin.Pair;
import kotlin.Triple;

import static com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder.SelectionState.UNSELECTED;

/**
 * QUADRAM. Created by ppajuelo on 02/10/2018.
 */

public class DataSetSectionFragment extends FragmentGlobalAbstract implements DataValueContract.View {

    FragmentDatasetSectionBinding binding;
    private DataSetTableContract.Presenter presenter;
    private DataSetTableActivity activity;
    private List<DataSetTableAdapter> adapters = new ArrayList<>();
    private String sectionName;
    private String dataSetUid;

    @Inject
    DataValuePresenter presenterFragment;

    private ArrayList<Integer> heights = new ArrayList<>();
    private MutableLiveData<Integer> currentTablePosition = new MutableLiveData<>();
    private DataSet dataSet;
    private Section section;
    private int tablesCount;

    @NonNull
    public static DataSetSectionFragment create(@NonNull String sectionUid, boolean accessDataWrite, String dataSetUid) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.DATA_SET_SECTION, sectionUid);
        bundle.putBoolean(Constants.ACCESS_DATA, accessDataWrite);
        DataSetSectionFragment dataSetSectionFragment = new DataSetSectionFragment();
        dataSetSectionFragment.setArguments(bundle);
        bundle.putString(Constants.DATA_SET_UID, dataSetUid);
        return dataSetSectionFragment;
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        getAbstractActivity().hideKeyboard();
        requireView().clearFocus();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (DataSetTableActivity) context;
        dataSetUid = getArguments().getString(Constants.DATA_SET_UID, dataSetUid);
        ((App) context.getApplicationContext()).userComponent().plus(new DataValueModule(dataSetUid, this)).inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_dataset_section, container, false);
        currentTablePosition.observe(this, this::loadHeader);
        binding.setPresenter(presenterFragment);
        presenter = activity.getPresenter();
        sectionName = getArguments().getString(Constants.DATA_SET_SECTION);
        presenterFragment.init(this, presenter.getOrgUnitUid(), presenter.getPeriodTypeName(),
                presenter.getPeriodFinalDate(), presenter.getCatCombo(), sectionName, presenter.getPeriodId());
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        presenterFragment.checkComplete();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        presenterFragment.onDettach();
    }

    @Override
    public void setDataAccess(boolean accessDataWrite) {
        binding.actionLayout.setVisibility(accessDataWrite ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setTableData(DataTableModel dataTableModel, List<List<FieldViewModel>> fields, List<List<String>> cells, Boolean accessDataWrite) {
        binding.programProgress.setVisibility(View.GONE);

        DataSetTableAdapter adapter = new DataSetTableAdapter(getAbstracContext(), presenterFragment.getProcessor(), presenterFragment.getProcessorOptionSet());
        adapters.add(adapter);
        adapter.setShowColumnTotal(section.uid().isEmpty() ? false : section.showColumnTotals());
        adapter.setShowRowTotal(section.uid().isEmpty() ? false : section.showRowTotals());
        TableView tableView = new TableView(getContext());
        tableView.setHasFixedWidth(true);


        List<List<CategoryOption>> columnHeaders = dataTableModel.header();
        adapter.setCatCombo(dataTableModel.catCombo().uid());
        adapter.setTableView(tableView);
        adapter.initializeRows(accessDataWrite);
        adapter.setDataElementDecoration(dataSet.dataElementDecoration());

        binding.tableLayout.addView(tableView);

        View view = new View(getContext());
        view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 15));
        view.setBackgroundColor(tableView.getSeparatorColor());
        binding.tableLayout.addView(view);

        tableView.setAdapter(adapter);
        tableView.setHeaderCount(columnHeaders.size());
        tableView.setShadowColor(ColorUtils.getPrimaryColor(getContext(), ColorUtils.ColorType.PRIMARY));

        adapter.swap(fields);

        Pair<Integer, Integer> savedMeasure = presenterFragment.getCurrentSectionMeasure();
        if (savedMeasure.getFirst() != 0) {
            adapter.setMaxLabel(MeasureExtensionsKt.maxLengthLabel(dataTableModel.rows()));
            tableView.setRowHeaderWidth(savedMeasure.getFirst());
            adapter.setColumnHeaderHeight(savedMeasure.getSecond());

        } else {

            int widthFactor;
            int maxColumns = dataTableModel.header().get(dataTableModel.header().size() - 1).size();
            if (OrientationUtilsKt.isPortrait()) {
                widthFactor = 2;
            } else {
                if (maxColumns > 1) {
                    widthFactor = 3;
                } else {
                    widthFactor = 2;
                }
            }

            Triple<String, Integer, Integer> labelMeasure = MeasureExtensionsKt.measureText(
                    dataTableModel.rows(),
                    getContext(),
                    widthFactor
            );
            adapter.setMaxLabel(labelMeasure.getFirst());
            tableView.setRowHeaderWidth(labelMeasure.getSecond());
            if (labelMeasure.getThird() != 0) {
                adapter.setColumnHeaderHeight(
                        labelMeasure.getThird() + getContext().getResources().getDimensionPixelSize(R.dimen.padding_5));
            }
        }

        adapter.setAllItems(
                columnHeaders,
                dataTableModel.rows(),
                cells,
                adapter.getShowRowTotal());


        presenterFragment.initializeProcessor(this);


        binding.scroll.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            int position = -1;
            if (checkTableHeights())
                for (int i = 0; i < heights.size(); i++) {
                    if (scrollY < heights.get(i))
                        position = position == -1 ? i : position;
                }

            if (position != -1 && currentTablePosition.getValue() != position)
                currentTablePosition.setValue(position);
        });
        currentTablePosition.setValue(0);
    }

    @Override
    public void setDataSet(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    @Override
    public void setSection(Section section) {
        this.section = section;
    }

    @Override
    public void updateTabLayout(int count) {
        this.tablesCount = count;
        activity.updateTabLayout(sectionName, count);
    }

    private void loadHeader(int position) {
        TableView tableView = (TableView) ((LinearLayout) binding.scroll.getChildAt(0)).getChildAt(position * 2);
        if (tableView != null) {
            List<CellRecyclerView> rvs = tableView.getBackupHeaders();
            binding.headerContainer.removeAllViews();
            for (CellRecyclerView crv : rvs)
                binding.headerContainer.addView(crv);

            View cornerView = LayoutInflater.from(getContext()).inflate(R.layout.table_view_corner_layout, null);
            LinearLayout.LayoutParams cornerParams = new LinearLayout.LayoutParams(
                    tableView.getAdapter().getRowHeaderWidth(),
                    binding.headerContainer.getChildAt(0).getLayoutParams().height
            );
            cornerParams.topMargin = binding.headerContainer.getChildAt(0).getLayoutParams().height *
                    (binding.headerContainer.getChildCount() - 1);
            cornerView.setLayoutParams(cornerParams);
            if (binding.headerContainer.getChildCount() > 1)
                cornerView.setTop((binding.headerContainer.getChildCount() - 2) * binding.headerContainer.getChildAt(0).getLayoutParams().height);

            cornerView.findViewById(R.id.buttonRowScaleAdd).setOnClickListener(view -> {
                        for (int i = 0; i < binding.tableLayout.getChildCount(); i++) {
                            if (binding.tableLayout.getChildAt(i) instanceof TableView) {
                                TableView table = (TableView) binding.tableLayout.getChildAt(i);
                                DataSetTableAdapter adapter = (DataSetTableAdapter) table.getAdapter();
                                adapter.scaleRowWidth(true);
                                ViewGroup.LayoutParams params = cornerView.getLayoutParams();
                                params.width = adapter.getRowHeaderWidth();
                                cornerView.setLayoutParams(params);
                                if (i == 0) {
                                    presenterFragment.saveCurrentSectionMeasures(
                                            adapter.getRowHeaderWidth(),
                                            adapter.getColumnHeaderHeight()
                                    );
                                    int scrollPos = table.getScrollHandler().getColumnPosition();
                                    table.scrollToColumnPosition(scrollPos);
                                    for (RecyclerView rv : rvs) {
                                        rv.getLayoutManager().scrollToPosition(scrollPos);
                                    }
                                }
                            }
                        }
                    }
            );
            cornerView.findViewById(R.id.buttonRowScaleMinus).setOnClickListener(view -> {
                        for (int i = 0; i < binding.tableLayout.getChildCount(); i++) {
                            if (binding.tableLayout.getChildAt(i) instanceof TableView) {
                                TableView table = (TableView) binding.tableLayout.getChildAt(i);
                                DataSetTableAdapter adapter = (DataSetTableAdapter) table.getAdapter();
                                adapter.scaleRowWidth(false);
                                ViewGroup.LayoutParams params = cornerView.getLayoutParams();
                                params.width = adapter.getRowHeaderWidth();
                                cornerView.setLayoutParams(params);
                                if (i == 0) {
                                    presenterFragment.saveCurrentSectionMeasures(
                                            adapter.getRowHeaderWidth(),
                                            adapter.getColumnHeaderHeight()
                                    );
                                    int scrollPos = table.getScrollHandler().getColumnPosition();
                                    table.scrollToColumnPosition(scrollPos);
                                    for (RecyclerView rv : rvs) {
                                        rv.getLayoutManager().scrollToPosition(scrollPos);
                                    }
                                }
                            }
                        }
                    }
            );
            binding.headerContainer.addView(cornerView);
        }
    }

    private boolean checkTableHeights() {
        if (heights.isEmpty()) {
            heights = new ArrayList<>();

            for (int i = 0; i < ((LinearLayout) binding.scroll.getChildAt(0)).getChildCount(); i++) {
                View view = ((LinearLayout) binding.scroll.getChildAt(0)).getChildAt(i);
                if (view instanceof TableView) {
                    if (i == ((LinearLayout) binding.scroll.getChildAt(0)).getChildCount() - 1)
                        heights.add(i != 0 ? heights.get(heights.size() - 1) + view.getHeight() : view.getHeight());
                    else {
                        View separator = ((LinearLayout) binding.scroll.getChildAt(0)).getChildAt(i + 1);
                        heights.add(i / 2 != 0 ? heights.get(i / 2 - 1) + view.getHeight() + separator.getHeight() : view.getHeight() + separator.getHeight());
                    }
                }
            }

        }
        return !heights.isEmpty();
    }

    @NonNull
    public Flowable<RowAction> rowActions() {
        return adapters.get(0).asFlowable();
    }

    public FlowableProcessor<Trio<String, String, Integer>> optionSetActions() {
        return adapters.get(0).asFlowableOptionSet();
    }

    public void updateData(RowAction rowAction, String catCombo) {
        for (DataSetTableAdapter adapter : adapters)
            if (adapter.getCatCombo().equals(catCombo))
                adapter.updateValue(rowAction);
    }

    @Override
    public void showSnackBar() {
        Snackbar mySnackbar = Snackbar.make(binding.getRoot(), R.string.datavalue_saved, Snackbar.LENGTH_SHORT);
        mySnackbar.show();
    }

    @Override
    public void goToTable(int numTable) {
        binding.scroll.scrollTo(0, binding.tableLayout.getChildAt(numTable * 2).getTop());
    }

    public int currentNumTables() {
        return tablesCount;
    }

    @Override
    public void showAlertDialog(String title, String message) {
        super.showInfoDialog(title, message);
    }

    @Override
    public boolean isOpenOrReopen() {
        return binding.actionButton.getText().equals(getString(R.string.complete));
    }

    @Override
    public void setCompleteReopenText(Boolean isCompleted) {
        if (!isCompleted)
            binding.actionButton.setText(activity.getString(R.string.complete));
        else
            binding.actionButton.setText(activity.getString(R.string.re_open));
    }

    @Override
    public void highligthHeaderRow(int table, int row, boolean mandatory) {
        AbstractViewHolder columnHeader = (AbstractViewHolder) adapters.get(table).getTableView().getRowHeaderRecyclerView()
                .findViewHolderForAdapterPosition(row);

        if (columnHeader != null) {
            columnHeader.setSelected(UNSELECTED);
            columnHeader.setBackgroundColor(mandatory ?
                    ContextCompat.getColor(getContext(), R.color.table_view_default_mandatory_background_color) :
                    ContextCompat.getColor(getContext(), R.color.table_view_default_all_required_background_color));
        }
    }

    @Override
    public void update(boolean modified) {
        if (modified) {
            activity.update();
        }
    }
}
