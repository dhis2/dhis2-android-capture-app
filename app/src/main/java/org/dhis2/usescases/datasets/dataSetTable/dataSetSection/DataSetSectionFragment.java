package org.dhis2.usescases.datasets.dataSetTable.dataSetSection;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.evrencoskun.tableview.TableView;
import com.evrencoskun.tableview.adapter.recyclerview.CellRecyclerView;
import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder;
import com.google.android.material.snackbar.Snackbar;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.data.forms.dataentry.tablefields.FieldViewModel;
import org.dhis2.data.forms.dataentry.tablefields.FieldViewModelFactoryImpl;
import org.dhis2.data.forms.dataentry.tablefields.RowAction;
import org.dhis2.data.tuples.Pair;
import org.dhis2.data.tuples.Trio;
import org.dhis2.databinding.FragmentDatasetSectionBinding;
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableActivity;
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableContract;
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableModel;
import org.dhis2.usescases.general.FragmentGlobalAbstract;
import org.dhis2.utils.Constants;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.custom_views.OptionSetCellDialog;
import org.dhis2.utils.custom_views.OptionSetCellPopUp;
import org.hisp.dhis.android.core.category.CategoryOptionModel;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.dataelement.DataElementModel;
import org.hisp.dhis.android.core.dataset.DataSetModel;
import org.hisp.dhis.android.core.option.OptionModel;
import org.hisp.dhis.android.core.period.PeriodModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.MutableLiveData;
import io.reactivex.Flowable;
import io.reactivex.processors.FlowableProcessor;

import static com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder.SelectionState.UNSELECTED;

/**
 * QUADRAM. Created by ppajuelo on 02/10/2018.
 */

public class DataSetSectionFragment extends FragmentGlobalAbstract implements DataValueContract.View {

    FragmentDatasetSectionBinding binding;
    private DataSetTableContract.Presenter presenter;
    private DataSetTableActivity activity;
    private List<DataSetTableAdapter> adapters = new ArrayList<>();
    private String section;
    private String dataSetUid;

    private PeriodModel periodModel;
    @Inject
    DataValueContract.Presenter presenterFragment;

