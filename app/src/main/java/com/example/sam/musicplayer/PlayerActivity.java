package com.example.sam.musicplayer;

import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;


import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class PlayerActivity extends AppCompatActivity {

    Button btnPlay,btnSkip,btnPrevious,btnRepeat;
    Boolean repeat = false;
    TextView txtTitle,txtArtist,txtCurrent,txtDuration;
    Timer timer;
    SeekBar seekPercent;
    HashMap<Integer,String> songHash;
    int hashSize, songIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        // releases media player to avoid stacking audio
        if (!(MainActivity.mediaPlayer == null)){
            MainActivity.mediaPlayer.release();
        }

        // create hash
        songHash = (HashMap<Integer, String>) getIntent().getSerializableExtra("hashMap");
        hashSize = songHash.size();
        // create index/pointer
        songIndex = getIntent().getIntExtra("songIndex",0);

        Play();
    }
    /** Plays next song in hash **/
    public void nextSong(){
        MainActivity.mediaPlayer.release();
        // set index to next song
        if (songIndex == (hashSize-1)){ //-1 for position(arrays start at 0)
            songIndex =0; // first soong
        } else {
            songIndex++;
        }
        // cancels repeating
        repeat = false;
        btnRepeat.setBackgroundResource(R.drawable.repeatgrey);

        Play();
    }
    /** Plays previous song in hash **/
    public void previousSong(){
        MainActivity.mediaPlayer.release();
        // set index to last song
        if (songIndex == 0){ // if at first song
            songIndex = hashSize -1; // last song (index 1 less than number of items)
        } else {
            songIndex--;
        }
        // cancels repeating
        repeat = false;
        btnRepeat.setBackgroundResource(R.drawable.repeatgrey);

        Play();
    }

    /** Plays song **/
    public void Play(){
        String song = songHash.get(songIndex);



        Log.d("song", song);
        // raw id of song with name from hashmap
        final int songId = getApplicationContext().getResources().getIdentifier(song, "raw",
                getApplicationContext().getPackageName());

        setTextFields(songId);
        // create mediaplayer
        MainActivity.mediaPlayer = MediaPlayer.create(getApplicationContext(), songId);
        MainActivity.mediaPlayer.start();

        //format duration
        int seconds = MainActivity.mediaPlayer.getDuration() / 1000 % 60 ;
        int minutes = MainActivity.mediaPlayer.getDuration() / (1000*60) % 60;
        String duration = String.format("%02d",minutes) + ":" + String.format("%02d",seconds);
        // set duration of song text
        txtDuration = (TextView) findViewById(R.id.textDuration);
        txtDuration.setText(String.valueOf(duration));

        timer = new Timer();
        startTimer();

        actionListeners();

    }
    /** Runs a new thread that controlles a timer and the seek bar **/
    public void startTimer(){

        seekPercent = (SeekBar) findViewById(R.id.seekBar);

        txtCurrent = (TextView) findViewById(R.id.textCurrent);

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            // create thread
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        seekPercent.setMax(MainActivity.mediaPlayer.getDuration());
                        if (MainActivity.mediaPlayer != null && MainActivity.mediaPlayer.isPlaying()) {
                            // sets and formats current time in song text field
                            int seconds = MainActivity.mediaPlayer.getCurrentPosition() / 1000 % 60 ;
                            int minutes = MainActivity.mediaPlayer.getCurrentPosition() / (1000*60) % 60;
                            String currentTime = String.format("%02d",minutes) + ":" + String.format("%02d",seconds);
                            txtCurrent.setText(String.valueOf(currentTime));
                            // ticks seekbar
                            seekPercent.setProgress(MainActivity.mediaPlayer.getCurrentPosition());
                        }
                    }
                });
            }
        }, 0, 200);
    }
    /** Toggles repeat **/
    public void repeat(){
        if (repeat){
            btnRepeat.setBackgroundResource(R.drawable.repeatgrey);
        } else {
            btnRepeat.setBackgroundResource(R.drawable.repeat);
        }
        repeat = !repeat;
    }

    /** method to play or pause **/
    public void play(){
        if(MainActivity.mediaPlayer.isPlaying()){
            MainActivity.mediaPlayer.pause();
            //btnPlay.setText("Play");
            btnPlay.setBackgroundResource(R.drawable.play);
            //Toast.makeText(getApplicationContext(),"Song Paused", Toast.LENGTH_LONG).show();
        } else {
            btnPlay.setBackgroundResource(R.drawable.pause);
            MainActivity.mediaPlayer.start();
        }
    }

    /** Set text fields values**/
    public void setTextFields(int songId){
        // get meta data
        Uri mediaPath = Uri.parse("android.resource://" + getPackageName() + "/" + songId);
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(this, mediaPath);
        // set strings with chosen meta data
        String sponsorTitle = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        String sponsorArtist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        // set title text field
        txtTitle = (TextView) findViewById(R.id.textTitle);
        txtTitle.setText(sponsorTitle);
        // set artist text field
        txtArtist = (TextView) findViewById(R.id.textArtist);
        txtArtist.setText(sponsorArtist);
    }
    /** Contains all action listeners **/
    public void actionListeners(){
        // When the song ends
        MainActivity.mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {


                if (!repeat){
                    nextSong();
                }else {
                    play();
                }
            }
        });
        // Play/pause listener
        btnPlay = (Button)findViewById(R.id.buttonPlay);
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play();
            }
        });
        // Skip 15s listener
        btnSkip = (Button) findViewById(R.id.buttonSkip);
        btnSkip.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
               // btnPlay.setText("Pause");
                btnPlay.setBackgroundResource(R.drawable.pause);
                nextSong();
            }
        });

        // Previous song listener
        btnPrevious = (Button) findViewById(R.id.buttonPrevious);
        btnPrevious.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //btnPlay.setText("Pause");
                btnPlay.setBackgroundResource(R.drawable.pause);
                previousSong();
            }
        });
        // Repeat listener
        btnRepeat = (Button) findViewById(R.id.buttonRepeat);
        btnRepeat.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //btnRepeat.setText("Pause");
                repeat();
            }
        });


        // Seekbar listener

        seekPercent.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                timer = new Timer();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(MainActivity.mediaPlayer != null && fromUser){
                    int ONE_SECOND = 1000;

                    int seconds = MainActivity.mediaPlayer.getCurrentPosition() / 1000 % 60 ;
                    int minutes = MainActivity.mediaPlayer.getCurrentPosition() / (1000*60) % 60;
                    String currentTime = String.format("%02d",minutes) + ":" + String.format("%02d",seconds);
                    txtCurrent.setText(String.valueOf(currentTime));

                    // prevents user skipping over songs by holding seek bar
                    int seekBarDraggableMaxValue = seekPercent.getMax() - (ONE_SECOND-1);
                    MainActivity.mediaPlayer.seekTo(progress > seekBarDraggableMaxValue ?
                            seekBarDraggableMaxValue : progress);





                }
            }
        });
    }
}
