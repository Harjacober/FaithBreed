package primetechnologies.faith_breed.downloadmanager;

import android.app.DownloadManager;
import android.app.IntentService;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import primetechnologies.faith_breed.database.AudioContract;
import primetechnologies.faith_breed.encryption.EncryptDecryptUtils;
import primetechnologies.faith_breed.encryption.FileUtils;

public class DownloadManagerService extends Service {
    private DownloadManager mDownloadManager;
    private String audioName;
    private String pathName;
    String audioUri;
    String audioArtist;
    String audioImage;
    private long refId;
    ArrayList<Long> list = new ArrayList<>();
    OnComplete onComplete;
    public static final String URILIST = "uri-list";
    public static final String PREFS_NAME = "downloaded-audio-uri-predf";

    @Override
    public void onCreate() {
        onComplete = new OnComplete();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        registerReceiver(onComplete, intentFilter);
        if (intent.hasExtra(DownloadPage.NAMEKEY)) {
            audioName = intent.getStringExtra(DownloadPage.NAMEKEY);
            audioUri = intent.getStringExtra(DownloadPage.PATHkEY);
            audioArtist = intent.getStringExtra(DownloadPage.ARTISTkEY);
            audioImage = intent.getStringExtra(DownloadPage.IMAGEkEY);
            Uri uri = Uri.parse(audioUri) ;

            downloadAudio(uri, audioName, audioArtist);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private long downloadAudio(Uri uri, String audioName, String audioArtist){

        pathName = Environment.getExternalStorageDirectory().toString()+"/.FaithBreed/.Audio";
        File dirs = new File(pathName);
        if (!dirs.exists()){
            dirs.mkdirs();
        }
        long downloadReference;
        mDownloadManager = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setTitle(audioName);
        request.setDescription(audioArtist);

        request.setDestinationInExternalFilesDir(DownloadManagerService.this,
                pathName,audioName+".mp3");

        refId = downloadReference = mDownloadManager.enqueue(request);
        //store id in a list
        list.add(refId);
        return downloadReference;
    }

    private boolean encryptAudioFile(String path, String title) {
        try {
            byte[] fileData = FileUtils.readFile(path);
            byte[] encodedBytes = EncryptDecryptUtils.encode(EncryptDecryptUtils
                    .getInstance(this).getSecretKey(title), fileData);
            FileUtils.saveFile(encodedBytes, path);

            return true;
        } catch (Exception e) {

        }
        return false;
    }

    private void sendBrodCastReceiver() {
    }

    private void saveAudioLinkToDataBase(String audioName, String audioArtist,
                                         String audioImageLink, String audioDownLoadlink) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(AudioContract.AudioEntry.COLUMN_NAME, audioName);
        contentValues.put(AudioContract.AudioEntry.COLUMN_ARTIST, audioArtist);
        contentValues.put(AudioContract.AudioEntry.COLUMN_IMAGE_LINK, audioImageLink);
        contentValues.put(AudioContract.AudioEntry.COLUMN_DOWNLOAD_LINK, audioDownLoadlink);
        getApplicationContext().getContentResolver().insert(AudioContract.AudioEntry.CONTENT_URI,
                contentValues);

    }


    private void DownloadStatus(Cursor cursor, long DownloadId){

        //column for download  status
        int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
        int status = cursor.getInt(columnIndex);

        String statusText = "";
        String reasonText = "";

        switch(status){
            case DownloadManager.STATUS_FAILED:
                statusText = "STATUS_PAUSED";
                break;
            case DownloadManager.STATUS_PAUSED:
                statusText = "STATUS_PAUSED";
                break;
            case DownloadManager.STATUS_PENDING:
                statusText = "STATUS_PENDING";
                break;
            case DownloadManager.STATUS_RUNNING:
                statusText = "STATUS_RUNNING";
                break;
            case DownloadManager.STATUS_SUCCESSFUL:
                processSomethinz();
                statusText = "STATUS_SUCCESSFUL";
                break;
        }
    }

    private void processSomethinz() {
    String pathName = Environment.getExternalStorageDirectory()
            +"/Android/data/primetechnologies.faith_breed/files/"
                +Environment.getExternalStorageDirectory()
                .toString()+"/.FaithBreed/.Audio/"+audioName+".mp3";
        saveAudioLinkToDataBase(audioName,
                audioArtist, audioImage, pathName);
        //notify user when download is complete
        sendBrodCastReceiver();
    }

    class OnComplete extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {


            long referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

            DownloadManager.Query audioDownloadQuery = new DownloadManager.Query();
            //set the query filter to our previously Enqueued download
            audioDownloadQuery.setFilterById(referenceId);
            //Query the download manager about downloads that have been requested.
            Cursor cursor = mDownloadManager.query(audioDownloadQuery);
            if(cursor.moveToFirst()){
                DownloadStatus(cursor, referenceId);
            }

        }
    }

    @Override
    public void onDestroy() {
        this.unregisterReceiver(onComplete);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
