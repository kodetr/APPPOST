package com.kodetr.apppost;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class AddActivity extends AppCompatActivity {

    private ImageView iv_image;
    private EditText et_note;
    private Button btn_save;

    private DatabaseReference mDReference;
    private FirebaseDatabase mFInstance;
    private StorageReference sReference;

    private Uri resultUriImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setTitle("ADD POST");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        init();
        initFirebase();
    }

    private void init() {

        iv_image = findViewById(R.id.iv_image);
        et_note = findViewById(R.id.et_add_note);
        btn_save = findViewById(R.id.btn_save);

        iv_image.setOnClickListener(view -> CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setRequestedSize(500, 250)
                .start(AddActivity.this));

        et_note.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() != 0) {
                    btn_save.setEnabled(true);
                    btn_save.setBackgroundResource(R.drawable.ractangel_edittext_color);
                    btn_save.setTextColor(getResources().getColor(R.color.white));
                } else {
                    btn_save.setEnabled(false);
                    btn_save.setBackgroundResource(R.drawable.ractangel_edittext_disable);
                    btn_save.setTextColor(getResources().getColor(R.color.gray));
                }
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogSavePost();
            }
        });
    }

    private void initFirebase() {
        FirebaseApp.initializeApp(this);
        mFInstance = FirebaseDatabase.getInstance();
        mDReference = mFInstance.getReference(Constants.POST);
        sReference = FirebaseStorage.getInstance().getReference(Constants.POST);
    }

    private void dialogSavePost() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sure want to save!");

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                savePost();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void savePost() {
        if (resultUriImage != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.show();

            ModelPost post = new ModelPost();
            post.setId(mDReference.push().getKey());

            UploadTask uploadTask = sReference.child(post.getId()).putFile(resultUriImage);
            uploadTask.addOnProgressListener(taskSnapshot -> {

                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                progressDialog.setMessage("Please wait " + ((int) progress) + "%...");

            }).addOnPausedListener(taskSnapshot -> {
                progressDialog.dismiss();
                System.out.println("Upload is paused");

            }).addOnFailureListener(exception -> {
                progressDialog.dismiss();
                Toast.makeText(this, "Process failed", Toast.LENGTH_SHORT).show();
            }).addOnSuccessListener(taskSnapshot -> {
                progressDialog.dismiss();

                Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();
                while (!uri.isComplete()) ;
                Uri url = uri.getResult();

                post.setNote(et_note.getText().toString());
                post.setImage_url(url.toString());

//                save db
                mDReference.child(post.getId())
                        .setValue(post).addOnSuccessListener(this, new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void mVoid) {
                                progressDialog.dismiss();
                                Toast.makeText(AddActivity.this, "saved successfully!", Toast.LENGTH_LONG).show();
                                finish();
                            }
                        });
            });

        } else {
            Toast.makeText(this, "Image not entered", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultUriImage = result.getUri();
                try {
                    InputStream inputStream = getContentResolver().openInputStream(resultUriImage);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    iv_image.setImageBitmap(bitmap);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}

