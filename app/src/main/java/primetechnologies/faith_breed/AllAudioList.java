package primetechnologies.faith_breed;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.ListPopupWindow;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import primetechnologies.faith_breed.adapters.AllAudioDisplayAdapter;
import primetechnologies.faith_breed.authentication.SignInActivity;
import primetechnologies.faith_breed.data.AudioDetails;
import primetechnologies.faith_breed.downloadmanager.DownloadPage;
import primetechnologies.faith_breed.mediaplayer.MainActivity;
import primetechnologies.faith_breed.payment.PaymentActivity;
import primetechnologies.faith_breed.utils.NetwotkUtils;

import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static primetechnologies.faith_breed.payment.PaymentActivity.BALANCE_PREF;
import static primetechnologies.faith_breed.payment.PaymentActivity.BALANCE_WALLET;


public class AllAudioList extends AppCompatActivity implements
AllAudioDisplayAdapter.ListItemClickListener, AllAudioDisplayAdapter.MoreMenuClickListener{

    private PopupMenu popupMenu;
    private List list;
    private AllAudioDisplayAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private ProgressBar mLoadIndicator;
    private LinearLayout mNoConnectionView;
    private String mName;
    private String mArtist;
    private String mImageLink;
    private String mDownloadLink;
    public static final String AMOUNT_TO_ADD = "amount-to-add";
    public static final String AUDNAME = "audio-name";
    public static final String AUDDOWNLINK = "audio-download-link";
    public static final String AUDIMAGELINK = "audio-image-link";
    public static final String AUDARTIST = "audio-artist";
    public static final String AUDSTREAMNAME = "audio-stream-name";
    public static final String AUDSTREAMLINK = "audio-stream-link";
    private static final int REQUEST_CODE = 30;
    String amount;
    FirebaseAuth mAuth;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_audio_list);
        requestAllPermission();
        mAuth = FirebaseAuth.getInstance();
        list = new ArrayList<>();
        mNoConnectionView = findViewById(R.id.no_connection_view);
        mLoadIndicator = findViewById(R.id.progressbar);
        mAdapter = new AllAudioDisplayAdapter(this,
                this, list, this);
        mRecyclerView = findViewById(R.id.audio_list_recyclerview);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter);
        loadData();



