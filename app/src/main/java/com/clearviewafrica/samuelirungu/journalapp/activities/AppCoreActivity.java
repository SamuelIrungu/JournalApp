package com.clearviewafrica.samuelirungu.journalapp.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.clearviewafrica.samuelirungu.journalapp.DatabaseHelper;
import com.clearviewafrica.samuelirungu.journalapp.R;
import com.clearviewafrica.samuelirungu.journalapp.adapters.EntriesAdapter;
import com.clearviewafrica.samuelirungu.journalapp.model.JournalModel;
import com.clearviewafrica.samuelirungu.journalapp.utils.Constants;
import com.clearviewafrica.samuelirungu.journalapp.utils.TouchSwipeManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppCoreActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    DatabaseHelper databaseHelper;
    EntriesAdapter entriesAdapter;
    RecyclerView recyclerView;
    FloatingActionButton floatingActionButton;
    Button sign_in_btn;

    private FirebaseAuth firebaseAuth;
    private GoogleApiClient googleApiClient;
    private boolean isUserLogged;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_core);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        databaseHelper = DatabaseHelper.getInstance(this);

        recyclerView = findViewById(R.id.recyler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        entriesAdapter = new EntriesAdapter(this);
        recyclerView.setAdapter(entriesAdapter);

        floatingActionButton = findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isUserLogged) {
                    startActivity(new Intent(AppCoreActivity.this, JournalEntryActivity.class));
                } else {
                    databaseHelper.showSnackBar(getString(R.string.login_first), sign_in_btn);
                }
            }
        });
        sign_in_btn = findViewById(R.id.signBut);
        sign_in_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isUserLogged)
                    signOut();
                else
                    signIn();
            }
        });
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.google_web_client_id))
                .requestEmail()
                .build();

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this, this)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        TouchSwipeManager touchSwipeManager = new TouchSwipeManager(this, recyclerView) {
            @Override
            public void initBehindButton(RecyclerView.ViewHolder viewHolder, List<behindButton> behindButtons) {
                behindButtons.add(new behindButton(
                        "Delete",
                        R.mipmap.cancel,
                        Color.parseColor("#FF3C30"),
                        new TouchSwipeManager.UnderlayButtonClickListener() {
                            @Override
                            public void onClick(int position) {
                                databaseHelper.showDebugLog("Deleting");
                                JournalModel journal = entriesAdapter.getJournal(position);
                                if (journal != null) {
                                    if (databaseHelper.deleteJournal(journal)) {
                                        entriesAdapter.removeAt(position);
                                        deleteFromFirebase(journal);
                                    }
                                }
                            }
                        }
                ));
            }
        };
    }

    /**
     * Inflate the menu, adds items to the action bar.
     *
     * @param menu menu
     * @return boolean
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    /**
     * Handle action bar item clicks
     *
     * @param item item
     * @return id of item clicked
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        entriesAdapter.clear();

        if (entriesAdapter.getItemCount() > 0) {
            findViewById(R.id.nrftv).setVisibility(View.GONE);
        } else
            findViewById(R.id.nrftv).setVisibility(View.VISIBLE);


    }

    /**
     * delete entry
     *
     * @param journalModel journal  entry
     */
    private void deleteFromFirebase(JournalModel journalModel) {

        String uid = DatabaseHelper.getSharedPreference(AppCoreActivity.this).getString("SUID", "");
        journalModel.setUnique_id(uid);

        Map<String, Object> childUpdates = new HashMap<>();

        childUpdates.put(getString(R.string.journal_path) + journalModel.getUnique_id() + "/" + journalModel.getJournal_key(), null);

        databaseReference.updateChildren(childUpdates)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        databaseHelper.showDebugLog(getString(R.string.data_delete_success));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        databaseHelper.showDebugLog(e.toString());
                        databaseHelper.showDebugLog(getString(R.string.data_delete_fail));
                    }
                });
    }


    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        updateUI(user);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            databaseHelper.showDebugLog(getString(R.string.sign_in_result) + result.getStatus());
            if (result.isSuccess()) {

                GoogleSignInAccount account = result.getSignInAccount();
                if (account != null) {
                    firebaseAuthGoogle(account);
                }
            } else
                Snackbar.make(sign_in_btn, R.string.sign_in_fail, Snackbar.LENGTH_SHORT).show();

        }

    }

    private void signIn() {
        if (firebaseAuth.getCurrentUser() == null) {
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
            startActivityForResult(signInIntent, Constants.RC_SIGN_IN);
            return;
        }
        signOut();
    }

    private void signOut() {
        databaseHelper.showSnackBar(getString(R.string.sign_out), sign_in_btn);
        firebaseAuth.signOut();
        updateUI(null);
        googleSignOut();
    }

    private void googleSignOut() {
        Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        isUserLogged = false;
                    }
                });
    }

    private void firebaseAuthGoogle(GoogleSignInAccount googleSignInAccount) {

        AuthCredential credential = GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(), null);

        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            updateUI(user);
                        } else {

                            try {
                                databaseHelper.showDebugLog(getString(R.string.sign_in_credential_fail) + task.getException());
                                databaseHelper.showSnackBar(getString(R.string.auth_fail) + task.getException(), sign_in_btn);
                            } catch (Exception e) {
                                databaseHelper.showDebugLog(e.toString());
                            }
                            updateUI(null);
                            googleSignOut();
                        }
                    }
                });
    }

    private void updateUI(final FirebaseUser user) {
        if (user != null) {
            isUserLogged = true;
            sign_in_btn.setText(String.format("%s%s", user.getDisplayName(), getString(R.string.signed_in)));
            databaseHelper.saveSharedPreference("SUID", user.getUid());

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    final Query query = databaseReference.child(getString(R.string.user_journals)).child(user.getUid());
                    query.addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                            JournalModel dwnJournalModel = dataSnapshot.getValue(JournalModel.class);
                            String journalKey = dataSnapshot.getKey();

                            if (dwnJournalModel != null) {
                                dwnJournalModel.setJournal_key(journalKey);
                            }
                            databaseHelper.createJournal(dwnJournalModel);
                        }

                        @Override
                        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String str) {
                            JournalModel dwnJournalModel = snapshot.getValue(JournalModel.class);
                            String journalKey = snapshot.getKey();

                            if (dwnJournalModel != null) {
                                dwnJournalModel.setJournal_key(journalKey);
                            }
                            databaseHelper.createJournal(dwnJournalModel);
                        }

                        @Override
                        public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                            JournalModel journalModel = snapshot.getValue(JournalModel.class);
                            String journalKey = snapshot.getKey();

                            if (journalModel != null) {
                                journalModel.setJournal_key(journalKey);
                            }
                            databaseHelper.showDebugLog(getString(R.string.delete_journal_entry) + databaseHelper.deleteJournal(journalModel));
                        }

                        @Override
                        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            databaseHelper.showDebugLog("onCancelled: " + databaseError.getMessage());
                        }
                    });

                }
            }, 3000);

            final Query query = databaseReference.child(getString(R.string.user_journals)).child(user.getUid());//.orderByChild("starCount");
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                        JournalModel dwnJournalModel = snapshot.getValue(JournalModel.class);
                        String journalKey = snapshot.getKey();

                        if (dwnJournalModel != null) {
                            dwnJournalModel.setJournal_key(journalKey);
                        }

                    }
                    entriesAdapter.clear();
                    entriesAdapter.addData(databaseHelper.getJournals(10));
                    if (entriesAdapter.getItemCount() > 0) {
                        findViewById(R.id.nrftv).setVisibility(View.GONE);
                    } else
                        findViewById(R.id.nrftv).setVisibility(View.VISIBLE);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });


        } else {
            sign_in_btn.setText(getString(R.string.sign_in));
            entriesAdapter.clear();
            findViewById(R.id.nrftv).setVisibility(View.VISIBLE);
        }


    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
