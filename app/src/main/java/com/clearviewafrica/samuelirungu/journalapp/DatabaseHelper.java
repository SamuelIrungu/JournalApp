package com.clearviewafrica.samuelirungu.journalapp;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import com.clearviewafrica.samuelirungu.journalapp.model.JournalModel;
import com.clearviewafrica.samuelirungu.journalapp.utils.Constants;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    @SuppressLint("StaticFieldLeak")
    private static DatabaseHelper getInstance;
    private Context context;
    private static SharedPreferences sharedPreferences;

    private DatabaseHelper(Context context) {
        super(context, Constants.DB_NAME, null, Constants.DB_VERSION);
        this.context = context;
    }

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (getInstance == null) {
            getInstance = new DatabaseHelper(context.getApplicationContext());
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        }
        return getInstance;
    }

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    /**
     * database creation
     *
     * @param database database
     */
    @Override
    public void onCreate(SQLiteDatabase database) {
        String CREATE_JOURNAL_TB = "CREATE TABLE IF NOT EXISTS "
                + Constants.JOURNALS_TABLE + "("
                + Constants.UNIQUE_ID + " INTEGER PRIMARY KEY, "
                + Constants.JOURNAL_DATE + " VARCHAR, "
                + Constants.JOURNAL_KEY + " VARCHAR, "
                + Constants.JOURNAL_MODE + " VARCHAR(1) default 0, "
                + Constants.JOURNAL_CONTENT + " VARCHAR, UNIQUE(" + Constants.JOURNAL_DATE + ") ON CONFLICT IGNORE )";
        database.execSQL(CREATE_JOURNAL_TB);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        onCreate(db);
        super.onOpen(db);
    }

    /**
     * Management of versioning
     *
     * @param liteDatabase liteDatabase
     * @param oldVersion   oldVersion
     * @param newVersion   newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase liteDatabase, int oldVersion, int newVersion) {

    }

    public boolean createJournal(JournalModel journalModel) {
        try {

            SQLiteDatabase liteDatabase = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            if (!journalModel.getJournal_id().equals(""))
                values.put(Constants.UNIQUE_ID, journalModel.getJournal_id());
            values.put(Constants.JOURNAL_DATE, journalModel.getJournal_date());
            values.put(Constants.JOURNAL_KEY, journalModel.getJournal_key());
            values.put(Constants.JOURNAL_CONTENT, journalModel.getJournal_entry());
            values.put(Constants.JOURNAL_MODE, journalModel.getJournal_mode());

            long count = 0;
            if (journalModel.getJournal_id().equals("")) {

                count = liteDatabase.insert(Constants.JOURNALS_TABLE, null, values);
                if (count > 0)
                    journalModel.setJournal_id(count + "");
            } else {
                count = liteDatabase.update(Constants.JOURNALS_TABLE, values, Constants.UNIQUE_ID + "=?", new String[]{journalModel.getJournal_id()});
                if (count <= 0)
                    count = liteDatabase.insert(Constants.JOURNALS_TABLE, null, values);
            }

            return count > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Get all entries in the database
     *
     * @return entries
     */
    public ArrayList<JournalModel> getJournals(int number) {
        ArrayList<JournalModel> entries = new ArrayList<>();
        try {
            SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();

            String selQuery = "SELECT * FROM " + Constants.JOURNALS_TABLE + "  ORDER BY " + Constants.JOURNAL_DATE + " ASC";

            Cursor cursor = sqLiteDatabase.rawQuery(selQuery, null);

            Calendar calendar = Calendar.getInstance();

            if (cursor.getCount() > 0 && cursor.moveToFirst()) {
                do {
                    JournalModel j = new JournalModel();
                    j.setJournal_id(cursor.getInt(cursor.getColumnIndex(Constants.UNIQUE_ID)) + "");
                    j.setJournal_entry(cursor.getString(cursor.getColumnIndex(Constants.JOURNAL_CONTENT)));
                    try {
                        calendar.setTimeInMillis(Long.parseLong(cursor.getString(cursor.getColumnIndex(Constants.JOURNAL_DATE))));
                        String st = new SimpleDateFormat("dd-MMM-yyyy", Locale.US).format(calendar.getTime());
                        j.setJournal_date(st);
                    } catch (NumberFormatException e) {

                        j.setJournal_date(cursor.getString(cursor.getColumnIndex(Constants.JOURNAL_DATE)));
                    }
                    j.setJournal_key(cursor.getString(cursor.getColumnIndex(Constants.JOURNAL_KEY)));
                    j.setJournal_mode(cursor.getString(cursor.getColumnIndex(Constants.JOURNAL_MODE)));
                    entries.add(j);

                } while (cursor.moveToNext());
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return entries;
    }

    public void showDebugLog(String message) {
        Log.e("database helper run: ", message);
    }

    public void showSnackBar(String message, View view) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
    }

    public static synchronized SharedPreferences getSharedPreference(Context context) {
        if (sharedPreferences == null) {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        }
        return sharedPreferences;
    }

    public void saveSharedPreference(String str, String value) {
        SharedPreferences.Editor edit = getSharedPreference(context).edit();
        edit.putString(str, value);
        edit.apply();
    }

    public boolean deleteJournal(JournalModel journalModel) {
        SQLiteDatabase liteDatabase = this.getWritableDatabase();
        return liteDatabase.delete(Constants.JOURNALS_TABLE, Constants.JOURNAL_KEY + "=?", new String[]{journalModel.getJournal_key()}) > 0;

    }
}
