package primetechnologies.faith_breed.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import primetechnologies.faith_breed.R;

public class AudioCategoryDisplayAdapter extends BaseAdapter {
    private Context mContext;


    public AudioCategoryDisplayAdapter(Context c, Integer[] ids, String[] labels) {
      mContext = c;
    }

    public int getCount() {
        return imageLabels.length;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.item_audio_category_gridview, parent, false);
        }
        ImageView imageView = convertView.findViewById(R.id.category_image);
        TextView textView = convertView.findViewById(R.id.category_label);

        imageView.setImageResource(imageIds[position]);
        textView.setText(imageLabels[position]);
        return convertView;
    }
    private Integer[] imageIds = {
            R.drawable.categoryimage1,
            R.drawable.categorymage2,
            R.drawable.categoryimage3,
            R.drawable.categoryimage4
    };
    private String[] imageLabels = {
            "All Messages",
            "Faith Alive",
            "Series",
            "Sundays and Wednesdays"
    };
}