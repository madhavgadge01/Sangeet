package com.example.sangeet;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.ArrayList;

public class play_song extends AppCompatActivity {

    TextView textView;
    ImageView play, next, prev;
    ArrayList<String> songs;
    MediaPlayer mediaPlayer;
    int position;
    SeekBar seekBar;
    Thread updateSeek;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_song);

        textView = findViewById(R.id.textView);
        play = findViewById(R.id.playy);
        next = findViewById(R.id.nextt);
        prev = findViewById(R.id.prevv);
        seekBar = findViewById(R.id.seekBarr);

        Intent intent = getIntent();
        songs = intent.getStringArrayListExtra("songsList");
        position = intent.getIntExtra("position", 0);

        playSong(position);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer != null) {
                    mediaPlayer.seekTo(seekBar.getProgress());
                }
            }
        });

        updateSeek = new Thread(() -> {
            try {
                while (mediaPlayer != null) {
                    if (mediaPlayer.isPlaying()) {
                        int currentPos = mediaPlayer.getCurrentPosition();
                        runOnUiThread(() -> seekBar.setProgress(currentPos));
                        Thread.sleep(800);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        updateSeek.start();

        play.setOnClickListener(v -> {
            if (mediaPlayer.isPlaying()) {
                play.setImageResource(R.drawable.playyy);
                mediaPlayer.pause();
            } else {
                play.setImageResource(R.drawable.pausee);
                mediaPlayer.start();
            }
        });

        prev.setOnClickListener(v -> {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
            }

            position = (position - 1 + songs.size()) % songs.size();
            playSong(position);
        });

        next.setOnClickListener(v -> {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
            }

            position = (position + 1) % songs.size();
            playSong(position);
        });
    }

    private void playSong(int pos) {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
            }

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(songs.get(pos));  // Using full path directly
            mediaPlayer.prepare();
            mediaPlayer.start();

            seekBar.setMax(mediaPlayer.getDuration());

            File file = new File(songs.get(pos));
            textView.setText(file.getName());

            play.setImageResource(R.drawable.pausee);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

        if (updateSeek != null) {
            updateSeek.interrupt();
        }
    }
}