package com.example.conversify_chatwithyourfriends;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Arrays;
import java.util.List;

public class AccountActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 1001;
    boolean verfied_number = false;
    String mobile_number;
    String url;

    //AccountActivity views which haa editText and a validate number button and also a account creation button
    EditText name_editText;
    Button validate_button, make_account_button;
    ImageView imageView;

    //fields for firestore and SharedPreferences
    public static final String SHAREDPREFERENCES_USERNAME_FIELD = "username";
    public static final String SHAREDPREFERENCES_PHONENUMBER_FIELD = "mobile number";
    public static final String USER_LIST = "user_list";

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference;
    StorageReference storageRef;
    Uri imageUri;

    public void startValidation(View view) {
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(new AuthUI.IdpConfig.PhoneBuilder().build());

        // Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                mobile_number = user.getPhoneNumber();
                verfied_number = true;
                make_account_button.setVisibility(View.VISIBLE);
                validate_button.setVisibility(View.INVISIBLE);
                // ...
            } else {
                // Sign in failed. If response is null the user canceled the

                if(response != null){
                    Toast.makeText(this, "Could not sign in", Toast.LENGTH_SHORT).show();
                    Toast.makeText(this, response.getError().toString(), Toast.LENGTH_SHORT).show();
                }
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
            }
        } else if (requestCode == 0){
            if (resultCode == RESULT_OK){
                if (data !=null && data.getData() !=null){
                    imageUri = data.getData();
                    imageView.setImageURI(imageUri);
                }
            }
        }
    }

    public void makeAccount(View view) {
        final String name = name_editText.getText().toString();

        collectionReference = db.collection("user_list");
        User user = new User(name, mobile_number,"online","Available");

        UploadProfilePic();

        collectionReference.document(mobile_number).set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        SharedPreferences sp = getSharedPreferences("com.example.conversify_chatwithyourfriends", MODE_PRIVATE);
                        sp.edit().putBoolean("isAccountCreated", true).apply();
                        Toast.makeText(AccountActivity.this, "Account created", Toast.LENGTH_SHORT).show();
                        sp.edit().putString(SHAREDPREFERENCES_USERNAME_FIELD, name).apply();
                        sp.edit().putString(SHAREDPREFERENCES_PHONENUMBER_FIELD, mobile_number).apply();

                        Intent intent = new Intent(AccountActivity.this,FriendListActivity.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AccountActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        name_editText = findViewById(R.id.editTextPersonName);

        storageRef = FirebaseStorage.getInstance().getReference("profile pics");

        make_account_button = findViewById(R.id.make_account);
        validate_button = findViewById(R.id.validate_button);
        imageView = findViewById(R.id.imageView);
    }

    //On tapping imageView
    public void pickImage(View view) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            getPhoto();
        } else{
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},0);
        }
    }

    public void getPhoto(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 0 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
          getPhoto();
        }
    }

    public void UploadProfilePic(){
        if (imageUri!=null){
            final StorageReference fileReference = storageRef.child(mobile_number + ".jpg");
            final ProgressDialog dialog = ProgressDialog.show(this, "Loading", "Please wait...", true);
            fileReference.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            dialog.dismiss();
                            Toast.makeText(AccountActivity.this, "Uploaded profile pic", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }


}