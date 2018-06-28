package primetechnologies.faith_breed.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import primetechnologies.faith_breed.R;

public class AudioCategoryDisplayAdapter extends BaseAdapter {
    private Context mContext;
    private Integer[] imageIds;
    private String[] labels;


    public AudioCategoryDisplayAdapter(Context c, Integer[] ids, String[] labels) {
      mContext = c;
      imageIds = ids;
      this.labels = labels;
    }

    public int getCount() {
        return labels.length;
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

        Picasso.with(mContext).load(imageIds[position]).resize(300,
                300).into(imageView);
        textView.setText(labels[position]);
        return convertView;
    }

}