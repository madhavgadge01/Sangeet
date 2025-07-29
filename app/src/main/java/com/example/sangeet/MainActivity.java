package com.example.sangeet;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ListView listView;
    ArrayList<File> mySongs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listView);

        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_AUDIO;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        Dexter.withContext(this)
                .withPermission(permission)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        mySongs = fetchSongs(Environment.getExternalStorageDirectory());
                        ArrayList<String> songNames = new ArrayList<>();
                        ArrayList<String> songPaths = new ArrayList<>();

                        for (File song : mySongs) {
                            songNames.add(song.getName().replace(".mp3", ""));
                            songPaths.add(song.getAbsolutePath());
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                MainActivity.this,
                                R.layout.custom_list_item,
                                R.id.textItem,
                                songNames
                        );
                        listView.setAdapter(adapter);

                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                Intent intent = new Intent(MainActivity.this, play_song.class);
                                intent.putStringArrayListExtra("songsList", songPaths);
                                intent.putExtra("currentSongTitle", songNames.get(position));
                                intent.putExtra("position", position);
                                startActivity(intent);
                            }
                        });
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(MainActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest request, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                })
                .check();
    }

    public ArrayList<File> fetchSongs(File file) {
        ArrayList<File> songList = new ArrayList<>();
        File[] files = file.listFiles();

        if (files != null) {
            for (File myFile : files) {
                if (!myFile.isHidden() && myFile.isDirectory()) {
                    songList.addAll(fetchSongs(myFile));
                } else if (myFile.getName().endsWith(".mp3") && !myFile.getName().startsWith(".")) {
                    songList.add(myFile);
                }
            }
        }

        return songList;
    }
}