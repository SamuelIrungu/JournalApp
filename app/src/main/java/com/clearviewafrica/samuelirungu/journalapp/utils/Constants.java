package com.clearviewafrica.samuelirungu.journalapp.utils;

import com.clearviewafrica.samuelirungu.journalapp.activities.MainActivity;

/**
 * Contains Constants for JournalModel app
 */
public class Constants {

    public static final String TAG = MainActivity.class.getName();

    public static final String ON_CREATE = "onCreate";
    public static final String ON_START = "onStart";
    public static final String ON_RESUME = "onResume";
    public static final String ON_PAUSE = "onPause";
    public static final String ON_STOP = "onStop";
    public static final String ON_RESTART = "onRestart";
    public static final String ON_DESTROY = "onDestroy";
    public static final String ON_SAVE_INSTANCE_STATE = "onSaveInstanceState";
    public static final int RC_SIGN_IN = 9001;

    public static final String DB_NAME = "JournalDB";
    public static final int DB_VERSION = 1;
    public static final String JOURNALS_TABLE = "journals";
    public static final String JOURNAL_MODE = "journal_mod";
    public static final String JOURNAL_CONTENT = "journal_content";
    public static final String JOURNAL_DATE  = "journal_date";
    public static final String UNIQUE_ID = "_uid_";
    public static final String JOURNAL_KEY = "_key_";
}
