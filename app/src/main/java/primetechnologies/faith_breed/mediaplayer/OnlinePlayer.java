package primetechnologies.faith_breed.mediaplayer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;

import java.io.IOException;
import java.lang.ref.WeakReference;

import primetechnologies.faith_breed.AllAudioList;
import primetechnologies.faith_breed.R;

public class OnlinePlayer extends AppCompatActivity implements MediaPlayer.OnPreparedListener, MediaController.MediaPlayerControl {
    private MediaPlayer mediaPlayer;
    private String mAudioLink;
    private MediaController mediaController;
    private ProgressDialog progressDialog;
    private boolean initialStage = true;
    private boolean playPause;
    private ImageView playOrPause;
    private Player player;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_player);

        playOrPause = findViewById(R.id.anchor);
        Intent intent = getIntent();
        mAudioLink = intent.getStringExtra(AllAudioList.AUDSTREAMLINK);
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        progressDialog = new ProgressDialog(this);
        mediaController = new MediaController(this){
            @Override
            public void hide() {
            }
        };
        android.support.v7.app.ActionBar actionbar=getSupportActionBar();
        if (actionbar!=null){
            actionbar.setDisplayHomeAsUpEnabled(true);
        }
        player=new Player(this);
        mediaPlayer.setOnPreparedListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home){
            NavUtils.navigateUpFromSameTask(this);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaController.setMediaPlayer(this);
        mediaController.setAnchorView(findViewById(R.id.image));
        mediaController.setEnabled(true);
        try {
            mediaController.show();
        }catch (Exception e){

        }
    }

    @Override
    public void start() {
        try {
            mediaPlayer.start();
            playOrPause.setImageResource(R.drawable.ic_pause_circle_outline_black_24dp);
            playPause = true;
        }catch (Exception e){

        }

    }


    @Override
    public void pause() {
        try {
            mediaPlayer.pause();
            playOrPause.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
            playPause = false;
        }catch (Exception e){
        }
        player.cancel(true);
    }

    @Override
    public int getDuration() {
        try {
            return mediaPlayer.getDuration();
        }catch (Exception e){
            return 0;
        }
    }

    @Override
    public int getCurrentPosition() {
        try {
            return mediaPlayer.getCurrentPosition();
        }catch (Exception e){
            return 0;
        }
    }

    @Override
    public void seekTo(int i) {
        mediaPlayer.seekTo(i);
    }

    @Override
    public boolean isPlaying() {
        return mediaPlayer.isPlaying();

    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.release();
        }
        mediaController = null;
        new Player(this).cancel(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mediaController.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        if(mediaController!=null){
            mediaController.setEnabled(false);
            mediaController.hide();
        }
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        new Player(this).cancel(true);
    }

    public void OnPlayPauseClicked(View view) {
        if (!playPause){
            playOrPause.setImageResource(R.drawable.ic_pause_circle_outline_black_24dp);
            //pause streaming
            if (initialStage){
                player.execute(mAudioLink);
            }else{
                if (!mediaPlayer.isPlaying()){
                    mediaPlayer.start();

                }
            }
            playPause = true;
        }else{
            playOrPause.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
            //launch streaming
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
            playPause = false;
        }
    }

    static class Player extends AsyncTask<String, Void, Boolean>{
        private WeakReference<OnlinePlayer> activityReference;

        // only retain a weak reference to the activity
        Player(OnlinePlayer context) {
            activityReference = new WeakReference<>(context);
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            OnlinePlayer activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;
            activity.progressDialog.setMessage("loading...");
            activity.progressDialog.show();

        }

        @Override
        protected Boolean doInBackground(String... strings) {

            Boolean prepared = false;
            final OnlinePlayer activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return false;
//            activity.mediaPlayer = new MediaPlayer();

            try{
                activity.mediaPlayer.setDataSource(strings[0]);

                activity.mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        activity.initialStage = true;
                        activity.playPause = false;
                        //launch streaming
                        mediaPlayer.stop();
                        mediaPlayer.reset();

                    }
                });

                activity.mediaPlayer.prepare();
                prepared = true;
            }catch (Exception e){
                prepared = false;
            }

            return prepared;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            OnlinePlayer activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;
            if (activity.progressDialog.isShowing()){
                activity.progressDialog.dismiss();
            }
            activity.mediaPlayer.start();
            activity.initialStage = true;
//            playOrPause.setVisibility(View.GONE);
        }
    }
}
