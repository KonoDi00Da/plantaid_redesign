package com.example.plantaid_redesign.Adapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.plantaid_redesign.Model.Journal;
import com.example.plantaid_redesign.R;
import com.example.plantaid_redesign.Utilities.DateUtils;

import java.util.List;

public class JournalAdapter extends RecyclerView.Adapter<JournalAdapter.ViewHolder> {

    private List<Journal> list;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }


    public JournalAdapter(List<Journal> list) {
        this.list = list;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView title, time, date;
        public ImageView imgContent;
        public ViewHolder(final View itemView){
            super(itemView);
            title = itemView.findViewById(R.id.txtTitle);
            time = itemView.findViewById(R.id.txtTime);
            imgContent = itemView.findViewById(R.id.imgJournal);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            mListener.onItemClick(position);
                        }
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolder vh = null;
        try {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View itemView = inflater.inflate(R.layout.adapter_journal_item, parent, false);
            vh = new ViewHolder(itemView);
        } catch (Exception e){
            Log.e("JournalError", "Message: ", e);
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Journal journal = list.get(position);

        String dateTime = DateUtils.convertDateString(journal.getDate()) + " • " + DateUtils.convertTimeString(journal.getTime());
        holder.title.setText(journal.getTitle());
        holder.time.setText(dateTime);


        Glide.with(holder.itemView.getContext()).load(journal.getPhoto()).into(holder.imgContent);


    }

    public void updateDataSet(List<Journal> newResult){
        if(newResult!=null){
            list = newResult;
        }
        notifyDataSetChanged();
    }
    public void clear() {
        int size = list.size();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                list.remove(0);
            }

            notifyItemRangeRemoved(0, size);
        }
    }
}