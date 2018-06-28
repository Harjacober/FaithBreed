package primetechnologies.faith_breed.mediaplayer;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import primetechnologies.faith_breed.AllAudioList;
import primetechnologies.faith_breed.R;
import primetechnologies.faith_breed.database.AudioContract;

public class MainActivity extends AppCompatActivity
        implements RecyclerView_Adapter.onItemClickListener,
RecyclerView_Adapter.onDialogCreated{

    public static final String Broadcast_PLAY_NEW_AUDIO = "com.valdioveliu.valdio.audioplayer.PlayNewAudio";

    private PlayerService player;
    boolean serviceBound = false;
    ArrayList<Audio> audioList;
    private ImageView playPause;
    RecyclerView_Adapter adapter;
    ImageView collapsingImageView;

    int imageIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        getSupportActionBar().setElevation(0);
        collapsingImageView = findViewById(R.id.collapsingImageView);

        playPause = findViewById(R.id.play_pause);
        audioList = new ArrayList<>();
        loadCollapsingImage(imageIndex);
        loadAudio();
        initRecyclerView();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                playAudio("https://upload.wikimedia.org/wikipedia/commons/6/6c/Grieg_Lyric_Pieces_Kobold.ogg");
                //play the first audio in the ArrayList
//                playAudio(2);
                if (imageIndex == 4) {
                    imageIndex = 0;
                    loadCollapsingImage(imageIndex);
                } else {
                    loadCollapsingImage(++imageIndex);
                }
            }
        });

        android.support.v7.app.ActionBar actionbar=getSupportActionBar();
        if (actionbar!=null){
            actionbar.setDisplayHomeAsUpEnabled(true);
        }
    }


    private void initRecyclerView() {
        if (audioList.size() > 0) {
            RecyclerView recyclerView =  findViewById(R.id.recyclerview);
            adapter = new RecyclerView_Adapter(audioList,
                    getApplication(), this, this);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

        }
    }

    private void loadCollapsingImage(int i) {
        TypedArray array = getResources().obtainTypedArray(R.array.images);
        Picasso.with(this).load(array.getResourceId(i, 0))
                .resize(400,400).into(collapsingImageView);
        array.recycle();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }else if (id == android.R.id.home){
           NavUtils.navigateUpFromSameTask(this);

        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putBoolean("serviceStatus", serviceBound);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        serviceBound = savedInstanceState.getBoolean("serviceStatus");
    }

    //Binding this Client to the AudioPlayer Service
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            PlayerService.LocalBinder binder = (PlayerService.LocalBinder) service;
            player = binder.getService();
            serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };


    private void playAudio(int audioIndex) {
        //Check is service is active
        if (!serviceBound) {
            //Store Serializable audioList to SharedPreferences
            StorageUtil storage = new StorageUtil(getApplicationContext());
            storage.storeAudio(audioList);
            storage.storeAudioIndex(audioIndex);

            Intent playerIntent = new Intent(this, PlayerService.class);
            startService(playerIntent);
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        } else {
            //Store the new audioIndex to SharedPreferences
            StorageUtil storage = new StorageUtil(getApplicationContext());
            storage.storeAudioIndex(audioIndex);

            //Service is active
            //Send a broadcast to the service -> PLAY_NEW_AUDIO
            Intent broadcastIntent = new Intent(Broadcast_PLAY_NEW_AUDIO);
            sendBroadcast(broadcastIntent);
        }
    }


    private void loadAudio() {
        ContentResolver contentResolver = getContentResolver();

        Uri uri = AudioContract.AudioEntry.CONTENT_URI;
//        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = AudioContract.AudioEntry.COLUMN_NAME + " ASC";
        Cursor cursor = contentResolver.query(uri,
                null, null,
                null, sortOrder);

        if (cursor != null && cursor.getCount() > 0) {
            audioList = new ArrayList<>();
            audioList.clear();
            while (cursor.moveToNext()) {
                String data = cursor.getString(cursor.getColumnIndex(AudioContract.AudioEntry.COLUMN_DOWNLOAD_LINK));
                String title = cursor.getString(cursor.getColumnIndex(AudioContract.AudioEntry.COLUMN_NAME));
//                String album = cursor.getString(cursor.getColumnIndex(AudioContract.AudioEntry.COLUMN_ARTIST));
                String artist = cursor.getString(cursor.getColumnIndex(AudioContract.AudioEntry.COLUMN_ARTIST));

                // Save to audioList
                audioList.add(new Audio(data, title, null, artist));
            }
        }
        cursor.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBound) {
            unbindService(serviceConnection);
            //service is active
        }
    }

    @Override
    public void onClick(boolean isHighlighted, int index, String data) {
        if (!isHighlighted){
            File file = new File(data);
            if (file.exists()) {
                playAudio(index);
            }else {
                Toast.makeText(this, "File has been modified or deletd",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showDeleteDialog(final List<Audio> list, final ArrayList<Audio> selectedItems2) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this Audio?")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //TODO Move this method to run in the background
                        for (Audio object : selectedItems2) {
                            list.remove(object);
                            selectedItems2.clear();
                            adapter.update(list);
                            File file = new File(object.getData());
                            if (file.exists()) {
                                if(file.delete()){
                                    String selection = AudioContract.AudioEntry.COLUMN_DOWNLOAD_LINK+"=?";
                                    String[] selectionArgs = new String[]{object.getData()};
                                    getContentResolver().delete(AudioContract.AudioEntry.CONTENT_URI,
                                            selection, selectionArgs);
                                }
                            }else{
                                String selection = AudioContract.AudioEntry.COLUMN_DOWNLOAD_LINK+"=?";
                                String[] selectionArgs = new String[]{object.getData()};
                                getContentResolver().delete(AudioContract.AudioEntry.CONTENT_URI,
                                        selection, selectionArgs);

                            }

                        }
                    }
                }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        AlertDialog dialog = builder.create();
//        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.show();
    }

    @Override
    public void onCreated(List<Audio> list, ArrayList<Audio> selectedList) {
        showDeleteDialog(list, selectedList);
    }
}