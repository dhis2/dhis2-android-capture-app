package org.dhis2.utils;

import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.View;

import androidx.core.widget.NestedScrollView;

import org.dhis2.R;
import org.dhis2.usescases.general.ActivityGlobalAbstract;

import java.util.ArrayList;
import java.util.List;

import me.toptas.fancyshowcase.FancyShowCaseQueue;
import me.toptas.fancyshowcase.FancyShowCaseView;
import me.toptas.fancyshowcase.FocusShape;
import me.toptas.fancyshowcase.listener.DismissListener;

/**
 * QUADRAM. Created by Administrador on 01/06/2018.
 */

public class HelpManager {

    private static HelpManager instance;
    private List<FancyShowCaseView> help;
    private String screen;
    private NestedScrollView scrollView;

    public enum TutorialName {
        SETTINGS_FRAGMENT, PROGRAM_FRAGMENT, TEI_DASHBOARD, TEI_SEARCH, PROGRAM_EVENT_LIST,
        EVENT_DETAIL, EVENT_SUMMARY, EVENT_INITIAL
    }

    public static HelpManager getInstance() {
        if (instance == null)
            instance = new HelpManager();

        return instance;
    }


    public void setScreenHelp(String screen, List<FancyShowCaseView> steps) {
        help = steps;
        this.screen = screen;
    }

    public void showHelp() {
        if (help != null) {
            if (scrollView != null)
                scrollView.scrollTo(0, 0);

            FancyShowCaseQueue queue = new FancyShowCaseQueue();
            for (FancyShowCaseView view : help) {
                queue.add(view);
            }
            queue.setCompleteListener(() -> {
                if (scrollView != null)
                    scrollView.scrollTo(0, 0);
            });
            queue.show();
        }
    }

    public boolean isTutorialReadyForScreen(String screen) {
        return this.screen != null && this.screen.equals(screen) && help != null && !help.isEmpty();
    }

    public void setScroll(NestedScrollView scrollView) {
        this.scrollView = scrollView;
    }

    public void show(ActivityGlobalAbstract activity, TutorialName name, SparseBooleanArray stepCondition) {
        help = new ArrayList<>();
        switch (name) {
            case PROGRAM_FRAGMENT:
                help = programFragmentTutorial(activity, stepCondition);
                break;
            case SETTINGS_FRAGMENT:
                help = settingsFragmentTutorial(activity);
                break;
            case TEI_DASHBOARD:
                help = teiDashboardTutorial(activity);
                break;
            case TEI_SEARCH:
                help = teiSearchTutorial(activity);
                break;
            case PROGRAM_EVENT_LIST:
                help = programEventListTutorial(activity, stepCondition);
                break;
            case EVENT_SUMMARY:
                help = eventSummaryTutorial(activity);
                break;
            case EVENT_INITIAL:
                help = eventInitialTutorial(activity, stepCondition);
                break;
        }
        if (!help.isEmpty())
            showHelp();
    }

    private List<FancyShowCaseView> eventInitialTutorial(ActivityGlobalAbstract activity, SparseBooleanArray stepCondition) {
        ArrayList<FancyShowCaseView> steps = new ArrayList<>();
        if (stepCondition.get(0)) {
            FancyShowCaseView tuto1 = new FancyShowCaseView.Builder(activity)
                    .title(activity.getString(R.string.tuto_event_initial_new_1))
                    .enableAutoTextPosition()
                    .closeOnTouch(true)
                    .build();
            steps.add(tuto1);

        } else {

            FancyShowCaseView tuto1 = new FancyShowCaseView.Builder(activity)
                    .title(activity.getString(R.string.tuto_event_initial_1))
                    .enableAutoTextPosition()
                    .focusOn(activity.findViewById(R.id.completion))
                    .closeOnTouch(true)
                    .build();
            steps.add(tuto1);

        }
        return steps;
    }

