package org.dhis2.usescases.eventsWithoutRegistration.eventSummary;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.dhis2.App;
import org.dhis2.BuildConfig;
import org.dhis2.R;
import org.dhis2.data.forms.FormSectionViewModel;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.databinding.ActivityEventSummaryBinding;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.DialogClickListener;
import org.dhis2.utils.HelpManager;
import org.dhis2.utils.custom_views.CustomDialog;
import org.dhis2.utils.custom_views.ProgressBarAnimation;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.program.ProgramModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import io.reactivex.functions.Consumer;
import me.toptas.fancyshowcase.FancyShowCaseView;
import me.toptas.fancyshowcase.FocusShape;

import static android.text.TextUtils.isEmpty;

/**
 * QUADRAM. Created by Cristian on 01/03/2018.
 */

public class EventSummaryActivity extends ActivityGlobalAbstract implements EventSummaryContract.View, ProgressBarAnimation.OnUpdate {

    private static final int PROGRESS_TIME = 2000;

    public static final String EVENT_ID = "event_id";
    public static final String PROGRAM_ID = "program_id";

    private Map<String, View> sections = new HashMap<>();

    @Inject
    EventSummaryContract.Presenter presenter;
    private ActivityEventSummaryBinding binding;
    private int completionPercent;
    private int totalFields;
    private int totalCompletedFields;
    private int fieldsToCompleteBeforeClosing;
    String eventId;
    String programId;
    private String messageOnComplete = "";
    private boolean canComplete = true;
    private CustomDialog dialog;
    private boolean fieldsWithErrors;
    private EventModel eventModel;
    private ProgramModel programModel;
    private ArrayList<String> sectionsToHide;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        if (getIntent().getExtras() != null && getIntent().getExtras().containsKey(EVENT_ID) && getIntent().getExtras().containsKey(PROGRAM_ID)
                && getIntent().getExtras().getString(EVENT_ID) != null && getIntent().getExtras().getString(PROGRAM_ID) != null) {
            eventId = getIntent().getExtras().getString(EVENT_ID);
            programId = getIntent().getExtras().getString(PROGRAM_ID);
            ((App) getApplicationContext()).userComponent().plus(new EventSummaryModule(this, eventId)).inject(this);
        } else {
            finish();
        }

        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_event_summary);
        binding.setPresenter(presenter);

        binding.actionButton.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.init(this, programId, eventId);

    }

    @Override
    protected void onPause() {
        presenter.onDettach();
        super.onPause();
    }

    @Override
    public void setProgram(@NonNull ProgramModel program) {
        binding.setName(program.displayName());
        programModel = program;
    }

    @Override
    public void onUpdate(boolean lost, float value) {
        String text = String.valueOf((int) value) + "%";
        binding.progress.setText(text);
    }

    @Override
    public void onEventSections(List<FormSectionViewModel> formSectionViewModels) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        for (FormSectionViewModel formSectionViewModel : formSectionViewModels) {
            View inflatedLayout = inflater.inflate(R.layout.event_section_row, null, false);
            ((TextView) inflatedLayout.findViewById(R.id.section_title)).setText(formSectionViewModel.label());
            binding.eventSectionRows.addView(inflatedLayout);
            sections.put(formSectionViewModel.sectionUid(), inflatedLayout);
            presenter.getSectionCompletion(formSectionViewModel.sectionUid());
        }
    }

    @Override
    public void setHideSection(String sectionUid) {
        if (sectionsToHide == null || sectionUid == null)
            sectionsToHide = new ArrayList<>();

        if (sectionUid != null && !sectionsToHide.contains(sectionUid))
            sectionsToHide.add(sectionUid);
    }


    @NonNull
    @Override
    public Consumer<List<FieldViewModel>> showFields(String sectionUid) {
        return fields -> swap(fields, sectionUid);
    }

    @Override
    public void onStatusChanged(EventModel event) {
        Toast.makeText(this, getString(R.string.event_updated), Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(this::finish, 1000);
    }

    @Override
    public void setActionButton(EventModel eventModel) {
        this.eventModel = eventModel;

    }

    @Override
    public void messageOnComplete(String content, boolean canComplete) {
        this.messageOnComplete = content;
        this.canComplete = canComplete;
    }

    @Override
    public void checkAction() {
        dialog = new CustomDialog(
                getContext(),
                getString(R.string.warning_error_on_complete_title),
                messageOnComplete,
                getString(R.string.button_ok),
                getString(R.string.cancel),
                1001,
                new DialogClickListener() {
                    @Override
                    public void onPositive() {
                        if (canComplete)
                            presenter.doOnComple();
                        dialog.dismiss();
                    }

                    @Override
                    public void onNegative() {
                        dialog.dismiss();
                    }
                });
        if (!isEmpty(messageOnComplete))
            dialog.show();
        else
            presenter.doOnComple();
    }

    @Override
    public void accessDataWrite(Boolean canWrite) {

        if (DateUtils.Companion.getInstance().isEventExpired(null, eventModel.completedDate(), programModel.completeEventsExpiryDays())){
            binding.actionButton.setVisibility(View.GONE);
        }
        else {
            switch (eventModel.status()) {
                case ACTIVE:
                    binding.actionButton.setText(getString(R.string.complete_and_close));
                    binding.actionButton.setVisibility(canWrite ? View.VISIBLE : View.GONE);
                    break;
                case SKIPPED:
                    binding.actionButton.setVisibility(View.GONE);
                    break;
                case VISITED:
                    binding.actionButton.setVisibility(View.GONE); //TODO: Can this happen?
                    break;
                case SCHEDULE:
                    binding.actionButton.setVisibility(View.GONE); //TODO: Can this happen?
                    break;
                case COMPLETED:
                    binding.actionButton.setText(getString(R.string.re_open));
                    binding.actionButton.setVisibility(canWrite ? View.VISIBLE : View.GONE);
                    break;
            }
        }

        if (!HelpManager.INSTANCE.isTutorialReadyForScreen(getClass().getName()))
            setTutorial();
    }

    @Override
    public void fieldWithError(boolean hasError) {
        fieldsWithErrors = hasError;
    }


    void swap(@NonNull List<FieldViewModel> updates, String sectionUid) {

        View sectionView = sections.get(sectionUid);
        if (sectionsToHide != null && sectionsToHide.contains(sectionUid)) {
            sectionView.setVisibility(View.GONE);
            sectionView.setVisibility(View.GONE);
        } else
            sectionView.setVisibility(View.VISIBLE);

        if (sectionView.getVisibility() == View.VISIBLE) {
            int completedSectionFields = calculateCompletedFields(updates);
            int totalSectionFields = updates.size();
            totalFields = totalFields + totalSectionFields;
            totalCompletedFields = totalCompletedFields + completedSectionFields;
            fieldsToCompleteBeforeClosing = fieldsToCompleteBeforeClosing + calculateMandatoryUnansweredFields(updates);
            String completionText = completedSectionFields + "/" + totalSectionFields;
            ((TextView) sectionView.findViewById(R.id.section_percent)).setText(completionText);
            sectionView.findViewById(R.id.completed_progress)
                    .setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT, (float) totalSectionFields - (float) completedSectionFields));
            sectionView.findViewById(R.id.empty_progress)
                    .setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT, completedSectionFields));

            List<String> missingMandatoryFields = new ArrayList<>();
            List<String> errorFields = new ArrayList<>();
            for (FieldViewModel fields : updates) {
                if (fields.error() != null)
                    errorFields.add(fields.label());
                if (fields.mandatory() && fields.value() == null)
                    missingMandatoryFields.add(fields.label());
            }
            if (!missingMandatoryFields.isEmpty() || !errorFields.isEmpty()) {
                sectionView.findViewById(R.id.section_info).setVisibility(View.VISIBLE);

                StringBuilder missingString = new StringBuilder(missingMandatoryFields.isEmpty() ? "" : "These fields are mandatory. Please check their values to be able to complete the event.");
                for (String missinField : missingMandatoryFields) {
                    missingString.append(String.format("\n- %s", missinField));
                }

                StringBuilder errorString = new StringBuilder(errorFields.isEmpty() ? "" : "These fields contain errors. Please check their values to be able to complete the event.");
                for (String errorField : errorFields) {
                    errorString.append(String.format("\n- %s", errorField));
                }

                String finalMessage = missingString.append("\n").append(errorString.toString()).toString();

                sectionView.findViewById(R.id.section_info).setOnClickListener(view ->
                        showInfoDialog("Error", finalMessage)
                );
            }

        }

        binding.summaryHeader.setText(String.format(getString(R.string.event_summary_header), String.valueOf(totalCompletedFields), String.valueOf(totalFields)));
        float completionPerone = (float) totalCompletedFields / (float) totalFields;
        completionPercent = (int) (completionPerone * 100);
        ProgressBarAnimation gainAnim = new ProgressBarAnimation(binding.progressGains, 0, completionPercent, false, this);
        gainAnim.setDuration(PROGRESS_TIME);
        binding.progressGains.startAnimation(gainAnim);
        checkButton();
    }

    private void checkButton() {
        binding.actionButton.setEnabled(fieldsToCompleteBeforeClosing <= 0 && !fieldsWithErrors);
    }

    private int calculateCompletedFields(@NonNull List<FieldViewModel> updates) {
        int total = 0;
        for (FieldViewModel fieldViewModel : updates) {
            if (fieldViewModel.value() != null && !fieldViewModel.value().isEmpty())
                total++;
        }
        return total;
    }

    private int calculateMandatoryUnansweredFields(@NonNull List<FieldViewModel> updates) {
        int total = 0;
        for (FieldViewModel fieldViewModel : updates) {
            if ((fieldViewModel.value() == null || fieldViewModel.value().isEmpty()) && fieldViewModel.mandatory())
                total++;
        }
        return total;
    }

    @Override
    public void setTutorial() {
        super.setTutorial();

        SharedPreferences prefs = getSharedPreferences();

        new Handler().postDelayed(() -> {
            ArrayList<FancyShowCaseView> steps = new ArrayList<>();


            FancyShowCaseView tuto1 = new FancyShowCaseView.Builder(getAbstractActivity())
                    .title(getString(R.string.tuto_event_summary))
                    .enableAutoTextPosition()
                    .focusOn(binding.actionButton)
                    .closeOnTouch(true)
                    .focusShape(FocusShape.ROUNDED_RECTANGLE)
                    .build();
            steps.add(tuto1);

            HelpManager.INSTANCE.setScreenHelp(getClass().getName(), steps);

            if (!prefs.getBoolean("TUTO_EVENT_SUMMARY", false) && !BuildConfig.DEBUG) {
                HelpManager.INSTANCE.showHelp();
                prefs.edit().putBoolean("TUTO_EVENT_SUMMARY", true).apply();
            }

        }, 500);
    }
}