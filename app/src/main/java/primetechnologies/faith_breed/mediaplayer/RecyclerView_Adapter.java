package primetechnologies.faith_breed.mediaplayer;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.view.ActionMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import primetechnologies.faith_breed.R;
import primetechnologies.faith_breed.data.AudioDetails;
import primetechnologies.faith_breed.database.AudioContract;


public class RecyclerView_Adapter extends RecyclerView.Adapter<RecyclerView_Adapter.ViewHolder> {

    public interface onItemClickListener {

        void onClick(boolean isHighlighted, int index, String data);
    }
    public interface onDialogCreated {

        void onCreated(List<Audio> list, ArrayList<Audio> selectedList);
    }
    private final onItemClickListener mOnclickListener;
    private final onDialogCreated dialogCreated;
    private List<Audio> list = Collections.emptyList();
    private Context context;
    private boolean isHighlighted = false;
    private boolean multiSelect = false;
    private ArrayList<Audio> selectedItems = new ArrayList<>();
    private ArrayList<Audio> selectedItems2 = new ArrayList<>();

    public RecyclerView_Adapter(List<Audio> list, Context context,
                                onItemClickListener clickListener, onDialogCreated created) {
        this.list = list;
        this.context = context;
        mOnclickListener = clickListener;
        this.dialogCreated = created;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Inflate the layout, initialize the View Holder
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_activity_main_list, parent, false);
        return new ViewHolder(v);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //Use the provided View Holder on the onCreateViewHolder method to populate the current row on the RecyclerView
        holder.bind(list.get(position));
    }

    @Override
    public int getItemCount() {
        //returns the number of elements the RecyclerView will display
        return list.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
    class ViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        ImageView play_pause;
        LinearLayout cardView;

        ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            play_pause = itemView.findViewById(R.id.play_pause);
            cardView = itemView.findViewById(R.id.cardview);
        }

        private ActionMode.Callback callback = new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                multiSelect = true;
                MenuInflater menuInflater = new MenuInflater(context);
                menuInflater.inflate(R.menu.contextual_menu, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.delete:
                        dialogCreated.onCreated(list, selectedItems2);
                        actionMode.finish();
                        isHighlighted = false;
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {
                multiSelect = false;
                isHighlighted = false;

                selectedItems.clear();
                notifyDataSetChanged();
            }
        };



        void selectItem(Audio item) {
            if (multiSelect) {
                if (selectedItems.contains(item)) {
                    selectedItems.remove(item);
                    selectedItems2.remove(item);
                    cardView.setBackgroundColor(Color.WHITE);
                } else {
                    selectedItems.add(item);
                    selectedItems2.add(item);
                    cardView.setBackgroundColor(Color.parseColor("#37966F"));
                }
            }
        }

        void bind(final Audio value) {
            title.setText(value.getTitle());
            if (selectedItems.contains(value)) {
                cardView.setBackgroundColor(Color.parseColor("#37966F"));
            } else {
                cardView.setBackgroundColor(Color.WHITE);
            }
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    isHighlighted = true;
                    ((AppCompatActivity)view.getContext()).startSupportActionMode(callback);
                    if (selectedItems.size() < 6) {
                        selectItem(value);
                        mOnclickListener.onClick(true, getAdapterPosition(),
                                list.get(getAdapterPosition()).getData());
                    }else {
                        Toast.makeText(context, "You can't select more than 5",
                                Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
            });
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isHighlighted) {
                        if (selectedItems.size() < 6) {
                            selectItem(value);
                        } else {
                            Toast.makeText(context, "You can't select more than 5",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        String data = list.get(getAdapterPosition()).getData();
                        mOnclickListener.onClick(false, getAdapterPosition(), data);
                    }
                }
            });
        }
    }
        void update(List<Audio> data){
            list = data;
            notifyDataSetChanged();
        }
}


