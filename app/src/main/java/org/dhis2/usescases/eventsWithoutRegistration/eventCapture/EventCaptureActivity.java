package org.dhis2.usescases.eventsWithoutRegistration.eventCapture;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.databinding.ActivityEventCaptureBinding;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.utils.Constants;
import org.dhis2.utils.CustomViews.ProgressBarAnimation;

import javax.inject.Inject;

import io.reactivex.functions.Consumer;

/**
 * QUADRAM. Created by ppajuelo on 19/11/2018.
 */
public class EventCaptureActivity extends ActivityGlobalAbstract implements EventCaptureContract.View {

    private ActivityEventCaptureBinding binding;
    @Inject
    EventCaptureContract.Presenter presenter;
    private int completionPercentage;

    public static Bundle getActivityBundle(@NonNull String eventUid, @NonNull String programUid) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.EVENT_UID, eventUid);
        bundle.putString(Constants.PROGRAM_UID, programUid);
        return bundle;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ((App) getApplicationContext()).userComponent().plus(new EventCaptureModule(
                getIntent().getStringExtra(Constants.EVENT_UID),
                getIntent().getStringExtra(Constants.PROGRAM_UID)
        )).inject(this);
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_event_capture);

    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.init(this);

    }

    @Override
    protected void onPause() {
        presenter.onDettach();
        super.onPause();
    }

    @Override
    public void setUp() {
        binding.eventViewPager.setAdapter(new EventCapturePagerAdapter(getSupportFragmentManager()));
    }

    @Override
    public Consumer<Float> updatePercentage() {
        return percentage -> {
            this.completionPercentage = (int) (percentage * 100);
            ProgressBarAnimation gainAnim = new ProgressBarAnimation(binding.progressGains, 0, completionPercentage, false,
                    (lost, interpolatedTime) -> {
                        int progress = (int) (completionPercentage * interpolatedTime);
                        String text = String.valueOf(progress) + "%";
                        binding.progress.setText(text);
                    });
            gainAnim.setDuration(1000);
            binding.progressGains.startAnimation(gainAnim);
        };
    }

    @Override
    public void renderInitialInfo(String stageName, String eventDate, String orgUnit, String catOption) {
        binding.programStageName.setText(stageName);
        binding.eventSecundaryInfo.setText(String.format("%s | %s | %s", eventDate, orgUnit, catOption));
    }

    @Override
    public EventCaptureContract.Presenter getPresenter() {
        return presenter;
    }
}