    private List<FancyShowCaseView> eventSummaryTutorial(ActivityGlobalAbstract activity) {
        ArrayList<FancyShowCaseView> steps = new ArrayList<>();
        FancyShowCaseView tuto1 = new FancyShowCaseView.Builder(activity)
                .title(activity.getString(R.string.tuto_event_summary))
                .enableAutoTextPosition()
                .focusOn(activity.findViewById(R.id.action_button))
                .closeOnTouch(true)
                .focusShape(FocusShape.ROUNDED_RECTANGLE)
                .build();
        steps.add(tuto1);
        return steps;
    }

    private List<FancyShowCaseView> programEventListTutorial(ActivityGlobalAbstract activity, SparseBooleanArray stepCondition) {
        ArrayList<FancyShowCaseView> steps = new ArrayList<>();

        FancyShowCaseView tuto1 = new FancyShowCaseView.Builder(activity)
                .title(activity.getString(R.string.tuto_program_event_1))
                .enableAutoTextPosition()
                .closeOnTouch(true)
                .build();
        steps.add(tuto1);

        if (stepCondition.get(2)) {
            FancyShowCaseView tuto2 = new FancyShowCaseView.Builder(activity)
                    .title(activity.getString(R.string.tuto_program_event_2))
                    .enableAutoTextPosition()
                    .focusOn(activity.findViewById(R.id.addEventButton))
                    .closeOnTouch(true)
                    .build();
            steps.add(tuto2);
        }
        return steps;
    }

    private List<FancyShowCaseView> teiSearchTutorial(ActivityGlobalAbstract activity) {
        FancyShowCaseView tuto1 = new FancyShowCaseView.Builder(activity)
                .title(activity.getString(R.string.tuto_search_1_v2))
                .enableAutoTextPosition()
                .closeOnTouch(true)
                .build();
        FancyShowCaseView tuto2 = new FancyShowCaseView.Builder(activity)
                .title(activity.getString(R.string.tuto_search_2))
                .enableAutoTextPosition()
                .focusShape(FocusShape.ROUNDED_RECTANGLE)
                .focusOn(activity.findViewById(R.id.program_spinner))
                .closeOnTouch(true)
                .build();
        FancyShowCaseView tuto3 = new FancyShowCaseView.Builder(activity)
                .title(activity.getString(R.string.tuto_search_3_v2))
                .enableAutoTextPosition()
                .focusOn(activity.findViewById(R.id.enrollmentButton))
                .closeOnTouch(true)
                .build();
        FancyShowCaseView tuto4 = new FancyShowCaseView.Builder(activity)
                .focusOn(activity.findViewById(R.id.clear_button))
                .title(activity.getString(R.string.tuto_search_4_v2))
                .enableAutoTextPosition()
                .closeOnTouch(true)
                .build();

        ArrayList<FancyShowCaseView> steps = new ArrayList<>();
        steps.add(tuto1);
        steps.add(tuto2);
        steps.add(tuto3);
        steps.add(tuto4);
        return steps;
    }

