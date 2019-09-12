package org.dhis2.usescases.searchTrackEntity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.transition.ChangeBounds;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.PopupMenu;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.databinding.BindingMethod;
import androidx.databinding.BindingMethods;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.DividerItemDecoration;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.mapbox.geojson.BoundingBox;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.markerview.MarkerViewManager;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.data.forms.dataentry.ProgramAdapter;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.dhis2.data.tuples.Trio;
import org.dhis2.databinding.ActivitySearchBinding;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.usescases.org_unit_selector.OUTreeActivity;
import org.dhis2.usescases.searchTrackEntity.adapters.FormAdapter;
import org.dhis2.usescases.searchTrackEntity.adapters.RelationshipLiveAdapter;
import org.dhis2.usescases.searchTrackEntity.adapters.SearchTeiLiveAdapter;
import org.dhis2.usescases.searchTrackEntity.adapters.SearchTeiModel;
import org.dhis2.utils.ColorUtils;
import org.dhis2.utils.Constants;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.HelpManager;
import org.dhis2.utils.filters.FilterManager;
import org.dhis2.utils.filters.FiltersAdapter;
import org.dhis2.utils.maps.MapLayerDialog;
import org.dhis2.utils.maps.MapLayerManager;
import org.hisp.dhis.android.core.arch.call.D2Progress;
import org.hisp.dhis.android.core.common.FeatureType;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Flowable;
import io.reactivex.functions.Consumer;
import kotlin.Pair;
import timber.log.Timber;

import static com.mapbox.mapboxsdk.style.expressions.Expression.all;
import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.gte;
import static com.mapbox.mapboxsdk.style.expressions.Expression.has;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.expressions.Expression.toNumber;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textField;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textSize;

/**
 * QUADRAM. Created by ppajuelo on 02/11/2017 .
 */
