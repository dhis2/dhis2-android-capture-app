package org.dhis2.utils.dialFloatingActionButton

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.marginBottom
import androidx.core.view.updateLayoutParams
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.dhis2.R
import org.dhis2.bindings.dp
import org.dhis2.bindings.hideDialItem
import org.dhis2.bindings.initDialItem
import org.dhis2.bindings.rotate
import org.dhis2.bindings.showDialItem
import org.dhis2.commons.resources.ColorType
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.databinding.DialFabItemBinding
import java.util.LinkedList

const val FAB_ID = 99

class DialFloatingActionButtonLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr) {
    private var isActive = false
    private val dialItemViews = mutableListOf<View>()
    private var fab: FloatingActionButton

    private val colorUtils: ColorUtils = ColorUtils()

    init {
        clipToPadding = false
        clipChildren = false
        initFab(context).also { fab ->
            this.fab = fab
            addView(fab)
            fab.updateLayoutParams<LayoutParams> {
                bottomToBottom = ConstraintSet.PARENT_ID
                endToEnd = ConstraintSet.PARENT_ID
            }
        }
    }

    fun addDialItems(dialItems: List<DialItem>, onDialItemClick: (Int) -> Unit) {
        if (isActive) {
            onFabClick()
        }
        this.dialItemViews.forEach { removeView(it) }
        this.dialItemViews.apply {
            clear()
            addAll(dialItems.map { generateDialItemView(it) })
            forEachIndexed { index, dialItemView ->
                dialItemView.initDialItem(true)
                dialItemView.setOnClickListener {
                    onFabClick()
                    onDialItemClick(dialItemView.id)
                }
                dialItemView.updateLayoutParams<LayoutParams> {
                    setMargins(0, 0, 0, if (index == 0) 16.dp else 0)
                    bottomToTop = if (index == 0) FAB_ID else dialItems[index - 1].id
                    endToEnd = ConstraintSet.PARENT_ID
                }
            }
        }
    }

    fun setFabVisible(isVisible: Boolean) {
        if (isVisible) {
            fab.show()
        } else {
            fab.hide()
        }
    }

    fun isFabVisible() = fab.visibility == View.VISIBLE

    private fun generateDialItemView(dialItem: DialItem): View {
        return DialFabItemBinding.inflate(
            LayoutInflater.from(context),
            this@DialFloatingActionButtonLayout,
            false,
        ).apply {
            dialLabel.text = dialItem.label
            dialIcon.setImageResource(dialItem.icon)
            val colorPrimaryDark = colorUtils.getPrimaryColor(
                context,
                ColorType.PRIMARY_DARK,
            )
            dialIcon.supportImageTintList =
                ColorStateList.valueOf(colorUtils.getContrastColor(colorPrimaryDark))
        }.root.apply {
            id = dialItem.id
            tag = dialItem.label
            addView(this)
        }
    }

    private fun initFab(context: Context) = FloatingActionButton(context).apply {
        id = FAB_ID
        setImageResource(R.drawable.ic_add_accent)
        val colorPrimary = colorUtils.getPrimaryColor(
            context,
            ColorType.PRIMARY,
        )
        supportBackgroundTintList = ColorStateList.valueOf(colorPrimary)
        supportImageTintList = ColorStateList.valueOf(colorUtils.getContrastColor(colorPrimary))
        setOnClickListener { onFabClick() }
        layoutParams =
            LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, 0, 0, 16.dp)
                marginEnd = 16.dp
            }

        size = FloatingActionButton.SIZE_NORMAL
    }

    private fun onFabClick() {
        isActive = fab.rotate(!isActive)
        if (isActive) {
            val showViewQueue = LinkedList(dialItemViews)
            showDialItem(showViewQueue, showViewQueue.poll())
        } else {
            val hideViewQueue = LinkedList(dialItemViews.reversed())
            hideDialItem(hideViewQueue, hideViewQueue.poll())
        }
    }

    private fun showDialItem(viewQueue: LinkedList<View>, nextViewToShow: View?) {
        nextViewToShow?.showDialItem { showDialItem(viewQueue, viewQueue.poll()) }
    }

    private fun hideDialItem(viewQueue: LinkedList<View>, nextViewToHide: View?) {
        nextViewToHide?.hideDialItem { hideDialItem(viewQueue, viewQueue.poll()) }
    }

    fun updateFabMargin(extraBottomMargin: Int) {
        fab.updateLayoutParams<LayoutParams> {
            setMargins(0, 0, 0, fab.marginBottom + extraBottomMargin)
            bottomToBottom = ConstraintSet.PARENT_ID
            endToEnd = ConstraintSet.PARENT_ID
        }
    }
}
