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
import org.dhis2.databinding.ActivitySynchronizationBinding;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.usescases.main.MainActivity;
import org.dhis2.utils.ColorUtils;
import org.dhis2.utils.Constants;

import javax.inject.Inject;

import timber.log.Timber;


public class SyncActivity extends ActivityGlobalAbstract implements SyncContracts.View {

    ActivitySynchronizationBinding binding;

    @Inject
    SyncContracts.Presenter presenter;
    private boolean metadataRunning;
    private boolean metadataDone;
    private boolean dataRunning;
    private boolean dataDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
       ((App) getApplicationContext()).userComponent().plus(new SyncModule()).inject(this);
        super.onCreate(savedInstanceState);

        WorkManager.getInstance(getApplicationContext()).getWorkInfosByTagLiveData(Constants.META).observe(this, workInfoList -> {
            if (workInfoList != null && !workInfoList.isEmpty()) {
                Timber.d("WORK %s WITH STATUS %s", Constants.META, workInfoList.get(0).getState().name());
                handleMetaState(workInfoList.get(0).getState());
            }
        });
        WorkManager.getInstance(getApplicationContext()).getWorkInfosByTagLiveData(Constants.DATA).observe(this, status -> {
            if (status != null && !status.isEmpty()) {
                Timber.d("WORK %s WITH STATUS %s", Constants.DATA, status.get(0).getState().name());
                handleDataState(status.get(0).getState());
            }
        });

        binding = DataBindingUtil.setContentView(this, R.layout.activity_synchronization);
        binding.setPresenter(presenter);
        presenter.init(this);
        presenter.syncMeta(getSharedPreferences().getInt(Constants.TIME_META, Constants.TIME_DAILY), Constants.META);
    }

    private void handleMetaState(WorkInfo.State metadataState) {
        switch (metadataState) {
            case RUNNING:
                metadataRunning = true;
                Bindings.setDrawableEnd(binding.metadataText, AppCompatResources.getDrawable(this, R.drawable.animator_sync));
                break;
            case ENQUEUED:
                if (metadataRunning) {
                    metadataDone = true;
                    binding.metadataText.setText(getString(R.string.configuration_ready));
                    Bindings.setDrawableEnd(binding.metadataText, AppCompatResources.getDrawable(this, R.drawable.animator_done));
                    presenter.getTheme();
                    presenter.syncData(getSharedPreferences().getInt(Constants.TIME_DATA, Constants.TIME_DAILY), Constants.DATA);
                }
                break;
        }
    }

    private void handleDataState(WorkInfo.State dataState) {
        switch (dataState) {
            case RUNNING:
                dataRunning = true;
                binding.eventsText.setText(getString(R.string.syncing_data));
                Bindings.setDrawableEnd(binding.eventsText, AppCompatResources.getDrawable(this, R.drawable.animator_sync));
                binding.eventsText.setAlpha(1.0f);
                break;
            case ENQUEUED:
                if (dataRunning && metadataDone) {
                    dataDone = true;
                    binding.eventsText.setText(getString(R.string.data_ready));
                    Bindings.setDrawableEnd(binding.eventsText, AppCompatResources.getDrawable(this, R.drawable.animator_done));
                    presenter.syncReservedValues();
                    startMain();
                }
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (binding.lottieView != null) {
            binding.lottieView.setRepeatCount(LottieDrawable.INFINITE);
            binding.lottieView.setRepeatMode(LottieDrawable.RESTART);
            binding.lottieView.useHardwareAcceleration(true);
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
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

}
