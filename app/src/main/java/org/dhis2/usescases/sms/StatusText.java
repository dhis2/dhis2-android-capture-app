package org.dhis2.usescases.sms;

import android.content.res.Resources;

import org.dhis2.R;
import org.hisp.dhis.android.core.sms.domain.interactor.SmsSubmitCase;

public class StatusText {

    public static String getTextForStatus(Resources r, SmsSendingService.SendingStatus state) {
        switch (state.state) {
            case STARTED:
                return r.getString(R.string.sms_state_started);
            case CONVERTED:
                return r.getString(R.string.sms_state_converted, state.submissionId);
            case WAITING_COUNT_CONFIRMATION:
                return r.getString(R.string.sms_waiting_amount_confirm, state.total);
            case COUNT_NOT_ACCEPTED:
                return r.getString(R.string.sms_count_error);
            case SENDING:
                return r.getString(R.string.sms_sending, state.sent, state.total);
            case SENT:
                return r.getString(R.string.sms_all_sent);
            case WAITING_RESULT:
                return r.getString(R.string.sms_waiting_confirmation);
            case RESULT_CONFIRMED:
                return r.getString(R.string.sms_confirmed);
            case WAITING_RESULT_TIMEOUT:
                return r.getString(R.string.sms_waiting_timeout);
            case COMPLETED:
                return r.getString(R.string.sms_submission_completed);
            case ERROR:
                return getErrorText(r, state.error);
            case ITEM_NOT_READY:
                return r.getString(R.string.sms_error_item_not_ready);
        }
        return "";
    }

    public static String getTextSubmissionType(Resources r, InputArguments inputArguments) {
        switch (inputArguments.getSubmissionType()) {
            case ENROLLMENT:
                return r.getString(R.string.sms_title_enrollment);
            case DATA_SET:
                return r.getString(R.string.sms_title_data_set);
            case SIMPLE_EVENT:
            case TRACKER_EVENT:
                return r.getString(R.string.sms_title_event);
        }
        return "";
    }

    private static String getErrorText(Resources res, Throwable error) {
        String text = res.getString(R.string.sms_error);
        if (error instanceof SmsSubmitCase.PreconditionFailed) {
            switch (((SmsSubmitCase.PreconditionFailed) error).getType()) {
                case NO_NETWORK:
                    text = res.getString(R.string.sms_error_no_network);
                    break;
                case NO_CHECK_NETWORK_PERMISSION:
                    text = res.getString(R.string.sms_error_no_check_network_permission);
                    break;
                case NO_RECEIVE_SMS_PERMISSION:
                    text = res.getString(R.string.sms_error_no_receive_sms_permission);
                    break;
                case NO_SEND_SMS_PERMISSION:
                    text = res.getString(R.string.sms_error_no_send_sms_permission);
                    break;
                case NO_GATEWAY_NUMBER_SET:
                    text = res.getString(R.string.sms_error_no_gateway_set);
                    break;
                case NO_USER_LOGGED_IN:
                    text = res.getString(R.string.sms_error_no_user_login);
                    break;
                case NO_METADATA_DOWNLOADED:
                    text = res.getString(R.string.sms_metadata_empty);
                    break;
                case SMS_MODULE_DISABLED:
                    text = res.getString(R.string.sms_error_module_disabled);
                    break;
            }
        }
        return text;
    }
}
