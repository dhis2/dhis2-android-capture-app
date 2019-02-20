package org.dhis2.usescases.datasets.dataSetTable.dataSetSection;

import android.content.Context;

import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.dhis2.App;

import com.evrencoskun.tableview.TableView;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.collect.Table;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.tablefields.FieldViewModel;
import org.dhis2.data.forms.dataentry.tablefields.FieldViewModelFactoryImpl;
import org.dhis2.data.forms.dataentry.tablefields.RowAction;
import org.dhis2.databinding.FragmentDatasetSectionBinding;
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableActivity;
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableContract;
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableModel;
import org.dhis2.usescases.general.FragmentGlobalAbstract;
import org.dhis2.utils.Constants;
import org.hisp.dhis.android.core.category.CategoryOptionModel;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.dataelement.DataElementModel;
import org.hisp.dhis.android.core.dataset.SectionModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;

import io.reactivex.Flowable;

/**
 * QUADRAM. Created by ppajuelo on 02/10/2018.
 */

public class DataSetSectionFragment extends FragmentGlobalAbstract implements DataValueContract.View {

    FragmentDatasetSectionBinding binding;
    private DataSetTableContract.Presenter presenter;
    private DataSetTableActivity activity;
    private DataSetTableAdapter adapter;
    private String sectionUid;
    private boolean accessDataWrite;
    private boolean tableCreated = false;
    private String dataSetUid;
    @Inject
    DataValueContract.Presenter presenterFragment;

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
        adapter = new DataSetTableAdapter(getAbstracContext(), accessDataWrite);
        binding.tableView.setAdapter(adapter);
        binding.tableView.setEnabled(false);
        binding.setPresenter(presenterFragment);
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        sectionUid = getArguments().getString(Constants.DATA_SET_SECTION);
        accessDataWrite = getArguments().getBoolean(Constants.ACCESS_DATA);
        presenterFragment.init(this, presenter.getOrgUnitUid(), presenter.getPeriodTypeName(),
                presenter.getPeriodInitialDate(), presenter.getCatCombo());
        presenterFragment.getData(this, sectionUid);
        presenterFragment.initializeProcessor(this);
    }


    public void createTable() {

        binding.tableView.setHeaderCount(presenterFragment.getCatOptions().get(sectionUid).size());

        ArrayList<List<String>> cells = new ArrayList<>();
        List<List<FieldViewModel>> listFields = new ArrayList<>();
        List<List<String>> listCatOptions = presenterFragment.getCatOptionCombos(presenterFragment.getMapWithoutTransform().get(sectionUid), 0, new ArrayList<>(), null);
        int countColumn = 0;
        Integer[] totalColumn = new Integer[listCatOptions.size()];
        boolean isNumber = true;
        int row = 0, column = 0;

        for (SectionModel section : presenterFragment.getSections()) {
            if (section.name().equals(sectionUid)) {
                adapter.setShowColumnTotal(section.showColumnTotals());
                adapter.setShowRowTotal(section.showRowTotals());
            }
        }

        for (DataElementModel de : presenterFragment.getDataElements().get(sectionUid)) {
            ArrayList<String> values = new ArrayList<>();
            ArrayList<FieldViewModel> fields = new ArrayList<>();
            int totalRow = 0;

            for (List<String> catOpts : listCatOptions) {
                boolean exitsValue = false;
                boolean compulsory = false;
                FieldViewModelFactoryImpl fieldFactory = createField();

                boolean editable = !presenterFragment.getDataElementDisabled().containsKey(sectionUid) || !presenterFragment.getDataElementDisabled().get(sectionUid).containsKey(de.uid())
                        || !presenterFragment.getDataElementDisabled().get(sectionUid).get(de.uid()).containsAll(catOpts);

                if (presenterFragment.getCompulsoryDataElement().containsKey(de.uid()) && presenterFragment.getCompulsoryDataElement().get(de.uid()).containsAll(catOpts))
                    compulsory = true;

                if (de.valueType() != ValueType.NUMBER && de.valueType() != ValueType.INTEGER) {
                    isNumber = false;
                }

                for (DataSetTableModel dataValue : presenterFragment.getDataValues()) {

                    if (dataValue.listCategoryOption().containsAll(catOpts)
                            && Objects.equals(dataValue.dataElement(), de.uid())) {

                        if (isNumber) {
                            if (adapter.getShowColumnTotal())
                                totalColumn[countColumn] = totalColumn[countColumn] != null ?
                                        Integer.parseInt(dataValue.value()) + totalColumn[countColumn] : Integer.parseInt(dataValue.value());
                            if (adapter.getShowRowTotal())
                                totalRow = totalRow + Integer.parseInt(dataValue.value());
                        }

                        fields.add(fieldFactory.create(dataValue.id().toString(), "", de.valueType(),
                                compulsory, "", dataValue.value(), sectionUid, true,
                                editable, null, null, de.uid(), catOpts, "", row, column, dataValue.categoryOptionCombo()));
                        values.add(dataValue.value());
                        exitsValue = true;
                    }
                }

                if (!exitsValue) {
                    //If value type is null, it is due to is dataElement for Total row/column
                    fields.add(fieldFactory.create("", "", de.valueType(),
                            compulsory, "", "", sectionUid, true,
                            editable, null, null, de.uid()== null ? "": de.uid(), catOpts, "", row, column,""/*SET CATEGORYOPTIONCOMBO*/ ));

                    values.add("");
                }
                if(totalColumn[countColumn] == null)
                    totalColumn[countColumn] = 0;
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

        if (isNumber) {
            if (adapter.getShowColumnTotal())
                setTotalColumn(totalColumn, listFields, cells, presenterFragment.getDataElements(), row, column);
            if (adapter.getShowRowTotal())
                presenterFragment.getCatOptions().get(sectionUid).get(presenterFragment.getCatOptions().get(sectionUid).size() - 1).
                        add(CategoryOptionModel.builder().displayName(getString(R.string.total)).build());
        }

        adapter.swap(listFields);
        if(!tableCreated)
            adapter.setAllItems(
                    //presenter.getCatOptions().get(sectionUid).get(presenter.getCatOptions().get(sectionUid).size() - 1),
                    presenterFragment.getCatOptions().get(sectionUid),
                    presenterFragment.getDataElements().get(sectionUid),
                    cells);
        else
            adapter.setCellItems(cells);

        tableCreated = true;
    }

    private void setTotalColumn(Integer[] totalColumn, List<List<FieldViewModel>> listFields, ArrayList<List<String>> cells,
                                Map<String, List<DataElementModel>> dataElements, int row, int columnPos) {
        FieldViewModelFactoryImpl fieldFactory = createField();

        ArrayList<FieldViewModel> fields = new ArrayList<>();
        ArrayList<String> values = new ArrayList<>();
        boolean existTotal = false;
        for (DataElementModel data : dataElements.get(sectionUid))
            if (data.displayName().equals(getContext().getString(R.string.total)))
                existTotal = true;

        for (Integer column : totalColumn) {
            fields.add(fieldFactory.create("", "", ValueType.INTEGER,
                    false, "", column.toString(), sectionUid, true,
                    false, null, null, "",new ArrayList<>(),"", row, columnPos, ""));

            values.add(column.toString());
        }

        if (existTotal){
            listFields.remove(listFields.size()-1);
            cells.remove(listFields.size()-1);
        }

        listFields.add(fields);
        cells.add(values);

        if(!existTotal)
            dataElements.get(sectionUid).add(DataElementModel.builder().displayName(getString(R.string.total)).valueType(ValueType.INTEGER).build());
    }

    private void setTotalRow(int totalRow, ArrayList<FieldViewModel> fields, ArrayList<String> values, int row, int column){
        FieldViewModelFactoryImpl fieldFactory = createField();
        fields.add(fieldFactory.create("", "", ValueType.INTEGER,
                false, "", String.valueOf(totalRow), sectionUid, true,
                false, null, null, "",new ArrayList<>(),"", row, column, ""));
        values.add(String.valueOf(totalRow));

    }

    private FieldViewModelFactoryImpl createField(){
        return new FieldViewModelFactoryImpl(
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "");
    }

    @NonNull
    public Flowable<RowAction> rowActions() {
        return adapter.asFlowable();
    }

    public void updateData(RowAction rowAction) {
        adapter.updateValue(rowAction);
    }

    @Override
    public void showSnackBar() {
        Snackbar mySnackbar = Snackbar.make(binding.getRoot(), R.string.datavalue_saved , Snackbar.LENGTH_SHORT);
        mySnackbar.show();
    }
}
