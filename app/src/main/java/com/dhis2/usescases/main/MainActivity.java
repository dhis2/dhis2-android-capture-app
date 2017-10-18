package com.dhis2.usescases.main;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;

import com.dhis2.App;
import com.dhis2.R;
import com.dhis2.databinding.ActivityMainBinding;
import com.dhis2.usescases.general.ActivityGlobalAbstract;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.functions.Consumer;


public class MainActivity extends ActivityGlobalAbstract implements MainContracts.View {

    ActivityMainBinding binding;
    @Inject
    MainContracts.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        AndroidInjection.inject(this);
        ((App) getApplicationContext()).getUserComponent().plus(new MainContractsModule()).inject(this);
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setPresenter(presenter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.init(this);
    }

    @Override
    protected void onPause() {
        presenter.onDetach();
        super.onPause();
    }

    @NonNull
    @Override
    public Consumer<String> renderUsername() {
        return username1 -> binding.text.setText(username1);
    }

    @NonNull
    @Override
    public Consumer<String> renderUserInfo() {
        return (userInitials) -> binding.text.append(userInitials);
    }

    @NonNull
    @Override
    public Consumer<String> renderUserInitials() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Consumer<List<HomeViewModel>> swapData() {

        return homeEntities -> binding.text.append(" list size : " + homeEntities.size());
    }

    @Override
    public void renderError(String message) {
        new AlertDialog.Builder(getActivity())
                .setPositiveButton(android.R.string.ok, null)
                .setTitle(getString(R.string.error))
                .setMessage(message)
                .show();
    }

}