package com.clearviewafrica.samuelirungu.journalapp.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.clearviewafrica.samuelirungu.journalapp.R;
import com.clearviewafrica.samuelirungu.journalapp.utils.Constants;

public class MainActivity extends AppCompatActivity {

    // Below are android lifecycle hooks which different methods can be called

    /**
     * method called on creation of activity
     * @param savedInstanceState savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lifecycleStageLogger(Constants.ON_CREATE);
    }


    @Override
    protected void onStart() {
        super.onStart();

        lifecycleStageLogger(Constants.ON_START);
    }

    @Override
    protected void onResume() {
        super.onResume();

        lifecycleStageLogger(Constants.ON_RESUME);
    }

    @Override
    protected void onPause() {
        super.onPause();

        lifecycleStageLogger(Constants.ON_PAUSE);
    }


    @Override
    protected void onStop() {
        super.onStop();

        lifecycleStageLogger(Constants.ON_STOP);
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        lifecycleStageLogger(Constants.ON_RESTART);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        lifecycleStageLogger(Constants.ON_DESTROY);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        lifecycleStageLogger(Constants.ON_SAVE_INSTANCE_STATE);
    }

    /**
     * Simple method to log lifecycle stage on console (debug level)
     * @param lifecycleEvent lifecycleEvent
     */
    private void lifecycleStageLogger(String lifecycleEvent) {
        Log.d(Constants.TAG, "Lifecycle Event: " + lifecycleEvent);
    }

}
