package org.dhis2.usescases.datasets.dataSetTable.dataSetSection;

import android.content.Context;
import android.database.DataSetObserver;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.tablefields.FieldViewModel;
import org.dhis2.data.forms.dataentry.tablefields.FieldViewModelFactory;
import org.dhis2.data.forms.dataentry.tablefields.FieldViewModelFactoryImpl;
import org.dhis2.data.forms.dataentry.tablefields.RowAction;
import org.dhis2.data.tuples.Pair;
import org.dhis2.databinding.FragmentDatasetSectionBinding;
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableActivity;
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableContract;
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableModel;
import org.dhis2.usescases.general.FragmentGlobalAbstract;
import org.dhis2.utils.Constants;
import org.hisp.dhis.android.core.category.CategoryModel;
import org.hisp.dhis.android.core.category.CategoryOptionModel;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.dataelement.DataElementModel;
import org.hisp.dhis.android.core.dataset.SectionModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.reactivex.Flowable;

/**
 * QUADRAM. Created by ppajuelo on 02/10/2018.
 */

public class DataSetSectionFragment extends FragmentGlobalAbstract {

    FragmentDatasetSectionBinding binding;
    private DataSetTableContract.Presenter presenter;
    private DataSetTableActivity activity;
    private DataSetTableAdapter adapter;
    private String sectionUid;
    private boolean accessDataWrite;


