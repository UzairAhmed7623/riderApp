package com.example.dashboard1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ViewHistoryAdapter extends RecyclerView.Adapter<ViewHistoryAdapter.ViewHolder> {

    private Context context;
    private List<String> addr = new ArrayList<>();

    public ViewHistoryAdapter(Context context, List<String> addr) {
        this.context = context;
        this.addr = addr;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.location_history_view, parent, false);
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHistoryAdapter.ViewHolder holder, int position) {
        String locName = addr.get(position);
        holder.tvLocationHistoryView.setText(locName);
    }

    @Override
    public int getItemCount() {
        return addr.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvLocationHistoryView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvLocationHistoryView = (TextView)itemView.findViewById(R.id.tvLocationHistoryView);

        }
    }
}
