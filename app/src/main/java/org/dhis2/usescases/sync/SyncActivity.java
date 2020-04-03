package org.dhis2.usescases.sync;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.TypedValue;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.databinding.DataBindingUtil;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.airbnb.lottie.LottieDrawable;

import org.dhis2.App;
import org.dhis2.Bindings.Bindings;
import org.dhis2.R;
import org.dhis2.data.prefs.Preference;
import org.dhis2.databinding.ActivitySynchronizationBinding;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.usescases.main.MainActivity;
import org.dhis2.utils.ColorUtils;
import org.dhis2.utils.Constants;

import javax.inject.Inject;


public class SyncActivity extends ActivityGlobalAbstract implements SyncContracts.View {

    ActivitySynchronizationBinding binding;

    @Inject
    SyncContracts.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((App) getApplicationContext()).userComponent().plus(new SyncModule()).inject(this);
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_synchronization);
        binding.setPresenter(presenter);
        presenter.init(this);
        presenter.sync();
    }

    @Override
    protected void onResume() {
        super.onResume();
        WorkManager.getInstance(getApplicationContext()).getWorkInfosForUniqueWorkLiveData(Constants.INITIAL_SYNC).observe(this, workInfoList -> {
            for (WorkInfo wi : workInfoList) {
                if (wi.getTags().contains(Constants.META_NOW))
                    handleMetaState(wi.getState());
                else if (wi.getTags().contains(Constants.DATA_NOW))
                    handleDataState(wi.getState());
            }
        });
    }

    private void handleMetaState(WorkInfo.State metadataState) {
        switch (metadataState) {
            case RUNNING:
                Bindings.setDrawableEnd(binding.metadataText, AppCompatResources.getDrawable(this, R.drawable.animator_sync));
                break;
            case SUCCEEDED:
                binding.metadataText.setText(getString(R.string.configuration_ready));
                Bindings.setDrawableEnd(binding.metadataText, AppCompatResources.getDrawable(this, R.drawable.animator_done));
                presenter.getTheme();
                break;
            default:
                break;
        }
    }

    private void handleDataState(WorkInfo.State dataState) {
        switch (dataState) {
            case RUNNING:
                binding.eventsText.setText(getString(R.string.syncing_data));
                Bindings.setDrawableEnd(binding.eventsText, AppCompatResources.getDrawable(this, R.drawable.animator_sync));
                binding.eventsText.setAlpha(1.0f);
                break;
            case SUCCEEDED:
                binding.eventsText.setText(getString(R.string.data_ready));
                Bindings.setDrawableEnd(binding.eventsText, AppCompatResources.getDrawable(this, R.drawable.animator_done));
                /*presenter.scheduleSync(getSharedPreferences().getInt(Constants.TIME_META, Constants.TIME_DAILY),
                        getSharedPreferences().getInt(Constants.TIME_DATA, Constants.TIME_15M));*/
                presenter.syncReservedValues();
                startMain();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (binding.lottieView != null) {
            binding.lottieView.setRepeatCount(LottieDrawable.INFINITE);
            binding.lottieView.setRepeatMode(LottieDrawable.RESTART);
            binding.lottieView.enableMergePathsForKitKatAndAbove(true);
            binding.lottieView.playAnimation();
        }
    }

    @Override
    protected void onStop() {
        if (binding.lottieView != null) {
            binding.lottieView.cancelAnimation();
        }
        presenter.onDettach();
        super.onStop();
    }

    @Override
    public void saveTheme(Integer themeId) {
        SharedPreferences prefs = getAbstracContext().getSharedPreferences(
                Constants.SHARE_PREFS, Context.MODE_PRIVATE);
        prefs.edit().putInt(Constants.THEME, themeId).apply();
        setTheme(themeId);

        int startColor = ColorUtils.getPrimaryColor(this, ColorUtils.ColorType.PRIMARY);
        TypedValue typedValue = new TypedValue();
        TypedArray a = obtainStyledAttributes(typedValue.data, new int[]{R.attr.colorPrimary});
        int endColor = a.getColor(0, 0);
        a.recycle();

        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), startColor, endColor);
        colorAnimation.setDuration(2000); // milliseconds
        colorAnimation.addUpdateListener(animator -> binding.logo.setBackgroundColor((int) animator.getAnimatedValue()));
        colorAnimation.start();

    }

    @Override
    public void saveFlag(String s) {
        SharedPreferences prefs = getAbstracContext().getSharedPreferences(
                Constants.SHARE_PREFS, Context.MODE_PRIVATE);
        prefs.edit().putString("FLAG", s).apply();

        binding.logoFlag.setImageResource(getResources().getIdentifier(s, "drawable", getPackageName()));
        ValueAnimator alphaAnimator = ValueAnimator.ofFloat(0f, 1f);
        alphaAnimator.setDuration(2000);
        alphaAnimator.addUpdateListener(animation -> {
            binding.logoFlag.setAlpha((float) animation.getAnimatedValue());
            binding.dhisLogo.setAlpha((float) 0);
        });
        alphaAnimator.start();

    }


    public void startMain() {
        getSharedPreferences().edit().putBoolean(Preference.INITIAL_SYNC_DONE, true).apply();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

}
