package primetechnologies.faith_breed;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import java.util.List;

import primetechnologies.faith_breed.adapters.AudioCategoryDisplayAdapter;
import primetechnologies.faith_breed.mediaplayer.MainActivity;

public class AudioCategories extends AppCompatActivity {
    AudioCategoryDisplayAdapter mAudioCategoryDisplayAdapter;
    public static final String POSITION = "category-position";

    private String[] imageLabels = {
            "All Messages",
            "Faith Alive",
            "Series",
            "Sundays and Wednesdays"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_categories);

        new AssignImageTask().execute();
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AudioCategories.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    public static String FACEBOOK_URL = "https://www.facebook.com/faithbreed.everywhere/?ref=br_rs";
    public static String FACEBOOK_PAGE_ID = "faithbreed.everywhere/?ref=br_rs";

    //method to get the right URL to use in the intent
    public String getFacebookPageURL(Context context) {
        PackageManager packageManager = context.getPackageManager();
        try {
            int versionCode = packageManager.getPackageInfo("com.facebook.katana", 0).versionCode;
            if (versionCode >= 3002850) { //newer versions of fb app
                return "fb://facewebmodal/f?href=" + FACEBOOK_URL;
            } else { //older versions of fb app
                return "fb://page/" + FACEBOOK_PAGE_ID;
            }
        } catch (PackageManager.NameNotFoundException e) {
            return FACEBOOK_URL; //normal web url
        }
    }

    public void OnFacebookClicked(View view) {
        Intent facebookIntent = new Intent(Intent.ACTION_VIEW);
        String facebookUrl = getFacebookPageURL(this);
        facebookIntent.setData(Uri.parse(facebookUrl));
        try {
            startActivity(facebookIntent);
        }catch (ActivityNotFoundException e){
            Toast.makeText(this, "Facebook not installed",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void OnTwitterClicked(View view) {
        Intent intent = null;
        try {
            // get the Twitter app if possible
            this.getPackageManager().getPackageInfo("com.twitter.android", 0);
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/faithbreed_cc"));
//            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("twitter://user?2551631337=USERID"));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        } catch (Exception e) {
            // no Twitter app, revert to browser
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/faithbreed_cc"));
        }
        this.startActivity(intent);
    }

    public void OnInstagramClicked(View view) {
        Uri uri = Uri.parse("http://instagram.com/_u/faithbreed_cc");
        Intent likeIng = new Intent(Intent.ACTION_VIEW, uri);

        likeIng.setPackage("com.instagram.android");

        try {
            startActivity(likeIng);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://instagram.com/faithbreed_cc")));

        }
    }

    public void OnTelegramClicked(View view) {
        Uri uri = Uri.parse("https://t.me/joinchat/AAAAAEKTyugwrhr9TrolCw");
        try {
            Intent telegram = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(telegram);
        }catch (ActivityNotFoundException e){

        }
    }

    public void OnWhatsAppClicked(View view) {
        Uri uri = Uri.parse("http://chat.whatsapp.com/JmKzBAw0ulDEfc8esA9mL1");
        Intent likeIng = new Intent(Intent.ACTION_VIEW, uri);

        likeIng.setPackage("com.whatsapp.android");

        try {
            startActivity(likeIng);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://chat.whatsapp.com/JmKzBAw0ulDEfc8esA9mL1")));

        }
    }
    class AssignImageTask extends AsyncTask<Void, Void, Integer[]>{

        @Override
        protected Integer[] doInBackground(Void... voids) {
            Integer[] imageIds = new Integer[] {
                    R.drawable.categoryimage1,
                    R.drawable.categorymage2,
                    R.drawable.categoryimage3,
                    R.drawable.categoryimage4
            };

            return imageIds;
        }

        @Override
        protected void onPostExecute(Integer[] imageIds) {
            mAudioCategoryDisplayAdapter = new AudioCategoryDisplayAdapter(getApplicationContext(),
                    imageIds,imageLabels);
            GridView gridView = findViewById(R.id.category_gridview);
            gridView.setAdapter(mAudioCategoryDisplayAdapter);

            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View v,
                                        int position, long id) {
                    Intent intent = new Intent(parent.getContext(), AllAudioList.class);
                    if (position == 0){
                        intent.putExtra(POSITION, 0);

                    }else if (position == 1){
                        intent.putExtra(POSITION, 1);
                    }
                    else if (position == 2){
                        intent.putExtra(POSITION, 2);
                    }
                    else if (position == 3){
                        intent.putExtra(POSITION, 3);
                    }
                    startActivity(intent);
                    finish();

                }
            });
            super.onPostExecute(imageIds);
        }
    }
}
