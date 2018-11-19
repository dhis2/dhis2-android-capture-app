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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.dhis2.R;
import org.dhis2.data.forms.FormSectionViewModel;
import org.dhis2.data.forms.dataentry.DataEntryAdapter;
import org.dhis2.data.forms.dataentry.DataEntryArguments;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.databinding.SectionSelectorFragmentBinding;
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity;
import org.dhis2.usescases.general.FragmentGlobalAbstract;
import org.dhis2.utils.ColorUtils;
import org.hisp.dhis.android.core.program.ProgramStageSectionRenderingType;

import java.util.List;

import io.reactivex.functions.Consumer;

/**
 * QUADRAM. Created by ppajuelo on 19/11/2018.
 */
public class EventCaptureFormFragment extends FragmentGlobalAbstract {
    private static EventCaptureFormFragment instance;
    private EventCaptureActivity activity;
    private SectionSelectorFragmentBinding binding;
    private DataEntryAdapter dataEntryAdapter;

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
        activity.getPresenter().subscribeToSection();

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void setSectionTitle(FormSectionViewModel formSectionViewModel) {
        binding.currentSectionTitle.sectionTitle.setText(formSectionViewModel.label());
        setUpRecyclerView(formSectionViewModel);
    }

    public void setSectionProgress(Integer sectionPosition, Integer sectionTotal) {
        binding.sectionSelector.sectionProgress.setProgress((sectionPosition + 1) * 100 / sectionTotal);
        binding.sectionSelector.sectionProgress.getProgressDrawable().setColorFilter(ColorUtils.getPrimaryColor(activity, ColorUtils.ColorType.PRIMARY_LIGHT), PorterDuff.Mode.SRC_IN);
    }

    private void setUpRecyclerView(FormSectionViewModel formSectionViewModel) {
        DataEntryArguments arguments =
                DataEntryArguments.forEventSection(formSectionViewModel.uid(),
                        formSectionViewModel.sectionUid(),
                        formSectionViewModel.renderType());

        dataEntryAdapter = new DataEntryAdapter(LayoutInflater.from(getActivity()),
                getChildFragmentManager(), arguments,
                activity.getPresenter().getOrgUnits(),
                new ObservableBoolean(true));

        RecyclerView.LayoutManager layoutManager;
        if (formSectionViewModel.renderType() != null && formSectionViewModel.renderType().equals(ProgramStageSectionRenderingType.MATRIX.name())) {
            layoutManager = new GridLayoutManager(getActivity(), 2);
        } else
            layoutManager = new LinearLayoutManager(getActivity(),
                    LinearLayoutManager.VERTICAL, false);

        binding.formRecycler.setAdapter(dataEntryAdapter);
        binding.formRecycler.setLayoutManager(layoutManager);

    }

    @NonNull
    public Consumer<List<FieldViewModel>> showFields() {
        return updates -> dataEntryAdapter.swap(updates);
    }
}
