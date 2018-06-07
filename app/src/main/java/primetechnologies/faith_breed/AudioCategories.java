package primetechnologies.faith_breed;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import primetechnologies.faith_breed.adapters.AllAudioDisplayAdapter;
import primetechnologies.faith_breed.adapters.AudioCategoryDisplayAdapter;

public class AudioCategories extends AppCompatActivity {
    AudioCategoryDisplayAdapter mAudioCategoryDisplayAdapter;
    public static final String POSITION = "category-position";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_categories);

        mAudioCategoryDisplayAdapter = new AudioCategoryDisplayAdapter(this, null, null);
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
    }
    }
