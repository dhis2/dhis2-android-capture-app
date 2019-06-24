package org.dhis2.utils

import androidx.core.widget.NestedScrollView
import me.toptas.fancyshowcase.FancyShowCaseQueue
import me.toptas.fancyshowcase.FancyShowCaseView
import me.toptas.fancyshowcase.listener.OnCompleteListener

object HelpManager {
    var help: List<FancyShowCaseView>? = null
    var screen: String? = null
    var scrollView: NestedScrollView? = null

    fun setScreenHelp(name: String, help: List<FancyShowCaseView>) {
        this.help = help
        this.screen = name
    }

    fun showHelp() {
        if (help != null) {
            scrollView?.scrollTo(0,0)
            val queue = FancyShowCaseQueue()
            for (view in help!!) {
                queue.add(view)
            }
            queue.completeListener = object: OnCompleteListener {
                override fun onComplete() {
                    scrollView?.scrollTo(0,0)
                }

            }
            queue.show()
        }
    }

    fun isTutorialReadyForScreen(screen: String): Boolean {
        return this.screen != null && this.screen == screen && help != null && help!!.isNotEmpty()
    }
}