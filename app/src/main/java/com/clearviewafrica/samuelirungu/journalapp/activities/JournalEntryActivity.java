package com.clearviewafrica.samuelirungu.journalapp.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.clearviewafrica.samuelirungu.journalapp.DatabaseHelper;
import com.clearviewafrica.samuelirungu.journalapp.R;
import com.clearviewafrica.samuelirungu.journalapp.model.JournalModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class JournalEntryActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private String entryText;
    public static String journal;

    DatabaseHelper databaseHelper;
    TextView textView;
    EditText editText;
    JournalModel journalModel;
    private DatabaseReference databaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal_entry);

        databaseHelper = DatabaseHelper.getInstance(this);
        textView = findViewById(R.id.journal_date);
        editText = findViewById(R.id.diary_edit_text);


        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar c = Calendar.getInstance();
                c.setTime(new Date());
        DatePickerDialog dialog = new DatePickerDialog(JournalEntryActivity.this,JournalEntryActivity.this,c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH));
                dialog.show();
            }
        });

        if(getIntent().getSerializableExtra(journal) != null){
            journalModel = (JournalModel) getIntent().getSerializableExtra(journal);
            textView.setText(journalModel.getJournal_date());
            editText.setText(journalModel.getJournal_entry());
        }
        else
            journalModel = new JournalModel();

        entryText = editText.getText().toString();
        databaseReference = FirebaseDatabase.getInstance().getReference();


    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR,year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH,dayOfMonth);

        String tStamp = new SimpleDateFormat("dd-MM-yyyy", Locale.UK).format(cal.getTime());
        textView.setText(tStamp);
        journalModel.setJournal_date(cal.getTimeInMillis()+"");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.save) {

            String dt = textView.getText().toString();
            String note = editText.getText().toString();
            if(!dt.equals("") && !note.equals("")){
                journalModel.setJournal_entry(note);
                journalModel.setJournal_mode((entryText.equalsIgnoreCase(note)? "0":"1"));
                if(journalModel.getJournal_key().equals(""))
                    journalModel.setJournal_key(databaseReference.child(getString(R.string.journals)).push().getKey());

                if(databaseHelper.createJournal(journalModel)){
                    showMessage(getString(R.string.saved_success));
                    enterInJournal(journalModel);
                }
                else {
                    showMessage(getString(R.string.save_fail));
                }
            }
            else
                showMessage(getString(R.string.fill_all_fields));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     *
     * @param model JournalModel
     */
    private void enterInJournal(JournalModel model) {

        String uid = DatabaseHelper.getSharedPreference(JournalEntryActivity.this).getString("SUID","");
        if(uid.equalsIgnoreCase("")){
            showMessage(getString(R.string.failure_no_user));
            return;
        }
        model.setUnique_id(uid);


        Map<String, Object> postValues = model.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(getString(R.string.journal_path) + model.getUnique_id() + "/" + model.getJournal_key(), postValues);

        databaseReference.updateChildren(childUpdates)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        databaseHelper.showDebugLog(getString(R.string.saved_data_success));
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        },2000);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        databaseHelper.showDebugLog(e.toString());
                        databaseHelper.showDebugLog(getString(R.string.data_failed));
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        },2000);
                    }
                });
    }

    void showMessage(String msg){
        Snackbar.make(textView,msg,Snackbar.LENGTH_SHORT).show();
    }
}
