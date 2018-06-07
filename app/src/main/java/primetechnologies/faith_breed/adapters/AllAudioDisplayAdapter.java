package primetechnologies.faith_breed.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import primetechnologies.faith_breed.R;
import primetechnologies.faith_breed.data.AudioDetails;

public class AllAudioDisplayAdapter  extends RecyclerView.Adapter<AllAudioDisplayAdapter.DataObjectHolder>{

    public interface ListItemClickListener{
        void onListItemClick(String downloadLink, String imageLink,
                             String audioName, String audioArtist);
    }
    public interface MoreMenuClickListener{
        void onMoreMenuClickListener(View view, String downloadLink,String imageLink,
                                     String audioName, String audioArtist);
    }
    private final ListItemClickListener mOnclickListener;
    private final MoreMenuClickListener mOnMoreMenuclickListener;
    List<AudioDetails> data;
    Context context;

public AllAudioDisplayAdapter(ListItemClickListener mOnclickListener, MoreMenuClickListener mOnMoreMenuclickListener, List<AudioDetails> data, Context context) {
    this.mOnclickListener = mOnclickListener;
    this.mOnMoreMenuclickListener = mOnMoreMenuclickListener;
    this.data = data;
    this.context = context;
    }

@Override
public AllAudioDisplayAdapter.DataObjectHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).
        inflate(R.layout.item_all_audio_listview,parent,false);
        return new DataObjectHolder(view);
        }

@Override
public void onBindViewHolder(AllAudioDisplayAdapter.DataObjectHolder holder, int position) {

        holder.bind(position);

        }

@Override
public int getItemCount() {
        return data.size();
        }
public void updateAdapter(List<AudioDetails> cards) {
        data = cards;
        notifyDataSetChanged();
        }

public class DataObjectHolder extends RecyclerView.ViewHolder{
    ImageView audioImage;
    TextView audioName;
    ImageView moreMenu;
    TextView audioArtist;
    ProgressBar loadIndictor;
    public DataObjectHolder(View itemView) {
        super(itemView);
        audioImage= itemView.findViewById(R.id.audio_image);
        audioArtist= itemView.findViewById(R.id.audio_name);
        audioName=  itemView.findViewById(R.id.audio_artist);
        moreMenu= itemView.findViewById(R.id.more_menu);
        loadIndictor = itemView.findViewById(R.id.image_progress_bar);
    }

    public void bind(final int position)  {
        audioName.setText(data.get(position).getAudioName());
        audioArtist.setText(data.get(position).getAudioArtist());
        loadIndictor.setVisibility(View.VISIBLE);
        audioImage.setVisibility(View.INVISIBLE);
        Picasso.with(context).load(data.get(position).getAudioImageLink()).into(audioImage,
                new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        loadIndictor.setVisibility(View.INVISIBLE);
                        audioImage.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onError() {

                    }
                });

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Streams Audio Directly Online
                String audioDownloadLink = data.get(position).getAudioDownloadLink();
                String audioImageLink = data.get(position).getAudioImageLink();
                String audioName = data.get(position).getAudioName();
                String audioArtist = data.get(position).getAudioArtist();
                mOnclickListener.onListItemClick(audioDownloadLink, audioImageLink,
                        audioName, audioArtist);
            }
        });
        moreMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String audioDownloadLink = data.get(position).getAudioDownloadLink();
                String audioImageLink = data.get(position).getAudioImageLink();
                String audioName = data.get(position).getAudioName();
                String audioArtist = data.get(position).getAudioArtist();
                mOnMoreMenuclickListener.onMoreMenuClickListener(view, audioDownloadLink,
                        audioImageLink, audioName, audioArtist);
            }
        });

    }
}
}

