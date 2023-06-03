package com.example.plantaid_redesign.Adapter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.plantaid_redesign.Common.LoadingDialog;
import com.example.plantaid_redesign.Model.Journal;
import com.example.plantaid_redesign.R;
import com.example.plantaid_redesign.Utilities.DateUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class TrashAdapter extends RecyclerView.Adapter<TrashAdapter.ViewHolder> {


    private List<Journal> list;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public interface OnItemCheckListener {
        void onItemCheck(Journal item);
        void onItemUncheck(Journal item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }
    private OnItemCheckListener onItemClick;
    private AdapterListener listener;

    private Context context;

    public interface AdapterListener {
        void onItemCountChanged();
    }

    public TrashAdapter(List<Journal> list, AdapterListener listener, OnItemCheckListener onItemCheckListener, Context context) {
        this.list = list;
        this.listener = listener;
        this.onItemClick = onItemCheckListener;
        this.context = context;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView title, time, date, content;
        public ImageView imgContent;

        public ImageButton btnMore;

        public CheckBox checkBox;

        public LinearLayout parent;
        public ViewHolder(final View itemView){
            super(itemView);
            parent = itemView.findViewById(R.id.parent);
            title = itemView.findViewById(R.id.textview_title);
            time = itemView.findViewById(R.id.textview_timestamp);
            imgContent = itemView.findViewById(R.id.imageview_item);
            content = itemView.findViewById(R.id.textview_content);
            checkBox = itemView.findViewById(R.id.checkbox_item);
            btnMore = itemView.findViewById(R.id.btnMore);
            checkBox.setClickable(false);

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

        public void setOnClickListener(View.OnClickListener onClickListener) {
            itemView.setOnClickListener(onClickListener);
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
            View itemView = inflater.inflate(R.layout.adapter_trash_items, parent, false);
            vh = new ViewHolder(itemView);
        } catch (Exception e){
            Log.e("JournalError", "Message: ", e);
        }
        return vh;
    }

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseDatabase database;
    private DatabaseReference adapterReference, adapterTrashRef;
    private LoadingDialog dialog;

    private boolean isFromHere;

    private boolean isCheckBoxVisible = false;

    public void setCheckBoxVisibility(boolean visible) {
        isCheckBoxVisible = visible;
        notifyDataSetChanged();
    }



    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Journal journal = list.get(position);

        //Initialize firebase
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        adapterReference = database.getReference().child("journal").child(currentUser.getUid());
        adapterTrashRef = database.getReference().child("trash").child(currentUser.getUid());
        dialog = new LoadingDialog(holder.itemView.getContext());
        isFromHere = true;

        ((ViewHolder) holder).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((ViewHolder) holder).checkBox.getVisibility() == View.VISIBLE) {
                    ((ViewHolder) holder).checkBox.setChecked(
                            !((ViewHolder) holder).checkBox.isChecked());
                    if (((ViewHolder) holder).checkBox.isChecked()) {
                        onItemClick.onItemCheck(journal);
                    } else {
                        onItemClick.onItemUncheck(journal);
                    }
                }
            }
        });

        TransitionManager.beginDelayedTransition(holder.parent);
        if(isCheckBoxVisible){
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.btnMore.setVisibility(View.GONE);
        } else {
            holder.checkBox.setVisibility(View.GONE);
            holder.btnMore.setVisibility(View.VISIBLE);
        }



        String id = journal.id;
        String content = journal.content;
        String dateTime = DateUtils.convertDateString(journal.getDate()) + " • " + DateUtils.convertTimeString(journal.getTime());
        holder.title.setText(journal.getTitle());
        holder.time.setText(dateTime);

        if(content.length() > 23){
            content = content.substring(0, Math.min(content.length(), 23));
            holder.content.setText(content + "...");
        } else {
            holder.content.setText(content);
        }

        Glide.with(context).load(journal.getPhoto()).into(holder.imgContent);


        holder.btnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    PopupMenu popupMenu  = new PopupMenu(holder.itemView.getContext(), view);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            switch (menuItem.getItemId()){
                                case R.id.optionRestore:
                                    if(isFromHere){
                                        restore(id, position, "restore");
                                    }
                                    return true;
                                case R.id.optionDelete:
                                    if(isFromHere){
                                        deletePermanently(id, position, "delete");
                                    }
                                    return true;
                                default:
                                    return false;
                            }
                        }
                    });

                    popupMenu.inflate(R.menu.options_trash_restore);
                    popupMenu.show();
                } catch (Exception e){
                    Log.e("Trash Adapter", "onClick: ", e);
                    Toast.makeText(holder.itemView.getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void restore(String id, int position, String action) {
        try {
            if(action.equals("restore")){
                Toast.makeText(context, "Restoring entry...", Toast.LENGTH_SHORT).show();
            }

            DatabaseReference reference1 = adapterTrashRef.child(id);
            reference1.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Journal journal = dataSnapshot.getValue(Journal.class);

                    if (journal != null){
                        adapterReference.child(id).setValue(journal).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(action.equals("restore")){
                                    Toast.makeText(context, "Restored successfully", Toast.LENGTH_SHORT).show();
                                    deletePermanently(id, position, action);
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context, "Please try again", Toast.LENGTH_SHORT).show();
                                Log.e("Move to trash", "Error ", e);
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e){
            Log.e("Trash Adapter", "restore: ", e);
            Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
    }

    private void deletePermanently(String id, int position, String action) {
        try {
            DatabaseReference reference1 = adapterTrashRef.child(id);
            if(action.equals("delete")){
                Toast.makeText(context, "Deleting entry...", Toast.LENGTH_SHORT).show();
            }

            reference1.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    list.remove(position);

                    listener.onItemCountChanged();

                    if(action.equals("delete")){
                        Toast.makeText(context, "Permanently deleted", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(context, "Please try again", Toast.LENGTH_SHORT).show();
                    Log.e("Delete value", "Error ", e);
                }
            });
        } catch (Exception e){
            Log.e("Trash Adapter", "deletePermanently: ", e);
            Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
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