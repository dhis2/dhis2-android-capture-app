package org.dhis2.android.rtsm.ui.managestock

import android.annotation.SuppressLint
import android.content.res.Resources
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import org.dhis2.android.rtsm.R
import org.dhis2.android.rtsm.commons.Constants.CLEAR_FIELD_DELAY
import org.dhis2.android.rtsm.data.AppConfig
import org.dhis2.android.rtsm.data.models.StockItem
import org.dhis2.android.rtsm.data.models.Transaction
import org.dhis2.android.rtsm.ui.base.ItemWatcher
import org.dhis2.android.rtsm.ui.base.SpeechController
import org.dhis2.android.rtsm.ui.base.TextInputDelegate
import org.dhis2.android.rtsm.utils.KeyboardUtils
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

class ManageStockAdapter(
    private val itemWatcher: @NotNull ItemWatcher<StockItem, String, String>,
    private var speechController: @Nullable SpeechController?,
    val appConfig: @NotNull AppConfig,
    private val transaction: Transaction,
    private var voiceInputEnabled: Boolean
): PagedListAdapter<
        StockItem, ManageStockAdapter.StockItemHolder>(DIFF_CALLBACK) {
    lateinit var resources: Resources
    private val textInputDelegate: TextInputDelegate = TextInputDelegate()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockItemHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.manage_stock_item_entry, parent, false)
        resources = parent.context.resources

        return StockItemHolder(itemView, itemWatcher)
    }

    override fun onBindViewHolder(holder: StockItemHolder, position: Int) {
        // Note that the item is a placeholder if it is null
        getItem(position)?.let { item -> holder.bindTo(item) }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<StockItem>() {
            override fun areItemsTheSame(
                oldItem: StockItem,
                newItem: StockItem
            ) = oldItem.id == newItem.id

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(
                oldItem: StockItem,
                newItem: StockItem
            ) = oldItem == newItem
        }
    }

    inner class StockItemHolder(
        itemView: View,
        private val watcher: ItemWatcher<StockItem, String, String>,
    ):
        RecyclerView.ViewHolder(itemView) {

        private val tvItemName: TextView = itemView.findViewById(R.id.item_name_text_view)
        private val tvStockOnHand: TextView = itemView.findViewById(R.id.stock_on_hand_value_text_view)
        private val etQty: TextInputLayout = itemView.findViewById(R.id.item_qty_text_field)

        init {
            KeyboardUtils.configureInputTypeForTransaction(transaction, etQty.editText)

            addTextListener()
            addFocusListener()
        }

        private fun addFocusListener() {
            etQty.editText?.setOnFocusChangeListener { _, hasFocus ->
                textInputDelegate.focusChanged(
                    speechController, etQty, hasFocus, voiceInputEnabled, adapterPosition)
            }
        }

        private fun addTextListener() {
            etQty.editText?.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (adapterPosition == RecyclerView.NO_POSITION) return

                    val qty = s?.toString()
                    getItem(adapterPosition)?.let {
                        textInputDelegate.textChanged(it, qty, adapterPosition, watcher)
                    }
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun afterTextChanged(s: Editable?) {}
            })
        }

        fun bindTo(item: StockItem) {
            tvItemName.text = item.name
            tvStockOnHand.text = watcher.getStockOnHand(item) ?: item.stockOnHand
            etQty.editText?.setText(watcher.getQuantity(item))

            var error: String? = null
            if (watcher.hasError(item)) {
                error = resources.getString(R.string.invalid_quantity)

                // Highlight the erroneous text for easy correction
                etQty.editText?.selectAll()

                // Clear the erroneous field after some time to prepare for next entry,
                // if input is via voice
                if (voiceInputEnabled)
                        textInputDelegate.clearFieldAfterDelay(
                            etQty.editText, CLEAR_FIELD_DELAY)
            } else {
                // set the cursor at the end
                etQty.editText?.setSelection(etQty.editText!!.text.length)
            }
            etQty.error = error
        }
    }

    fun voiceInputStateChanged(enabled: Boolean) {
        voiceInputEnabled = enabled

        textInputDelegate.voiceInputStateChanged(this)
    }
}