//        setTitle("Faith Alive");

    }

    private void loadData() {
        if (NetwotkUtils.isConnected(this)){
            fetchDataFromFirebase();
        }else{
            mRecyclerView.setVisibility(View.INVISIBLE);
            mLoadIndicator.setVisibility(View.INVISIBLE);
            mNoConnectionView.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.music_library) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }else if (id == R.id.wallet){
            openWalletDialog();
        }else if (id == R.id.add_money){
            openAmountDialog();
        }else if (id == R.id.home){
            Intent intent = new Intent(this, AudioCategories.class);
            startActivity(intent);
            finish();
        }else if (id == R.id.signout){
            mAuth.signOut();
            Intent intent = new Intent(this, SignInActivity.class);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void openWalletDialog() {
        //Get the wallet balance from sharedPreference and display it here
        //the balance is saved to shared preferences from the server whenever payment is successful
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).
                inflate(R.layout.wallet_layout, null);
        final TextView balanceView = view.findViewById(R.id.balance);
        balanceView.setText("...");
        builder.setView(view);
        final AlertDialog dialog = builder.create();
        FirebaseUser user = mAuth.getCurrentUser();
        if ( user != null) {
            final String uId = user.getUid();
            DatabaseReference rootReference = FirebaseDatabase.getInstance()
                    .getReference().child("users").child(uId);
            rootReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String balance = (String) dataSnapshot.child("balance").getValue();
                    SharedPreferences preferences =AllAudioList.this
                            .getSharedPreferences(BALANCE_PREF, MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(BALANCE_WALLET, balance);
                    editor.commit();
                    balanceView.setText(balance);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            Button addMoney = view.findViewById(R.id.add_money);
            addMoney.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Open new Dialog
                    openAmountDialog();
                    dialog.dismiss();
                }
            });
            dialog.show();
        }
    }

    private void openAmountDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(
                R.layout.amount_layout, null);
        EditText editText = view.findViewById(R.id.amount_edit_text);
        Button proceed = view.findViewById(R.id.proceed);
        builder.setView(view);
        final AlertDialog dialog = builder.create();
        amount = editText.getText().toString();
        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AllAudioList.this, PaymentActivity.class);
                intent.putExtra(AMOUNT_TO_ADD, amount);
                startActivity(intent);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    public void onListItemClick(String downloadLink, String imageLink,
                                String audioName, String audioArtist) {
        streamAudio(audioName, downloadLink);

    }

    @Override
    public void onMoreMenuClickListener(View anchor, String downloadLink, String imageLink,
                                        String audioName, String audioArtist) {
        popupMenu = new PopupMenu(AllAudioList.this, anchor);
        popupMenu.setOnDismissListener(new OnDismissListener());
        popupMenu.setOnMenuItemClickListener(new OnMenuItemClickListener());
        popupMenu.inflate(R.menu.popup_list);
        popupMenu.show();
        mName = audioName;
        mArtist = audioArtist;
        mImageLink = imageLink;
        mDownloadLink = downloadLink;
    }

    private class OnDismissListener implements PopupMenu.OnDismissListener {

        @Override
        public void onDismiss(PopupMenu menu) {
            // TODO Auto-generated method stub
        }

    }

    private class OnMenuItemClickListener implements
            PopupMenu.OnMenuItemClickListener {

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            // TODO Auto-generated method stub
            switch (item.getItemId()) {
                case R.id.play:
                    Toast.makeText(getApplicationContext(), "playing...",
                            Toast.LENGTH_SHORT).show();
                    streamAudio(mName, mDownloadLink);
                    return true;
                case R.id.download:
                    if (NetwotkUtils.isConnected(AllAudioList.this)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(AllAudioList.this);
                        builder.setTitle("#100 will be deducted from your wallet\n" +
                                "Do you wish to continue?").setNegativeButton("No",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                }).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                isWalletFunded();
                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                        return true;
                    }else {
                        Toast.makeText(AllAudioList.this, "No Internet Connection",
                                Toast.LENGTH_LONG).show();
                    }

            }
            return false;
        }
    }

    private void isWalletFunded() {
        FirebaseUser user = mAuth.getCurrentUser();
        if ( user != null) {
            String uId = user.getUid();
            DatabaseReference rootReference = FirebaseDatabase.getInstance()
                    .getReference().child("users").child(uId);
            rootReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String balance = (String) dataSnapshot.child("balance").getValue();
                    Integer balanceInt = Integer.valueOf(balance);
                    if (balanceInt >= 100){
                        downloadAudio(mDownloadLink, mImageLink,
                                mName, mArtist);
                        String deductedAmount = "-100";
                        PaymentActivity.addMoneyToUserBalance(deductedAmount,
                                AllAudioList.this, mAuth);
                    }else{
                        Toast.makeText(AllAudioList.this,
                                "Insufficient balance", Toast.LENGTH_SHORT).show();
                        openAmountDialog();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    void fetchDataFromFirebase(){
        mLoadIndicator.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.INVISIBLE);
        mNoConnectionView.setVisibility(View.INVISIBLE);

        DatabaseReference rootReference = null;
        Intent intentFromCategory = getIntent();
        int position = intentFromCategory.getIntExtra(AudioCategories.POSITION, 0);
        if (position == 0){
            rootReference = FirebaseDatabase.getInstance()
                    .getReference().child("audio");
            rootReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    list.clear();
                    for (DataSnapshot shots : dataSnapshot.getChildren()) {
                        for (DataSnapshot postShots : shots.getChildren()) {
                            AudioDetails audioDetails = postShots.getValue(AudioDetails.class);
                            list.add(audioDetails);
                            mAdapter.updateAdapter(list);
                        }
                        mLoadIndicator.setVisibility(View.INVISIBLE);
                        mNoConnectionView.setVisibility(View.INVISIBLE);
                        mRecyclerView.setVisibility(View.VISIBLE);
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    mRecyclerView.setVisibility(View.INVISIBLE);
                    mLoadIndicator.setVisibility(View.INVISIBLE);
                    mNoConnectionView.setVisibility(View.VISIBLE);
                }
            });
        }else{
            if (position == 1){
                rootReference = FirebaseDatabase.getInstance()
                        .getReference().child("audio").child("FAITH ALIVE");
            }else if (position == 2){
                rootReference = FirebaseDatabase.getInstance()
                        .getReference().child("audio").child("SERIES");
            }else if (position == 3){
                rootReference = FirebaseDatabase.getInstance()
                        .getReference().child("audio").child("SUNDAYS AND WEDNESDAYS");
            }
            rootReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    list.clear();
                    for(DataSnapshot postShots : dataSnapshot.getChildren()) {
                        AudioDetails audioDetails = postShots.getValue(AudioDetails.class);
                        list.add(audioDetails);
                        mAdapter.updateAdapter(list);

                        //Storing the data in JSON format
                        /*String audioName = (String) postShots.child("audioName").getValue();
                        String audioArtist = (String) postShots.child("audioArtist").getValue();
                        String audioImageLink = (String) postShots.child("audioImageLink").getValue();
                        String audioDownloadLink = (String) postShots.child("audioDownloadLink").getValue();*/

                    }
                    mLoadIndicator.setVisibility(View.INVISIBLE);
                    mNoConnectionView.setVisibility(View.INVISIBLE);
                    mRecyclerView.setVisibility(View.VISIBLE);

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    mRecyclerView.setVisibility(View.INVISIBLE);
                    mLoadIndicator.setVisibility(View.INVISIBLE);
                    mNoConnectionView.setVisibility(View.VISIBLE);
                }
            });
        }

    }

    public void OnRetryButtonClicked(View view) {
        loadData();
    }

    private void downloadAudio(String downloadLink, String imageLink,
                               String audioName, String audioArtist) {
        Intent intent = new Intent(this, DownloadPage.class);
        intent.putExtra(AUDNAME, audioName);
        intent.putExtra(AUDDOWNLINK, downloadLink);
        intent.putExtra(AUDIMAGELINK, imageLink);
        intent.putExtra(AUDARTIST, audioArtist);
        startActivity(intent);
    }

    public void streamAudio(String audioName, String audioLink){
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(AUDSTREAMNAME, audioName);
        intent.putExtra(AUDSTREAMLINK, audioLink);
        startActivity(intent);
    }

    public void requestAllPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED){
            }else {
                requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE, READ_PHONE_STATE}, REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE){
            if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED){
            }
        }else{
            Toast.makeText(this, "All Permission needs to be granted", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

}
