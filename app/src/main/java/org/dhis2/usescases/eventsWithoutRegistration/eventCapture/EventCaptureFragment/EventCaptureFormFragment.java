package org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureFragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.DataEntryAdapter;
import org.dhis2.data.forms.dataentry.DataEntryArguments;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.dhis2.data.tuples.Trio;
import org.dhis2.databinding.SectionSelectorFragmentBinding;
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity;
import org.dhis2.usescases.general.FragmentGlobalAbstract;
import org.dhis2.utils.Constants;
import org.dhis2.utils.recyclers.StickyHeaderItemDecoration;
import org.hisp.dhis.android.core.program.ProgramStageSectionRenderingType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

import static android.text.TextUtils.isEmpty;

public class EventCaptureFormFragment extends FragmentGlobalAbstract implements EventCaptureFormView {

    @Inject
    EventCaptureFormPresenter presenter;

    private EventCaptureActivity activity;
    private SectionSelectorFragmentBinding binding;
    private DataEntryAdapter dataEntryAdapter;
    private FlowableProcessor<RowAction> flowableProcessor;
    private FlowableProcessor<String> sectionProcessor;
    private FlowableProcessor<Trio<String, String, Integer>> flowableOptions;

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
        this.flowableProcessor = PublishProcessor.create();
        this.sectionProcessor = PublishProcessor.create();
        this.flowableOptions = PublishProcessor.create();

        binding.actionButton.setOnClickListener(view -> {
            presenter.onActionButtonClick();
        });

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
    public void showFields(@NonNull List<FieldViewModel> updates, @NonNull String lastFocusItem) {

        if (!isEmpty(lastFocusItem)) {
            dataEntryAdapter.setLastFocusItem(lastFocusItem);
        }

        if (dataEntryAdapter == null) {
            createDataEntry();
        }

        LinearLayoutManager myLayoutManager = (LinearLayoutManager) binding.formRecycler.getLayoutManager();
        if (myLayoutManager == null) return;

        int myFirstPositionIndex = myLayoutManager.findFirstVisibleItemPosition();
        View myFirstPositionView = myLayoutManager.findViewByPosition(myFirstPositionIndex);
        int offset = 0;
        if (myFirstPositionView != null) {
            offset = myFirstPositionView.getTop();
        }

        if (dataEntryAdapter == null) {
            createDataEntry();
        }

        dataEntryAdapter.swap(updates, () -> { });

        myLayoutManager.scrollToPositionWithOffset(myFirstPositionIndex, offset);
    }

    @Override
    public FlowableProcessor<RowAction> dataEntryFlowable() {
        return flowableProcessor;
    }

    @Override
    public FlowableProcessor<String> sectionSelectorFlowable() {
        return sectionProcessor;
    }

    private void createDataEntry() {

        dataEntryAdapter = new DataEntryAdapter(LayoutInflater.from(activity),
                activity.getSupportFragmentManager(),
                DataEntryArguments.forEvent("", ProgramStageSectionRenderingType.LISTING.name()),
                flowableProcessor,
                sectionProcessor,
                flowableOptions);

        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return dataEntryAdapter.getItemSpan(position);
            }
        });

        binding.formRecycler.setLayoutManager(layoutManager);
        binding.formRecycler.addItemDecoration(
                new StickyHeaderItemDecoration(binding.formRecycler,
                        false, itemPosition -> itemPosition >= 0 &&
                                itemPosition < dataEntryAdapter.getItemCount() &&
                                dataEntryAdapter.getItemViewType(itemPosition) == dataEntryAdapter.sectionViewType()
                )
        );
        binding.formRecycler.setAdapter(dataEntryAdapter);

        binding.formRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    dataEntryAdapter.setLastFocusItem(null);
                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(recyclerView.getWindowToken(), 0);
                    binding.dummyFocusView.requestFocus();
                }

            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            binding.formRecycler.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                checkLastItem();
            });
        } else {
            binding.formRecycler.setOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    checkLastItem();
                }
            });
        }
    }

    private void checkLastItem() {
        GridLayoutManager layoutManager = (GridLayoutManager) binding.formRecycler.getLayoutManager();
        int lastVisiblePosition = layoutManager.findLastVisibleItemPosition();
        boolean shouldShowFab =
                lastVisiblePosition == dataEntryAdapter.getItemCount() - 1 ||
                        dataEntryAdapter.getItemViewType(lastVisiblePosition) == 17;
        animateFabButton(shouldShowFab);
    }

    private void animateFabButton(boolean sectionIsVisible) {
        binding.actionButton.animate()
                .translationX(sectionIsVisible ? 0 : 1000)
                .setDuration(500)
                .start();
    }
}
