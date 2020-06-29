package com.example.conversify_chatwithyourfriends;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;

public class ContactListActivity extends AppCompatActivity {


    //All countries Country codes
    public static final String[] countryAreaCodes = { "93", "355", "213",
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
            "39", "58", "84", "681", "967", "260", "263" };

    ListView contacts;
    ArrayList<String> contact_list;
    ArrayList<String> resultSetList;
    ArrayList<String> phone_number_list = new ArrayList<>();
    final ArrayList<String> firebaseList = new ArrayList<>();
    ArrayAdapter<String> adapter;
    ContentResolver cr;
    TextView info_textView;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                String [] projection = new String[] {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,ContactsContract.CommonDataKinds.Phone.NUMBER};
                Cursor cursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection, null, null, null);
                if (cursor != null) {
                    int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                    int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                    cursor.moveToFirst();

                    while (!cursor.isAfterLast()) {
                        String name = cursor.getString(nameIndex);
                        String number = cursor.getString(numberIndex);
                        contact_list.add(name);
                        phone_number_list.add(number);
                        cursor.moveToNext();
                    }

                    get_users_in_conversify_of_contact_list();

                    cursor.close();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        finish();
        Intent intent = new Intent(this,FriendListActivity.class);
        startActivity(intent);
    }

    public void get_users_in_conversify_of_contact_list(){
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final ArrayList<String> numbers_in_firestore_list = new ArrayList<>();
        CollectionReference cRef = db.collection("user_list");
                cRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (DocumentSnapshot documentSnapshot : task.getResult()){
                        User user;
                        user = documentSnapshot.toObject(User.class);
                        firebaseList.add(user.getMobile());
                    }

                    int index_searched = 0;
                    SharedPreferences sp = getSharedPreferences("com.example.conversify_chatwithyourfriends",MODE_PRIVATE);
                    final String my_number = sp.getString(AccountActivity.SHAREDPREFERENCES_PHONENUMBER_FIELD,"00000");
                    for (String user_no : phone_number_list){
                        if (firebaseList.contains(user_no) && !user_no.contentEquals(my_number)){
                            resultSetList.add(contact_list.get(index_searched));
                            numbers_in_firestore_list.add(user_no);
                        }
                        else {
                            for (String countryCode : countryAreaCodes){
                                if (firebaseList.contains("+" + countryCode + user_no) && !("+"+countryCode+user_no).contentEquals(my_number)){
                                    if (!resultSetList.contains(contact_list.get(index_searched))) {
                                        resultSetList.add(contact_list.get(index_searched));
                                        numbers_in_firestore_list.add("+" + countryCode + user_no);
                                    }
                                }
                            }
                        }
                        index_searched++;
                    }

                    adapter.notifyDataSetChanged();
                    contacts.setAdapter(adapter);

                    contacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            String other_number = numbers_in_firestore_list.get(position);

                            DocumentReference myRef,otherRef;

                            myRef = db.collection("user_list").document(my_number).collection("my chats").document(other_number);
                            otherRef = db.collection("user_list").document(other_number).collection("my chats").document(my_number);

                            HashMap<String,String> details = new HashMap<>();
                            HashMap<String,String> details_on_other_phone = new HashMap<>();

                            details_on_other_phone.put("person number",my_number);
                            details.put("person number",other_number);

                            myRef.set(details);
                            otherRef.set(details_on_other_phone);

                            db.collection("user_list").document(my_number).collection("my chats").document(other_number).collection("all messages");
                            db.collection("user_list").document(other_number).collection("my chats").document(my_number).collection("all messages");
                            Intent intent = new Intent(ContactListActivity.this,ChatActivity.class);
                            intent.putExtra("other person number",other_number);
                            intent.putExtra("other person name",resultSetList.get(position));
                            finish();
                            startActivity(intent);
                        }
                    });

                    if (resultSetList.size() == 0){
                        info_textView.setVisibility(View.VISIBLE);
                    }
                    else{
                        info_textView.setVisibility(View.INVISIBLE);
                    }
                }
                else{
                    Toast.makeText(ContactListActivity.this, "Error loading contacts", Toast.LENGTH_SHORT).show();
                }
            }

        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);

        contacts = findViewById(R.id.contacts_on_conversify);
        info_textView = findViewById(R.id.textView4);

        contact_list = new ArrayList<>();
        resultSetList = new ArrayList<>();

        adapter = new ArrayAdapter<String>(ContactListActivity.this, android.R.layout.simple_list_item_1,resultSetList);
        cr = this.getContentResolver();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS,}, 0);
        } else {
            String[] projection    = new String[] {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER};

            Cursor cursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,projection, null, null, null);
            if (cursor != null) {
                int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                cursor.moveToFirst();

                while (!cursor.isAfterLast()) {
                    String name = cursor.getString(nameIndex);
                    String number = cursor.getString(numberIndex);
                    contact_list.add(name);
                    phone_number_list.add(number);
                    cursor.moveToNext();
                }

                get_users_in_conversify_of_contact_list();

                cursor.close();
            }
        }
    }
}