    private ArrayList<Integer> heights = new ArrayList<>();
    private MutableLiveData<Integer> currentTablePosition = new MutableLiveData<>();

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
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (DataSetTableActivity) context;
        presenter = ((DataSetTableActivity) context).getPresenter();
        dataSetUid = getArguments().getString(Constants.DATA_SET_UID, dataSetUid);
        ((App) context.getApplicationContext()).userComponent().plus(new DataValueModule(dataSetUid)).inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_dataset_section, container, false);
        currentTablePosition.observe(this, this::loadHeader);
        binding.setPresenter(presenterFragment);
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        section = getArguments().getString(Constants.DATA_SET_SECTION);
        presenterFragment.init(this, presenter.getOrgUnitUid(), presenter.getPeriodTypeName(),
                presenter.getPeriodFinalDate(), presenter.getCatCombo(), section, presenter.getPeriodId());
        presenterFragment.getData(this, section);
    }


    void createTable(DataTableModel dataTableModel) {
        DataSetModel dataSet = dataTableModel.dataSet();
        boolean isEditable = false;
        if (dataSet.accessDataWrite() &&
                !isExpired(dataTableModel.dataSet()) &&
                (presenterFragment.getDataInputPeriodModel().size() == 0 || (presenterFragment.checkHasInputPeriod() != null &&
                        DateUtils.getInstance().isInsideInputPeriod(presenterFragment.checkHasInputPeriod())))) {
            isEditable = true;
        }

        presenterFragment.setCurrentNumTables(new ArrayList<>(dataTableModel.catCombos().values()));
        activity.updateTabLayout(section, dataTableModel.catCombos().size());

        int table = 0;
        for (String catCombo : dataTableModel.catCombos().keySet()) {
            DataSetTableAdapter adapter = new DataSetTableAdapter(getAbstracContext(), presenterFragment.getProcessor(), presenterFragment.getProcessorOptionSet());
            adapters.add(adapter);
            List<List<CategoryOptionModel>> columnHeaderItems = dataTableModel.headers().get(catCombo);
            ArrayList<List<String>> cells = new ArrayList<>();
            List<List<FieldViewModel>> listFields = new ArrayList<>();
            List<DataElementModel> rows = new ArrayList<>();
            List<List<String>> listCatOptions = presenterFragment.getCatOptionCombos(dataTableModel.listCatOptionsCatComboOptions().get(catCombo), 0, new ArrayList<>(), null);
            int countColumn = 0;
            boolean isNumber = false;
            int row = 0, column = 0;
            adapter.setShowColumnTotal(dataTableModel.section() == null ? false : dataTableModel.section().showColumnTotals());
            adapter.setShowRowTotal(dataTableModel.section() == null ? false : dataTableModel.section().showRowTotals());
            TableView tableView = new TableView(getContext());
            tableView.setHasFixedWidth(true);

            adapter.setCatCombo(catCombo);
            adapter.setTableView(tableView);
            adapter.initializeRows(isEditable);

            binding.tableLayout.addView(tableView);

            if (!new ArrayList<>(dataTableModel.catCombos().keySet()).get(dataTableModel.catCombos().keySet().size() - 1).equals(catCombo)) {
                View view = new View(getContext());
                view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 15));
                view.setBackgroundColor(tableView.getSeparatorColor());

                binding.tableLayout.addView(view);
            }
            tableView.setAdapter(adapter);
            tableView.setHeaderCount(columnHeaderItems.size());
            for (DataElementModel de : dataTableModel.rows()) {
                if (de.categoryCombo().equals(catCombo))
                    rows.add(de);

                ArrayList<String> values = new ArrayList<>();
                ArrayList<FieldViewModel> fields = new ArrayList<>();
                int totalRow = 0;
                if (de.categoryCombo().equals(catCombo)) {
                    for (List<String> catOpts : listCatOptions) {
                        boolean exitsValue = false;
                        boolean compulsory = false;
                        FieldViewModelFactoryImpl fieldFactory = createField();

                        boolean editable = true;
                        for(Pair<String, List<String>> listDisabled: dataTableModel.dataElementDisabled()){
                            if(listDisabled.val0().equals(de.uid()) && listDisabled.val1().containsAll(catOpts)){
                                editable = false;
                            }
                        }

                        for(CategoryOptionModel catOption: dataTableModel.catOptions()){
                            for(String option: catOpts){
                                //Revert this when Jose tell us how to do disabled CategoryOptions
                                /*if(catOption.uid().equals(option) && !catOption.accessDataWrite())
                                    editable = false;*/
                            }
                        }

                        if (dataTableModel.compulsoryCells().containsKey(de.uid()) && dataTableModel.compulsoryCells().get(de.uid()).containsAll(catOpts))
                            compulsory = true;

                        if (de.valueType() == ValueType.NUMBER || de.valueType() == ValueType.INTEGER) {
                            isNumber = true;
                        }

                        for (DataSetTableModel dataValue : dataTableModel.dataValues()) {

                            if (dataValue.listCategoryOption().containsAll(catOpts)
                                    && Objects.equals(dataValue.dataElement(), de.uid()) && dataValue.catCombo().equals(catCombo)) {

                                if (isNumber) {
                                    if (adapter.getShowRowTotal())
                                        totalRow = totalRow + Integer.parseInt(dataValue.value());
                                }

                                fields.add(fieldFactory.create(dataValue.id().toString(), "", de.valueType(),
                                        compulsory, de.optionSet(), dataValue.value(), section, true,
                                        editable, null, null, de.uid(), catOpts, "", row, column, dataValue.categoryOptionCombo(), dataValue.catCombo()));
                                values.add(dataValue.value());
                                exitsValue = true;
                            }
                        }

                        if (!exitsValue) {
                            //If value type is null, it is due to is dataElement for Total row/column
                            fields.add(fieldFactory.create("", "", de.valueType(),
                                    compulsory, de.optionSet(), "", section, true,
                                    editable, null, null, de.uid() == null ? "" : de.uid(), catOpts, "", row, column, ""/*SET CATEGORYOPTIONCOMBO*/, catCombo));

                            values.add("");
                        }
                        countColumn++;
                        column++;
                    }
                    countColumn = 0;
                    if (isNumber && adapter.getShowRowTotal()) {
                        setTotalRow(totalRow, fields, values, row, column);
                    }
                    listFields.add(fields);
                    cells.add(values);
                    column = 0;
                    row++;
                }
            }

            if (isNumber) {
                if (adapter.getShowColumnTotal())
                    setTotalColumn(listFields, cells, rows, row, column);
                if (adapter.getShowRowTotal())
                    for (int i = 0; i < columnHeaderItems.size(); i++) {
                        if (i == columnHeaderItems.size() - 1)
                            columnHeaderItems.get(i).add(CategoryOptionModel.builder().displayName(getString(R.string.total)).build());
                        else
                            columnHeaderItems.get(i).add(CategoryOptionModel.builder().displayName("").build());
                    }

            }

            adapter.swap(listFields);

            adapter.setAllItems(
                    dataTableModel.headers().get(catCombo),
                    rows,
                    cells, adapter.getShowRowTotal());

            presenterFragment.addCells(table, listFields);
            table++;

            if (!catCombo.equals(new ArrayList<>(dataTableModel.catCombos().keySet()).get(dataTableModel.catCombos().keySet().size() - 1)))
                adapter = new DataSetTableAdapter(getAbstracContext(), presenterFragment.getProcessor(), presenterFragment.getProcessorOptionSet());

        }

        presenterFragment.initializeProcessor(this);

        binding.scroll.scrollTo(0, 1000);

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

    private void loadHeader(int position) {
        TableView tableView = (TableView) ((LinearLayout) binding.scroll.getChildAt(0)).getChildAt(position*2);
        if (tableView != null) {
            List<CellRecyclerView> rvs = tableView.getBackupHeaders();
            binding.headerContainer.removeAllViews();
            for (CellRecyclerView crv : rvs)
                binding.headerContainer.addView(crv);
        }
    }

    private boolean checkTableHeights() {
        if (heights.isEmpty()) {
            heights = new ArrayList<>();

            for (int i = 0; i < ((LinearLayout) binding.scroll.getChildAt(0)).getChildCount(); i++) {
                View view = ((LinearLayout) binding.scroll.getChildAt(0)).getChildAt(i);
                if (view instanceof TableView) {
                    if (i == ((LinearLayout) binding.scroll.getChildAt(0)).getChildCount() - 1)
                        heights.add(i!=0?heights.get(heights.size() - 1) + view.getHeight():view.getHeight());
                    else {
                        View separator = ((LinearLayout) binding.scroll.getChildAt(0)).getChildAt(i + 1);
                        heights.add(i/2 != 0 ? heights.get(i/2 - 1) + view.getHeight() + separator.getHeight() : view.getHeight() + separator.getHeight());
                    }
                }
            }

        }
        return !heights.isEmpty();
    }

    private void setTotalColumn(List<List<FieldViewModel>> listFields, ArrayList<List<String>> cells,
                                List<DataElementModel> dataElements, int row, int columnPos) {
        FieldViewModelFactoryImpl fieldFactory = createField();

        ArrayList<FieldViewModel> fields = new ArrayList<>();
        ArrayList<String> values = new ArrayList<>();
        boolean existTotal = false;
        for (DataElementModel data : dataElements)
            if (data.displayName().equals(getContext().getString(R.string.total)))
                existTotal = true;

        if (existTotal) {
            listFields.remove(listFields.size() - 1);
            cells.remove(listFields.size() - 1);
        }


        int[] totals = new int[cells.get(0).size()];
        for (List<String> dataValues : cells) {
            for (int i = 0; i < dataValues.size(); i++) {
                if (!dataValues.get(i).isEmpty())
                    totals[i] += Integer.parseInt(dataValues.get(i));
            }
        }

        for (int column : totals) {
            fields.add(fieldFactory.create("", "", ValueType.INTEGER,
                    false, "", String.valueOf(column), section, true,
                    false, null, null, "", new ArrayList<>(), "", row, columnPos, "", ""));

            values.add(String.valueOf(column));
        }


        listFields.add(fields);
        cells.add(values);

        if (!existTotal)
            dataElements.add(DataElementModel.builder().displayName(getString(R.string.total)).valueType(ValueType.INTEGER).build());
    }

    private void setTotalRow(int totalRow, ArrayList<FieldViewModel> fields, ArrayList<String> values, int row, int column) {
        FieldViewModelFactoryImpl fieldFactory = createField();
        fields.add(fieldFactory.create("", "", ValueType.INTEGER,
                false, "", String.valueOf(totalRow), section, true,
                false, null, null, "", new ArrayList<>(), "", row, column, "", ""));
        values.add(String.valueOf(totalRow));

    }

    private FieldViewModelFactoryImpl createField() {
        return new FieldViewModelFactoryImpl(
                "",
                "");
    }

    @NonNull
    public Flowable<RowAction> rowActions() {
        return adapters.get(0).asFlowable();
    }

    public FlowableProcessor<Trio<String, String, Integer>> optionSetActions() {
        return adapters.get(0).asFlowableOptionSet();
    }

    public void updateData(RowAction rowAction, String catCombo) {
        for(DataSetTableAdapter adapter : adapters)
            if(adapter.getCatCombo().equals(catCombo))
                adapter.updateValue(rowAction);
    }

    @Override
    public void showSnackBar() {
        Snackbar mySnackbar = Snackbar.make(binding.getRoot(), R.string.datavalue_saved, Snackbar.LENGTH_SHORT);
        mySnackbar.show();
    }

    @Override
    public void onComplete() {
        binding.actionButton.setText(getString(R.string.re_open));
    }

    @Override
    public void setPeriod(PeriodModel periodModel) {
        this.periodModel = periodModel;
    }

    private Boolean isExpired(DataSetModel dataSet) {

        if (0 == dataSet.expiryDays()) {
            return false;
        }
        /*if (presenter.getPeriodFinalDate() != null) {
            try {
                return DateUtils.getInstance().isDataSetExpired(dataSet.expiryDays(), DateUtils.databaseDateFormat().parse(presenter.getPeriodFinalDate()));
            } catch (ParseException e) {
                Timber.e(e);
            }
        }*/

        return DateUtils.getInstance().isDataSetExpired(dataSet.expiryDays(), periodModel.endDate());
    }

    @Override
    public void goToTable(int numTable) {
        binding.scroll.scrollTo(0, binding.tableLayout.getChildAt(numTable*2).getTop());
    }

    public List<String> currentNumTables() {
        return presenterFragment != null ? presenterFragment.getCurrentNumTables() : new ArrayList<>();
    }

    @Override
    public void showAlertDialog(String title, String message) {
        super.showInfoDialog(title, message);
    }

    @Override
    public void setListOptions(List<OptionModel> options) {
        if (OptionSetCellDialog.isCreated())
            OptionSetCellDialog.newInstance().setOptions(options);
        else if (OptionSetCellPopUp.isCreated())
            OptionSetCellPopUp.getInstance().setOptions(options);
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

        if(columnHeader != null) {
            columnHeader.setSelected(UNSELECTED);
            columnHeader.setBackgroundColor(mandatory ?
                    ContextCompat.getColor(getContext(), R.color.table_view_default_mandatory_background_color) :
                    ContextCompat.getColor(getContext(), R.color.table_view_default_all_required_background_color));
        }
    }

    @Override
    public void update(boolean modified) {
        if(modified) {
            activity.update();
        }
    }
}