    private List<FancyShowCaseView> teiDashboardTutorial(ActivityGlobalAbstract activity) {
        FancyShowCaseView tuto1 = new FancyShowCaseView.Builder(activity)
                .title(activity.getString(R.string.tuto_dashboard_1))
                .enableAutoTextPosition()
                .closeOnTouch(true)
                .build();
        FancyShowCaseView tuto2 = new FancyShowCaseView.Builder(activity)
                .title(activity.getString(R.string.tuto_dashboard_2))
                .enableAutoTextPosition()
                .focusOn(activity.findViewById(R.id.viewMore))
                .focusShape(FocusShape.ROUNDED_RECTANGLE)
                .titleGravity(Gravity.BOTTOM)
                .closeOnTouch(true)
                .build();
        FancyShowCaseView tuto3 = new FancyShowCaseView.Builder(activity)
                .title(activity.getString(R.string.tuto_dashboard_3))
                .enableAutoTextPosition()
                .focusOn(activity.findViewById(R.id.shareContainer))
                .focusShape(FocusShape.ROUNDED_RECTANGLE)
                .titleGravity(Gravity.BOTTOM)
                .closeOnTouch(true)
                .build();
        FancyShowCaseView tuto4 = new FancyShowCaseView.Builder(activity)
                .title(activity.getString(R.string.tuto_dashboard_4))
                .enableAutoTextPosition()
                .focusOn(activity.findViewById(R.id.follow_up))
                .closeOnTouch(true)
                .build();
        FancyShowCaseView tuto5 = new FancyShowCaseView.Builder(activity)
                .title(activity.getString(R.string.tuto_dashboard_5))
                .enableAutoTextPosition()
                .focusOn(activity.findViewById(R.id.fab))
                .closeOnTouch(true)
                .build();
        FancyShowCaseView tuto6 = new FancyShowCaseView.Builder(activity)
                .title(activity.getString(R.string.tuto_dashboard_6))
                .enableAutoTextPosition()
                .focusOn(activity.findViewById(R.id.tei_recycler))
                .focusShape(FocusShape.ROUNDED_RECTANGLE)
                .titleGravity(Gravity.TOP)
                .closeOnTouch(true)
                .build();
        FancyShowCaseView tuto7 = new FancyShowCaseView.Builder(activity)
                .title(activity.getString(R.string.tuto_dashboard_7))
                .enableAutoTextPosition()
                .focusOn(activity.findViewById(R.id.tab_layout))
                .focusShape(FocusShape.ROUNDED_RECTANGLE)
                .closeOnTouch(true)
                .build();
        FancyShowCaseView tuto8 = new FancyShowCaseView.Builder(activity)
                .title(activity.getString(R.string.tuto_dashboard_8))
                .enableAutoTextPosition()
                .focusOn(activity.findViewById(R.id.program_selector_button))
                .closeOnTouch(true)
                .build();

        ArrayList<FancyShowCaseView> steps = new ArrayList<>();
        steps.add(tuto1);
        steps.add(tuto2);
        steps.add(tuto3);
        steps.add(tuto4);
        steps.add(tuto5);
        steps.add(tuto6);
        steps.add(tuto7);
        steps.add(tuto8);
        return steps;
    }

    private List<FancyShowCaseView> settingsFragmentTutorial(ActivityGlobalAbstract activity) {
        if (scrollView == null)
            throw new NullPointerException("ScrollView must be provided");

        FancyShowCaseView tuto1 = new FancyShowCaseView.Builder(activity)
                .focusOn(activity.findViewById(R.id.settingsItemData))
                .title(activity.getString(R.string.tuto_settings_1))
                .enableAutoTextPosition()
                .closeOnTouch(true)
                .focusShape(FocusShape.ROUNDED_RECTANGLE)
                .build();

        FancyShowCaseView tuto2 = new FancyShowCaseView.Builder(activity)
                .focusOn(activity.findViewById(R.id.settingsItemMeta))
                .title(activity.getString(R.string.tuto_settings_2))
                .enableAutoTextPosition()
                .focusShape(FocusShape.ROUNDED_RECTANGLE)
                .closeOnTouch(true)
                .build();

        FancyShowCaseView tuto3 = new FancyShowCaseView.Builder(activity)
                .focusOn(activity.findViewById(R.id.settingsItemParams))
                .title(activity.getString(R.string.tuto_settings_3))
                .enableAutoTextPosition()
                .focusShape(FocusShape.ROUNDED_RECTANGLE)
                .closeOnTouch(true)
                .dismissListener(new DismissListener() {
                    @Override
                    public void onDismiss(String id) {
                        if (scrollView != null) {
                            scrollView.scrollTo((int) activity.findViewById(R.id.settingsItemValues).getX(),
                                    (int) activity.findViewById(R.id.settingsItemValues).getY());
                        }
                    }

                    @Override
                    public void onSkipped(String id) {
                        // unused
                    }
                })
                .build();

        FancyShowCaseView tuto4 = new FancyShowCaseView.Builder(activity)
                .focusOn(activity.findViewById(R.id.settingsItemValues))
                .title(activity.getString(R.string.tuto_settings_reserved))
                .enableAutoTextPosition()
                .focusShape(FocusShape.ROUNDED_RECTANGLE)
                .closeOnTouch(true)
                .build();

        FancyShowCaseView tuto5 = new FancyShowCaseView.Builder(activity)
                .focusOn(activity.findViewById(R.id.settingsItemLog))
                .title(activity.getString(R.string.tuto_settings_errors))
                .enableAutoTextPosition()
                .focusShape(FocusShape.ROUNDED_RECTANGLE)
                .closeOnTouch(true)
                .build();

        FancyShowCaseView tuto6 = new FancyShowCaseView.Builder(activity)
                .focusOn(activity.findViewById(R.id.settingsItemDeleteData))
                .title(activity.getString(R.string.tuto_settings_reset))
                .enableAutoTextPosition()
                .focusShape(FocusShape.ROUNDED_RECTANGLE)
                .closeOnTouch(true)
                .build();

        FancyShowCaseView tuto7 = new FancyShowCaseView.Builder(activity)
                .focusOn(activity.findViewById(R.id.settingsReset))
                .title(activity.getString(R.string.tuto_settings_4))
                .enableAutoTextPosition()
                .closeOnTouch(true)
                .focusShape(FocusShape.ROUNDED_RECTANGLE)
                .build();


        ArrayList<FancyShowCaseView> steps = new ArrayList<>();
        steps.add(tuto1);
        steps.add(tuto2);
        steps.add(tuto3);
        steps.add(tuto4);
        steps.add(tuto5);
        steps.add(tuto6);
        steps.add(tuto7);

        return steps;
    }

