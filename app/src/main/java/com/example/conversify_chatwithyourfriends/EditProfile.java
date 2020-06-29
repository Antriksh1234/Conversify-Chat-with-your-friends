package com.example.conversify_chatwithyourfriends;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class EditProfile extends AppCompatActivity {

    ImageView profile_image;
    EditText my_name,my_status;
    FirebaseUser me;
    Uri imageUri;
    ProgressDialog dialog;
    boolean user_dp_changed;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == RESULT_OK && data != null && data.getData() != null){
            profile_image.setImageURI(data.getData());
            imageUri = data.getData();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 0 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent,0);
        }
    }

    //Save button to update profile
    public void saveChanges(View view){
        if (imageUri!=null) {
            StorageReference imageReference = FirebaseStorage.getInstance().getReference("profile pics/" + me.getPhoneNumber() + ".jpg");
            dialog = ProgressDialog.show(this, "Loading", "Please wait...", true);
            imageReference.putFile(imageUri)
            .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (!task.isSuccessful()){
                        Toast.makeText(EditProfile.this, "Something went wrong :( Please try later", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    else{
                        dialog.dismiss();
                        user_dp_changed = true;
                    }
                }
            });
        }

        if (my_name.getText().length() > 0 && my_status.getText().length() > 0){
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference my_details_document = db.collection("user_list").document(me.getPhoneNumber());
            my_details_document.update("name",my_name.getText().toString());
            my_details_document.update("about",my_status.getText().toString());
        }
        Toast.makeText(this, "Changes saved", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected void onDestroy() {
        if (user_dp_changed)
            dialog.dismiss();
        super.onDestroy();
    }

    public void changeDp(View view){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent,0);
        } else{
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},0);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        imageUri = null;

        my_name = findViewById(R.id.my_profile_name);
        profile_image = findViewById(R.id.my_photo_on_conversify);
        my_status = findViewById(R.id.status_of_my_account);

        me = FirebaseAuth.getInstance().getCurrentUser();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String my_number = me.getPhoneNumber() + "";

        Toast.makeText(this, ""+my_number, Toast.LENGTH_SHORT).show();

        DocumentReference reference = db.collection("user_list").document(my_number);

        reference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String name =  documentSnapshot.get("name").toString();
                String status_of_my_account = documentSnapshot.get("about").toString();
                my_name.setText(name);
                my_status.setText(status_of_my_account);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EditProfile.this, "Not able to fetch your username", Toast.LENGTH_SHORT).show();
            }
        });

        StorageReference imageReference = FirebaseStorage.getInstance().getReference("profile pics");

        StorageReference fileRef = imageReference.child(my_number + ".jpg");

        fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                profile_image.setBackgroundResource(android.R.color.transparent);
                Glide.with(EditProfile.this).load(uri).into(profile_image);
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //Nothing just made so that program continues
            }
        });
    }
}