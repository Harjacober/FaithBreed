package primetechnologies.faith_breed.downloadmanager;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import primetechnologies.faith_breed.AllAudioList;
import primetechnologies.faith_breed.R;

public class DownloadPage extends AppCompatActivity {
    public static final String PATHkEY = "path-key";
    public static final String NAMEKEY = "name-key";
    public static final String ARTISTkEY = "artist-key";
    public static final String IMAGEkEY = "image-key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_page);

        startDownload();

    }

    void startDownload(){
        Intent intent = getIntent();
        String downloadLink = intent.getStringExtra(AllAudioList.AUDDOWNLINK);
        String audioName = intent.getStringExtra(AllAudioList.AUDNAME);
        String audioArtist = intent.getStringExtra(AllAudioList.AUDARTIST);
        String audioImage = intent.getStringExtra(AllAudioList.AUDIMAGELINK);
        Intent intent1 = new Intent(this, DownloadManagerService.class);
        intent1.putExtra(PATHkEY ,downloadLink);
        intent1.putExtra(NAMEKEY ,audioName);
        intent1.putExtra(ARTISTkEY ,audioArtist);
        intent1.putExtra(IMAGEkEY ,audioImage);
        startService(intent1);
        Toast.makeText(getApplicationContext(), "downloading...", Toast.LENGTH_LONG).show();
        finish();
    }

}