    private List<FancyShowCaseView> programFragmentTutorial(ActivityGlobalAbstract activity, SparseBooleanArray stepCondition) {
        FancyShowCaseView tuto1 = new FancyShowCaseView.Builder(activity)
                .title(activity.getString(R.string.tuto_main_1))
                .enableAutoTextPosition()
                .closeOnTouch(true)
                .build();
        FancyShowCaseView tuto2 = new FancyShowCaseView.Builder(activity)
                .title(activity.getString(R.string.tuto_main_2))
                .enableAutoTextPosition()
                .closeOnTouch(true)
                .build();

        FancyShowCaseView tuto3 = new FancyShowCaseView.Builder(activity)
                .title(activity.getString(R.string.tuto_main_3))
                .enableAutoTextPosition()
                .focusOn(activity.getAbstractActivity().findViewById(R.id.filter))
                .closeOnTouch(true)
                .build();

        FancyShowCaseView tuto4 = new FancyShowCaseView.Builder(activity)
                .title(activity.getString(R.string.tuto_main_4))
                .enableAutoTextPosition()
                .focusShape(FocusShape.ROUNDED_RECTANGLE)
                .closeOnTouch(true)
                .build();

        FancyShowCaseView tuto5 = new FancyShowCaseView.Builder(activity)
                .title(activity.getString(R.string.tuto_main_5))
                .enableAutoTextPosition()
                .focusShape(FocusShape.ROUNDED_RECTANGLE)
                .closeOnTouch(true)
                .build();

        FancyShowCaseView tuto6 = new FancyShowCaseView.Builder(activity)
                .title(activity.getString(R.string.tuto_main_6))
                .enableAutoTextPosition()
                .focusOn(activity.findViewById(R.id.menu))
                .closeOnTouch(true)
                .build();

        ArrayList<FancyShowCaseView> steps = new ArrayList<>();
        steps.add(tuto1);
        steps.add(tuto2);
        steps.add(tuto3);
        steps.add(tuto4);
        steps.add(tuto5);
        steps.add(tuto6);

        if (stepCondition.get(7)) {
            FancyShowCaseView tuto7 = new FancyShowCaseView.Builder(activity)
                    .title(activity.getString(R.string.tuto_main_11))
                    .enableAutoTextPosition()
                    .focusOn(activity.findViewById(R.id.sync_status))
                    .closeOnTouch(true)
                    .build();
            steps.add(tuto7);
        }

        return steps;
    }
}
