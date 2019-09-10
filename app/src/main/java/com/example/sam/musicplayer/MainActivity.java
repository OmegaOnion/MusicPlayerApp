package com.example.sam.musicplayer;

import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;


public class MainActivity extends AppCompatActivity {

    ListView lstSongs;
    ArrayList<String> songNames;
    ArrayAdapter<String> adapter;
    HashMap<Integer, String> songHash;
    Button btnShuffle;

    public static MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //fill ArrayList
        songNames = getRawFiles();

        lstSongs = (ListView) findViewById(R.id.lvSongs);
        lstSongs.setNestedScrollingEnabled(true);

        // Coloring for listview
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, songNames){
            @Override
            public View getView(int position, View v, ViewGroup parent){
                View view = super.getView(position,v,parent);
                TextView tv = view.findViewById(android.R.id.text1);
                tv.setTextColor(Color.WHITE);

                return view;
            }
        };

        lstSongs.setAdapter(adapter);

        songHash = setHash(songNames);

        // Detects item clicked in list view
        lstSongs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int songIndex = position;
                showPlayer(songIndex);

            }

    });

        // Shuffle
        btnShuffle = (Button) findViewById(R.id.buttonShuffle);
        btnShuffle.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                shuffle();
                showPlayer(0);
            }
        });
    }


    /** gets titles of files in res/raw folder **/
    public ArrayList<String> getRawFiles() {
        ArrayList<String> rawList = new ArrayList<>();
        Field[] fields = R.raw.class.getFields();

        // loop for every file in raw folder
        for (int i = 0; i < fields.length; i++) {
            String fileName = fields[i].getName();
            if (fileName!="serialVersionUID" && fileName!= "$change"){
                rawList.add(fileName);
            }

        }


        return rawList;
    }
     /** Runs the player activity **/
    public void showPlayer(int songIndex) {
        Intent intent = new Intent(this, PlayerActivity.class);

        intent.putExtra("songIndex", songIndex);
        intent.putExtra("hashMap", songHash);

        startActivity(intent);
    }
    /** Creates HashMap of Id and songname **/
    public HashMap<Integer, String> setHash(ArrayList<String> songNames){
        songHash = new HashMap<>();
        int i = 0;
        for(String song: songNames){
            songHash.put(i,song);
            i++;
        }
        return songHash;


    }

    /** shuffles the hashmap ordering by randomising ids **/
    public void shuffle(){
        // random number
        Random r = new Random();

        ArrayList<String> temp = new ArrayList<>();
        // populate temp list
        for (String song: songNames){
            temp.add(song);
        }
        ArrayList<String> shuffled = new ArrayList<>();
        int rand;
        Log.d("temp", temp.toString());
        for(int i = temp.size(); i > 0;i--) {
            // current random n umber
            rand = r.nextInt(temp.size());
            String song = temp.get(rand);
            temp.remove(rand);
            shuffled.add(song);
        }
        // set the hash to the shuffled version
        songHash = setHash(shuffled);
    }
}
