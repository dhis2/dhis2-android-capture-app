package org.dhis2.usescases.programEventDetail;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.transition.ChangeBounds;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.DividerItemDecoration;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.mapbox.mapboxsdk.plugins.markerview.MarkerView;
import com.mapbox.mapboxsdk.plugins.markerview.MarkerViewManager;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions;

import org.dhis2.App;
import org.dhis2.BuildConfig;
import org.dhis2.R;
import org.dhis2.data.tuples.Pair;
import org.dhis2.databinding.ActivityProgramEventDetailBinding;
import org.dhis2.databinding.InfoWindowEventBinding;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.usescases.org_unit_selector.OUTreeActivity;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.HelpManager;
import org.dhis2.utils.filters.FilterManager;
import org.dhis2.utils.filters.FiltersAdapter;
import org.hisp.dhis.android.core.category.CategoryCombo;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;
import org.hisp.dhis.android.core.program.Program;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static org.dhis2.R.layout.activity_program_event_detail;

/**
 * QUADRAM. Created by Cristian on 13/02/2018.
 */

public class ProgramEventDetailActivity extends ActivityGlobalAbstract implements ProgramEventDetailContract.View {

    private ActivityProgramEventDetailBinding binding;

    @Inject
    ProgramEventDetailContract.Presenter presenter;

    private ProgramEventDetailLiveAdapter liveAdapter;
    private boolean backDropActive;
    private FiltersAdapter filtersAdapter;
    private String programUid;
    private MapboxMap map;
    private SymbolManager symbolManager;
    private MarkerViewManager markerViewManager;
    private MarkerView currentMarker;

    public static Bundle getBundle(String programUid, String period, List<Date> dates) {
        Bundle bundle = new Bundle();
        bundle.putString("PROGRAM_UID", programUid);
        bundle.putString("CURRENT_PERIOD", period);
        bundle.putSerializable("DATES", (ArrayList) dates);
        return bundle;
    }

