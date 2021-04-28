package com.inkhornsolutions.riderapp;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class Profile extends AppCompatActivity {

    private EditText etFirstName, etLastName, etEmailAddress, etPhoneNumber;
    private Button btnSave;
    private ImageButton ivAddImage;
    private CircleImageView ivProfile;
    private Uri imageUri;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private DocumentReference documentReference;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#02AA4E")));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("ProfileDetail");

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        documentReference = firebaseFirestore.collection("Users").document(firebaseAuth.getUid());
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        etFirstName = (EditText) findViewById(R.id.etFirstName);
        etLastName = (EditText) findViewById(R.id.etLastName);
        etEmailAddress = (EditText) findViewById(R.id.etEmailAddress);
        etPhoneNumber = (EditText) findViewById(R.id.etPhoneNumber);
        btnSave = (Button) findViewById(R.id.btnSave);
        ivAddImage = (ImageButton) findViewById(R.id.ivAddImage);
        ivProfile = (CircleImageView) findViewById(R.id.ivProfile);

        progressDialog = new ProgressDialog(this);
        progressDialog.show();
        progressDialog.setCancelable(false);
        progressDialog.setContentView(R.layout.progress_layout);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()) {

                        progressDialog.dismiss();

                        String phone = documentSnapshot.getString("phoneNumber");
                        etPhoneNumber.setText(phone);

                        if (documentSnapshot.getString("firstName") != null ||
                                documentSnapshot.getString("lastName") != null ||
                                documentSnapshot.getString("emailAddress") != null ||
                                documentSnapshot.getString("driverProfileImage") != null) {

                            String fName = documentSnapshot.getString("firstName");
                            etFirstName.setText(fName);
                            String lName = documentSnapshot.getString("lastName");
                            etLastName.setText(lName);
                            String email = documentSnapshot.getString("emailAddress");
                            etEmailAddress.setText(email);
                            String imageUri = documentSnapshot.getString("driverProfileImage");
                            Glide.with(Profile.this).load(imageUri).placeholder(ContextCompat.getDrawable(getApplicationContext(), R.drawable.person)).into(ivProfile);
                        } else {
                            Snackbar.make(findViewById(android.R.id.content), "No data found!", Snackbar.LENGTH_LONG).setBackgroundTint(getResources().getColor(R.color.myColor)).show();
                        }
                    }
                    else {
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                progressDialog.dismiss();

                            }
                        }, 1000);
                        Snackbar.make(findViewById(android.R.id.content), "No data found!", Snackbar.LENGTH_LONG).setBackgroundTint(getResources().getColor(R.color.myColor)).show();
                    }
                } else {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            progressDialog.dismiss();

                        }
                    }, 1000);
                    Log.d("TAG", task.getException().getMessage());
                }
            }
        });

        ivAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dexter.withContext(Profile.this).withPermission(Manifest.permission.READ_EXTERNAL_STORAGE).withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(intent, 1002);
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        Snackbar.make(findViewById(android.R.id.content), "Please accept permission!", Snackbar.LENGTH_LONG).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fName = etFirstName.getText().toString().trim();
                String lName = etLastName.getText().toString().trim();
                String email = etEmailAddress.getText().toString().trim();

                if (etFirstName.getText().toString().isEmpty()) {
                    etFirstName.setError("Please write your first name!");
                } else if (etLastName.getText().toString().isEmpty()) {
                    etLastName.setError("Please write your last name!");
                } else if (etEmailAddress.getText().toString().isEmpty()) {
                    etEmailAddress.setError("Please write your email!");
                } else {

                    progressDialog.show();
                    progressDialog.setCancelable(false);
                    progressDialog.setContentView(R.layout.progress_layout);
                    progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

                    HashMap<String, Object> userProfile = new HashMap<>();
                    userProfile.put("firstName", fName);
                    userProfile.put("lastName", lName);
                    userProfile.put("emailAddress", email);

                    documentReference.update(userProfile).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                uploadImage(imageUri);

                                progressDialog.dismiss();

                                Snackbar.make(findViewById(android.R.id.content), "Profile created successfully!", Snackbar.LENGTH_LONG).setBackgroundTint(getResources().getColor(R.color.myColor)).show();

                                Handler handler1 = new Handler();
                                handler1.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        Intent intent = new Intent(Profile.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                    }
                                }, 2500);
                            }
                            else {

                                progressDialog.dismiss();

                                Toast.makeText(Profile.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1002 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            ivProfile.setImageURI(imageUri);
        } else {
            Snackbar.make(findViewById(R.id.content), "No data found!", Snackbar.LENGTH_LONG).show();
        }
    }

    private String getFileExtention(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage(Uri imageUri) {

        String userId = firebaseAuth.getUid();

        StorageReference riversRef = storageReference.child("images/Drivers" + "." + getFileExtention(imageUri) + " " + userId);

        riversRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!urlTask.isSuccessful()) ;
                Uri downloadUrl = urlTask.getResult();

                final String sdownload_url = String.valueOf(downloadUrl);

                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("driverProfileImage", sdownload_url);
                hashMap.put("userId", userId);

                firebaseFirestore.collection("Users").document(firebaseAuth.getUid()).update(hashMap)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Snackbar.make(findViewById(android.R.id.content), "Image Uploaded", Snackbar.LENGTH_LONG).setBackgroundTint(getResources().getColor(R.color.myColor)).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Snackbar.make(findViewById(android.R.id.content), e.getMessage(), Snackbar.LENGTH_LONG).setBackgroundTint(getResources().getColor(R.color.myColor)).show();
                            }
                        });
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Snackbar.make(findViewById(android.R.id.content), "Failed to Upload", Snackbar.LENGTH_LONG).setBackgroundTint(getResources().getColor(R.color.myColor)).show();
                    }
                });
    }
}