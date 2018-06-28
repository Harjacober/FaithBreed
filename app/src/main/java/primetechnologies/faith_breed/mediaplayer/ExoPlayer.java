package primetechnologies.faith_breed.mediaplayer;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.NalUnitUtil;
import com.google.android.exoplayer2.util.Util;
import com.squareup.picasso.Picasso;

import primetechnologies.faith_breed.AllAudioList;
import primetechnologies.faith_breed.R;

public class ExoPlayer extends AppCompatActivity {

    SimpleExoPlayerView mPlayerView;
    SimpleExoPlayer mExoPlayer;
    ProgressDialog progressDialog;
    String audioUri;
    boolean playWhenReady = true;
    long playBackPosition = 0;
    int currentWindow = 0;
    private ComponentListener componentListener;
    private ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exo_player);

        imageView = findViewById(R.id.image_view);
        Picasso.with(this).load(R.drawable.categoryimage4)
                .resize(500,500).into(imageView);
        progressDialog = new ProgressDialog(this);
        mPlayerView = findViewById(R.id.playerView);
        TextView titleArtist = findViewById(R.id.title_artist);
        Intent intent = getIntent();
        String audioName = intent.getStringExtra(AllAudioList.AUDSTREAMNAME);
        String audioArtist = intent.getStringExtra(AllAudioList.AUDSTREAMARTIST);
        audioUri = intent.getStringExtra(AllAudioList.AUDSTREAMLINK);
        titleArtist.setText(audioName+"\n"+audioArtist);
        setTitle(audioName);
        componentListener = new ComponentListener();

        android.support.v7.app.ActionBar actionbar=getSupportActionBar();
        if (actionbar!=null){
            actionbar.setDisplayHomeAsUpEnabled(true);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
        }
        return super.onOptionsItemSelected(item);
    }

    private void initializePlayer(Uri parse) {
        if (mExoPlayer == null){
            TrackSelector trackSelector = new DefaultTrackSelector();
            LoadControl loadControl = new DefaultLoadControl();
            mExoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector, loadControl);
            mPlayerView.setPlayer(mExoPlayer);
            String userAgent = Util.getUserAgent(this, "I don't know");
            MediaSource mediaSource = buildMediaSource(Uri.parse(audioUri));
            mExoPlayer.prepare(mediaSource, true, false);
            mExoPlayer.setPlayWhenReady(playWhenReady);
            mExoPlayer.seekTo(currentWindow, playBackPosition);
            mExoPlayer.addListener(componentListener);
        }
    }

    private MediaSource buildMediaSource(Uri uri) {
        return new ExtractorMediaSource.Factory(
                new DefaultHttpDataSourceFactory("exoplayer-codelab")).
                createMediaSource(uri);
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23){
            initializePlayer(Uri.parse(audioUri));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUi();
        if ((Util.SDK_INT <= 23 || mExoPlayer == null)){
            initializePlayer(Uri.parse(audioUri));
        }
    }

    @SuppressLint("InlinedApi")
    private void hideSystemUi() {
        mPlayerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressDialog!= null && progressDialog.isShowing()){
            progressDialog.dismiss();
        }
        releasePlayer();
    }

    private void releasePlayer() {
        if (mExoPlayer != null) {
            playBackPosition = mExoPlayer.getCurrentPosition();
            currentWindow = mExoPlayer.getCurrentWindowIndex();
            playWhenReady = mExoPlayer.getPlayWhenReady();
            mExoPlayer.removeListener(componentListener);
            mExoPlayer.release();
            mExoPlayer = null;
        }
    }

    public void onBackCLicked(View view) {
        NavUtils.navigateUpFromSameTask(this);
    }

    private class ComponentListener extends Player.DefaultEventListener {
        @Override
        public void onPlayerStateChanged(boolean playWhenReady,
                                         int playbackState) {
            String stateString;
            switch (playbackState) {
                case com.google.android.exoplayer2.ExoPlayer.STATE_IDLE:
                    progressDialog.dismiss();
                    stateString = "ExoPlayer.STATE_IDLE      -";
                    break;
                case com.google.android.exoplayer2.ExoPlayer.STATE_BUFFERING:
                    progressDialog.setMessage("loading...");
                    progressDialog.show();
                    stateString = "ExoPlayer.STATE_BUFFERING -";
                    break;
                case com.google.android.exoplayer2.ExoPlayer.STATE_READY:
                    progressDialog.dismiss();
                    stateString = "ExoPlayer.STATE_READY     -";
                    break;
                case com.google.android.exoplayer2.ExoPlayer.STATE_ENDED:
                    progressDialog.dismiss();
                    stateString = "ExoPlayer.STATE_ENDED     -";
                    break;
                default:
                    stateString = "UNKNOWN_STATE             -";
                    break;
            }

        }
    }
}