    @Override
    protected void onStart() {
        super.onStart();
        binding.mapView.onStart();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Mapbox.getInstance(this, BuildConfig.MAPBOX_ACCESS_TOKEN);
        ((App) getApplicationContext()).userComponent().plus(new ProgramEventDetailModule(getIntent().getStringExtra("PROGRAM_UID"))).inject(this);
        super.onCreate(savedInstanceState);

        FilterManager.getInstance().clearCatOptCombo();
        FilterManager.getInstance().clearEventStatus();

        this.programUid = getIntent().getStringExtra("PROGRAM_UID");
        binding = DataBindingUtil.setContentView(this, activity_program_event_detail);

        binding.setPresenter(presenter);
        binding.setTotalFilters(FilterManager.getInstance().getTotalFilters());

        liveAdapter = new ProgramEventDetailLiveAdapter(presenter);
        binding.recycler.setAdapter(liveAdapter);
        binding.recycler.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        filtersAdapter = new FiltersAdapter();
        filtersAdapter.addEventStatus();
        try {
            binding.filterLayout.setAdapter(filtersAdapter);

        } catch (Exception e) {
            Timber.e(e);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.init(this);
        binding.mapView.onResume();
        binding.setTotalFilters(FilterManager.getInstance().getTotalFilters());
        filtersAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onPause() {
        presenter.onDettach();
        binding.mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (symbolManager != null)
            symbolManager.onDestroy();
        if (markerViewManager != null)
            markerViewManager.onDestroy();
        binding.mapView.onDestroy();

        FilterManager.getInstance().clearEventStatus();
        FilterManager.getInstance().clearCatOptCombo();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        binding.mapView.onSaveInstanceState(outState);
    }

    @Override
    public void setProgram(Program program) {
        binding.setName(program.displayName());
    }

    @Override
    public void setLiveData(LiveData<PagedList<ProgramEventViewModel>> pagedListLiveData) {
        pagedListLiveData.observe(this, pagedList -> {
            binding.programProgress.setVisibility(View.GONE);
            liveAdapter.submitList(pagedList, () -> {
                if (binding.recycler.getAdapter() != null && binding.recycler.getAdapter().getItemCount() == 0) {
                    binding.emptyTeis.setVisibility(View.VISIBLE);
                    binding.recycler.setVisibility(View.GONE);
                } else {
                    binding.emptyTeis.setVisibility(View.GONE);
                    binding.recycler.setVisibility(View.VISIBLE);
                }
            });

        });

    }

    @Override
    public void setOptionComboAccess(Boolean canCreateEvent) {
        switch (binding.addEventButton.getVisibility()) {
            case View.VISIBLE:
                binding.addEventButton.setVisibility(canCreateEvent ? View.VISIBLE : View.GONE);
                break;
            case View.GONE:
                binding.addEventButton.setVisibility(View.GONE);
                break;
        }

    }

    @Override
    public void renderError(String message) {
        if (getActivity() != null)
            new AlertDialog.Builder(getActivity())
                    .setPositiveButton(android.R.string.ok, null)
                    .setTitle(getString(R.string.error))
                    .setMessage(message)
                    .show();
    }

    @Override
    public void showHideFilter() {
        Transition transition = new ChangeBounds();
        transition.setDuration(200);
        TransitionManager.beginDelayedTransition(binding.backdropLayout, transition);
        backDropActive = !backDropActive;
        ConstraintSet initSet = new ConstraintSet();
        initSet.clone(binding.backdropLayout);
        binding.filterOpen.setVisibility(backDropActive ? View.VISIBLE : View.GONE);

        if (backDropActive) {
            initSet.connect(R.id.eventsLayout, ConstraintSet.TOP, R.id.filterLayout, ConstraintSet.BOTTOM, 50);
        }
        else {
            initSet.connect(R.id.eventsLayout, ConstraintSet.TOP, R.id.backdropGuideTop, ConstraintSet.BOTTOM, 0);
        }

        initSet.applyTo(binding.backdropLayout);
    }

    @Override
    public void clearFilters() {
        filtersAdapter.notifyDataSetChanged();
    }

    @Override
    public void setWritePermission(Boolean canWrite) {
        switch (binding.addEventButton.getVisibility()) {
            case View.VISIBLE:
                binding.addEventButton.setVisibility(canWrite ? View.VISIBLE : View.GONE);
                break;
            case View.GONE:
                binding.addEventButton.setVisibility(View.GONE);
                break;
        }
        if (binding.addEventButton.getVisibility() == View.VISIBLE) {
            binding.emptyTeis.setText(R.string.empty_tei_add);
        } else {
            binding.emptyTeis.setText(R.string.empty_tei_no_add);
        }
    }

    @Override
    public void setTutorial() {
        new Handler().postDelayed(() -> {
            SparseBooleanArray stepConditions = new SparseBooleanArray();
            stepConditions.put(2, findViewById(R.id.addEventButton).getVisibility() == View.VISIBLE);
            HelpManager.getInstance().show(getActivity(), HelpManager.TutorialName.PROGRAM_EVENT_LIST,
                    stepConditions);

        }, 500);
    }

    @Override
    public void updateFilters(int totalFilters) {
        binding.setTotalFilters(totalFilters);
        binding.executePendingBindings();
    }

    @Override
    public void setCatOptionComboFilter(Pair<CategoryCombo, List<CategoryOptionCombo>> categoryOptionCombos) {
        filtersAdapter.addCatOptCombFilter(categoryOptionCombos);
    }

    @Override
    public void showPeriodRequest(FilterManager.PeriodRequest periodRequest) {
        if (periodRequest == FilterManager.PeriodRequest.FROM_TO) {
            DateUtils.getInstance().showFromToSelector(this, FilterManager.getInstance()::addPeriod);
        } else {
            DateUtils.getInstance().showPeriodDialog(this, datePeriods -> {
                        FilterManager.getInstance().addPeriod(datePeriods);
                    },
                    true);
        }
    }

    @Override
    public void openOrgUnitTreeSelector() {
        Intent ouTreeIntent = new Intent(this, OUTreeActivity.class);
        Bundle bundle = OUTreeActivity.getBundle(programUid);
        ouTreeIntent.putExtras(bundle);
        startActivityForResult(ouTreeIntent, FilterManager.OU_TREE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == FilterManager.OU_TREE && resultCode == Activity.RESULT_OK) {
            filtersAdapter.notifyDataSetChanged();
            updateFilters(FilterManager.getInstance().getTotalFilters());
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void showTutorial(boolean shaked) {
        setTutorial();
    }

    @Override
    public void setMap(List<SymbolOptions> options) {
        binding.mapView.getMapAsync(mapboxMap -> {
            map = mapboxMap;
            if (map.getStyle() == null)
                map.setStyle(Style.MAPBOX_STREETS, style -> {

                            style.addImage("ICON_ID", BitmapFactory.decodeResource(getResources(), R.drawable.mapbox_marker_icon_default));

                            setLayer(style);

                            markerViewManager = new MarkerViewManager(binding.mapView, map);
                            symbolManager = new SymbolManager(binding.mapView, map, style, null,
                                    new GeoJsonOptions().withTolerance(0.4f));

                            symbolManager.setIconAllowOverlap(true);
                            symbolManager.setTextAllowOverlap(true);
                            symbolManager.create(options);

                            symbolManager.addClickListener(symbol -> {
                                presenter.getEventInfo(symbol.getTextField(), symbol.getLatLng());
                            });

                        }
                );
            else
                symbolManager.create(options);
        });
    }

    @Override
    public void setEventInfo(Pair<ProgramEventViewModel, LatLng> eventInfo) {
        if (currentMarker != null)
            markerViewManager.removeMarker(currentMarker);
        InfoWindowEventBinding binding = InfoWindowEventBinding.inflate(LayoutInflater.from(this));
        binding.setEvent(eventInfo.val0());
        binding.setPresenter(presenter);
        View view = binding.getRoot();
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        currentMarker = new MarkerView(eventInfo.val1(), view);
        markerViewManager.addMarker(currentMarker);
    }

    private void setLayer(Style style) {
        SymbolLayer symbolLayer = new SymbolLayer("LAYER_ID", "events").withProperties(
                PropertyFactory.iconImage("ICON_ID"),
                iconAllowOverlap(true),
                iconOffset(new Float[]{0f, -9f})
        );
        style.addLayer(symbolLayer);
    }

    @Override
    public void showMoreOptions(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view, Gravity.BOTTOM);
        try {
            Field[] fields = popupMenu.getClass().getDeclaredFields();
            for (Field field : fields) {
                if ("mPopup".equals(field.getName())) {
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(popupMenu);
                    Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                    Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                    setForceIcons.invoke(menuPopupHelper, true);
                    break;
                }
            }
        } catch (Exception e) {
            Timber.e(e);
        }
        popupMenu.getMenuInflater().inflate(R.menu.event_list_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.showHelp:
                    showTutorial(false);
                    break;
                case R.id.menu_list:
                    showMap(false);
                    break;
                case R.id.menu_map:
                    showMap(true);
                    break;
                default:
                    break;
            }
            return false;
        });
        popupMenu.getMenu().getItem(0).setVisible(binding.mapView.getVisibility() == View.GONE);
        popupMenu.getMenu().getItem(1).setVisible(binding.recycler.getVisibility() == View.GONE);
        popupMenu.show();
    }

    private void showMap(boolean showMap) {
        binding.recycler.setVisibility(showMap ? View.GONE : View.VISIBLE);
        binding.mapView.setVisibility(showMap ? View.VISIBLE : View.GONE);

        if (showMap)
            presenter.getMapData();
    }
}