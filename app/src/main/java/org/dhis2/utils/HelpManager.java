package org.dhis2.utils;

import java.util.List;

import androidx.core.widget.NestedScrollView;
import me.toptas.fancyshowcase.FancyShowCaseQueue;
import me.toptas.fancyshowcase.FancyShowCaseView;

/**
 * QUADRAM. Created by Administrador on 01/06/2018.
 */

public class HelpManager {

    private static HelpManager instance;
    private List<FancyShowCaseView> help;
    private String screen;
    private NestedScrollView scrollView;

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
            if(scrollView!=null)
                scrollView.scrollTo(0,0);

            FancyShowCaseQueue queue = new FancyShowCaseQueue();
            for (FancyShowCaseView view : help) {
                queue.add(view);
            }
            queue.setCompleteListener(() -> {
                if(scrollView!=null)
                    scrollView.scrollTo(0,0);
            });
            queue.show();
        }
    }

    public boolean isTutorialReadyForScreen(String screen) {
        return this.screen != null && this.screen.equals(screen) && help != null && !help.isEmpty();
    }
    public void setScroll(NestedScrollView scrollView){
        this.scrollView = scrollView;
    }
}
