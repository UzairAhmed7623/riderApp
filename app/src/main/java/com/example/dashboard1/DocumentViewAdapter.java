package com.example.dashboard1;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class DocumentViewAdapter extends RecyclerView.Adapter<DocumentViewAdapter.ViewHolder> {

    private Context context;
    private List<String> data = new ArrayList<>();

    public DocumentViewAdapter(Context context, List<String> data) {
        this.context = context;
        this.data = data;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.history_recycleriew, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String doc = data.get(position);
        holder.tvDocuments.setText(doc);
        
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(view.getContext(), doc  + " was clicked!", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(view.getContext(), ViewHistoryOnMap.class);
                intent.putExtra("doc", data.get(position));
                view.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView tvDocuments;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvDocuments = (TextView)itemView.findViewById(R.id.tvDocuments);
        }
    }
}
