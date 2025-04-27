package org.dhis2.usescases.sms.presentation.contentprovider

import android.content.Context
import androidx.core.content.ContextCompat
import org.dhis2.R
import javax.inject.Inject


class SpipSmsContentResourcesProvider @Inject constructor(
  private val context: Context,
) {

  fun onSmsSentSuccessfully() = context.getString(R.string.send_sms)
  fun onSmsSentEnSuccessfully() = context.getString(R.string.sent_sms_successfully)
  fun onSmsSentError() = context.getString(R.string.sent_sms_using_en_successfully)
  fun onSmsSentGenericError() = context.getString(R.string.sent_sms_template_error)
  fun onSuccessMessageBackgroundColor() = ContextCompat.getColor(context, R.color.colorPrimaryDark_2e7)
  fun onErrorMessageBackgroundColor() = ContextCompat.getColor(context, R.color.colorPrimaryDarkRed)
  fun getOnMessageBackground(
    isSuccess: Boolean
  ) = if (isSuccess) onSuccessMessageBackgroundColor() else onErrorMessageBackgroundColor()
}