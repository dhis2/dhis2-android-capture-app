package org.dhis2.usescases.datasets.dataSetTable.dataSetSection;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
        presenter.test(this);
    }

    public void setData(Map<String, List<DataElementModel>> dataElements, Map<String, List<List<CategoryOptionModel>>> catOptions, List<DataSetTableModel> dataValues,
                        Map<String, List<List<Pair<CategoryOptionModel, CategoryModel>>>> mapWithoutTransform, Map<String, Map<String, List<String>>> dataElementDisabled,
                        Map<String, List<String>> compulsoryDataElement, List<SectionModel> sections){

        ArrayList<List<String>> cells = new ArrayList<>();
        List<List<FieldViewModel>> listFields = new ArrayList<>();
        List<List<String>> listCatOptions = presenter.getCatOptionCombos(mapWithoutTransform.get(sectionUid), 0, new ArrayList<>(), null);
        int countColumn = 0;
        Integer[] totalColumn = new Integer[listCatOptions.size()];
        boolean showColumnTotal = false;
        boolean showRowTotal = false;
        boolean isNumber = true;
        for(SectionModel section: sections) {
            if(section.name().equals(sectionUid)) {
                showColumnTotal = section.showColumnTotals();
                showRowTotal = section.showRowTotals();
            }
        }
        for (DataElementModel de : dataElements.get(sectionUid)) {
            ArrayList<String> values = new ArrayList<>();
            ArrayList<FieldViewModel> fields = new ArrayList<>();
            int totalRow = 0;

            for (List<String> catOpts : listCatOptions) {
                boolean exitsValue = false;
                boolean compulsory = false;
                FieldViewModelFactoryImpl fieldFactory = createField();

                boolean editable = !dataElementDisabled.containsKey(sectionUid) || !dataElementDisabled.get(sectionUid).containsKey(de.uid())
                        || !dataElementDisabled.get(sectionUid).get(de.uid()).containsAll(catOpts);

                if(compulsoryDataElement.containsKey(de.uid()) && compulsoryDataElement.get(de.uid()).containsAll(catOpts))
                    compulsory = true;

                if(de.valueType() != ValueType.NUMBER && de.valueType() != ValueType.INTEGER) {
                    isNumber = false;
                }

                for (DataSetTableModel dataValue : dataValues) {

                    if (dataValue.listCategoryOption().containsAll(catOpts)
                            && Objects.equals(dataValue.dataElement(), de.uid())) {

                        if(isNumber) {
                            if(showColumnTotal)
                                totalColumn[countColumn] = totalColumn[countColumn] != null ?
                                        Integer.parseInt(dataValue.value()) + totalColumn[countColumn] : Integer.parseInt(dataValue.value());
                            if(showRowTotal)
                                totalRow = totalRow + Integer.parseInt(dataValue.value());
                        }

                        fields.add(fieldFactory.create(dataValue.id().toString(), "", de.valueType(),
                                compulsory, "", dataValue.value(), sectionUid, true,
                                editable, null, null));
                        values.add(dataValue.value());
                        exitsValue = true;
                    }
                }

                if (!exitsValue) {
                    fields.add(fieldFactory.create("", "", de.valueType(),
                            compulsory, "", "", sectionUid, true,
                            editable, null, null));

                    values.add("");
                }

                countColumn++;
            }
            countColumn = 0;
            if(isNumber && showRowTotal) {
                setTotalRow(totalRow, fields, values);
            }
            listFields.add(fields);
            cells.add(values);
        }
        if(isNumber) {
            if(showColumnTotal)
                setTotalColumn(totalColumn, listFields, cells, dataElements);
            if(showRowTotal)
                catOptions.get(sectionUid).get(catOptions.get(sectionUid).size() - 1).add(CategoryOptionModel.builder().displayName("Total").build());
        }
        adapter.swap(listFields);
        adapter.setAllItems(
                catOptions.get(sectionUid).get(catOptions.get(sectionUid).size()-1),
                dataElements.get(sectionUid),
                cells);
    }

    private void setTotalColumn( Integer[] totalColumn, List<List<FieldViewModel>> listFields, ArrayList<List<String>> cells,
                                 Map<String, List<DataElementModel>> dataElements){
        FieldViewModelFactoryImpl fieldFactory = createField();

        ArrayList<FieldViewModel> fields = new ArrayList<>();
        ArrayList<String> values = new ArrayList<>();

        for(Integer column: totalColumn){
            fields.add(fieldFactory.create("", "", ValueType.INTEGER,
                    false, "", column.toString(), sectionUid, true,
                    false, null, null));

            values.add(column.toString());
        }

        listFields.add(fields);
        cells.add(values);
        dataElements.get(sectionUid).add(DataElementModel.builder().displayName("Total").build());
    }

    private void setTotalRow(int totalRow, ArrayList<FieldViewModel> fields, ArrayList<String> values){
        FieldViewModelFactoryImpl fieldFactory = createField();
        fields.add(fieldFactory.create("", "", ValueType.INTEGER,
                false, "", String.valueOf(totalRow), sectionUid, true,
                false, null, null));
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
