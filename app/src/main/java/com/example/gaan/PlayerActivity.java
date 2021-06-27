package com.example.gaan;


import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.cunoraz.gifview.library.GifView;

import java.io.File;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class PlayerActivity extends AppCompatActivity {

    Button PlayButton, NextButton, PreviousButton,FastForward, FastBackward;
    TextView SongName, SongStart, SongEnd;
    SeekBar SeekMusicBar;

    ImageView imageView;
    GifView gifView;

    String Name;
    public static final String Extra_name="song_name";
    static MediaPlayer mediaPlayer;
    int position;
    ArrayList<File> mySongs;

    Thread updateSeekbar;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        getSupportActionBar().setTitle("Bajate Raho");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        PlayButton=findViewById(R.id.PlayButton);
        NextButton=findViewById(R.id.Next);
        PreviousButton=findViewById(R.id.Previous);
        FastForward=findViewById(R.id.Forward);
        FastBackward=findViewById(R.id.Rewind);

        SongName=findViewById(R.id.SongName);
        SongStart=findViewById(R.id.startTime);
        SongEnd=findViewById(R.id.endTime);

        SeekMusicBar=findViewById(R.id.seekBar);
        //BarVisualizer barVisualizer =findViewById(R.id.wave);
        
        imageView=findViewById(R.id.imageView);

        gifView=(GifView)findViewById(R.id.gifView);
        gifView.setVisibility(View.VISIBLE);

       // Glide.with(this).load(R.drawable.tenor).into(imageView);

        if (mediaPlayer != null){
            mediaPlayer.start();
            mediaPlayer.release();
        }
        Intent intent=getIntent();
        Bundle bundle=intent.getExtras();

        mySongs=(ArrayList)bundle.getParcelableArrayList("songs");
        String sName=intent.getStringExtra("songname");
        position = bundle.getInt("position",0);
        SongName.setSelected(true);
        Uri uri=Uri.parse(mySongs.get(position).toString());
        Name=mySongs.get(position).getName();
        SongName.setText(Name);
        mediaPlayer = MediaPlayer.create(getApplicationContext(),uri);
        mediaPlayer.start();

        updateSeekbar=new Thread(){
            @Override
            public void run() {
                int totalDuration = mediaPlayer.getDuration();
                int currentPosition = 0;
                //updates the progress bar or seekbar
                while (currentPosition < totalDuration){
                    try {
                        sleep(500);
                        currentPosition = mediaPlayer.getCurrentPosition();
                        SeekMusicBar.setProgress(currentPosition);
                    }catch (InterruptedException | IllegalStateException e){
                        e.printStackTrace();
                    }
                }
                super.run();
            }
        };

        SeekMusicBar.setMax(mediaPlayer.getDuration());
        updateSeekbar.start();
        SeekMusicBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.purple_200), PorterDuff.Mode.MULTIPLY);
        SeekMusicBar.getThumb().setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_IN);

        //manually sync the seekbar
        SeekMusicBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
            }
        });

        String endTime=createTime(mediaPlayer.getDuration());
        SongEnd.setText(endTime);

        //to update the start time with the progressing song
        final Handler handler = new Handler();
        final int delay = 1000;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String currentTime = createTime(mediaPlayer.getCurrentPosition());
                SongStart.setText(currentTime);
                handler.postDelayed(this,delay);
            }
        },delay);

        PlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaPlayer.isPlaying()){
                    PlayButton.setBackgroundResource(R.drawable.play_btn);
                    mediaPlayer.pause();
                    gifView.pause();
                }else{
                    PlayButton.setBackgroundResource(R.drawable.pause_btn);
                    mediaPlayer.start();
                    gifView.play();
                }
            }
        });
        //if one song finishes,it will automatically click on nextButton and play next song
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                NextButton.performClick();
            }
        });


        //next button will perform below when clicked
        NextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position=((position+1) % mySongs.size());
                Uri uri=Uri.parse(mySongs.get(position).toString());
                mediaPlayer=MediaPlayer.create(getApplicationContext(), uri);
                Name=mySongs.get(position).getName();
                SongName.setText(Name);
                //String endTime=createTime(mediaPlayer.getDuration());
                SongEnd.setText(createTime(mediaPlayer.getDuration()));
                PlayButton.setBackgroundResource(R.drawable.pause_btn);
                mediaPlayer.start();
                gifView.play();
            }
        });
        PreviousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position=((position-1)<0) ? (mySongs.size()-1) : (position-1);
                Uri uri=Uri.parse(mySongs.get(position).toString());
                mediaPlayer=MediaPlayer.create(getApplicationContext(), uri);
                Name=mySongs.get(position).getName();
                SongName.setText(Name);
               // String endTime=createTime(mediaPlayer.getDuration());
                SongEnd.setText(createTime(mediaPlayer.getDuration()));
                mediaPlayer.start();
                PlayButton.setBackgroundResource(R.drawable.pause_btn);
                gifView.play();
            }
        });
        FastForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer.isPlaying()){
                    //10 sec = 10000 milli seconds
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()+10000);
                }
            }
        });
        FastBackward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer.isPlaying()){
                        //10 sec = 10000 milli seconds, decreasing by 10 sec
                        mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()-10000);
                    }
            }
        });
    }
    public String createTime(int duration){
        String time="";
        int min = (duration/1000)/60;
        int sec = (duration/1000)%60;
        time = time+min+":";
        if(sec<10){
            time+="0";
        }
        time+=sec;
        return time;
    }
}