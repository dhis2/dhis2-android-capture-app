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
import org.dhis2.databinding.FragmentDatasetSectionBinding;
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableActivity;
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableContract;
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableModel;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.usescases.general.FragmentGlobalAbstract;
import org.dhis2.utils.Constants;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.category.CategoryOptionModel;
import org.hisp.dhis.android.core.dataelement.DataElementModel;
import org.hisp.dhis.android.core.datavalue.DataValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * QUADRAM. Created by ppajuelo on 02/10/2018.
 */

public class DataSetSectionFragment extends FragmentGlobalAbstract {

    FragmentDatasetSectionBinding binding;
    private DataSetTableContract.Presenter presenter;
    private ActivityGlobalAbstract activity;
    private DataSetTableAdapter adapter;

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
        activity = (ActivityGlobalAbstract) context;
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

        adapter = new DataSetTableAdapter(getAbstracContext());
        binding.tableView.setAdapter(adapter);

        String dataSetSection = getArguments().getString(Constants.DATA_SET_SECTION);

        List<DataElementModel> dataElements = presenter.getDataElements(dataSetSection);
        List<CategoryOptionModel> catOptions = presenter.getCatOptionCombos(dataSetSection);
        List<DataSetTableModel> dataValues = presenter.getDataValues();
        ArrayList<List<String>> cells = new ArrayList<>();
        for (DataElementModel de : dataElements) {
            ArrayList<String> values = new ArrayList<>();
            for (CategoryOptionModel catOpt : catOptions) {
                boolean exitsValue = false;
                for(DataSetTableModel dataValue: dataValues){

                    if(Objects.equals(dataValue.catOption(), catOpt.uid())
                            && Objects.equals(dataValue.dataElement(), de.uid()) ) {
                        values.add(dataValue.value());
                        exitsValue = true;
                    }
                }
                if(!exitsValue)
                    values.add("");
            }
            cells.add(values);
        }

        adapter.setAllItems(
                catOptions,
                dataElements,
                cells);
    }
}
