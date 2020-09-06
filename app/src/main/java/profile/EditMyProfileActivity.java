package profile;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.healthcare.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditMyProfileActivity extends AppCompatActivity {
    TextInputLayout displayName, fullName, birthDay, phoneNum, height, weight, homeAddress;
    Button btnSave;
    ImageView clickUpload;
    Uri imageUri;
    CircleImageView uploadedImage;
    ProgressDialog progressDialog;


    DatabaseReference rootReference;
    FirebaseUser firebaseUser;
    FirebaseAuth mFirebaseAuth;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_my_profile);
        btnSave = findViewById(R.id.btnSave);
        clickUpload = findViewById(R.id.clickUpload);
        uploadedImage = findViewById(R.id.uploadedImage);

        displayName = findViewById(R.id.txtDisplayName);
        fullName = findViewById(R.id.txtFullName);
        birthDay = findViewById(R.id.txtBirthDay);
        phoneNum = findViewById(R.id.txtPhoneNum);
        height = findViewById(R.id.txtHeight);
        weight = findViewById(R.id.txtWeight);
        homeAddress = findViewById(R.id.txtHomeAddress);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        mFirebaseAuth = FirebaseAuth.getInstance();
        rootReference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference("Patients").child("ProfileImage");
        firebaseUser = mFirebaseAuth.getCurrentUser();
        progressDialog = new ProgressDialog(this);


        clickUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, 12);
            }
        });


        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = Objects.requireNonNull(displayName.getEditText()).getText().toString();
                String fName = Objects.requireNonNull(fullName.getEditText()).getText().toString();
                String bod = Objects.requireNonNull(birthDay.getEditText()).getText().toString();
                String phone = Objects.requireNonNull(phoneNum.getEditText()).getText().toString();
                String heightValue = Objects.requireNonNull(height.getEditText()).getText().toString();
                String weightValue = Objects.requireNonNull(weight.getEditText()).getText().toString();
                String address = Objects.requireNonNull(homeAddress.getEditText()).getText().toString();

                if (name.isEmpty() || fName.isEmpty() || bod.isEmpty() || phone.isEmpty() || heightValue.isEmpty() || weightValue.isEmpty() || address.isEmpty()) {
                    Toast.makeText(EditMyProfileActivity.this, "Please fill all the fields!!", Toast.LENGTH_SHORT).show();
                } else {
                    User myDetails = new User(name, fName, bod, phone, heightValue, weightValue, address);
                    rootReference.child("Patients").child(firebaseUser.getUid()).child("MyProfile").setValue(myDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isComplete()) {
                                Toast.makeText(EditMyProfileActivity.this, "Update Data Successfully", Toast.LENGTH_SHORT).show();
                                finish();
                                Intent myIntent = new Intent(getApplicationContext(), MyProfileActivity.class);
                                startActivity(myIntent);

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
        if (requestCode == 12 && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            uploadedImage.setImageURI(imageUri);

            //upload the image to Firebase
            StorageReference myRef = storageReference.child(firebaseUser.getUid() + ".jpg");
            progressDialog.setMessage("Uploading Image");
            progressDialog.show();
            myRef.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Could not upload image. Try again", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
