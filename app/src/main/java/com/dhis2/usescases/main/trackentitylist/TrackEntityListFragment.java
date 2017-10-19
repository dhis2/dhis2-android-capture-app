package com.dhis2.usescases.main.trackentitylist;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dhis2.R;
import com.dhis2.databinding.FragmentTrackEntityListBinding;
import com.dhis2.usescases.general.FragmentGlobalAbstract;
import com.evrencoskun.tableview.TableView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;


public class TrackEntityListFragment extends FragmentGlobalAbstract implements TrackEntityListContractsModule.View {

    FragmentTrackEntityListBinding binding;
    @Inject
    TrackEntityListPresenter presenter;
    
    private TableView tableView;
    private TrackEntityAdapter adapter;
    private List<RowHeaderModel> rowHeaderList = new ArrayList<>();
    private List<ColumnHeaderModel> columnHeaderList = new ArrayList<>();
    private List<List<CellModel>> cellList = new ArrayList<>();

    public TrackEntityListFragment() {
        //UNUSED
    }

    public static TrackEntityListFragment newInstance() {
        return new TrackEntityListFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        AndroidSupportInjection.inject(this);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_track_entity_list, container, false);
        binding.setPresenter(presenter);
        return binding.getRoot();

    }

    @Override
    public TableView setupTable() {
        tableView = new TableView(getContext());
        adapter = new TrackEntityAdapter(getContext(), presenter);
        tableView.setAdapter(adapter);
        return tableView;
    }

    @Override
    public void onResume() {
        super.onResume();
        tableView = setupTable();
        binding.fragmentContainer.addView(tableView);

        for (int i = 0; i < 20 ; i++) {
            cellList.add(new ArrayList<CellModel>());
        }
        
        loadData();
    }

    private void loadData() {

        List<List<CellModel>> cellsList = getCellList();
        
        rowHeaderList.addAll(getRowlist());
        columnHeaderList.addAll(getColumnList());
        for (int i = 0; i < cellsList.size(); i++) {
            cellList.get(i).addAll(cellsList.get(i));
        }


        adapter.setAllItems(columnHeaderList, rowHeaderList, cellList);
    }

    private List<List<CellModel>> getCellList() {
        List<List<CellModel>> list = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            List<CellModel> cellList = new ArrayList<>();
            for (int j = 0; j < 20; j++) {
                String strText = "celda " + j + " " + i;
                CellModel cell = new CellModel(j, strText);
                cellList.add(cell);
            }
            list.add(cellList);
        }

        return list;
    }

    private List<ColumnHeaderModel> getColumnList() {
        List<ColumnHeaderModel> list = new ArrayList<>();

        for (int i = 0; i < 20; i++) {
            String strTitle = "columna " + i;
            ColumnHeaderModel header = new ColumnHeaderModel(i, strTitle);
            list.add(header);
        }

        return list;
    }

    private List<RowHeaderModel> getRowlist() {
        List<RowHeaderModel> list = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            RowHeaderModel header = new RowHeaderModel(i, "fila " + i);
            list.add(header);
        }

        return list;
    }


}