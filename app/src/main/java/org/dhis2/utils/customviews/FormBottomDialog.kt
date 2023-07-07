package org.dhis2.utils.customviews

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.dhis2.databinding.FormBottomDialogBinding

class FormBottomDialog : BottomSheetDialogFragment() {
    private var mListener: OnFormBottomDialogItemSelection? = null
    private var mCanComplete = false
    private var mReopen = false
    private var mSkip = false
    private var mReschedule = false
    private var mIsEnrollmentOpen = true
    private var mAccessDataWrite = true
    private var mHasExpired = false
    private var mMandatoryFields = false
    private var mMessageOnComplete: String? = null
    private var mFieldsWithErrors = false
    private var mEmptyMandatoryFields: Map<String, String> = HashMap()
    private val presenter = FormBottomDialogPresenter()

    companion object {
        @JvmStatic
        val instance: FormBottomDialog
            get() = FormBottomDialog()
    }

    fun setAccessDataWrite(canWrite: Boolean): FormBottomDialog {
        mAccessDataWrite = canWrite
        return this
    }

    fun setSkip(skip: Boolean): FormBottomDialog {
        mSkip = skip
        return this
    }

    fun setReschedule(reschedule: Boolean): FormBottomDialog {
        mReschedule = reschedule
        return this
    }

    fun setIsExpired(hasExpired: Boolean): FormBottomDialog {
        mHasExpired = hasExpired
        return this
    }

    enum class ActionType {
        FINISH_ADD_NEW, SKIP, RESCHEDULE, FINISH, COMPLETE_ADD_NEW, COMPLETE, CHECK_FIELDS, NONE
    }

    fun setListener(listener: OnFormBottomDialogItemSelection?): FormBottomDialog {
        mListener = listener
        return this
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FormBottomDialogBinding.inflate(inflater, container, false).apply {
            canWrite = mAccessDataWrite
            isEnrollmentOpen = mIsEnrollmentOpen
            hasExpired = mHasExpired
            setListener { actionType ->
                mListener!!.onActionSelected(actionType)
                dismiss()
            }
            canComplete = mCanComplete
            setReopen(mReopen)
            setSkip(mSkip)
            setReschedule(mReschedule)
            mandatoryFields = mMandatoryFields
            fieldsWithErrors = mFieldsWithErrors
            messageOnComplete = mMessageOnComplete
            txtMandatoryFields.text = presenter.appendMandatoryFieldList(
                mMandatoryFields,
                mEmptyMandatoryFields,
                txtMandatoryFields.text.toString()
            )
        }.root
    }

    // This is necessary to show the bottomSheet dialog with full height on landscape
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.viewTreeObserver
            .addOnGlobalLayoutListener {
                val dialog = dialog as BottomSheetDialog?
                val bottomSheet =
                    dialog!!.findViewById<FrameLayout>(
                        com.google.android.material.R.id.design_bottom_sheet
                    )
                val behavior: BottomSheetBehavior<*> =
                    BottomSheetBehavior.from<FrameLayout?>(bottomSheet!!)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.setPeekHeight(0)
            }
    }

    override fun show(manager: FragmentManager, tag: String?) {
        requireNotNull(mListener) { "Call this method after setting listener" }
        super.show(manager, tag)
    }
}
