package com.example.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class MainActivity extends AppCompatActivity {

    private StorageReference mStorageRef;
    private SharedPreferences sharedPreferences;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;
    private final int REQUEST_CODE = 1234;
    private ImageView imageView;
    private final String SHARED_PREF = "uploads";
    private Uri filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        mStorageRef = FirebaseStorage.getInstance().getReference();
        imageView = findViewById(R.id.selected_image);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Registers a photo picker activity launcher in single-select mode.
        pickMedia = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            // Callback is invoked after the user selects a media item or closes the
            // photo picker.
            if (uri == null) {
                Log.d("PhotoPicker", "No media selected");
                return;
            }

            Log.d("PhotoPicker", "Selected URI: " + uri);
            imageView.setImageURI(uri); // setting selected image to image view
            filePath = uri; // setting selected image path to filePath
        });

        findViewById(R.id.select_button).setOnClickListener(this::onSelectImageButtonClick);
        findViewById(R.id.upload_button).setOnClickListener(this::onUploadImageButtonClick);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu); //creating menu
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.view_images) {
            sharedPreferences = getSharedPreferences(SHARED_PREF, MODE_PRIVATE); //getting shared preferences
            final int count = sharedPreferences.getInt("count", 0); //getting count
            if (count > 0) {
                startActivity(new Intent(getApplicationContext(), UploadedActivity.class)); //starting activity
            } else {
                showToast("Upload an image to view!"); // showing toast
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void onUploadImageButtonClick(View view) {
        if (filePath == null) {
            showToast("Upload failed!"); // showing toast
            return;
        }

        final ProgressDialog progressDialog = new ProgressDialog(this); //creating progress dialog
        progressDialog.setTitle("Uploading"); // setting title for progress dialog
        progressDialog.show(); // showing progress dialog

        sharedPreferences = getSharedPreferences(SHARED_PREF, MODE_PRIVATE); // getting shared preferences
        final int count = sharedPreferences.getInt("count", 0); //getting count

        StorageReference storageReference = mStorageRef.child(count + ".jpg"); // setting path/filename to be saved in server

        storageReference.putFile(filePath) // adding file
                //called when file upload is success
                .addOnSuccessListener(taskSnapshot -> {
                    // Get a URL to the uploaded content
                    Task<Uri> downloadUri = taskSnapshot.getStorage().getDownloadUrl();
                    // wait for upload to complete
                    downloadUri.addOnSuccessListener(uri -> {
                        SharedPreferences.Editor editor = sharedPreferences.edit(); //getting shared preference file
                        editor.putString("uri" + count, String.valueOf(uri)); //adding url of uploaded image
                        showToast("File Uploaded!"); // showing toast
                        editor.putInt("count", count + 1); // incrementing count in shared preferences
                        editor.apply(); // commit changes
                        imageView.setImageDrawable(null); //setting image view to empty
                        progressDialog.dismiss(); // dismissing progress dialog
                        filePath = null; // setting file path to null
                    });
                })
                //called when upload fails
                .addOnFailureListener(exception -> {
                    progressDialog.dismiss(); // dismissing progress dialog
                    showToast("Upload failed! Reason: " + exception.getMessage()); // showing toast
                })
                //called when upload progresses
                .addOnProgressListener(taskSnapshot -> {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    progressDialog.setMessage("Uploaded (" + progress + "%)"); // setting message on how much has been uploaded
                });
    }

    private void onSelectImageButtonClick(View view) {
        // Launch the photo picker and let the user choose only images.
        pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    private void showToast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }
}