@BindingMethods({
        @BindingMethod(type = FloatingActionButton.class, attribute = "app:srcCompat", method = "setImageDrawable")
})
public class SearchTEActivity extends ActivityGlobalAbstract implements SearchTEContractsModule.View,
        MapboxMap.OnMapClickListener {

    ActivitySearchBinding binding;
    @Inject
    SearchTEContractsModule.Presenter presenter;

    private String initialProgram;
    private String tEType;

    private boolean fromRelationship = false;
    private String fromRelationshipTeiUid;
    private boolean backDropActive;
    /**
     * 0 - it is general filter
     * 1 - it is search filter
     * 2 - it was closed
     */
    private int switchOpenClose = 2;
    private FiltersAdapter filtersAdapter;

    private BroadcastReceiver networkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    };

    ObservableBoolean needsSearch = new ObservableBoolean(true);

    private SearchTeiLiveAdapter liveAdapter;
    private RelationshipLiveAdapter relationshipLiveAdapter;
    private FeatureType featureType;
    private MapboxMap map;
    private MarkerViewManager markerViewManager;
    private SymbolManager symbolManager;
    //---------------------------------------------------------------------------------------------

    //region LIFECYCLE
    @Override
    protected void onStart() {
        super.onStart();
        binding.mapView.onStart();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        tEType = getIntent().getStringExtra("TRACKED_ENTITY_UID");

        ((App) getApplicationContext()).userComponent().plus(new SearchTEModule(tEType)).inject(this);

        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_search);
        binding.setPresenter(presenter);
        initialProgram = getIntent().getStringExtra("PROGRAM_UID");
        binding.setNeedsSearch(needsSearch);
        binding.setTotalFilters(FilterManager.getInstance().getTotalFilters());
        binding.setTotalFiltersSearch(0);

        try {
            fromRelationship = getIntent().getBooleanExtra("FROM_RELATIONSHIP", false);
            fromRelationshipTeiUid = getIntent().getStringExtra("FROM_RELATIONSHIP_TEI");
        } catch (Exception e) {
            Timber.d(e.getMessage());
        }

        if (fromRelationship) {
            relationshipLiveAdapter = new RelationshipLiveAdapter(presenter);
            binding.scrollView.setAdapter(relationshipLiveAdapter);
        } else {
            liveAdapter = new SearchTeiLiveAdapter(presenter);
            binding.scrollView.setAdapter(liveAdapter);
        }

        binding.scrollView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        binding.formRecycler.setAdapter(new FormAdapter(getSupportFragmentManager(), this));

        binding.enrollmentButton.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.requestFocus();
            }
            if (event.getAction() == MotionEvent.ACTION_UP) {
                v.clearFocus();
                v.performClick();
            }
            return true;
        });

        filtersAdapter = new FiltersAdapter();
        try {
            binding.filterLayout.setAdapter(filtersAdapter);

        } catch (Exception e) {
            Timber.e(e);
        }
        binding.mapLayerButton.setOnClickListener(view ->
                new MapLayerDialog(map.getStyle().getImage("ICON_ID"), map.getStyle().getImage("ICON_ENROLLMENT_ID"))
                        .show(getSupportFragmentManager(), MapLayerDialog.class.getSimpleName()));

        binding.executePendingBindings();
        showHideFilter();
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.mapView.onResume();
        presenter.init(this, tEType, initialProgram);
        presenter.initSearch(this);
        registerReceiver(networkReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
        binding.setTotalFilters(FilterManager.getInstance().getTotalFilters());
        binding.setTotalFiltersSearch(FilterManager.getInstance().getTotalSearchTeiFilter());
        filtersAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onPause() {
        binding.mapView.onPause();
        presenter.onDestroy();
        unregisterReceiver(networkReceiver);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        binding.mapView.onDestroy();
        if (markerViewManager != null)
            markerViewManager.onDestroy();
        if (symbolManager != null)
            symbolManager.onDestroy();
        MapLayerManager.Companion.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        binding.mapView.onSaveInstanceState(outState);
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
    public void updateFilters(int totalFilters) {
        binding.setTotalFilters(totalFilters);
        binding.executePendingBindings();
    }

    @Override
    public void updateFiltersSearch(int totalFilters) {
        FilterManager.getInstance().setTotalSearchTeiFilter(totalFilters);
        binding.setTotalFiltersSearch(totalFilters);
    }

    @Override
    public Consumer<FeatureType> featureType() {
        return featureType -> this.featureType = featureType;
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
        popupMenu.getMenuInflater().inflate(R.menu.search_menu, popupMenu.getMenu());
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
        popupMenu.getMenu().getItem(0).setVisible(binding.mapView.getVisibility() == View.GONE && featureType != FeatureType.NONE);
        popupMenu.getMenu().getItem(1).setVisible(binding.scrollView.getVisibility() == View.GONE && featureType != FeatureType.NONE);
        popupMenu.show();
    }

    //endregion

    //-----------------------------------------------------------------------
    //region SearchForm

    private void showMap(boolean showMap) {
        binding.scrollView.setVisibility(showMap ? View.GONE : View.VISIBLE);
        binding.mapView.setVisibility(showMap ? View.VISIBLE : View.GONE);
        binding.mapLayerButton.setVisibility(showMap ? View.VISIBLE : View.GONE);

        if (showMap)
            presenter.getMapData();
    }

    @Override
    public void setForm(List<TrackedEntityAttribute> trackedEntityAttributes, @Nullable Program program, HashMap<String, String> queryData) {

        //TODO: refreshData for recycler

        //Form has been set.
        FormAdapter formAdapter = (FormAdapter) binding.formRecycler.getAdapter();
        formAdapter.setList(trackedEntityAttributes, program, queryData);
    }

    @NonNull
    public Flowable<RowAction> rowActionss() {
        return ((FormAdapter) binding.formRecycler.getAdapter()).asFlowableRA();
    }

    @Override
    public void clearData() {
        binding.progressLayout.setVisibility(View.VISIBLE);
        binding.scrollView.setVisibility(View.GONE);
    }

    @Override
    public void setTutorial() {
        new Handler().postDelayed(() ->
                        HelpManager.getInstance().show(getActivity(),
                                HelpManager.TutorialName.TEI_SEARCH,
                                null),
                500);
    }

    //endregion

    //---------------------------------------------------------------------
    //region TEI LIST

    @Override
    public void setLiveData(LiveData<PagedList<SearchTeiModel>> liveData) {
        if (!fromRelationship) {
            liveData.observe(this, searchTeiModels -> {
                Trio<PagedList<SearchTeiModel>, String, Boolean> data = presenter.getMessage(searchTeiModels);
                if (data.val1().isEmpty()) {
                    binding.messageContainer.setVisibility(View.GONE);
                    binding.scrollView.setVisibility(View.VISIBLE);
                    liveAdapter.submitList(data.val0());
                    binding.progressLayout.setVisibility(View.GONE);
                } else {
                    binding.progressLayout.setVisibility(View.GONE);
                    binding.messageContainer.setVisibility(View.VISIBLE);
                    binding.message.setText(data.val1());
                    binding.scrollView.setVisibility(View.GONE);
                }

                if (!presenter.getQueryData().isEmpty() && data.val2())
                    setFabIcon(false);

            });
        } else {
            liveData.observeForever(searchTeiModels -> {
                Trio<PagedList<SearchTeiModel>, String, Boolean> data = presenter.getMessage(searchTeiModels);
                if (data.val1().isEmpty()) {
                    binding.messageContainer.setVisibility(View.GONE);
                    binding.scrollView.setVisibility(View.VISIBLE);
                    relationshipLiveAdapter.submitList(data.val0());
                    binding.progressLayout.setVisibility(View.GONE);
                } else {
                    binding.progressLayout.setVisibility(View.GONE);
                    binding.messageContainer.setVisibility(View.VISIBLE);
                    binding.message.setText(data.val1());
                }
                if (!presenter.getQueryData().isEmpty() && data.val2())
                    setFabIcon(false);
            });
        }
    }

    @Override
    public void clearList(String uid) {
        this.initialProgram = uid;
        if (uid == null)
            binding.programSpinner.setSelection(0);
    }
    //endregion

    @Override
    public void setPrograms(List<Program> programs) {
        binding.programSpinner.setAdapter(new ProgramAdapter(this, R.layout.spinner_program_layout, R.id.spinner_text, programs, presenter.getTrackedEntityName().displayName()));
        if (initialProgram != null && !initialProgram.isEmpty())
            setInitialProgram(programs);
        else
            binding.programSpinner.setSelection(0);
        try {
            Field popup = Spinner.class.getDeclaredField("mPopup");
            popup.setAccessible(true);

            // Get private mPopup member variable and try cast to ListPopupWindow
            android.widget.ListPopupWindow popupWindow = (android.widget.ListPopupWindow) popup.get(binding.programSpinner);

            // Set popupWindow height to 500px
            popupWindow.setHeight(500);
        } catch (NoClassDefFoundError | ClassCastException | NoSuchFieldException | IllegalAccessException e) {
            // silently fail...
        }
        binding.programSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                if (pos > 0) {
                    Program selectedProgram = (Program) adapterView.getItemAtPosition(pos - 1);
                    setProgramColor(presenter.getProgramColor(selectedProgram.uid()));
                    presenter.setProgram((Program) adapterView.getItemAtPosition(pos - 1));
                } else if (programs.size() == 1 && pos != 0) {
                    presenter.setProgram(programs.get(0));
                } else
                    presenter.setProgram(null);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void setInitialProgram(List<Program> programs) {
        for (int i = 0; i < programs.size(); i++) {
            if (programs.get(i).uid().equals(initialProgram)) {
                binding.programSpinner.setSelection(i + 1);
            }
        }
    }

    @Override
    public void setProgramColor(String color) {
        int programTheme = ColorUtils.getThemeFromColor(color);
        int programColor = ColorUtils.getColorFrom(color, ColorUtils.getPrimaryColor(getContext(), ColorUtils.ColorType.PRIMARY));


        SharedPreferences prefs = getAbstracContext().getSharedPreferences(
                Constants.SHARE_PREFS, Context.MODE_PRIVATE);
        if (programTheme != -1) {
            prefs.edit().putInt(Constants.PROGRAM_THEME, programTheme).apply();
            binding.enrollmentButton.setBackgroundTintList(ColorStateList.valueOf(programColor));
            binding.mainToolbar.setBackgroundColor(programColor);
            binding.backdropLayout.setBackgroundColor(programColor);
        } else {
            prefs.edit().remove(Constants.PROGRAM_THEME).apply();
            int colorPrimary;
            switch (prefs.getInt(Constants.THEME, R.style.AppTheme)) {
                case R.style.AppTheme:
                    colorPrimary = R.color.colorPrimary;
                    break;
                case R.style.RedTheme:
                    colorPrimary = R.color.colorPrimaryRed;
                    break;
                case R.style.OrangeTheme:
                    colorPrimary = R.color.colorPrimaryOrange;
                    break;
                case R.style.GreenTheme:
                    colorPrimary = R.color.colorPrimaryGreen;
                    break;
                default:
                    colorPrimary = R.color.colorPrimary;
                    break;
            }
            binding.enrollmentButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, colorPrimary)));
            binding.mainToolbar.setBackgroundColor(ContextCompat.getColor(this, colorPrimary));
            binding.backdropLayout.setBackgroundColor(ContextCompat.getColor(this, colorPrimary));
        }

        binding.executePendingBindings();
        setTheme(prefs.getInt(Constants.PROGRAM_THEME, prefs.getInt(Constants.THEME, R.style.AppTheme)));

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            TypedValue typedValue = new TypedValue();
            TypedArray a = obtainStyledAttributes(typedValue.data, new int[]{R.attr.colorPrimaryDark});
            int colorToReturn = a.getColor(0, 0);
            a.recycle();
            window.setStatusBarColor(colorToReturn);
        }
    }

    @Override
    public String fromRelationshipTEI() {
        return fromRelationshipTeiUid;
    }

    @Override
    public void setFabIcon(boolean needsSearch) {
        this.needsSearch.set(needsSearch);
        animSearchFab(needsSearch);
    }

    private void animSearchFab(boolean hasQuery) {
        if (hasQuery) {
            binding.enrollmentButton.startAnimation(
                    AnimationUtils.loadAnimation(binding.enrollmentButton.getContext(), R.anim.bounce_animation));
        } else {
            binding.enrollmentButton.clearAnimation();
            hideKeyboard();
        }
    }

    @Override
    public void showHideFilter() {
        binding.filterLayout.setVisibility(View.GONE);
        binding.formRecycler.setVisibility(View.VISIBLE);

        swipeFilters(false);
    }

    @Override
    public void showHideFilterGeneral() {
        binding.filterLayout.setVisibility(View.VISIBLE);
        binding.formRecycler.setVisibility(View.GONE);

        swipeFilters(true);
    }

    private void swipeFilters(boolean general) {
        Transition transition = new ChangeBounds();
        transition.setDuration(200);
        TransitionManager.beginDelayedTransition(binding.backdropLayout, transition);
        if (backDropActive && !general && switchOpenClose == 0)
            switchOpenClose = 1;
        else if (backDropActive && general && switchOpenClose == 1)
            switchOpenClose = 0;
        else {
            switchOpenClose = general ? 0 : 1;
            backDropActive = !backDropActive;
        }
        binding.filterOpen.setVisibility(backDropActive ? View.VISIBLE : View.GONE);

        activeFilter(general);
    }

    private void activeFilter(boolean general) {
        ConstraintSet initSet = new ConstraintSet();
        initSet.clone(binding.backdropLayout);

        if (backDropActive) {
            initSet.connect(R.id.mainLayout, ConstraintSet.TOP, general ? R.id.filterLayout : R.id.form_recycler, ConstraintSet.BOTTOM, 50);
        } else {
            initSet.connect(R.id.mainLayout, ConstraintSet.TOP, R.id.backdropGuideTop, ConstraintSet.BOTTOM, 0);
        }

        initSet.applyTo(binding.backdropLayout);
    }

    @Override
    public void closeFilters() {
        if (switchOpenClose == 0)
            showHideFilterGeneral();
        else
            showHideFilter();
    }

    @Override
    public void clearFilters() {
        if (switchOpenClose == 0) {
            FilterManager.getInstance().clearAllFilters();
            filtersAdapter.notifyDataSetChanged();
        } else
            presenter.onClearClick();

    }

    @Override
    public void showTutorial(boolean shaked) {
        setTutorial();
    }

    @Override
    public void openOrgUnitTreeSelector() {
        Intent ouTreeIntent = new Intent(this, OUTreeActivity.class);
        Bundle bundle = OUTreeActivity.getBundle(initialProgram);
        ouTreeIntent.putExtras(bundle);
        startActivityForResult(ouTreeIntent, FilterManager.OU_TREE);
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

    /*region MAP*/
    @Override
    public Consumer<Pair<HashMap<String, FeatureCollection>, BoundingBox>> setMap() {
        return data -> {
            if (map == null)
                binding.mapView.getMapAsync(mapboxMap -> {
                    map = mapboxMap;
                    if (map.getStyle() == null)
                        map.setStyle(Style.MAPBOX_STREETS, style -> {
                                    MapLayerManager.Companion.init(style, "teis", featureType);
                                    MapLayerManager.Companion.instance().setEnrollmentLayerData(
                                            ColorUtils.getColorFrom(presenter.getProgram().style() != null ? presenter.getProgram().style().color() : null, ColorUtils.getPrimaryColor(getContext(), ColorUtils.ColorType.PRIMARY)),
                                            presenter.getProgram().featureType() != null ? presenter.getProgram().featureType() : FeatureType.NONE
                                    );
                                    MapLayerManager.Companion.instance().showEnrollmentLayer().observe(this, show -> {
                                        if (show)
                                            presenter.getEnrollmentMapData();
                                    });
                                    map.addOnMapClickListener(this);

                                    style.addImage("ICON_ID", presenter.getSymbolIcon());
                                    style.addImage("ICON_ENROLLMENT_ID", presenter.getEnrollmentSymbolIcon());

                                    setSource(style, data.component1());

                                    setLayer(style);

                                    LatLngBounds bounds = LatLngBounds.from(data.component2().north(),
                                            data.component2().east(),
                                            data.component2().south(),
                                            data.component2().west());

                                    map.easeCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50), 1200);

                                    markerViewManager = new MarkerViewManager(binding.mapView, map);
                                    symbolManager = new SymbolManager(binding.mapView, map, style, null,
                                            new GeoJsonOptions().withTolerance(0.4f));

                                    symbolManager.setIconAllowOverlap(true);
                                    symbolManager.setTextAllowOverlap(true);
                                    symbolManager.create(data.component1().get("TEI"));

                                }
                        );
                    else {
                        ((GeoJsonSource) mapboxMap.getStyle().getSource("teis")).setGeoJson(data.component1().get("TEI"));
                        ((GeoJsonSource) mapboxMap.getStyle().getSource("enrollments")).setGeoJson(data.component1().get("ENROLLMENT"));
                        LatLngBounds bounds = LatLngBounds.from(data.component2().north(),
                                data.component2().east(),
                                data.component2().south(),
                                data.component2().west());

                        map.easeCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50), 1200);
                    }
                });
            else {
                ((GeoJsonSource) map.getStyle().getSource("teis")).setGeoJson(data.component1().get("TEI"));
                ((GeoJsonSource) map.getStyle().getSource("enrollments")).setGeoJson(data.component1().get("ENROLLMENT"));
                LatLngBounds bounds = LatLngBounds.from(data.component2().north(),
                        data.component2().east(),
                        data.component2().south(),
                        data.component2().west());

                map.easeCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50), 1200);
            }
        };
    }

    @Override
    public Consumer<D2Progress> downloadProgress() {
        return progress -> Snackbar.make(binding.getRoot(), String.format("Downloading %s", String.valueOf(progress.percentage())) + "%", Snackbar.LENGTH_SHORT);
    }

    private void setSource(Style style, HashMap<String, FeatureCollection> featCollectionMap) {
        style.addSource(new GeoJsonSource("teis", featCollectionMap.get("TEI")/*,new GeoJsonOptions().withCluster(true).withClusterMaxZoom(14).withClusterRadius(30)*/));
        style.addSource(new GeoJsonSource("enrollments", featCollectionMap.get("ENROLLMENT")));
    }

    private void setLayer(Style style) {

        SymbolLayer symbolLayer = new SymbolLayer("POINT_LAYER", "teis").withProperties(
                PropertyFactory.iconImage("ICON_ID"),
                iconAllowOverlap(true)
        );

        style.addLayer(symbolLayer);

        if (featureType != FeatureType.POINT)
            style.addLayerBelow(new FillLayer("POLYGON_LAYER", "teis").withProperties(
                    fillColor(
                            ColorUtils.getPrimaryColorWithAlpha(this, ColorUtils.ColorType.PRIMARY_LIGHT, 150f)
                    )
                    ), "POINT_LAYER"
            );

        /*SymbolLayer countLayer = new SymbolLayer("count", "teis").withProperties(
                textField(Expression.toString(get("point_count"))),
                textSize(12f),
                textColor(Color.BLACK),
                textIgnorePlacement(true),
                textOffset(new Float[]{-1.5f,0f}),
                textAllowOverlap(true)
        );
        style.addLayer(countLayer);*/
    }

    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        PointF pointf = map.getProjection().toScreenLocation(point);
        RectF rectF = new RectF(pointf.x - 10, pointf.y - 10, pointf.x + 10, pointf.y + 10);
        List<Feature> features = map.queryRenderedFeatures(rectF, featureType == FeatureType.POINT ? "POINT_LAYER" : "POLYGON_LAYER");
        if (!features.isEmpty()) {
            presenter.onTEIClick(features.get(0).getStringProperty("teiUid"), false);
            return true;
        }

        return false;
    }

    /*endregion*/
}
