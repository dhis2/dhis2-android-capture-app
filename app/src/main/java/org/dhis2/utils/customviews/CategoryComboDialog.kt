package org.dhis2.utils.customviews

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.Window

import com.google.android.material.textfield.TextInputEditText

import org.dhis2.App
import org.dhis2.databinding.CatComboDialogNewBinding
import org.dhis2.databinding.CategorySelectorBinding
import org.hisp.dhis.android.core.category.Category
import org.hisp.dhis.android.core.category.CategoryCombo
import org.hisp.dhis.android.core.category.CategoryOption

import java.util.ArrayList
import java.util.HashMap

import javax.inject.Inject

/**
 * QUADRAM. Created by frodriguez on 5/4/2018.
 */

class CategoryComboDialog(
        private val mContext: Context,
        private val categoryCombo: CategoryCombo,
        val requestCode: Int,
        private val listenerNew: OnCatOptionComboSelected,
        private val title: String? = categoryCombo.displayName()) : AlertDialog(mContext) {

    @Inject
    lateinit var presenter: CategoryComboDialogPresenter

    private lateinit var dialog: AlertDialog

    private val selectedCatOption = HashMap<String, CategoryOption>()

    override fun show() {
        if ((mContext.applicationContext as App).serverComponent() != null) {
            (mContext.applicationContext as App).serverComponent()!!.plus(CategoryComboDialogModule(categoryCombo)).inject(this)
            setDialog()
            dialog.show()
        }
    }

    private fun setDialog() {
        val builder = Builder(mContext)
        val binding = CatComboDialogNewBinding.inflate(LayoutInflater.from(mContext), null, false)
        builder.setCancelable(false)
        builder.setView(binding.root)
        dialog = builder.create()
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        binding.titleDialog.text = title

        binding.categoryLayout.removeAllViews()
        for (category in categoryCombo.categories()!!) {
            val catSelectorBinding = CategorySelectorBinding.inflate(LayoutInflater.from(mContext))
            catSelectorBinding.catCombLayout.hint = category.displayName()
            catSelectorBinding.catCombo.setOnClickListener { openSelector(category, catSelectorBinding.catCombo, catSelectorBinding.root) }

            binding.categoryLayout.addView(catSelectorBinding.root)
        }
    }

    private fun openSelector(category: Category, categoryEditText: TextInputEditText, anchor: View) {
        CategoryOptionPopUp.getInstance()
                .setCategory(category)
                .setOnClick { item ->
                    if (item != null) {
                        selectedCatOption[category.uid()] = item
                    }else {
                        selectedCatOption.remove(category.uid())
                    }
                    categoryEditText.setText(item?.displayName())
                    if (selectedCatOption.size == categoryCombo.categories()!!.size) {
                        listenerNew.onCatOptionComboSelected(
                                presenter!!.getCatOptionCombo(ArrayList(selectedCatOption.values)))
                        dismiss()
                    }
                }
                .show(mContext, anchor)
    }

    override fun dismiss() {
        dialog.dismiss()
    }

    interface OnCatOptionComboSelected {
        fun onCatOptionComboSelected(categoryOptionComboUid: String)
    }
}
