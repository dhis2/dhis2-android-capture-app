package org.dhis2.usescases.eventsWithoutRegistration.eventCapture.eventCaptureFragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import org.dhis2.Bindings.ViewExtensionsKt;
import org.dhis2.R;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.databinding.SectionSelectorFragmentBinding;
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity;
import org.dhis2.usescases.general.FragmentGlobalAbstract;
import org.dhis2.utils.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import javax.inject.Inject;

import kotlin.Unit;

public class EventCaptureFormFragment extends FragmentGlobalAbstract implements EventCaptureFormView {

    @Inject
    EventCaptureFormPresenter presenter;

    private EventCaptureActivity activity;
    private SectionSelectorFragmentBinding binding;

    public static EventCaptureFormFragment newInstance(String eventUid) {
        EventCaptureFormFragment fragment = new EventCaptureFormFragment();
        Bundle args = new Bundle();
        args.putString(Constants.EVENT_UID, eventUid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        this.activity = (EventCaptureActivity) context;
        activity.eventCaptureComponent.plus(
                new EventCaptureFormModule(
                        this,
                        getArguments().getString(Constants.EVENT_UID))
        ).inject(this);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.section_selector_fragment, container, false);
        binding.setPresenter(activity.getPresenter());
        binding.actionButton.setOnClickListener(view -> {
            ViewExtensionsKt.closeKeyboard(view);
            performSaveClick();
        });
        activity.subscribe(binding.formView);
        binding.formView.setOnNewGeometryValue((fieldUid, geometry) -> {
            presenter.saveValue(fieldUid, geometry.coordinates());
            return Unit.INSTANCE;
        });
        binding.formView.setScrollCallback(isSectionVisible -> {
            animateFabButton(isSectionVisible);
            return Unit.INSTANCE;
        });
        binding.formView.setOnLocationRequest(fieldUid -> {
            activity.requestCurrentLocation(geometry -> {
                binding.formView.getOnNewGeometryValue().invoke(fieldUid, geometry);
                return Unit.INSTANCE;
            });
            return Unit.INSTANCE;
        });
        binding.formView.setOnMapRequest((featureType, initialData) -> {
            activity.requestMapLocation(featureType, initialData);
            return Unit.INSTANCE;
        });
        binding.formView.init(this);

        presenter.init();

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.onDetach();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void showFields(@NonNull List<FieldViewModel> updates) {
        binding.formView.render(updates);
    }

    private void animateFabButton(boolean sectionIsVisible) {
        int translationX = 1000;
        if (sectionIsVisible) translationX = 0;

        binding.actionButton.animate().translationX(translationX).setDuration(500).start();
    }

    @Override
    public void performSaveClick() {
        if (activity.getCurrentFocus() instanceof EditText) {
            presenter.setFinishing();
            activity.getCurrentFocus().clearFocus();
        } else {
            presenter.onActionButtonClick();
        }
    }
}