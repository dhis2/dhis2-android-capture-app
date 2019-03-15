package org.dhis2.usescases.splash;

import android.os.Bundle;
import android.view.View;

import org.dhis2.App;
import org.dhis2.AppComponent;
import org.dhis2.R;
import org.dhis2.data.server.ServerComponent;
import org.dhis2.databinding.ActivitySplashBinding;
import org.dhis2.usescases.general.ActivityGlobalAbstract;

import javax.inject.Inject;

import androidx.databinding.DataBindingUtil;
import io.reactivex.functions.Consumer;

public class SplashActivity extends ActivityGlobalAbstract implements SplashContracts.View {

    ActivitySplashBinding binding;

    @Inject
    SplashContracts.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppComponent appComponent = ((App) getApplicationContext()).appComponent();
        ServerComponent serverComponent = ((App) getApplicationContext()).serverComponent();
        appComponent.plus(new SplashModule(serverComponent)).inject(this);
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash);
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.init(this);
    }

    @Override
    protected void onPause() {
        presenter.destroy();
        super.onPause();
    }

    @Override
    public Consumer<Integer> renderFlag() {
        return flag -> {
            if (flag != -1) {
                binding.flag.setImageResource(flag);
                binding.logo.setVisibility(View.GONE);
                binding.flag.setVisibility(View.VISIBLE);
            }
            presenter.isUserLoggedIn();
        };
    }
}