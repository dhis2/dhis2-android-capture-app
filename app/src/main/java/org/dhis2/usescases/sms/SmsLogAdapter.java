package org.dhis2.usescases.sms;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.dhis2.R;

import java.util.ArrayList;
import java.util.List;

public class SmsLogAdapter extends RecyclerView.Adapter<SmsLogAdapter.ViewHolder> {

    private List<SmsSendingService.SendingStatus> states;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.sms_log_item, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        boolean firstItem = position == 0;
        position = states.size() - position - 1;
        Resources res = h.item.getResources();
        SmsSendingService.SendingStatus state = states.get(position);
        h.item.setText(StatusText.getTextForStatus(res, state));
        int firstItemColor = ContextCompat.getColor(h.item.getContext(), R.color.sms_sync_last_event);
        int standardColor = ContextCompat.getColor(h.item.getContext(), R.color.text_black_333);
        h.item.setTextColor(firstItem ? firstItemColor : standardColor);
    }

    @Override
    public int getItemCount() {
        if (states == null) {
            return 0;
        }
        return states.size();
    }

    void setStates(List<SmsSendingService.SendingStatus> states) {
        this.states = states;
        notifyDataSetChanged();
    }

    List<SmsSendingService.SendingStatus> getStates() {
        if (states == null) return new ArrayList<>();
        return states;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView item;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            item = itemView.findViewById(R.id.smsLogItem);
        }
    }
}
