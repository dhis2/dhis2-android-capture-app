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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.transition.ChangeBounds;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.databinding.BindingMethod;
import androidx.databinding.BindingMethods;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.DividerItemDecoration;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

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
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Flowable;
import timber.log.Timber;

/**
 * QUADRAM. Created by ppajuelo on 02/11/2017 .
 */
@BindingMethods({
        @BindingMethod(type = FloatingActionButton.class, attribute = "app:srcCompat", method = "setImageDrawable")
})
public class SearchTEActivity extends ActivityGlobalAbstract implements SearchTEContractsModule.View {

    ActivitySearchBinding binding;
    @Inject
    SearchTEContractsModule.Presenter presenter;

    private String initialProgram;
    private String tEType;

    private boolean fromRelationship = false;
    private String fromRelationshipTeiUid;
    private boolean backDropActive;
    /**
     *  0 - it is general filter
     *  1 - it is search filter
     *  2 - it was closed
     * */
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
    //---------------------------------------------------------------------------------------------
    //region LIFECYCLE

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

        View rootView = binding.scrollView;

        ViewGroup.LayoutParams layoutParams = rootView.getLayoutParams();
        if(binding.formRecycler.getHeight() > binding.backdropGuide.getHeight()){
            layoutParams.height = binding.backdropGuide.getHeight();
            rootView.setLayoutParams(layoutParams);
        }

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

        binding.executePendingBindings();
        /*binding.appbatlayout.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            float elevationPx = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    7,
                    getResources().getDisplayMetrics()
            );
            boolean isHidden = binding.formRecycler.getHeight() + verticalOffset == 0;
            ViewCompat.setElevation(binding.mainToolbar, isHidden ? elevationPx : 0);
            ViewCompat.setElevation(appBarLayout, isHidden ? 0 : elevationPx);
        });*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.init(this, tEType, initialProgram);
        presenter.initSearch(this);
        registerReceiver(networkReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
        binding.setTotalFilters(FilterManager.getInstance().getTotalFilters());
        binding.setTotalFiltersSearch(FilterManager.getInstance().getTotalSearchTeiFilter());
        filtersAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onPause() {
        presenter.onDestroy();
        unregisterReceiver(networkReceiver);
        super.onPause();
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
    public void updateFiltersSearch(int totalFilters){
        FilterManager.getInstance().setTotalSearchTeiFilter(totalFilters);
        binding.setTotalFiltersSearch(totalFilters);
    }

    //endregion

    //-----------------------------------------------------------------------
    //region SearchForm

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
            liveData.observeForever(searchTeiModels -> {
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
                    animSearchFab(false);
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
                } else if (programs.size() == 1 && pos != 0){
                    presenter.setProgram(programs.get(0));
                }else
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

    private void swipeFilters(boolean general){
        Transition transition = new ChangeBounds();
        transition.setDuration(200);
        TransitionManager.beginDelayedTransition(binding.backdropLayout, transition);
        if(backDropActive && !general && switchOpenClose == 0)
            switchOpenClose = 1;
        else if(backDropActive && general && switchOpenClose == 1)
            switchOpenClose = 0;
        else {
            switchOpenClose = general ? 0 : 1;
            backDropActive = !backDropActive;
        }

        activeFilter(general);
    }

    private void activeFilter(boolean general){
        ConstraintSet initSet = new ConstraintSet();
        initSet.clone(binding.backdropLayout);

        if (backDropActive) {
            initSet.connect(R.id.scrollView, ConstraintSet.TOP, general?R.id.filterLayout:R.id.form_recycler, ConstraintSet.BOTTOM, 0);
            initSet.connect(R.id.messageContainer, ConstraintSet.TOP, general?R.id.filterLayout:R.id.form_recycler, ConstraintSet.BOTTOM, 0);
        }
        else {
            initSet.connect(R.id.scrollView, ConstraintSet.TOP, R.id.backdropGuideTop, ConstraintSet.BOTTOM, 0);
            initSet.connect(R.id.messageContainer, ConstraintSet.TOP, R.id.backdropGuideTop, ConstraintSet.BOTTOM, 0);
        }

        initSet.applyTo(binding.backdropLayout);
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
}
