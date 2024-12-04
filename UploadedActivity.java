package com.example.myapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class UploadedActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uploaded);

        RecyclerView uploadedImagesView = findViewById(R.id.uploaded_images_recycler_view);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        uploadedImagesView.setLayoutManager(linearLayoutManager);

        UploadedImageAdapter imageAdapter = new UploadedImageAdapter(getUris(), getApplicationContext()); // getting adapter
        uploadedImagesView.setAdapter(imageAdapter); // setting adapter
    }

    /**
     * Generates list of all urls in shared preferences
     *
     * @return a list of strings if not null
     */
    private List<String> getUris() {
        String SHARED_PREF = "uploads";
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF, MODE_PRIVATE);
        int count = sharedPreferences.getInt("count", 0);
        if (count > 0) {
            List<String> uris = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                String uri = sharedPreferences.getString("uri" + i, null);
                if (uri != null) {
                    uris.add(uri);
                    Log.d("URI:UPLOAD ACT", uri);
                }
            }
            return uris;
        }
        return null;
    }
}
