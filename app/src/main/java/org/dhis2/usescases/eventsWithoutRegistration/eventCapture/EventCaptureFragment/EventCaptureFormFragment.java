package org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureFragment;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableBoolean;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.dhis2.R;
import org.dhis2.data.forms.FormSectionViewModel;
import org.dhis2.data.forms.dataentry.DataEntryAdapter;
import org.dhis2.data.forms.dataentry.DataEntryArguments;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.dhis2.databinding.SectionSelectorFragmentBinding;
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity;
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventSectionModel;
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.SectionSelectorAdapter;
import org.dhis2.usescases.general.FragmentGlobalAbstract;
import org.dhis2.utils.ColorUtils;
import org.dhis2.utils.RulesActionCallbacks;
import org.hisp.dhis.android.core.program.ProgramStageSectionRenderingType;
import org.hisp.dhis.rules.models.RuleActionShowError;

import java.util.List;

import io.reactivex.functions.Consumer;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

/**
 * QUADRAM. Created by ppajuelo on 19/11/2018.
 */
public class EventCaptureFormFragment extends FragmentGlobalAbstract implements RulesActionCallbacks {
    private static EventCaptureFormFragment instance;
    private EventCaptureActivity activity;
    private SectionSelectorFragmentBinding binding;
    private DataEntryAdapter dataEntryAdapter;
    private SectionSelectorAdapter sectionSelectorAdapter;
    private String currentSection;
    private ObservableBoolean isFirstPosition = new ObservableBoolean(true);
    private ObservableBoolean isLastPosition = new ObservableBoolean(false);
    private FlowableProcessor<RowAction> flowableProcessor;

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
        activity.getPresenter().subscribeToSection();
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        activity.getPresenter().initCompletionPercentage(sectionSelectorAdapter.completionPercentage());
    }

    public void setSectionTitle(DataEntryArguments arguments, FormSectionViewModel formSectionViewModel) {
        this.currentSection = formSectionViewModel.sectionUid();
        binding.currentSectionTitle.sectionTitle.setText(formSectionViewModel.label());
        binding.currentSectionTitle.setIsCurrentSection(new ObservableBoolean(true));
        binding.currentSectionTitle.setOrder(-1);

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

        dataEntryAdapter = new DataEntryAdapter(LayoutInflater.from(getActivity()),
                getChildFragmentManager(), arguments,
                activity.getPresenter().getOrgUnits(),
                new ObservableBoolean(true),
                flowableProcessor);

        binding.formRecycler.setAdapter(dataEntryAdapter);

        RecyclerView.LayoutManager layoutManager;
        if (arguments.renderType() != null && arguments.renderType().equals(ProgramStageSectionRenderingType.MATRIX.name())) {
            layoutManager = new GridLayoutManager(getActivity(), 2);
        } else
            layoutManager = new LinearLayoutManager(getActivity(),
                    LinearLayoutManager.VERTICAL, false);

        binding.formRecycler.setLayoutManager(layoutManager);
    }

    @NonNull
    public Consumer<List<FieldViewModel>> showFields() {
        return updates -> {
            dataEntryAdapter.swap(updates);
            int completedValues = 0;
            for (FieldViewModel fieldViewModel : updates)
                if (!TextUtils.isEmpty(fieldViewModel.value()))
                    completedValues++;

            binding.currentSectionTitle.sectionValues.setText(String.format("%s/%s", completedValues, updates.size()));
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

    @Override
    public void setShowError(@NonNull RuleActionShowError showError) {

    }

    @Override
    public void unsupportedRuleAction() {

    }

    @Override
    public void save(@NonNull String uid, @Nullable String value) {

    }

    @Override
    public void setDisplayKeyValue(String label, String value) {

    }

    @Override
    public void sethideSection(String sectionUid) {

    }

    @Override
    public void setMessageOnComplete(String content, boolean canComplete) {

    }

    @Override
    public void setHideProgramStage(String programStageUid) {

    }

}
