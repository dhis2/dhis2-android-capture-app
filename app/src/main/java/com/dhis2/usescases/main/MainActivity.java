package com.dhis2.usescases.main;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.widget.Toast;

import com.dhis2.R;
import com.dhis2.databinding.ActivityMainBinding;
import com.dhis2.usescases.general.ActivityGlobalAbstract;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import io.reactivex.functions.Consumer;


public class MainActivity extends ActivityGlobalAbstract implements MainContractsModule.View {

    ActivityMainBinding binding;
    MainAdapter adapter;
    @Inject
    MainPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setPresenter(presenter);
        adapter = new MainAdapter(new ArrayList<>());
        binding.programRecycler.setAdapter(adapter);

    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.init();
    }

    @Override
    public void renderError(String error) {
        Toast.makeText(this,error,Toast.LENGTH_LONG).show();
    }

    @Override
    public Consumer<List<HomeViewModel>> swapData() {
        return homeEntities -> adapter.swapData(homeEntities);
    }
}