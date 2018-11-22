package org.dhis2.usescases.eventsWithoutRegistration.eventCapture;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Gravity;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.databinding.ActivityEventCaptureBinding;
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureFragment.EventCaptureFormFragment;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.utils.Constants;
import org.dhis2.utils.CustomViews.CustomDialog;
import org.dhis2.utils.CustomViews.ProgressBarAnimation;
import org.dhis2.utils.DialogClickListener;
import org.dhis2.utils.Utils;

import java.util.Map;

import javax.inject.Inject;

import io.reactivex.functions.Consumer;

/**
 * QUADRAM. Created by ppajuelo on 19/11/2018.
 */
public class EventCaptureActivity extends ActivityGlobalAbstract implements EventCaptureContract.View {

    private ActivityEventCaptureBinding binding;
    @Inject
    EventCaptureContract.Presenter presenter;
    private int completionPercentage = 0;

    public static Bundle getActivityBundle(@NonNull String eventUid, @NonNull String programUid) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.EVENT_UID, eventUid);
        bundle.putString(Constants.PROGRAM_UID, programUid);
        return bundle;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ((App) getApplicationContext()).userComponent().plus(
                new EventCaptureModule(
                        getIntent().getStringExtra(Constants.EVENT_UID),
                        getIntent().getStringExtra(Constants.PROGRAM_UID)))
                .inject(this);
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_event_capture);
        binding.setPresenter(presenter);

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
            int newPercentage = (int) (percentage * 100);

            ProgressBarAnimation gainAnim = new ProgressBarAnimation(binding.progressGains, completionPercentage, 0, newPercentage, false,
                    (lost, value) -> {
                        String text = String.valueOf((int) value) + "%";
                        binding.progress.setText(text);
                    });
            gainAnim.setDuration(1000);
            binding.progressGains.startAnimation(gainAnim);

            this.completionPercentage = (int) (percentage * 100);

        };
    }

    @Override
    public void setMandatoryWarning(Map<String, FieldViewModel> emptyMandatoryFields) {
        new CustomDialog(
                getAbstracContext(),
                getAbstracContext().getString(R.string.missing_mandatory_fields_title),
                getAbstracContext().getString(R.string.missing_mandatory_fields_events),
                getAbstracContext().getString(R.string.button_ok),
                "Check",
                Constants.RQ_MANDATORY_EVENTS,
                new DialogClickListener() {
                    @Override
                    public void onPositive() {
                        showCompleteActions(false);
                    }

                    @Override
                    public void onNegative() {
                        presenter.goToSection(emptyMandatoryFields.get(emptyMandatoryFields.entrySet().iterator().next().getKey()).programStageSection());
                    }
                })
                .show();
    }

    @Override
    public void showCompleteActions(boolean canComplete) {
        Utils.getPopUpMenu(this,
                EventCaptureFormFragment.getInstance().getSectionSelector(),
                R.menu.event_form_finish_menu,
                Gravity.TOP,
                item -> {
                    switch (item.getItemId()) {
                        case R.id.complete:

                            break;
                        case R.id.completeAndAddNew:
                            break;
                        case R.id.completeLater:
                            break;
                    }
                    return false;
                },
                true);
    }

    @Override
    public void attemptToFinish() {
       showCompleteActions(true);
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
