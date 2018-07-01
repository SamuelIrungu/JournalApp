package com.clearviewafrica.samuelirungu.journalapp.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.clearviewafrica.samuelirungu.journalapp.EntriesViewHolder;
import com.clearviewafrica.samuelirungu.journalapp.model.JournalModel;
import com.clearviewafrica.samuelirungu.journalapp.R;

import java.util.ArrayList;
import java.util.List;

public class EntriesAdapter extends RecyclerView.Adapter<EntriesViewHolder> {

    private List<JournalModel> list = new ArrayList<>();
    private Context context;

    public EntriesAdapter(Context context) {
        this.context = context;
    }


    @NonNull
    @Override
    public EntriesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewtype) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_child, parent, false);
        return new EntriesViewHolder(view, context);
    }

    @Override
    public void onBindViewHolder(@NonNull EntriesViewHolder holder, int position) {
        JournalModel journalModel = list.get(position);
        holder.bind(journalModel);
    }

    public void clear(){
        list.clear();
        notifyDataSetChanged();
    }

    public void addData(ArrayList<JournalModel> mlist){
        this.list.addAll(mlist);
        notifyDataSetChanged();
    }

    public JournalModel getJournal(int position){
        return list.get(position);
    }

    public void removeAt(int position){
        list.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
