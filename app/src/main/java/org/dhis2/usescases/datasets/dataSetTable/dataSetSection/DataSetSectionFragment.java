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
import org.dhis2.data.forms.dataentry.tablefields.FieldViewModelFactoryImpl;
import org.dhis2.data.tuples.Pair;
import org.dhis2.databinding.FragmentDatasetSectionBinding;
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableActivity;
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableContract;
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableModel;
import org.dhis2.usescases.general.FragmentGlobalAbstract;
import org.dhis2.utils.Constants;
import org.hisp.dhis.android.core.category.CategoryModel;
import org.hisp.dhis.android.core.category.CategoryOptionModel;
import org.hisp.dhis.android.core.dataelement.DataElementModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * QUADRAM. Created by ppajuelo on 02/10/2018.
 */

public class DataSetSectionFragment extends FragmentGlobalAbstract {

    FragmentDatasetSectionBinding binding;
    private DataSetTableContract.Presenter presenter;
    private DataSetTableActivity activity;
    private DataSetTableAdapter adapter;
    private String sectionUid;

    @NonNull
    public static DataSetSectionFragment create(@NonNull String sectionUid) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.DATA_SET_SECTION, sectionUid);

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
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        sectionUid = getArguments().getString(Constants.DATA_SET_SECTION);

        presenter.getData(this, sectionUid);

        adapter = new DataSetTableAdapter(getAbstracContext());
        binding.tableView.setAdapter(adapter);

    }

    public void setData(Map<String, List<DataElementModel>> dataElements, Map<String, List<List<CategoryOptionModel>>> catOptions, List<DataSetTableModel> dataValues,
                        Map<String, List<List<Pair<CategoryOptionModel, CategoryModel>>>> mapWithoutTransform){

        ArrayList<List<String>> cells = new ArrayList<>();
        List<List<FieldViewModel>> listFields = new ArrayList<>();
        for (DataElementModel de : dataElements.get(sectionUid)) {
            ArrayList<String> values = new ArrayList<>();
            ArrayList<FieldViewModel> fields = new ArrayList<>();
            for (List<String> catOpts : presenter.getCatOptionCombos(mapWithoutTransform.get(sectionUid), 0, new ArrayList<>(), null)) {
                boolean exitsValue = false;
                FieldViewModelFactoryImpl fieldFactory = new FieldViewModelFactoryImpl(
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "");
                for (DataSetTableModel dataValue : dataValues) {

                    if (dataValue.listCategoryOption().containsAll(catOpts)
                            && Objects.equals(dataValue.dataElement(), de.uid())) {


                        fields.add(fieldFactory.create(dataValue.id().toString(), "", de.valueType(),
                                false, "", dataValue.value(), sectionUid, true,
                                true, null, de.description()));
                        values.add(dataValue.value());
                        exitsValue = true;
                    }
                }
                if (!exitsValue) {
                    fields.add(fieldFactory.create("", "", de.valueType(),
                            false, "", "", sectionUid, true,
                            true, null, de.description()));

                    values.add("");
                }
            }
            listFields.add(fields);
            cells.add(values);
        }

        adapter.swap(listFields);
        adapter.setAllItems(
                catOptions.get(sectionUid).get(catOptions.get(sectionUid).size()-1),
                dataElements.get(sectionUid),
                cells);
    }
}
