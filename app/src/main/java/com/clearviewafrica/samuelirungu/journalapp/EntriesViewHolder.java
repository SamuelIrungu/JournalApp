package com.clearviewafrica.samuelirungu.journalapp;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.clearviewafrica.samuelirungu.journalapp.activities.JournalEntryActivity;
import com.clearviewafrica.samuelirungu.journalapp.model.JournalModel;

public class EntriesViewHolder extends RecyclerView.ViewHolder {

    private TextView journalSum, journalDay, journalMonth;
    private JournalModel journalModel;
    public EntriesViewHolder(View itemView, final Context context) {
        super(itemView);
        journalSum = itemView.findViewById(R.id.journal_summary);
        journalDay = itemView.findViewById(R.id.day);
        journalMonth = itemView.findViewById(R.id.month);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent crEntryAct = new Intent(context, JournalEntryActivity.class);
                crEntryAct.putExtra(JournalEntryActivity.journal, journalModel);
                context.startActivity(crEntryAct);
            }
        });
    }

    public void bind(JournalModel journalModel){
        if(journalModel != null){
            this.journalModel = journalModel;
            journalSum.setText(journalModel.getJournalSum());
            journalDay.setText(journalModel.getJournalDay());
            journalMonth.setText(journalModel.getJournalMonth());
        }
    }
}
