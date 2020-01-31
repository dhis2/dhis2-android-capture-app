package org.dhis2.usescases.datasets.dataSetTable.dataSetSection;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableField;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.evrencoskun.tableview.TableView;
import com.evrencoskun.tableview.adapter.recyclerview.CellRecyclerView;
import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder;
import com.google.android.material.snackbar.Snackbar;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.data.forms.dataentry.tablefields.RowAction;
import org.dhis2.databinding.FragmentDatasetSectionBinding;
import org.dhis2.databinding.TableViewCornerLayoutBinding;
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableActivity;
import org.dhis2.usescases.general.FragmentGlobalAbstract;
import org.dhis2.utils.Constants;
import org.hisp.dhis.android.core.dataset.DataSet;
import org.hisp.dhis.android.core.dataset.Section;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Flowable;

import static com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder.SelectionState.UNSELECTED;
import static org.dhis2.utils.analytics.AnalyticsConstants.LEVEL_ZOOM;
import static org.dhis2.utils.analytics.AnalyticsConstants.ZOOM_TABLE;

/**
 * QUADRAM. Created by ppajuelo on 02/10/2018.
 */

public class DataSetSectionFragment extends FragmentGlobalAbstract implements DataValueContract.View {

    private FragmentDatasetSectionBinding binding;
    private DataSetTableActivity activity;
    private String sectionName;
    private String dataSetUid;
    private TableRecyclerAdapter tableAdapter;
    private final ObservableField<DataSetTableAdapter.TableScale> currentTableScale =
            new ObservableField<>(DataSetTableAdapter.TableScale.DEFAULT);

    @Inject
    DataValuePresenter presenterFragment;

    private MutableLiveData<Integer> currentTablePosition = new MutableLiveData<>(0);
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
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        activity = (DataSetTableActivity) context;
        dataSetUid = requireArguments().getString(Constants.DATA_SET_UID, dataSetUid);
        ((App) context.getApplicationContext()).userComponent().plus(new DataValueModule(dataSetUid, this)).inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_dataset_section, container, false);
        currentTablePosition.observe(this, this::loadHeader);
        binding.setPresenter(presenterFragment);
        sectionName = requireArguments().getString(Constants.DATA_SET_SECTION);
        presenterFragment.init(
                this,
                activity.getPresenter().getOrgUnitUid(),
                activity.getPresenter().getPeriodTypeName(),
                activity.getPresenter().getPeriodFinalDate(),
                activity.getPresenter().getCatCombo(),
                sectionName,
                activity.getPresenter().getPeriodId());
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        presenterFragment.onDetach();
    }

    @Override
    public void setDataAccess(boolean accessDataWrite) {
        binding.actionLayout.setVisibility(accessDataWrite ? View.VISIBLE : View.GONE);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void setTableData(DataSetTable dataSetTable) {
        if (tableAdapter == null) {
            tableAdapter = new TableRecyclerAdapter(getAbstracContext(), dataSet, section, new ArrayList<>(), new ArrayList<>());
            binding.tableRecycler.setAdapter(tableAdapter);
        }

        DataSetTableAdapter adapter = new DataSetTableAdapter(
                getAbstracContext(),
                presenterFragment.getProcessor(),
                presenterFragment.getProcessorOptionSet(),
                currentTableScale
        );

        tableAdapter.addTable(dataSetTable, adapter);

        if (tableAdapter.getAdapterList().size() == 1) {
            presenterFragment.initializeProcessor(this);

            binding.tableRecycler.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                int position = ((LinearLayoutManager) binding.tableRecycler.getLayoutManager()).findFirstVisibleItemPosition();
                if (position != -1 && currentTablePosition.getValue() != position)
                    currentTablePosition.setValue(position);
            });
        }
    }

    @Override
    public void finishTableLoading() {
        tableAdapter.finishLoading();
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
        if(tableAdapter==null || tableAdapter.getTables().isEmpty()){
            return;
        }
        TableView tableView = tableAdapter.getTables().get(position);

        if (tableView != null) {
            List<CellRecyclerView> rvs = tableView.getBackupHeaders();
            binding.headerContainer.removeAllViews();
            for (CellRecyclerView crv : rvs)
                binding.headerContainer.addView(crv);

            TableViewCornerLayoutBinding cornerViewBinding =
                    TableViewCornerLayoutBinding.inflate(
                            LayoutInflater.from(getContext()),
                            null,
                            false);
            cornerViewBinding.getRoot().setLayoutParams(new ViewGroup.LayoutParams(
                    tableView.getAdapter().getRowHeaderWidth(),
                    binding.headerContainer.getChildAt(0).getLayoutParams().height
            ));
            setUpZoomViewImage(cornerViewBinding);
            cornerViewBinding.buttonScale.setOnClickListener(view -> {

                DataSetTableAdapter.TableScale nextTableScale = currentTableScale.get().getNext();
                currentTableScale.set(nextTableScale);
                setUpZoomViewImage(cornerViewBinding);

                for (int i = 0; i < tableAdapter.getTables().size(); i++) {
                    if (tableAdapter.getTables().get(i) instanceof TableView) {
                        TableView table = tableAdapter.getTables().get(i);
                        DataSetTableAdapter adapter = (DataSetTableAdapter) table.getAdapter();
                        adapter.scale();
                    }
                }
                analyticsHelper().setEvent(LEVEL_ZOOM, currentTableScale.get().name(), ZOOM_TABLE);
            });
            binding.headerContainer.addView(cornerViewBinding.getRoot());
        }
    }

    private void setUpZoomViewImage(TableViewCornerLayoutBinding cornerViewBinding) {
        int zoomImageResource = -1;
        switch (currentTableScale.get()) {
            case LARGE:
                zoomImageResource = R.drawable.ic_zoomx3;
                break;
            case SMALL:
                zoomImageResource = R.drawable.ic_zoomx1;
                break;
            case DEFAULT:
                zoomImageResource = R.drawable.ic_zoomx2;
                break;
        }
        cornerViewBinding.buttonScale.setImageDrawable(AppCompatResources.getDrawable(
                cornerViewBinding.getRoot().getContext(),
                zoomImageResource));
    }

    @NonNull
    public Flowable<RowAction> rowActions() {
        return tableAdapter.getAdapterList().get(0).asFlowable();
    }

    public void updateData(RowAction rowAction, String catCombo) {
        for (DataSetTableAdapter adapter : tableAdapter.getAdapterList())
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
        binding.tableRecycler.smoothScrollToPosition(numTable);
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
        AbstractViewHolder columnHeader = (AbstractViewHolder) tableAdapter.getAdapterList().get(table).getTableView().getRowHeaderRecyclerView()
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
