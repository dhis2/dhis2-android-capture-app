package org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureFragment;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.dhis2.R;
import org.dhis2.data.forms.FormSectionViewModel;
import org.dhis2.data.forms.dataentry.DataEntryAdapter;
import org.dhis2.data.forms.dataentry.DataEntryArguments;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.dhis2.data.tuples.Trio;
import org.dhis2.databinding.SectionSelectorFragmentBinding;
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity;
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventSectionModel;
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.SectionSelectorAdapter;
import org.dhis2.usescases.general.FragmentGlobalAbstract;
import org.dhis2.utils.ColorUtils;
import org.hisp.dhis.android.core.program.ProgramStageSectionRenderingType;

import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableBoolean;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.Flowable;
import io.reactivex.functions.Consumer;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

import static android.text.TextUtils.isEmpty;

/**
 * QUADRAM. Created by ppajuelo on 19/11/2018.
 */
public class EventCaptureFormFragment extends FragmentGlobalAbstract {
    private static EventCaptureFormFragment instance;
    private EventCaptureActivity activity;
    private SectionSelectorFragmentBinding binding;
    private DataEntryAdapter dataEntryAdapter;
    private SectionSelectorAdapter sectionSelectorAdapter;
    private String currentSection;
    private ObservableBoolean isFirstPosition = new ObservableBoolean(true);
    private ObservableBoolean isLastPosition = new ObservableBoolean(false);
    private FlowableProcessor<RowAction> flowableProcessor;
    private FlowableProcessor<Trio<String, String, Integer>> flowableOptions;

    public static EventCaptureFormFragment getInstance() {
        if (instance == null) {
            instance = new EventCaptureFormFragment();
        }
        return instance;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.activity = (EventCaptureActivity) context;
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.section_selector_fragment, container, false);
        binding.setPresenter(activity.getPresenter());
        binding.sectionSelector.setIsFirstPosition(isFirstPosition);
        binding.sectionSelector.setIsLastPosition(isLastPosition);
        sectionSelectorAdapter = new SectionSelectorAdapter(activity.getPresenter());
        binding.sectionRecycler.setAdapter(sectionSelectorAdapter);
        this.flowableProcessor = PublishProcessor.create();
        this.flowableOptions = PublishProcessor.create();

        binding.sectionSelector.buttonNext.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && binding.sectionSelector.buttonNext.getVisibility() == View.VISIBLE)
                binding.sectionSelector.buttonNext.performClick();
            else
                binding.sectionSelector.buttonEnd.requestFocus();

        });

        binding.sectionSelector.buttonEnd.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && binding.sectionSelector.buttonEnd.getVisibility() == View.VISIBLE)
                binding.sectionSelector.buttonEnd.performClick();

        });

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        activity.getPresenter().initCompletionPercentage(sectionSelectorAdapter.completionPercentage());
        activity.getPresenter().subscribeToSection();
    }

    public void setSectionTitle(DataEntryArguments arguments, FormSectionViewModel formSectionViewModel) {
        this.currentSection = formSectionViewModel.sectionUid();
        binding.currentSectionTitle.sectionTitle.setText(formSectionViewModel.label());
        binding.currentSectionTitle.setSectionUid(formSectionViewModel.sectionUid());
        binding.currentSectionTitle.setOrder(-1);
        binding.currentSectionTitle.setCurrentSection(activity.getPresenter().getCurrentSection());

        setUpRecyclerView(arguments);
    }

    public void setSectionProgress(Integer sectionPosition, Integer sectionTotal) {
        isFirstPosition.set(sectionPosition == 0);
        isLastPosition.set(sectionPosition == sectionTotal - 1);

        binding.sectionSelector.sectionProgress.setProgress((sectionPosition + 1) * 100 / sectionTotal);
        binding.sectionSelector.sectionProgress.getProgressDrawable().setColorFilter(ColorUtils.getPrimaryColor(activity, ColorUtils.ColorType.PRIMARY_LIGHT), PorterDuff.Mode.SRC_IN);
    }

    public void setSingleSection(DataEntryArguments arguments, FormSectionViewModel formSectionViewModel) {
        this.currentSection = "NO_SECTION";
        binding.currentSectionTitle.root.setVisibility(View.GONE);

        setUpRecyclerView(arguments);
    }

    private void setUpRecyclerView(DataEntryArguments arguments) {

        dataEntryAdapter = new DataEntryAdapter(LayoutInflater.from(activity),
                activity.getSupportFragmentManager(), arguments,
                activity.getPresenter().getOrgUnits(),
                new ObservableBoolean(true),
                flowableProcessor,
                flowableOptions);

        binding.formRecycler.setAdapter(dataEntryAdapter);

        RecyclerView.LayoutManager layoutManager;
        if (arguments.renderType() != null && arguments.renderType().equals(ProgramStageSectionRenderingType.MATRIX.name())) {
            layoutManager = new GridLayoutManager(activity, 2);
        } else
            layoutManager = new LinearLayoutManager(activity,
                    RecyclerView.VERTICAL, false);

        binding.formRecycler.setLayoutManager(layoutManager);

    }

    @NonNull
    public Consumer<List<FieldViewModel>> showFields() {
        return updates -> {
            if (currentSection.equals("NO_SECTION") ||
                    updates.get(0).programStageSection().equals(currentSection)) {
                dataEntryAdapter.swap(updates);
                int completedValues = 0;
                HashMap<String, Boolean> fields = new HashMap<>();
                for (FieldViewModel fieldViewModel : updates) {
                    fields.put(fieldViewModel.optionSet() == null ? fieldViewModel.uid() : fieldViewModel.optionSet(), !isEmpty(fieldViewModel.value()));
                }
                for (String key : fields.keySet())
                    if (fields.get(key))
                        completedValues++;
                binding.currentSectionTitle.sectionValues.setText(String.format("%s/%s", completedValues, fields.keySet().size()));
            }
        };

    }

    public Consumer<List<EventSectionModel>> setSectionSelector() {
        return data -> sectionSelectorAdapter.swapData(currentSection, data);
    }

    public FlowableProcessor<RowAction> dataEntryFlowable() {
        return flowableProcessor;
    }


    public void showSectionSelector() {
        binding.sectionRecycler.setVisibility(binding.sectionRecycler.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        binding.currentSectionTitle.root.setVisibility(binding.currentSectionTitle.root.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
    }

    public View getSectionSelector() {
        return binding.sectionSelector.getRoot();
    }

    public Flowable<Trio<String, String, Integer>> optionSetActions() {
        return dataEntryAdapter.asFlowableOption();
    }
}
