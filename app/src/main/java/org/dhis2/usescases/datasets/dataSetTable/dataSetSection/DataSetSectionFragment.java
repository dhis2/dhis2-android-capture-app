package org.dhis2.usescases.datasets.dataSetTable.dataSetSection;

import android.content.Context;
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

import com.evrencoskun.tableview.TableView;
import com.evrencoskun.tableview.adapter.recyclerview.CellRecyclerView;
import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder;
import com.google.android.material.snackbar.Snackbar;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.data.forms.dataentry.tablefields.FieldViewModel;
import org.dhis2.data.forms.dataentry.tablefields.FieldViewModelFactoryImpl;
import org.dhis2.data.forms.dataentry.tablefields.RowAction;
import org.dhis2.data.tuples.Trio;
import org.dhis2.databinding.FragmentDatasetSectionBinding;
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableActivity;
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableContract;
import org.dhis2.usescases.general.FragmentGlobalAbstract;
import org.dhis2.utils.ColorUtils;
import org.dhis2.utils.Constants;
import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.category.Category;
import org.hisp.dhis.android.core.category.CategoryOption;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.dataelement.DataElement;
import org.hisp.dhis.android.core.dataset.DataSet;
import org.hisp.dhis.android.core.dataset.Section;
import org.hisp.dhis.android.core.period.Period;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Flowable;
import io.reactivex.processors.FlowableProcessor;
import timber.log.Timber;

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

    private Period periodModel;
    @Inject
    DataValueContract.Presenter presenterFragment;

    private ArrayList<Integer> heights = new ArrayList<>();
    private MutableLiveData<Integer> currentTablePosition = new MutableLiveData<>();
    private DataSet dataSet;
    private Section section;

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
        dataSetUid = getArguments().getString(Constants.DATA_SET_UID, dataSetUid);
        ((App) context.getApplicationContext()).userComponent().plus(new DataValueModule(dataSetUid)).inject(this);
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
    public void onDestroyView() {
        super.onDestroyView();
        presenterFragment.onDettach();
    }

    @Override
    public void setTableData(DataTableModel dataTableModel, List<List<FieldViewModel>> fields, String catCombo, List<List<String>> cells, List<DataElement> rows){
        binding.programProgress.setVisibility(View.GONE);
        activity.updateTabLayout(sectionName, fields.size());

        DataSetTableAdapter adapter = new DataSetTableAdapter(getAbstracContext(), presenterFragment.getProcessor(), presenterFragment.getProcessorOptionSet());
        adapters.add(adapter);
        adapter.setShowColumnTotal(section == null ? false : section.showColumnTotals());
        adapter.setShowRowTotal(section == null ? false : section.showRowTotals());
        TableView tableView = new TableView(getContext());
        tableView.setHasFixedWidth(true);


        List<List<CategoryOption>> columnHeaders = dataTableModel.header();
        /*for(Category category : dataTableModel.categories()){
            for(int i=0; i < dataTableModel.categories().size(); i++)
                columnHeaders.add(category.categoryOptions());
        }*/
        adapter.setCatCombo(catCombo);
        adapter.setTableView(tableView);
        adapter.initializeRows(true);
        adapter.setDataElementDecoration(dataSet.dataElementDecoration());

        Timber.tag("BREAKPOINT").d(catCombo);
        binding.tableLayout.addView(tableView);

        View view = new View(getContext());
        view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 15));
        view.setBackgroundColor(tableView.getSeparatorColor());
        binding.tableLayout.addView(view);

        tableView.setAdapter(adapter);
        tableView.setHeaderCount(columnHeaders.size());
        tableView.setHeadersColor(ColorUtils.getPrimaryColor(getContext(), ColorUtils.ColorType.PRIMARY_LIGHT));
        tableView.setShadowColor(ColorUtils.getPrimaryColor(getContext(), ColorUtils.ColorType.PRIMARY_DARK));

        adapter.swap(fields);

        adapter.setAllItems(
                columnHeaders,
                rows,
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
        Timber.tag("BREAKPOINT").d("showtables");
        currentTablePosition.setValue(0);
    }

    @Override
    public void createTable(DataTableModel dataTableModel) {
        /*binding.programProgress.setVisibility(View.GONE);
        DataSet dataSet = dataTableModel.dataSet();
        boolean isEditable = false;
        if (dataSet.access().data().write() &&
                !isExpired(dataTableModel.dataSet()) &&
                (presenterFragment.getDataInputPeriodModel().size() == 0 || (presenterFragment.checkHasInputPeriod() != null &&
                        DateUtils.getInstance().isInsideInputPeriod(presenterFragment.checkHasInputPeriod())))
                && !dataTableModel.approval()) {
            isEditable = true;
        }

        presenterFragment.setCurrentNumTables(new ArrayList<>(dataTableModel.catCombos().values()));
        activity.updateTabLayout(sectionName, dataTableModel.catCombos().size());

        int table = 0;
        for (String catCombo : dataTableModel.catCombos().keySet()) {
            DataSetTableAdapter adapter = new DataSetTableAdapter(getAbstracContext(), presenterFragment.getProcessor(), presenterFragment.getProcessorOptionSet());
            adapters.add(adapter);
            List<List<CategoryOption>> columnHeaderItems = dataTableModel.headers().get(catCombo);
            ArrayList<List<String>> cells = new ArrayList<>();
            List<List<FieldViewModel>> listFields = new ArrayList<>();
            List<DataElement> rows = new ArrayList<>();
            List<List<String>> listCatOptions = presenterFragment.getCatOptionCombos(dataTableModel.listCatOptionsCatComboOptions().get(catCombo), 0, new ArrayList<>(), null);
            boolean isNumber = false;
            int row = 0, column = 0;
            adapter.setShowColumnTotal(dataTableModel.sectionName() == null ? false : dataTableModel.sectionName().showColumnTotals());
            adapter.setShowRowTotal(dataTableModel.sectionName() == null ? false : dataTableModel.sectionName().showRowTotals());
            TableView tableView = new TableView(getContext());
            tableView.setHasFixedWidth(true);

            adapter.setCatCombo(catCombo);
            adapter.setTableView(tableView);
            adapter.initializeRows(isEditable);
            adapter.setDataElementDecoration(dataSet.dataElementDecoration());

            Timber.tag("BREAKPOINT").d(catCombo);
            binding.tableLayout.addView(tableView);

            if (!new ArrayList<>(dataTableModel.catCombos().keySet()).get(dataTableModel.catCombos().keySet().size() - 1).equals(catCombo)) {
                View view = new View(getContext());
                view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 15));
                view.setBackgroundColor(tableView.getSeparatorColor());

                binding.tableLayout.addView(view);
            }
            tableView.setAdapter(adapter);
            tableView.setHeaderCount(columnHeaderItems.size());
            tableView.setHeadersColor(ColorUtils.getPrimaryColor(getContext(), ColorUtils.ColorType.PRIMARY_LIGHT));
            tableView.setShadowColor(ColorUtils.getPrimaryColor(getContext(), ColorUtils.ColorType.PRIMARY_DARK));
            for (DataElement de : dataTableModel.rows()) {
                if (de.categoryCombo().uid().equals(catCombo))
                    rows.add(de);

                ArrayList<String> values = new ArrayList<>();
                ArrayList<FieldViewModel> fields = new ArrayList<>();
                int totalRow = 0;
                if (de.categoryCombo().uid().equals(catCombo)) {
                    for (List<String> catOpts : listCatOptions) {
                        boolean exitsValue = false;
                        boolean compulsory = false;
                        FieldViewModelFactoryImpl fieldFactory = createField();

                        boolean editable = true;
                        for (Pair<String, List<String>> listDisabled : dataTableModel.dataElementDisabled()) {
                            if (listDisabled.val0().equals(de.uid()) && listDisabled.val1().containsAll(catOpts)) {
                                editable = false;
                            }
                        }

                        for (CategoryOption catOption : dataTableModel.catOptions()) {
                            for (String option : catOpts) {
                                if (catOption.uid().equals(option) && !catOption.access().data().write())
                                    editable = false;
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
                                        compulsory, de.optionSetUid(), dataValue.value(), sectionName, true,
                                        editable, null, null, de.uid(), catOpts, "android", row, column, dataValue.categoryOptionCombo(), dataValue.catCombo()));
                                values.add(dataValue.value());
                                exitsValue = true;
                            }
                        }

                        if (!exitsValue) {
                            //If value type is null, it is due to is dataElement for Total row/column
                            fields.add(fieldFactory.create("", "", de.valueType(),
                                    compulsory, de.optionSetUid(), "", sectionName, true,
                                    editable, null, null, de.uid() == null ? "" : de.uid(), catOpts, "android", row, column, presenter.getCatOptComboFromOptionList(catOpts), catCombo));

                            values.add("");
                        }
                        column++;
                    }
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
                            columnHeaderItems.get(i).add(CategoryOption.builder().uid("").displayName(getString(R.string.total)).build());
                        else
                            columnHeaderItems.get(i).add(CategoryOption.builder().uid("").displayName("").build());
                    }

            }

            adapter.swap(listFields);

            adapter.setAllItems(
                    dataTableModel.headers().get(catCombo),
                    rows,
                    cells, adapter.getShowRowTotal());

            presenterFragment.addCells(table, listFields);
            table++;

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
        Timber.tag("BREAKPOINT").d("showtables");
        currentTablePosition.setValue(0);*/
    }

    @Override
    public void setDataSet(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    @Override
    public void setSectionName(Section section) {
        this.section = section;
    }

    private void loadHeader(int position) {
        TableView tableView = (TableView) ((LinearLayout) binding.scroll.getChildAt(0)).getChildAt(position * 2);
        if (tableView != null) {
            List<CellRecyclerView> rvs = tableView.getBackupHeaders();
            binding.headerContainer.removeAllViews();
            for (CellRecyclerView crv : rvs)
                binding.headerContainer.addView(crv);

            View cornerView = LayoutInflater.from(getContext()).inflate(R.layout.table_view_corner_layout, null);
            cornerView.setLayoutParams(new ViewGroup.LayoutParams(
                    tableView.getAdapter().getRowHeaderWidth(),
                    binding.headerContainer.getChildAt(0).getLayoutParams().height
            ));
            cornerView.findViewById(R.id.buttonScale).setOnClickListener(view -> {
                for (int i = 0; i < binding.tableLayout.getChildCount(); i++) {
                    if (binding.tableLayout.getChildAt(i) instanceof TableView) {
                        TableView table = (TableView) binding.tableLayout.getChildAt(i);
                        DataSetTableAdapter adapter = (DataSetTableAdapter) table.getAdapter();
                        adapter.scale();
                    }
                }
            });
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

    private void setTotalColumn(List<List<FieldViewModel>> listFields, ArrayList<List<String>> cells,
                                List<DataElement> dataElements, int row, int columnPos) {
        FieldViewModelFactoryImpl fieldFactory = createField();

        ArrayList<FieldViewModel> fields = new ArrayList<>();
        ArrayList<String> values = new ArrayList<>();
        boolean existTotal = false;
        for (DataElement data : dataElements)
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
                    false, "", String.valueOf(column), sectionName, true,
                    false, null, null, "", new ArrayList<>(), "", row, columnPos, "", ""));

            values.add(String.valueOf(column));
        }


        listFields.add(fields);
        cells.add(values);

        if (!existTotal)
            dataElements.add(DataElement.builder().uid("").displayName(getString(R.string.total)).valueType(ValueType.INTEGER).build());
    }

    private void setTotalRow(int totalRow, ArrayList<FieldViewModel> fields, ArrayList<String> values, int row, int column) {
        FieldViewModelFactoryImpl fieldFactory = createField();
        fields.add(fieldFactory.create("", "", ValueType.INTEGER,
                false, "", String.valueOf(totalRow), sectionName, true,
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
    public void setPeriod(Period periodModel) {
        this.periodModel = periodModel;
    }

    private Boolean isExpired(DataSet dataSet) {

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
        binding.scroll.scrollTo(0, binding.tableLayout.getChildAt(numTable * 2).getTop());
    }

    public List<String> currentNumTables() {
        return presenterFragment != null ? presenterFragment.getCurrentNumTables() : new ArrayList<>();
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
