package com.dhis2.usescases.main;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import com.dhis2.App;
import com.dhis2.R;
import com.dhis2.databinding.ActivityMainBinding;
import com.dhis2.usescases.general.ActivityGlobalAbstract;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;
import io.reactivex.functions.Consumer;


public class MainActivity extends ActivityGlobalAbstract implements MainContractsModule.View {

    ActivityMainBinding binding;
    @Inject
    MainContractsModule.Presenter presenter;

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

   }