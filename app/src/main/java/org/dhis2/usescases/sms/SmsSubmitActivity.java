package org.dhis2.usescases.sms;

import android.Manifest;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.dhis2.R;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static org.dhis2.usescases.sms.SmsSendingService.*;

public class SmsSubmitActivity extends ActivityGlobalAbstract {
    private static String STATE_FINISHED = "submission_finished";
    private static String STATE_STATES_LIST = "states_list";
    private static final int SMS_PERMISSIONS_REQ_ID = 102;

    private InputArguments inputArguments = new InputArguments(null);
    private SmsLogAdapter adapter;
    private View titleBar;
    private TextView state;
    private RotateAnimation rotate = new RotateAnimation(
            0, 360,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
    );
    private boolean submissionFinished = false;
    private SmsSendingService smsSendingService = null;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        findViewById(R.id.smsLogOverlay).setOnClickListener(v -> finish());
        titleBar = findViewById(R.id.smsLogTitleBar);
        state = findViewById(R.id.smsLogState);
        adapter = new SmsLogAdapter();
        RecyclerView recycler = findViewById(R.id.smsLogRecycler);
        recycler.setAdapter(adapter);
        recycler.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));

        inputArguments = new InputArguments(getIntent().getExtras());

        TextView title = findViewById(R.id.smsLogTitle);
        title.setText(StatusText.getTextSubmissionType(getResources(), inputArguments));
        recoverState(savedInstanceState);
    }

    private void recoverState(Bundle state) {
        if (state == null) return;
        submissionFinished = state.getBoolean(STATE_FINISHED, false);
        try {
            ArrayList<SendingStatus> states = (ArrayList<SendingStatus>) state.getSerializable(STATE_STATES_LIST);
            stateChanged(states);
        } catch (Exception e) {
            if (submissionFinished) {
                finish(); // nothing to show, will not get it from service
            }
            // nothing scary, will get it from service later
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (submissionFinished) {
            // not connecting to service or starting animation
            return;
        }
        rotate.setDuration(1000);
        rotate.setRepeatCount(Animation.INFINITE);
        findViewById(R.id.smsLogIcon).startAnimation(rotate);
        Intent intent = new Intent(this, SmsSendingService.class);
        startService(intent);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        rotate.cancel();
        try {
            unbindService(connection);
        } catch (IllegalArgumentException error) {
            // service not registered, maybe disconnected before automatically
        }
        smsSendingService = null;
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_FINISHED, submissionFinished);
        outState.putSerializable(STATE_STATES_LIST, new ArrayList<>(adapter.getStates()));
    }

    private void linkToService() {
        smsSendingService.sendingState().observe(this, this::stateChanged);
        if (!smsSendingService.setInputArguments(inputArguments)) {
            showOtherServiceRunningError();
            return;
        }
        if (checkPermissions()) {
            smsSendingService.sendSMS();
        }
    }

    private void showOtherServiceRunningError() {
        Toast.makeText(this, R.string.sms_error_ongoing_sunmission, Toast.LENGTH_LONG).show();
        finish();
    }

    private boolean hasPermissions(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != SMS_PERMISSIONS_REQ_ID) return;
        // Try to send anyway. It will show a right message in case of important permission missing.
        if (smsSendingService != null) smsSendingService.sendSMS();
    }

    private boolean checkPermissions() {
        // check permissions
        String[] smsPermissions = new String[]{Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.SEND_SMS,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_SMS};
        if (!hasPermissions(smsPermissions)) {
            ActivityCompat.requestPermissions(this, smsPermissions, SMS_PERMISSIONS_REQ_ID);
            return false;
        }
        return true;
    }

    private void askForMessagesAmount(int amount) {
        Bundle args = new Bundle();
        args.putInt(MessagesAmountDialog.ARG_AMOUNT, amount);
        MessagesAmountDialog dialog = new MessagesAmountDialog();
        dialog.setArguments(args);
        dialog.show(getSupportFragmentManager(), null);
    }

    private void stateChanged(List<SendingStatus> states) {
        state.setText("");
        if (states == null) return;
        adapter.setStates(states);
        if (states.size() == 0) {
            return;
        }
        SendingStatus lastState = states.get(states.size() - 1);
        switch (lastState.state) {
            case WAITING_COUNT_CONFIRMATION:
                askForMessagesAmount(lastState.total);
            case STARTED:
            case CONVERTED:
            case SENDING:
            case WAITING_RESULT:
            case RESULT_CONFIRMED:
            case SENT:
                state.setText(R.string.sms_bar_state_sending);
                break;
            case ITEM_NOT_READY:
            case COUNT_NOT_ACCEPTED:
            case WAITING_RESULT_TIMEOUT:
            case ERROR:
                titleBar.setBackgroundColor(ContextCompat.getColor(this, R.color.sms_sync_title_bar_error));
                state.setText(R.string.sms_bar_state_failed);
                finishSubmission();
                break;
            case COMPLETED:
                state.setText(R.string.sms_bar_state_sent);
                finishSubmission();
                break;
        }
    }

    private void finishSubmission() {
        rotate.cancel();
        submissionFinished = true;
    }

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            SmsSendingService.LocalBinder binder = (SmsSendingService.LocalBinder) service;
            smsSendingService = binder.getService();
            linkToService();
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            smsSendingService = null;
        }
    };

    private void acceptSMSCount(boolean accept) {
        if (smsSendingService == null) return;
        smsSendingService.acceptSMSCount(accept);
    }

    public static class MessagesAmountDialog extends DialogFragment {
        static final String ARG_AMOUNT = "amount";

        public MessagesAmountDialog() {
        }

        @NotNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int amount = getArguments().getInt(ARG_AMOUNT);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(getString(R.string.sms_amount_question, amount));

            builder.setPositiveButton(android.R.string.yes, (dialog, which) ->
                    ((SmsSubmitActivity) getActivity()).acceptSMSCount(true)
            );
            builder.setNegativeButton(android.R.string.no, (dialog, which) ->
                    ((SmsSubmitActivity) getActivity()).acceptSMSCount(false)
            );
            return builder.create();
        }

        @Override
        public void onStart() {
            super.onStart();
            AlertDialog dialog = (AlertDialog) getDialog();
            int color = ContextCompat.getColor(getActivity(), R.color.colorPrimary);
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(color);
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(color);
        }

        @Override
        public void onCancel(@NonNull DialogInterface dialog) {
            FragmentActivity activity = getActivity();
            if (activity instanceof SmsSubmitActivity) {
                ((SmsSubmitActivity) getActivity()).acceptSMSCount(false);
            }
        }

        @Override
        public void onPause() {
            dismiss();
            super.onPause();
        }
    }
}
