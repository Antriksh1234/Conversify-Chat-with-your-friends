package com.example.conversify_chatwithyourfriends;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class FriendListActivity extends AppCompatActivity {

    //Contains list of contacted friends on Conversify
    static ArrayList<String> friends_name = new ArrayList<>();

    //Consists of their last messages
    static ArrayList<String> lastMessages = new ArrayList<>();

    //List of images name
    ArrayList<String> profile_pic = new ArrayList<>();

    final ArrayList<String> my_chats_numbers_on_firebase = new ArrayList<>();
    final ArrayList<String> my_phone_number_list = new ArrayList<>();
    final ArrayList<String> my_contacts_name_list = new ArrayList<>();

    static MyFriendListAdapter adapter;
    ListView chat_listView;
    CollectionReference my_chats_ref;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    TextView chat_indicator;

    static FirebaseUser me = FirebaseAuth.getInstance().getCurrentUser();
    //All countries Country codes
    public static final String[] countryAreaCodes = {"93", "355", "213",
            "376", "244", "672", "54", "374", "297", "61", "43", "994", "973",
            "880", "375", "32", "501", "229", "975", "591", "387", "267", "55",
            "673", "359", "226", "95", "257", "855", "237", "1", "238", "236",
            "235", "56", "86", "61", "61", "57", "269", "242", "682", "506",
            "385", "53", "357", "420", "45", "253", "670", "593", "20", "503",
            "240", "291", "372", "251", "500", "298", "679", "358", "33",
            "689", "241", "220", "995", "49", "233", "350", "30", "299", "502",
            "224", "245", "592", "509", "504", "852", "36", "91", "62", "98",
            "964", "353", "44", "972", "39", "225", "1876", "81", "962", "7",
            "254", "686", "965", "996", "856", "371", "961", "266", "231",
            "218", "423", "370", "352", "853", "389", "261", "265", "60",
            "960", "223", "356", "692", "222", "230", "262", "52", "691",
            "373", "377", "976", "382", "212", "258", "264", "674", "977",
            "31", "687", "64", "505", "227", "234", "683", "850", "47", "968",
            "92", "680", "507", "675", "595", "51", "63", "870", "48", "351",
            "1", "974", "40", "7", "250", "590", "685", "378", "239", "966",
            "221", "381", "248", "232", "65", "421", "386", "677", "252", "27",
            "82", "34", "94", "290", "508", "249", "597", "268", "46", "41",
            "963", "886", "992", "255", "66", "228", "690", "676", "216", "90",
            "993", "688", "971", "256", "44", "380", "598", "1", "998", "678",
            "39", "58", "84", "681", "967", "260", "263"};

    DocumentReference reference;

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        reference = db.collection("user_list").document(me.getPhoneNumber());

        reference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String account_status = documentSnapshot.get("about").toString();
                String name = documentSnapshot.get("name").toString();
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
                map.put("status","offline");
                map.put("name",name);
                map.put("about",account_status);
                reference.set(map);
            }
        });

        MainActivity.continueLoop = false;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_edit,menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.edit_your_profile){
            Intent intent = new Intent(this,EditProfile.class);
            startActivity(intent);
            return true;
        }
        else{
            return false;
        }
    }

    class MyFriendListAdapter extends ArrayAdapter<String> {

        ArrayList<String> friend_list;
        ArrayList<String> last_messages;
        ArrayList<String> profile_pic;
        StorageReference storageReference;
        ArrayList<String> my_chats_number;
        boolean user_dp_exists;
        public MyFriendListAdapter(Context context, ArrayList<String> friend_list, ArrayList<String> last_messages,ArrayList<String> profile_pic,ArrayList<String> my_chats_number) {
            super(context, R.layout.custom_friend_listview, friend_list);

            this.friend_list = friend_list;
            this.last_messages = last_messages;
            this.profile_pic = profile_pic;
            this.my_chats_number = my_chats_number;
        }

        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View view = getLayoutInflater().inflate(R.layout.custom_friend_listview, null, true);

            TextView friend_name_textView = view.findViewById(R.id.friend_chat_name);
            TextView last_message_textView = view.findViewById(R.id.last_message_text);
            final ImageView display_picture = view.findViewById(R.id.profile_pic);

            display_picture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(FriendListActivity.this,ViewOtherProfile.class);
                    intent.putExtra("name",friend_list.get(position));
                    intent.putExtra("number",my_chats_number.get(position));
                    startActivity(intent);
                }
            });

            user_dp_exists = true;

            storageReference = FirebaseStorage.getInstance().getReference("profile pics/" + profile_pic.get(position));

            storageReference.getDownloadUrl().addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    user_dp_exists = false;
                }
            })
            .addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    //dp exists
                    Glide.with(FriendListActivity.this).load(uri).into(display_picture);
                }
            });

            friend_name_textView.setText(friend_list.get(position));
            last_message_textView.setText(last_messages.get(position));

            return view;
        }
    }

    public void showAllContactsOnConversify(View view) {
        Intent intent = new Intent(this, ContactListActivity.class);
        finish();
        startActivity(intent);
    }

    public void getListOfAllContactsNamesAndNumbers() {
        ContentResolver cr = this.getContentResolver();

        String[] projection = {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER};

        Cursor c = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection, null, null, null);

        int nameIndex = c.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
        int numberIndex = c.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER);

        c.moveToFirst();

        while (!c.isAfterLast()) {
            my_contacts_name_list.add(c.getString(nameIndex));
            my_phone_number_list.add(c.getString(numberIndex));
            c.moveToNext();
        }

        my_chats_ref = db.collection("user_list").document(me.getPhoneNumber()).collection("my chats");

        my_chats_ref.orderBy("timestamp", Query.Direction.DESCENDING).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot documentSnapshot : task.getResult()) {
                        String number = documentSnapshot.getId().toString();
                        String last_message = documentSnapshot.get("last message").toString();
                        String last_message_sender = documentSnapshot.get("last message sender").toString();

                        my_chats_numbers_on_firebase.add(number);
                        if (last_message_sender.contentEquals(me.getPhoneNumber()))
                            lastMessages.add("You: " + last_message);
                        else
                            lastMessages.add(last_message);
                    }

                    int index_searched = 0;
                    for (String number : my_chats_numbers_on_firebase) {
                        if (my_phone_number_list.contains(number)) {
                            index_searched = my_phone_number_list.indexOf(number);
                            friends_name.add(my_contacts_name_list.get(index_searched));
                        } else if (my_phone_number_list.contains(number.substring(2))) {
                            index_searched = my_phone_number_list.indexOf(number.substring(2));
                            friends_name.add(my_contacts_name_list.get(index_searched));
                        } else if (my_phone_number_list.contains(number.substring(3))) {
                            index_searched = my_phone_number_list.indexOf(number.substring(3));
                            friends_name.add(my_contacts_name_list.get(index_searched));
                        } else if (my_phone_number_list.contains(number.substring(4))) {
                            index_searched = my_phone_number_list.indexOf(number.substring(4));
                            friends_name.add(my_contacts_name_list.get(index_searched));
                        } else if (my_phone_number_list.contains(number.substring(5))) {
                            index_searched = my_phone_number_list.indexOf(number.substring(5));
                            friends_name.add(my_contacts_name_list.get(index_searched));
                        } else if (my_phone_number_list.contains(number.substring(6))) {
                            index_searched = my_phone_number_list.indexOf(number.substring(6));
                            friends_name.add(my_contacts_name_list.get(index_searched));
                        } else if (my_phone_number_list.contains(number.substring(7))) {
                            index_searched = my_phone_number_list.indexOf(number.substring(7));
                            friends_name.add(my_contacts_name_list.get(index_searched));
                        } else {
                            friends_name.add(number);
                        }

                        profile_pic.add(number + ".jpg");
                    }

                    adapter = new MyFriendListAdapter(FriendListActivity.this, friends_name, lastMessages,profile_pic,my_chats_numbers_on_firebase);

                    chat_listView.setAdapter(adapter);

                    chat_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Intent intent = new Intent(FriendListActivity.this,ChatActivity.class);
                            intent.putExtra("other person name",friends_name.get(position));
                            intent.putExtra("other person number",my_chats_numbers_on_firebase.get(position));
                            finish();
                            startActivity(intent);
                        }
                    });

                    if (friends_name.size() == 0) {
                        chat_indicator.setVisibility(View.VISIBLE);
                    } else {
                        chat_indicator.setVisibility(View.INVISIBLE);
                    }

                } else {
                    Toast.makeText(FriendListActivity.this, "Something went wrong,could not load contacts", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getListOfAllContactsNamesAndNumbers();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        chat_indicator = findViewById(R.id.chats_indicator);

        chat_listView = findViewById(R.id.user_all_chats_listview);

        friends_name.clear();
        profile_pic.clear();
        my_chats_numbers_on_firebase.clear();
        my_phone_number_list.clear();
        lastMessages.clear();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            getListOfAllContactsNamesAndNumbers();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, 100);
        }

    }
}