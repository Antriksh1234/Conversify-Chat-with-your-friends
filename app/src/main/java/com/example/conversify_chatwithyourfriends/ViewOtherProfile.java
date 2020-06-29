package com.example.conversify_chatwithyourfriends;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ViewOtherProfile extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_other_profile);

        final TextView status_text = findViewById(R.id.status_text);
        TextView contact_name = findViewById(R.id.contact_name_of_profile);
        final ImageView other_person_dp = findViewById(R.id.image_of_other_person);

        Intent intent = getIntent();
        final String name = intent.getStringExtra("name");
        String number = intent.getStringExtra("number");

        contact_name.setText(name);

        final DocumentReference document = FirebaseFirestore.getInstance().collection("user_list").document(number);

        document.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String status = documentSnapshot.get("about").toString();
                status_text.setText(status);
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

        StorageReference imageReference = FirebaseStorage.getInstance().getReference("profile pics/" + number + ".jpg");
        imageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(ViewOtherProfile.this)
                .load(uri)
                .into(other_person_dp);
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }
}