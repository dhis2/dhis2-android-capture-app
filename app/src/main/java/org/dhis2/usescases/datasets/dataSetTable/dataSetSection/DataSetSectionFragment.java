package org.dhis2.usescases.datasets.dataSetTable.dataSetSection;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.dhis2.R;
import org.dhis2.databinding.FragmentDatasetSectionBinding;
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableActivity;
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableContract;
import org.dhis2.usescases.general.FragmentGlobalAbstract;
import org.dhis2.utils.Constants;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.dataelement.DataElement;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

/**
 * QUADRAM. Created by ppajuelo on 02/10/2018.
 */

public class DataSetSectionFragment extends FragmentGlobalAbstract {

    private FragmentDatasetSectionBinding binding;
    private DataSetTableContract.DataSetTablePresenter presenter;

    @NonNull
    public static DataSetSectionFragment create(@NonNull String sectionUid) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.DATA_SET_SECTION, sectionUid);

        DataSetSectionFragment dataSetSectionFragment = new DataSetSectionFragment();
        dataSetSectionFragment.setArguments(bundle);

        return dataSetSectionFragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
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

        DataSetTableAdapter adapter = new DataSetTableAdapter(getAbstracContext());
        binding.tableView.setAdapter(adapter);

        String dataSetSection = getArguments().getString(Constants.DATA_SET_SECTION);

        List<DataElement> dataElements = presenter.getDataElements(dataSetSection);
        List<CategoryOptionComboModel> catOptions = presenter.getCatOptionCombos(dataSetSection);

        ArrayList<List<String>> cells = new ArrayList<>();
        ArrayList<String> values = new ArrayList<>();
        for (CategoryOptionComboModel catOpt : catOptions) {
            values.add(catOpt.uid());
        }
        cells.add(values);

        adapter.setAllItems(
                catOptions,
                dataElements,
                cells);
    }
}
