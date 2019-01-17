package org.dhis2.usescases.searchTrackEntity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import androidx.databinding.BindingMethod;
import androidx.databinding.BindingMethods;
import androidx.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Spinner;

import org.dhis2.App;
import org.dhis2.BuildConfig;
import org.dhis2.R;
import org.dhis2.data.forms.dataentry.ProgramAdapter;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.dhis2.data.metadata.MetadataRepository;
import org.dhis2.data.tuples.Pair;
import org.dhis2.data.tuples.Trio;
import org.dhis2.databinding.ActivitySearchBinding;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.usescases.searchTrackEntity.adapters.FormAdapter;
import org.dhis2.usescases.searchTrackEntity.adapters.SearchRelationshipAdapter;
import org.dhis2.usescases.searchTrackEntity.adapters.SearchTEAdapter;
import org.dhis2.usescases.searchTrackEntity.adapters.SearchTeiModel;
import org.dhis2.utils.ColorUtils;
import org.dhis2.utils.Constants;
import org.dhis2.utils.custom_views.OptionSetDialog;
import org.dhis2.utils.EndlessRecyclerViewScrollListener;
import org.dhis2.utils.HelpManager;
import org.dhis2.utils.NetworkUtils;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeModel;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Flowable;
import io.reactivex.functions.Consumer;
import io.reactivex.processors.PublishProcessor;
import me.toptas.fancyshowcase.FancyShowCaseView;
import me.toptas.fancyshowcase.FocusShape;
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
    @Inject
    MetadataRepository metadataRepository;

    private String initialProgram;
    private String tEType;
    private SearchTEAdapter searchTEAdapter;
    private SearchRelationshipAdapter searchRelationshipAdapter;

    private boolean fromRelationship = false;
    private String fromRelationshipTeiUid;

    private BroadcastReceiver networkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    };
    private ProgramModel program;
    private static PublishProcessor<Integer> onlinePagerProcessor;
    private PublishProcessor<Integer> offlinePagerProcessor;
    private EndlessRecyclerViewScrollListener endlessRecyclerViewScrollListener;
    //---------------------------------------------------------------------------------------------
    //region LIFECYCLE

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        tEType = getIntent().getStringExtra("TRACKED_ENTITY_UID");

        ((App) getApplicationContext()).userComponent().plus(new SearchTEModule(tEType)).inject(this);

        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_search);
        binding.setPresenter(presenter);
        initialProgram = getIntent().getStringExtra("PROGRAM_UID");

        try {
            fromRelationship = getIntent().getBooleanExtra("FROM_RELATIONSHIP", false);
            fromRelationshipTeiUid = getIntent().getStringExtra("FROM_RELATIONSHIP_TEI");
        } catch (Exception e) {
            Timber.d(e.getMessage());
        }

        if (fromRelationship) {
            searchRelationshipAdapter = new SearchRelationshipAdapter(presenter, metadataRepository, false);
            binding.scrollView.setAdapter(searchRelationshipAdapter);
        } else {
            searchTEAdapter = new SearchTEAdapter(presenter, metadataRepository);
            binding.scrollView.setAdapter(searchTEAdapter);
        }

        binding.scrollView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        binding.formRecycler.setAdapter(new FormAdapter(getSupportFragmentManager(), LayoutInflater.from(this), presenter.getOrgUnits(), this));

        onlinePagerProcessor = PublishProcessor.create();
        offlinePagerProcessor = PublishProcessor.create();
        endlessRecyclerViewScrollListener = new EndlessRecyclerViewScrollListener(binding.scrollView.getLayoutManager(), 2,
                NetworkUtils.isOnline(this) ? 1 : 0) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                if (NetworkUtils.isOnline(SearchTEActivity.this))
                    onlinePagerProcessor.onNext(page);
                else
                    offlinePagerProcessor.onNext(page);
            }
        };
        binding.scrollView.addOnScrollListener(endlessRecyclerViewScrollListener);

    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.init(this, tEType, initialProgram);
        registerReceiver(networkReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
    }


    @Override
    protected void onPause() {
        presenter.onDestroy();
        unregisterReceiver(networkReceiver);
        super.onPause();
    }

    //endregion

    //-----------------------------------------------------------------------
    //region SearchForm

    @Override
    public void setForm(List<TrackedEntityAttributeModel> trackedEntityAttributeModels, @Nullable ProgramModel program, HashMap<String, String> queryData) {

        this.program = program;

        if (!HelpManager.getInstance().isTutorialReadyForScreen(getClass().getName()))
            setTutorial();

        //TODO: refreshData for recycler

        //Form has been set.
        FormAdapter formAdapter = (FormAdapter) binding.formRecycler.getAdapter();
        formAdapter.setList(trackedEntityAttributeModels, program, queryData);
    }

    @NonNull
    public Flowable<RowAction> rowActionss() {
        return ((FormAdapter) binding.formRecycler.getAdapter()).asFlowableRA();
    }

    @Override
    public Flowable<Trio<String, String, Integer>> optionSetActions(){
        return ((FormAdapter) binding.formRecycler.getAdapter()).asFlowableOption();
    }

    @Override
    public Flowable<Integer> onlinePage() {
        if (program != null)
            return onlinePagerProcessor;
        else
            return Flowable.just(-1);
    }

    @Override
    public Flowable<Integer> offlinePage() {
        return offlinePagerProcessor;
    }

    @Override
    public void clearData() {
        binding.progressLayout.setVisibility(View.VISIBLE);
        endlessRecyclerViewScrollListener.resetState(NetworkUtils.isOnline(this) ? 1 : 0);
        if (fromRelationship)
            searchRelationshipAdapter.clear();
        else
            searchTEAdapter.clear();
    }

    @Override
    public void setTutorial() {
        SharedPreferences prefs = getAbstracContext().getSharedPreferences(
                "org.dhis2", Context.MODE_PRIVATE);

        new Handler().postDelayed(() -> {
            FancyShowCaseView tuto1 = new FancyShowCaseView.Builder(getAbstractActivity())
                    .title(getString(R.string.tuto_search_1))
                    .closeOnTouch(true)
                    .build();
            FancyShowCaseView tuto2 = new FancyShowCaseView.Builder(getAbstractActivity())
                    .title(getString(R.string.tuto_search_2))
                    .focusShape(FocusShape.ROUNDED_RECTANGLE)
                    .focusOn(getAbstractActivity().findViewById(R.id.program_spinner))
                    .closeOnTouch(true)
                    .build();
            FancyShowCaseView tuto3 = new FancyShowCaseView.Builder(getAbstractActivity())
                    .title(getString(R.string.tuto_search_3))
                    .focusOn(getAbstractActivity().findViewById(R.id.enrollmentButton))
                    .closeOnTouch(true)
                    .build();
            FancyShowCaseView tuto4 = new FancyShowCaseView.Builder(getAbstractActivity())
                    .focusOn(getAbstractActivity().findViewById(R.id.clear_button))
                    .title(getString(R.string.tuto_search_4))
                    .closeOnTouch(true)
                    .build();

            ArrayList<FancyShowCaseView> steps = new ArrayList<>();
            steps.add(tuto1);
            steps.add(tuto2);
            steps.add(tuto3);
            steps.add(tuto4);

            HelpManager.getInstance().setScreenHelp(getClass().getName(), steps);

            if (!prefs.getBoolean("TUTO_SEARCH_SHOWN", false) && !BuildConfig.DEBUG) {
                HelpManager.getInstance().showHelp();/* getAbstractActivity().fancyShowCaseQueue.show();*/
                prefs.edit().putBoolean("TUTO_SEARCH_SHOWN", true).apply();
            }

        }, 500);
    }

    //endregion

    //---------------------------------------------------------------------
    //region TEI LIST

    @Override
    public Consumer<Pair<List<SearchTeiModel>, String>> swapTeiListData() {
        return data -> {
            binding.progressLayout.setVisibility(View.GONE);
            if (!fromRelationship) {
                if (data.val1().isEmpty()) {
                    binding.messageContainer.setVisibility(View.GONE);
                    searchTEAdapter.setTeis(data.val0());
                } else if (searchTEAdapter.getItemCount() == 0) {
                    binding.messageContainer.setVisibility(View.VISIBLE);
                    binding.message.setText(data.val1());
                }


            } else {
                if (data.val1().isEmpty()) {
                    binding.messageContainer.setVisibility(View.GONE);
                    searchRelationshipAdapter.setItems(data.val0());
                } else if (searchTEAdapter.getItemCount() == 0) {
                    binding.messageContainer.setVisibility(View.VISIBLE);
                    binding.message.setText(data.val1());
                }
            }
        };
    }

    @Override
    public void clearList(String uid) {
        this.initialProgram = uid;

        if (uid == null)
            binding.programSpinner.setSelection(0);
    }
    //endregion

    @Override
    public void setPrograms(List<ProgramModel> programModels) {
        binding.programSpinner.setAdapter(new ProgramAdapter(this, R.layout.spinner_program_layout, R.id.spinner_text, programModels, presenter.getTrackedEntityName().displayName()));
        if (initialProgram != null && !initialProgram.isEmpty())
            setInitialProgram(programModels);
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
                    ProgramModel selectedProgram = (ProgramModel) adapterView.getItemAtPosition(pos - 1);
                    setProgramColor(presenter.getProgramColor(selectedProgram.uid()));
                    presenter.setProgram((ProgramModel) adapterView.getItemAtPosition(pos - 1));
                    binding.enrollmentButton.setVisibility(View.VISIBLE);
                } else {
                    presenter.setProgram(null);
                    binding.enrollmentButton.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void setInitialProgram(List<ProgramModel> programModels) {
        for (int i = 0; i < programModels.size(); i++) {
            if (programModels.get(i).uid().equals(initialProgram)) {
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
            binding.appbatlayout.setBackgroundColor(programColor);
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
            binding.appbatlayout.setBackgroundColor(ContextCompat.getColor(this, colorPrimary));
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
    public void setListOptions(List<String> options) {
        OptionSetDialog.newInstance().setOptions(options);
    }
}
