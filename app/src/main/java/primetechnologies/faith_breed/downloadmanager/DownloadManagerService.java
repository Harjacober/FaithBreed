package primetechnologies.faith_breed.downloadmanager;

import android.app.DownloadManager;
import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import java.io.File;

import primetechnologies.faith_breed.database.AudioContract;
import primetechnologies.faith_breed.encryption.EncryptDecryptUtils;
import primetechnologies.faith_breed.encryption.FileUtils;

public class DownloadManagerService extends IntentService {
    DownloadManager mDownloadManager;
    String audioName;
    OnComplete onComplete ;
    String pathName;
    public DownloadManagerService() {
        super("Download manager Service");
    }

    @Override
    public void onCreate() {
        onComplete = new OnComplete();
        super.onCreate();
    }
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        registerReceiver(onComplete, intentFilter);
        Log.i("uuuuuuuu", "service started");
        audioName = intent.getStringExtra(DownloadPage.NAMEKEY);
        String audioUri = intent.getStringExtra(DownloadPage.PATHkEY);
        String audioArtist = intent.getStringExtra(DownloadPage.ARTISTkEY);
        String audioImage = intent.getStringExtra(DownloadPage.IMAGEkEY);
        Uri uri = Uri.parse(audioUri) ;

        downloadAudio(uri, audioName,
                audioArtist, audioImage, audioUri);

    }

    private long downloadAudio(Uri uri, String audioName, String audioArtist,
                               String audioImageLink, String audioDownloadLink){

        pathName = Environment.getExternalStorageDirectory().toString()+"/FaithBreed/Audio";
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
        downloadReference = mDownloadManager.enqueue(request);
        saveAudioLinkToDataBase(audioName,
        audioArtist, audioImageLink, audioDownloadLink);
        //notify user when download is complete
        sendBrodCastReceiver();
        return downloadReference;
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

    private void CheckAudioStatus(long audioDwonloadId) {

        DownloadManager.Query audioDownloadQuery = new DownloadManager.Query();
        //set the query filter to our previously Enqueued download
        audioDownloadQuery.setFilterById(audioDwonloadId);

        //Query the download manager about downloads that have been requested.
        Cursor cursor = mDownloadManager.query(audioDownloadQuery);
        if(cursor.moveToFirst()){
            DownloadStatus(cursor, audioDwonloadId);
        }

    }

    private void DownloadStatus(Cursor cursor, long DownloadId){

        //column for download  status
        int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
        int status = cursor.getInt(columnIndex);
        //column for reason code if the download failed or paused
        int columnReason = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);
        int reason = cursor.getInt(columnReason);
        //get the download filename
        int filenameIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME);
        String filename = cursor.getString(filenameIndex);

        String statusText = "";
        String reasonText = "";

        switch(status){
            case DownloadManager.STATUS_FAILED:
                statusText = "STATUS_FAILED";
                switch(reason){
                    case DownloadManager.ERROR_CANNOT_RESUME:
                        reasonText = "ERROR_CANNOT_RESUME";
                        break;
                    case DownloadManager.ERROR_DEVICE_NOT_FOUND:
                        reasonText = "ERROR_DEVICE_NOT_FOUND";
                        break;
                    case DownloadManager.ERROR_FILE_ALREADY_EXISTS:
                        reasonText = "ERROR_FILE_ALREADY_EXISTS";
                        break;
                    case DownloadManager.ERROR_FILE_ERROR:
                        reasonText = "ERROR_FILE_ERROR";
                        break;
                    case DownloadManager.ERROR_HTTP_DATA_ERROR:
                        reasonText = "ERROR_HTTP_DATA_ERROR";
                        break;
                    case DownloadManager.ERROR_INSUFFICIENT_SPACE:
                        reasonText = "ERROR_INSUFFICIENT_SPACE";
                        break;
                    case DownloadManager.ERROR_TOO_MANY_REDIRECTS:
                        reasonText = "ERROR_TOO_MANY_REDIRECTS";
                        break;
                    case DownloadManager.ERROR_UNHANDLED_HTTP_CODE:
                        reasonText = "ERROR_UNHANDLED_HTTP_CODE";
                        break;
                    case DownloadManager.ERROR_UNKNOWN:
                        reasonText = "ERROR_UNKNOWN";
                        break;
                }
                break;
            case DownloadManager.STATUS_PAUSED:
                statusText = "STATUS_PAUSED";
                switch(reason){
                    case DownloadManager.PAUSED_QUEUED_FOR_WIFI:
                        reasonText = "PAUSED_QUEUED_FOR_WIFI";
                        break;
                    case DownloadManager.PAUSED_UNKNOWN:
                        reasonText = "PAUSED_UNKNOWN";
                        break;
                    case DownloadManager.PAUSED_WAITING_FOR_NETWORK:
                        reasonText = "PAUSED_WAITING_FOR_NETWORK";
                        break;
                    case DownloadManager.PAUSED_WAITING_TO_RETRY:
                        reasonText = "PAUSED_WAITING_TO_RETRY";
                        break;
                }
                break;
            case DownloadManager.STATUS_PENDING:
                statusText = "STATUS_PENDING";
                break;
            case DownloadManager.STATUS_RUNNING:
                statusText = "STATUS_RUNNING";
                break;
            case DownloadManager.STATUS_SUCCESSFUL:
                statusText = "STATUS_SUCCESSFUL";
                reasonText = "Filename:\n" + filename;
                break;
        }

        Toast toast = Toast.makeText(DownloadManagerService.this,
                "Music Download Status:" + "\n" + statusText + "\n" +
                        reasonText,
                Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP, 25, 400);
        toast.show();
        // Make a delay of 3 seconds so that next toast (Music Status) will not merge with this one.
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
            }
        }, 3000);

    }
    private class OnComplete extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            /*encrypt(pathName);*/
            Log.i("pppppppp", "downloadComplete" );
        }
    }

    @Override
    public void onDestroy() {
        this.unregisterReceiver(onComplete);
        super.onDestroy();
    }
    /*private boolean encrypt(String path){
        try{
            byte[] fileData = FileUtils.readFile(path);
            byte[] encodedBytes = EncryptDecryptUtils.encode(EncryptDecryptUtils.getInstance(this)
                    .getSecretKey(), fileData);
            FileUtils.saveFile(encodedBytes, path);
        return true;
    } catch (Exception e) {

    }
        return false;
    }*/
}
