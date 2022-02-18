package com.savonikaleksandr.scaner;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

public class HelpActivity extends AppCompatActivity {

    VideoView videoView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        videoView = findViewById(R.id.video_view);
        Intent i = getIntent();
        if (i!= null){
            switch (i.getIntExtra("id",1)){
                case 1 :
                    videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() +"/"+R.raw.admin));
                    videoView.setMediaController(new MediaController(this));
                    videoView.requestFocus(0);
                    videoView.start();
                    break;
                case 2 :
                    videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() +"/"+R.raw.user));
                    videoView.setMediaController(new MediaController(this));
                    videoView.requestFocus(0);
                    videoView.start();
                    break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(HelpActivity.this, Admin_BaseActivity.class);
        startActivity(intent);
    }
}