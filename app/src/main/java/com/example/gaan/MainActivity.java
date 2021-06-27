package com.example.gaan;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    ListView ListOfSongs;
    String[] NamesOfSongs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListOfSongs=(ListView)findViewById(R.id.listView);
        runtimePermission();
    }

    public void runtimePermission(){
        Dexter.withContext(this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        displaySong();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }

    //this method fetches the song files in the phone
    public ArrayList<File> findSong(File file){
        ArrayList<File> al=new ArrayList<>();
            File[] files=file.listFiles();
            for (File singleSong : files) {
                if (singleSong.isDirectory() && !singleSong.isHidden()) {
                    al.addAll(findSong(singleSong));
                } else if (singleSong.getName().endsWith(".mp3") || singleSong.getName().endsWith(".m4a") || singleSong.getName().endsWith(".aac")) {
                    al.add(singleSong);
                }
            }
        return al;
    }
    //display the songs fetched
    public void displaySong(){
            ArrayList<File> al2 = findSong(Environment.getExternalStorageDirectory());
            NamesOfSongs = new String[al2.size()];
            for (int i = 0; i < al2.size(); i++) {
                NamesOfSongs[i] = al2.get(i).getName().toString();
            }
            CustomAdapter customAdapter = new CustomAdapter();
            ListOfSongs.setAdapter(customAdapter);
            ListOfSongs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    String songName=(String) ListOfSongs.getItemAtPosition(i);
                    startActivity(new Intent(getApplicationContext(),PlayerActivity.class).putExtra("songs" ,
                            al2).putExtra("songname",songName).putExtra("position",i));

                }
            });
    }

    class CustomAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return NamesOfSongs.length;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            @SuppressLint("ViewHolder") View view1= getLayoutInflater().inflate(R.layout.list_items,null);
            TextView mySong=view1.findViewById(R.id.SongName);
            mySong.setSelected(true);
            mySong.setText(NamesOfSongs[i]);
            return view1;
        }
    }
}
