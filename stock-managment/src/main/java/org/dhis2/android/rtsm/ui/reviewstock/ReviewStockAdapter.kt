package org.dhis2.android.rtsm.ui.reviewstock;

import android.content.Context
import android.content.res.Resources
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import org.dhis2.android.rtsm.R
import org.dhis2.android.rtsm.commons.Constants
import org.dhis2.android.rtsm.data.AppConfig
import org.dhis2.android.rtsm.data.TransactionType
import org.dhis2.android.rtsm.data.models.StockEntry
import org.dhis2.android.rtsm.data.models.Transaction
import org.dhis2.android.rtsm.ui.base.ItemWatcher
import org.dhis2.android.rtsm.ui.base.SpeechController
import org.dhis2.android.rtsm.ui.base.TextInputDelegate
import org.dhis2.android.rtsm.utils.ActivityManager
import org.dhis2.android.rtsm.utils.KeyboardUtils
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

class ReviewStockAdapter(
    private val itemWatcher: @NotNull ItemWatcher<StockEntry, String, String>,
    private val speechController: @Nullable SpeechController?,
    val appConfig: @NotNull AppConfig,
    private val transaction: Transaction,
    private var voiceInputEnabled: Boolean
): ListAdapter<StockEntry, ReviewStockAdapter.StockEntryViewHolder>(DIFF_CALLBACK) {
    private lateinit var context: Context
    private lateinit var resources: Resources
    private val textInputDelegate: TextInputDelegate = TextInputDelegate()

    companion object {
        // TODO: Find a way to use a type-aware DIFF_CALLBACK for different adapters for reusability
        private val DIFF_CALLBACK = object: DiffUtil.ItemCallback<StockEntry> () {
            override fun areItemsTheSame(oldItem: StockEntry, newItem: StockEntry) =
                oldItem.item.id == newItem.item.id

            override fun areContentsTheSame(oldItem: StockEntry, newItem: StockEntry) =
                oldItem == newItem
        }
    }

    inner class StockEntryViewHolder(
        itemView: View,
        private val watcher: ItemWatcher<StockEntry, String, String>
    ): RecyclerView.ViewHolder(itemView) {
        private val tvItemName: TextView = itemView.findViewById(R.id.review_stock_item_name_text_view)
        private val tvStockOnHand: TextView = itemView.findViewById(R.id.review_stock_on_hand_value_text_view)
        private val tvItemQtyLayout: TextInputLayout = itemView.findViewById(R.id.review_item_qty_layout)
        private val btnRemoveItem: ImageButton = itemView.findViewById(R.id.remove_stock_item_image_button)

        init {
            KeyboardUtils.configureInputTypeForTransaction(transaction, tvItemQtyLayout.editText)

            btnRemoveItem.setOnClickListener {
                val messageResId = when (transaction.transactionType) {
                    TransactionType.DISTRIBUTION -> R.string.remove_distribution_item_confirmation_message
                    TransactionType.DISCARD -> R.string.remove_discard_item_confirmation_message
                    TransactionType.CORRECTION -> R.string.remove_correction_item_confirmation_message
                }

                ActivityManager.showDialog(
                    context,
                    R.string.confirmation,
                    context.getString(messageResId, getItem(adapterPosition).item.name)
                ) {
                    watcher.removeItem(getItem(adapterPosition))
                }
            }

            addTextListener()
            addFocusListener()
        }

        fun bindTo(entry: StockEntry) {
            tvItemName.text = entry.item.name
            tvStockOnHand.text = entry.stockOnHand

            val qty = watcher.getQuantity(entry)
            tvItemQtyLayout.editText?.setText(qty)

            var error: String? = null
            // A reviewed quantity should not be left empty
            if (watcher.hasError(entry) || qty.isNullOrEmpty()) {
                error = resources.getString(R.string.invalid_quantity)

                // Highlight the erroneous text for easy correction
                if (!qty.isNullOrEmpty())
                    tvItemQtyLayout.editText?.selectAll()

                // Clear the erroneous field after some time to prepare for next entry,
                // if input is via voice
                if (voiceInputEnabled)
                    textInputDelegate.clearFieldAfterDelay(
                        tvItemQtyLayout.editText, Constants.CLEAR_FIELD_DELAY
                    )
            } else {
                // set the cursor at the end
                tvItemQtyLayout.editText?.setSelection(tvItemQtyLayout.editText!!.text.length)
            }
            tvItemQtyLayout.error = error
        }

        private fun addTextListener() {
            tvItemQtyLayout.editText?.addTextChangedListener(object: TextWatcher {
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (adapterPosition == RecyclerView.NO_POSITION) return

                    val qty = s?.toString()
                    getItem(adapterPosition)?.let {
                        textInputDelegate.textChanged(it, qty, adapterPosition, watcher)
                    }
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun afterTextChanged(p0: Editable?) {}
            })
        }

        private fun addFocusListener() {
            tvItemQtyLayout.editText?.setOnFocusChangeListener { _, hasFocus ->
                textInputDelegate.focusChanged(
                    speechController, tvItemQtyLayout, hasFocus, voiceInputEnabled, adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockEntryViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.review_stock_item_entry, parent, false)
        resources = parent.context.resources
        context = parent.context

        return StockEntryViewHolder(itemView, itemWatcher)
    }

    override fun onBindViewHolder(holder: StockEntryViewHolder, position: Int) {
        holder.bindTo(getItem(position))
    }

    fun voiceInputStateChanged(status: Boolean) {
        voiceInputEnabled = status
        textInputDelegate.voiceInputStateChanged(this)
    }
}