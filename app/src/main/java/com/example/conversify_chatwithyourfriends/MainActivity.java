package com.example.conversify_chatwithyourfriends;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser me = FirebaseAuth.getInstance().getCurrentUser();
    String my_mobile_number;
    String name,account_status;

    static boolean continueLoop = true;

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    DocumentReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (isNetworkAvailable()){

            if (me != null) {
                reference = db.collection("user_list").document(me.getPhoneNumber());
                final SharedPreferences sp = this.getSharedPreferences("com.example.conversify_chatwithyourfriends",MODE_PRIVATE);

                reference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        account_status = documentSnapshot.get("about").toString();
                        name = documentSnapshot.get("name").toString();
                        Calendar calendar = Calendar.getInstance();
                        String day = calendar.get(Calendar.DAY_OF_MONTH) + "";
                        String month = (calendar.get(Calendar.MONTH) + 1) + "";
                        String year = calendar.get(Calendar.YEAR) + "";
                        String minute = calendar.get(Calendar.MINUTE) + "";
                        String hour = calendar.get(Calendar.HOUR_OF_DAY) + "";

                        Map<String,String> map = new HashMap<>();
                        map.put("day",day);
                        map.put("month",month);
                        map.put("year",year);
                        map.put("minute",minute);
                        map.put("hour",hour);
                        map.put("mobile",me.getPhoneNumber() + "");
                        map.put("status","online");
                        map.put("name",name);
                        map.put("about",account_status);
                        reference.set(map);
                    }
                });

            }
        }

        CountDownTimer timer = new CountDownTimer(3000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                //Do nothing
            }

            @Override
            public void onFinish() {
                finish();
                Intent intent;

                SharedPreferences sp = getSharedPreferences("com.example.conversify_chatwithyourfriends", MODE_PRIVATE);
                boolean isAccountCreated = sp.getBoolean("isAccountCreated", false);
                if (isAccountCreated) {
                    intent = new Intent(MainActivity.this, FriendListActivity.class);
                } else {
                    intent = new Intent(MainActivity.this, com.example.conversify_chatwithyourfriends.AccountActivity.class);
                }
                startActivity(intent);
            }
        }.start();
    }

}