package com.sachinsah.emailsystem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;

public class StoreUserDetailActivity extends AppCompatActivity {

    private EditText userNameEditText, userPhoneNumberEditText, userAddressEditText, aadCardEditText, serviceTypeEditText, userEmailIdEditText;
    private RadioGroup radioSexGroup;
    private RadioButton radioSexButton;
    ImageView userImageView;

    public static final String FB_STORAGE_PATH = "userImage/";
    public static final String FB_DATABASE_PATH = "userDetail";
    public static final int REQUEST_CODE = 1;
    private AlertDialog dialog_verifying;
    private Uri imgUri, uri;
    Bitmap bm;
    String type;

    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_user_detail);

        userNameEditText = findViewById(R.id.userNameEditText);
        userPhoneNumberEditText = findViewById(R.id.userPhoneNumberEditText);
        userAddressEditText = findViewById(R.id.userAddressEditText);
        aadCardEditText = findViewById(R.id.aadCardEditText);
        serviceTypeEditText = findViewById(R.id.serviceTypeEditText);
        userEmailIdEditText = findViewById(R.id.userEmailIdEditText);

        userImageView = findViewById(R.id.userImageView);
        radioSexGroup = findViewById(R.id.genderRadioGroup);

        mDatabaseRef = FirebaseDatabase.getInstance().getReference(FB_DATABASE_PATH);
        mStorageRef = FirebaseStorage.getInstance().getReference();

        Intent intent = getIntent();
        type = intent.getStringExtra("type");

        if(type.equals("email")){
            userEmailIdEditText.setText(intent.getStringExtra("emailId"));
            userEmailIdEditText.setEnabled(false);
        }else if(type.equals("phone")){
            userPhoneNumberEditText.setText(intent.getStringExtra("phoneNumber"));
            userPhoneNumberEditText.setEnabled(false);
        }else if(type.equals("gmail")){
            userEmailIdEditText.setText(intent.getStringExtra("mailId"));
            userNameEditText.setText(intent.getStringExtra("name"));
            userEmailIdEditText.setEnabled(false);
            userNameEditText.setEnabled(false);

            uri = Uri.parse(intent.getStringExtra("uri"));
            try{
                Glide.with(this).load(uri).into(userImageView);
            }catch (NullPointerException e){
                Toast.makeText(getApplicationContext(),"image not found",Toast.LENGTH_LONG).show();
            }

        }
    }


    // uploading profile detail
    public void btnBrowse_Click(View v) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imgUri = data.getData();


            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(1, 1)
                    .start(this);

            /*   bm = MediaStore.Images.Media.getBitmap(getContentResolver(), imgUri);
                imageView.setImageBitmap(bm);
            } catch (FileNotFoundException e) {
                e.printStackTrace(); */
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                assert result != null;
                Uri cropUri = result.getUri();

                try {
                    bm = MediaStore.Images.Media.getBitmap(getContentResolver(), cropUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                userImageView.setImageBitmap(bm);

            }
        }
    }


    public String getImageExt(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    public void Upload_Click(View view) {

        // get selected radio button from radioGroup
        int selectedId = radioSexGroup.getCheckedRadioButtonId();
        // find the radiobutton by returned id
        radioSexButton = (RadioButton) findViewById(selectedId);

        if(serviceTypeEditText.getText() == null || userPhoneNumberEditText.getText() == null || userAddressEditText.getText() == null|| userNameEditText.getText() == null || userAddressEditText.getText() == null || userEmailIdEditText.getText() == null){
            Toast.makeText(getApplicationContext(), "Please fill all the details", Toast.LENGTH_SHORT).show();
            return;
        }

        if(type.equals("gmail")) {
            //Display success toast msg
            Toast.makeText(getApplicationContext(), "Profile Created", Toast.LENGTH_SHORT).show();
            UserDetailActivity userDetail = new UserDetailActivity(uri.toString(), userNameEditText.getText().toString(), userAddressEditText.getText().toString(), userPhoneNumberEditText.getText().toString(), aadCardEditText.getText().toString(), serviceTypeEditText.getText().toString(), radioSexButton.getText().toString(), userEmailIdEditText.getText().toString());

            //Save image info in to firebase database
            String uploadId = mDatabaseRef.push().getKey();
            assert uploadId != null;
            mDatabaseRef.child(uploadId).setValue(userDetail);
        }

        if (imgUri != null){

            LayoutInflater inflater = getLayoutInflater();
            View alertLayout = inflater.inflate(R.layout.processing_dialog, null);
            AlertDialog.Builder show = new AlertDialog.Builder(StoreUserDetailActivity.this);

            alertLayout.setBackgroundColor(292929);
            show.setView(alertLayout);
            show.setCancelable(false);
            dialog_verifying = show.create();
            dialog_verifying.setMessage("Creating profile please wait...");
            dialog_verifying.show();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            //here you can choose quality factor in third parameter(ex. i choosen 25)
            bm.compress(Bitmap.CompressFormat.JPEG, 25, baos);
            byte[] fileInBytes = baos.toByteArray();

            //Get the storage reference
            final StorageReference ref = mStorageRef.child(FB_STORAGE_PATH + System.currentTimeMillis() + "." + imgUri.getLastPathSegment());

            //Add file to reference
            ref.putFile(imgUri).continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw Objects.requireNonNull(task.getException());
                }
                return ref.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {

                    //Dimiss dialog when success
                    dialog_verifying.dismiss();

                    Uri downloadUri = task.getResult();

                    //Display success toast msg
                    Toast.makeText(getApplicationContext(), "Profile Created", Toast.LENGTH_SHORT).show();
                    assert downloadUri != null;
                    UserDetailActivity userDetail = new UserDetailActivity(downloadUri.toString(), userNameEditText.getText().toString(), userAddressEditText.getText().toString(), userPhoneNumberEditText.getText().toString(), aadCardEditText.getText().toString(), serviceTypeEditText.getText().toString(), radioSexButton.getText().toString(), userEmailIdEditText.getText().toString());

                    //Save image info in to firebase database
                    String uploadId = mDatabaseRef.push().getKey();
                    assert uploadId != null;
                    mDatabaseRef.child(uploadId).setValue(userDetail);

                } else {
                    Toast.makeText(getApplicationContext(), "Please select your profile picture" + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    //Dimiss dialog when success
                    dialog_verifying.dismiss();
                }
            });
        }

    }
}
