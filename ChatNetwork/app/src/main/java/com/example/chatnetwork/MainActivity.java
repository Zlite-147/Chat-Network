package com.example.chatnetwork;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.app.Activity;
import android.provider.Settings;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;



public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getName();

    private EditText metText;
    private Button mbtSent;
     Dialog dialog;
    private DatabaseReference mFirebaseRef;

    private List<Chat> mChats;
    private RecyclerView mRecyclerView;
    private ChatAdapter mAdapter;
    private String mId;

    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        try {
            if(isNetworkAvailable())
            {
              //  dialog.dismiss();
                metText = (EditText) findViewById(R.id.etText);
                mbtSent = (Button) findViewById(R.id.btSent);
                mRecyclerView = (RecyclerView) findViewById(R.id.rvChat);
                mChats = new ArrayList<>();

                mId = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
                mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                //mRecyclerView.setItemAnimator(new SlideInOutLeftItemAnimator(mRecyclerView));
                mAdapter = new ChatAdapter(mChats, mId);
                mRecyclerView.setAdapter(mAdapter);

                /**
                 * Firebase - Inicialize
                 */
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                mFirebaseRef = database.getReference("message");

                metText.addTextChangedListener(new TextWatcher() {
                    public void afterTextChanged(Editable s) {
                        mbtSent.setBackgroundResource(R.drawable.ic_send_blue_24dp);
                        String Text = metText.getText().toString().trim();
                        if (Text.isEmpty()) {
                            mbtSent.setBackgroundResource(R.drawable.ic_send_black_24dp);
                        }
                    }

                    public void beforeTextChanged(CharSequence s, int start, int count, int after){
                    }
                    public void onTextChanged(CharSequence s, int start, int before, int count){
                    }
                });


                mbtSent.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String message = metText.getText().toString();

                        if (!message.isEmpty()) {
                            /**
                             * Firebase - Send message
                             */
                            mFirebaseRef.push().setValue(new Chat(message, mId));
                        }

                        metText.setText("");
                    }
                });



                mFirebaseRef.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        if (dataSnapshot != null && dataSnapshot.getValue() != null) {
                            try {

                                Chat model = dataSnapshot.getValue(Chat.class);

                                mChats.add(model);
                                mRecyclerView.scrollToPosition(mChats.size() - 1);
                                mAdapter.notifyItemInserted(mChats.size() - 1);
                            } catch (Exception ex) {
                                Log.e(TAG, ex.getMessage());
                            }
                        }
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(TAG, databaseError.getMessage());
                    }
                });
            }
            else
            {

                dialog = new Dialog(this);
                dialog.setContentView(R.layout.layout_custom_dialog);

                final Button btn_Retry = dialog.findViewById(R.id.btn_retry);

                btn_Retry.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = getIntent();
                        finish();
                        startActivity(intent);
                    }
                });
                Toast.makeText(this, "Offline", Toast.LENGTH_SHORT).show();
                dialog.show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private boolean isNetworkAvailable() throws IOException {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
