package org.dhis2.utils;

import static org.dhis2.utils.OrientationUtilsKt.isPortrait;

import android.util.SparseBooleanArray;
import android.view.Gravity;

import androidx.core.widget.NestedScrollView;

import org.dhis2.R;
import org.dhis2.usescases.general.ActivityGlobalAbstract;

import java.util.ArrayList;
import java.util.List;

import me.toptas.fancyshowcase.FancyShowCaseQueue;
import me.toptas.fancyshowcase.FancyShowCaseView;
import me.toptas.fancyshowcase.FocusShape;
import me.toptas.fancyshowcase.listener.DismissListener;

public class HelpManager {

    private static HelpManager instance;
    private List<FancyShowCaseView> help;
    private NestedScrollView scrollView;

    public enum TutorialName {
        PROGRAM_FRAGMENT, TEI_DASHBOARD,
        EVENT_INITIAL
    }

    public static HelpManager getInstance() {
        if (instance == null)
            instance = new HelpManager();

        return instance;
    }

    public boolean isReady() {
        return help != null;
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

    public void setScroll(NestedScrollView scrollView) {
        this.scrollView = scrollView;
    }

    public void show(ActivityGlobalAbstract activity, TutorialName name, SparseBooleanArray stepCondition) {
        help = new ArrayList<>();
        switch (name) {
            case PROGRAM_FRAGMENT -> help = programFragmentTutorial(activity, stepCondition);
            case TEI_DASHBOARD -> help = teiDashboardTutorial(activity);
            case EVENT_INITIAL -> help = eventInitialTutorial(activity, stepCondition);
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

    private List<FancyShowCaseView> teiDashboardTutorial(ActivityGlobalAbstract activity) {
        FancyShowCaseView tuto2 = null;
        FancyShowCaseView tuto1 = new FancyShowCaseView.Builder(activity)
                .title(activity.getString(R.string.tuto_dashboard_1))
                .enableAutoTextPosition()
                .closeOnTouch(true)
                .build();

        if (isPortrait()) {
            tuto2 = new FancyShowCaseView.Builder(activity)
                    .title(activity.getString(R.string.tuto_dashboard_2))
                    .enableAutoTextPosition()
                    .focusOn(activity.findViewById(R.id.editButton))
                    .focusShape(FocusShape.ROUNDED_RECTANGLE)
                    .titleGravity(Gravity.BOTTOM)
                    .closeOnTouch(true)
                    .build();
        }

        FancyShowCaseView tuto3 = new FancyShowCaseView.Builder(activity)
                .title(activity.getString(R.string.tuto_dashboard_6))
                .enableAutoTextPosition()
                .focusOn(activity.findViewById(R.id.tei_recycler))
                .focusShape(FocusShape.ROUNDED_RECTANGLE)
                .titleGravity(Gravity.TOP)
                .closeOnTouch(true)
                .build();
        FancyShowCaseView tuto4 = new FancyShowCaseView.Builder(activity)
                .title(activity.getString(R.string.tuto_dashboard_7))
                .enableAutoTextPosition()
                .focusOn(activity.findViewById(R.id.navigationBar))
                .focusShape(FocusShape.ROUNDED_RECTANGLE)
                .closeOnTouch(true)
                .build();

        ArrayList<FancyShowCaseView> steps = new ArrayList<>();
        steps.add(tuto1);
        if (tuto2 != null) steps.add(tuto2);
        steps.add(tuto3);
        steps.add(tuto4);
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
