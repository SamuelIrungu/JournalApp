package com.clearviewafrica.samuelirungu.journalapp.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class JournalModel implements Serializable {

    private String journal_id = "";
    private String journal_key = "";
    private String unique_id;
    private String journal_date;
    private String journal_entry;
    private String journal_mode;


    /**
     * Constructor
     * @param journal_id journal_id
     * @param unique_id unique_id unique id
     * @param journal_date journal_date
     * @param journal_entry journal_entry
     * @param journal_mode journal_mode
     */
    public JournalModel(String journal_id, String unique_id, String journal_date, String journal_entry, String journal_mode) {
        this.journal_id = journal_id;
        this.unique_id = unique_id;
        this.journal_date = journal_date;
        this.journal_entry = journal_entry;
        this.journal_mode = journal_mode;
    }

    /**
     * Constructor with no uid
     * @param journal_id journal_id
     * @param journal_date journal_date
     * @param journal_entry journal_entry
     * @param journal_mode journal_mode
     */
    public JournalModel(String journal_id, String journal_date, String journal_entry, String journal_mode) {
        this.journal_id = journal_id;
        this.journal_date = journal_date;
        this.journal_entry = journal_entry;
        this.journal_mode = journal_mode;
    }

    /**
     * Empty Constructor
     */
    public JournalModel(){ }


    /**
     * @return string
     */
    @Override
    public String toString() {
        return "JournalModel{" + "journal_id='" + journal_id + '\'' + ", journal_key='" + journal_key + '\'' +
                ", journal_date='" + journal_date + '\'' + ", journal_entry='" + journal_entry + '\'' +
                ", journal_mode='" + journal_mode + '\'' + '}';
    }

    public String getJournal_key() {
        return journal_key;
    }

    public void setJournal_key(String journal_key) {
        this.journal_key = journal_key;
    }

    public String getUnique_id() {
        return unique_id;
    }

    public void setUnique_id(String unique_id) {
        this.unique_id = unique_id;
    }

    public void setJournal_date(String journal_date) {
        this.journal_date = journal_date;
    }

    public String getJournal_id() {
        return journal_id;
    }

    public void setJournal_id(String journal_id) {
        this.journal_id = journal_id;
    }

    public String getJournal_date() {
        return journal_date;
    }

    public String getJournal_entry() {
        return journal_entry;
    }

    public void setJournal_entry(String journal_entry) {
        this.journal_entry = journal_entry;
    }

    public String getJournal_mode() {
        return journal_mode;
    }

    public void setJournal_mode(String journal_mode) {
        this.journal_mode = journal_mode;
    }


    public String getJournalSum() {
        if(journal_entry.length() <=100)
            return journal_entry;
        else
            return journal_entry.substring(0,100);
    }

    public String getJournalDay() {
        if(!journal_date.equals(""))
            return journal_date.split("-")[0];
        return "";
    }

    public String getJournalMonth() {
        if(!journal_date.equals(""))
            return journal_date.split("-")[1];
        return "";
    }

    public JournalModel(String date, String entry){
        setJournal_date(date);
        setJournal_entry(entry);
    }


    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("journal_id", journal_id);
        result.put("journal_date", journal_date);
        result.put("journal_entry", journal_entry);
        result.put("journal_mode", journal_mode);

        return result;
    }
}
