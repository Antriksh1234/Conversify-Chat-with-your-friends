package com.example.conversify_chatwithyourfriends;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firestore.v1.DocumentTransform;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ChatActivity<eventListener> extends AppCompatActivity {

    ListView listView;
    TextView friend_name, online_status;
    ArrayList<String> message;
    ArrayList<Integer> message_hour;
    ArrayList<Integer> message_minutes;
    MyCustomAdapter adapter;
    String otherPerson, otherPersonNumber;
    String me;
    EventListener<QuerySnapshot> eventListener;
    ArrayList<Integer> message_sender;
    EditText message_box;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference myChatsRef, otherChatRef;
    boolean check_for_person = true;
    ImageView display_picture;


    @Override
    public void onBackPressed() {
        super.onBackPressed();

        check_for_person = false;

        String msg = message.get(message.size() - 1);
        int msgSender = message_sender.get(message_sender.size() - 1);

        HashMap<String, Object> map = new HashMap<>();
        map.put("last message", msg);
        if (msgSender == 0)
            map.put("last message sender", me);
        else
            map.put("last message sender", otherPersonNumber);

        map.put("timestamp",FieldValue.serverTimestamp());

        db.collection("user_list").document(me).collection("my chats").document(otherPersonNumber).set(map);
        db.collection("user_list").document(otherPersonNumber).collection("my chats").document(me).set(map);

        FriendListActivity.adapter.notifyDataSetChanged();

        Intent intent = new Intent(ChatActivity.this, FriendListActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();

        message_hour.clear();
        message.clear();
        message_minutes.clear();
        message_sender.clear();
        myChatsRef.orderBy("timestamp", Query.Direction.ASCENDING).addSnapshotListener(this, eventListener);
        adapter.notifyDataSetChanged();
    }

    public void addChat(View view) {
        if (message_box.getText().toString().contentEquals("")) {
            Toast.makeText(this, "Write a message", Toast.LENGTH_SHORT).show();
        } else {
            String message = message_box.getText().toString();
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minutes = calendar.get(Calendar.MINUTE);
            Message shared_message = new Message(me, message, hour, minutes);
            Map<String, Object> updates = new HashMap<>();
            updates.put("sender", me);
            updates.put("text", message);
            updates.put("timestamp", FieldValue.serverTimestamp());
            updates.put("hour", hour);
            updates.put("minutes", minutes);
            myChatsRef.add(updates);
            otherChatRef.add(updates);

            myChatsRef.orderBy("timestamp", Query.Direction.ASCENDING).addSnapshotListener(this, eventListener);
            adapter.notifyDataSetChanged();
            message_box.setText("");
        }
    }

    public class MyCustomAdapter extends ArrayAdapter<String> {

        ArrayList<String> messages;
        ArrayList<Integer> sender;
        ArrayList<Integer> message_hour;
        ArrayList<Integer> message_minutes;
        Context context;

        public MyCustomAdapter(Context context, ArrayList<String> messages, ArrayList<Integer> sender, ArrayList<Integer> message_hour, ArrayList<Integer> message_minutes) {
            super(context, R.layout.mytext, messages);
            this.context = context;
            this.messages = messages;
            this.sender = sender;
            this.message_hour = message_hour;
            this.message_minutes = message_minutes;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View view;
            TextView message_TextView, time_TextView;
            if (sender.get(position) == 0) {

                view = getLayoutInflater().inflate(R.layout.mytext, null, true);
                message_TextView = view.findViewById(R.id.my_message);
                time_TextView = view.findViewById(R.id.time_of_my_text);

                if (message_minutes.get(position) < 10) {
                    String time_text = message_hour.get(position) + ":0" + message_minutes.get(position);
                    time_TextView.setText(time_text);
                } else {
                    String time_text = message_hour.get(position) + ":" + message_minutes.get(position);
                    time_TextView.setText(time_text);
                }

                String msg = messages.get(position);
                message_TextView.setText(msg);
            } else {
                view = getLayoutInflater().inflate(R.layout.othertextfile, null, true);
                message_TextView = view.findViewById(R.id.other_message);
                time_TextView = view.findViewById(R.id.time_of_other_text);

                if (message_minutes.get(position) < 10) {
                    String time_text = message_hour.get(position) + ":0" + message_minutes.get(position);
                    time_TextView.setText(time_text);
                } else {
                    String time_text = message_hour.get(position) + ":" + message_minutes.get(position);
                    time_TextView.setText(time_text);
                }

                String msg = messages.get(position);
                message_TextView.setText(msg);
            }
            return view;
        }
    }

    private void setOnline_status() {
        DocumentReference reference = db.collection("user_list").document(otherPersonNumber);
        reference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                online_status.setText(documentSnapshot.get("status").toString());
                String current_status = documentSnapshot.get("status").toString();
                String status;
                if (current_status.contentEquals("offline")){
                    int day = Integer.parseInt(documentSnapshot.getString("day"));
                    int month = Integer.parseInt(documentSnapshot.getString("month"));
                    int year = Integer.parseInt(documentSnapshot.getString("year"));
                    int hour = Integer.parseInt(documentSnapshot.getString("hour"));
                    int minute = Integer.parseInt(documentSnapshot.getString("minute"));

                    Calendar today = Calendar.getInstance();

                    Calendar last_seen_day = Calendar.getInstance();
                    last_seen_day.set(Calendar.DAY_OF_MONTH,day);
                    last_seen_day.set(Calendar.MONTH,month-1);
                    last_seen_day.set(Calendar.YEAR,year);

                    if (today.compareTo(last_seen_day) == 0){
                        if (minute > 10) {
                            status = "last seen today at " + hour + ":" + minute;
                        } else {
                            status = "last seen today at " + hour + ":0" + minute;
                        }
                    } else {
                        today.add(Calendar.DATE,-1);
                        if (today.compareTo(last_seen_day) == 0){
                            if (minute > 10)
                                status = "last seen yesterday at " + hour + ":" + minute;
                            else
                                status = "last seen yesterday at " + hour + ":0" + minute;
                        }
                        else{
                            if (minute > 10)
                                status = "last seen at " + day + "/" + month + "/" + year + " on " + hour + ":" + minute;
                            else
                                status = "last seen at " + day + "/" + month + "/" + year + " on " + hour + ":0" + minute;
                        }
                    }
                  online_status.setText(status);
                }else {
                    online_status.setText(current_status);
                }
            }
        });
    }

    public void setDisplay_picture(){
        StorageReference reference = FirebaseStorage.getInstance().getReference("profile pics/" + otherPersonNumber + ".jpg");

        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(ChatActivity.this).load(uri).into(display_picture);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        message_box = findViewById(R.id.message_box);
        online_status = findViewById(R.id.online_status);
        friend_name = findViewById(R.id.friend_name);
        display_picture = findViewById(R.id.display_picture);

        //ArrayList of messages
        message = new ArrayList<>();

        //0 for me and 1 for other
        message_sender = new ArrayList<>();

        message_hour = new ArrayList<>();
        message_minutes = new ArrayList<>();

        Intent intent = getIntent();
        otherPersonNumber = intent.getStringExtra("other person number");
        otherPerson = intent.getStringExtra("other person name");

        friend_name.setText(otherPerson);
        setDisplay_picture();

        final Handler handler =new Handler();
        final Runnable r = new Runnable() {
            public void run() {
                handler.postDelayed(this, 3000);
                setOnline_status();
            }
        };
        handler.postDelayed(r, 3000);

        eventListener = new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                message_sender.clear();
                message.clear();
                message_minutes.clear();
                message_hour.clear();
                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    Message msg = documentSnapshot.toObject(Message.class);
                    String sender = msg.getSender();
                    int hour, minutes;
                    hour = msg.getHour();
                    minutes = msg.getMinutes();

                    message_hour.add(hour);
                    message_minutes.add(minutes);

                    String sent_message = msg.getText();
                    message.add(sent_message);
                    if (sender.contentEquals(me)) {
                        message_sender.add(0);
                    } else {
                        message_sender.add(1);
                    }
                }

                adapter.notifyDataSetChanged();
            }
        };

        SharedPreferences sp = getSharedPreferences("com.example.conversify_chatwithyourfriends", MODE_PRIVATE);
        me = sp.getString(AccountActivity.SHAREDPREFERENCES_PHONENUMBER_FIELD, "0000");

        myChatsRef = db.collection("user_list").document(me).collection("my chats").document(otherPersonNumber).collection("all messages");
        otherChatRef = db.collection("user_list").document(otherPersonNumber).collection("my chats").document(me).collection("all messages");

        myChatsRef.orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(this, new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        message_hour.clear();
                        message_minutes.clear();
                        message_sender.clear();
                        message.clear();

                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            Message msg = documentSnapshot.toObject(Message.class);
                            String sender = msg.getSender();
                            String sent_message = msg.getText();
                            int hour, minutes;
                            hour = msg.getHour();
                            minutes = msg.getMinutes();

                            message_hour.add(hour);
                            message_minutes.add(minutes);

                            message.add(sent_message);
                            if (sender.contentEquals(me)) {
                                message_sender.add(0);
                            } else {
                                message_sender.add(1);
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ChatActivity.this, "Something went wrong,could not load messages", Toast.LENGTH_SHORT).show();
                    }
                });


        listView = findViewById(R.id.chat_listView);

        adapter = new MyCustomAdapter(this, message, message_sender, message_hour, message_minutes);

        listView.setAdapter(adapter);
    }

}