    @NonNull
    public static DataSetSectionFragment create(@NonNull String sectionUid, boolean accessDataWrite) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.DATA_SET_SECTION, sectionUid);
        bundle.putBoolean(Constants.ACCESS_DATA, accessDataWrite);
        DataSetSectionFragment dataSetSectionFragment = new DataSetSectionFragment();
        dataSetSectionFragment.setArguments(bundle);

        return dataSetSectionFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (DataSetTableActivity) context;
        presenter = ((DataSetTableActivity) context).getPresenter();

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_dataset_section, container, false);
        adapter = new DataSetTableAdapter(getAbstracContext(), accessDataWrite);
        binding.tableView.setAdapter(adapter);
        binding.tableView.setEnabled(false);
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        sectionUid = getArguments().getString(Constants.DATA_SET_SECTION);
        accessDataWrite = getArguments().getBoolean(Constants.ACCESS_DATA);
        presenter.getData(this, sectionUid);
        presenter.initializeProcessor(this);
    }


    public void createTable(RowAction rowAction) {

        ArrayList<List<String>> cells = new ArrayList<>();
        List<List<FieldViewModel>> listFields = new ArrayList<>();
        List<List<String>> listCatOptions = presenter.getCatOptionCombos(presenter.getMapWithoutTransform().get(sectionUid), 0, new ArrayList<>(), null);
        int countColumn = 0;
        Integer[] totalColumn = new Integer[listCatOptions.size()];
        boolean showColumnTotal = false;
        boolean showRowTotal = false;
        boolean isNumber = true;
        int rowChanged = 0;
        for (SectionModel section : presenter.getSections()) {
            if (section.name().equals(sectionUid)) {
                showColumnTotal = section.showColumnTotals();
                showRowTotal = section.showRowTotals();
            }
        }
        int countRow = 0;
        for (DataElementModel de : presenter.getDataElements().get(sectionUid)) {
            ArrayList<String> values = new ArrayList<>();
            ArrayList<FieldViewModel> fields = new ArrayList<>();
            int totalRow = 0;

            for (List<String> catOpts : listCatOptions) {
                boolean exitsValue = false;
                boolean compulsory = false;
                FieldViewModelFactoryImpl fieldFactory = createField();

                boolean editable = !presenter.getDataElementDisabled().containsKey(sectionUid) || !presenter.getDataElementDisabled().get(sectionUid).containsKey(de.uid())
                        || !presenter.getDataElementDisabled().get(sectionUid).get(de.uid()).containsAll(catOpts);

                if (presenter.getCompulsoryDataElement().containsKey(de.uid()) && presenter.getCompulsoryDataElement().get(de.uid()).containsAll(catOpts))
                    compulsory = true;

                if (de.valueType() != ValueType.NUMBER && de.valueType() != ValueType.INTEGER) {
                    isNumber = false;
                }

                for (DataSetTableModel dataValue : presenter.getDataValues()) {

                    if (dataValue.listCategoryOption().containsAll(catOpts)
                            && Objects.equals(dataValue.dataElement(), de.uid())) {

                        if (isNumber) {
                            if (showColumnTotal)
                                totalColumn[countColumn] = totalColumn[countColumn] != null ?
                                        Integer.parseInt(dataValue.value()) + totalColumn[countColumn] : Integer.parseInt(dataValue.value());
                            if (showRowTotal)
                                totalRow = totalRow + Integer.parseInt(dataValue.value());
                        }
                        if(rowAction != null && de.uid().equals(rowAction.dataElement()) &&
                                catOpts.containsAll(rowAction.listCategoryOption()))
                            rowChanged = countRow;
                        fields.add(fieldFactory.create(dataValue.id().toString(), "", de.valueType(),
                                compulsory, "", dataValue.value(), sectionUid, true,
                                editable, null, null, de.uid(), catOpts, ""));
                        values.add(dataValue.value());
                        exitsValue = true;
                    }
                }

                if (!exitsValue) {
                    //If value type is null, it is due to is dataElement for Total row/column
                    fields.add(fieldFactory.create("", "", de.valueType(),
                            compulsory, "", "", sectionUid, true,
                            editable, null, null, de.uid()== null ? "": de.uid(), catOpts, ""));

                    values.add("");
                }
                if(totalColumn[countColumn] == null)
                    totalColumn[countColumn] = 0;
                countColumn++;
            }
            countColumn = 0;
            if (isNumber && showRowTotal) {
                setTotalRow(totalRow, fields, values);
            }
            listFields.add(fields);
            cells.add(values);
            countRow++;
        }

        if (isNumber) {
            if (showColumnTotal)
                setTotalColumn(totalColumn, listFields, cells, presenter.getDataElements());
            if (showRowTotal)
                presenter.getCatOptions().get(sectionUid).get(presenter.getCatOptions().get(sectionUid).size() - 1).
                        add(CategoryOptionModel.builder().displayName(getString(R.string.total)).build());
        }
        //adapter.setRowPos(rowChanged);

        /*if(rowAction != null){
            adapter = new DataSetTableAdapter(getAbstracContext(), accessDataWrite);
            binding.tableView.setAdapter(adapter);
            adapter.swap(listFields);
            adapter.setAllItems(
                    presenter.getCatOptions().get(sectionUid).get(presenter.getCatOptions().get(sectionUid).size() - 1),
                    presenter.getDataElements().get(sectionUid),
                    cells);
        }
        else {*/
            adapter.swap(listFields);
            adapter.setAllItems(
                    presenter.getCatOptions().get(sectionUid).get(presenter.getCatOptions().get(sectionUid).size() - 1),
                    presenter.getDataElements().get(sectionUid),
                    cells);
        //}
    }

    private void setTotalColumn(Integer[] totalColumn, List<List<FieldViewModel>> listFields, ArrayList<List<String>> cells,
                                Map<String, List<DataElementModel>> dataElements) {
        FieldViewModelFactoryImpl fieldFactory = createField();

        ArrayList<FieldViewModel> fields = new ArrayList<>();
        ArrayList<String> values = new ArrayList<>();
        boolean existTotal = false;
        for (DataElementModel data : dataElements.get(sectionUid))
            if (data.displayName().equals(getString(R.string.total)))
                existTotal = true;

        for (Integer column : totalColumn) {
            fields.add(fieldFactory.create("", "", ValueType.INTEGER,
                    false, "", column.toString(), sectionUid, true,
                    false, null, null, "",new ArrayList<>(),""));

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

    private void setTotalRow(int totalRow, ArrayList<FieldViewModel> fields, ArrayList<String> values){
        FieldViewModelFactoryImpl fieldFactory = createField();
        fields.add(fieldFactory.create("", "", ValueType.INTEGER,
                false, "", String.valueOf(totalRow), sectionUid, true,
                false, null, null, "",new ArrayList<>(),""));
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

}
