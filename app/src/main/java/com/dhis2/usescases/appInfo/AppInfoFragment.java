package com.dhis2.usescases.appInfo;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dhis2.Components;
import com.dhis2.R;
import com.dhis2.databinding.FragmentInfoBinding;
import com.dhis2.usescases.general.FragmentGlobalAbstract;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by ppajuelo on 31/01/2018.
 */

public class AppInfoFragment extends FragmentGlobalAbstract {

    FragmentInfoBinding binding;
    CompositeDisposable compositeDisposable;
    @Inject
    InfoRepository infoRepository;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((Components) getActivity().getApplicationContext()).userComponent()
                .plus(new InfoModule()).inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_info, container, false);
        compositeDisposable = new CompositeDisposable();
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        getData();
    }

    private void getData() {

        compositeDisposable.add(infoRepository.programs()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> binding.programs.setText("Programs: " + data.size()))
        );

        compositeDisposable.add(infoRepository.events()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> binding.events.setText("Events: " + data.size()))
        );

        compositeDisposable.add(infoRepository.orgUnits()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> binding.orgUnit.setText("OrgUnits: " + data.size()))
        );

        compositeDisposable.add(infoRepository.trackedEntityInstances()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> binding.tei.setText("TEI: " + data.size()))
        );

    }

    @Override
    public void onDestroy() {
        compositeDisposable.dispose();
        super.onDestroy();
    }


}
