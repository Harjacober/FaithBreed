package primetechnologies.faith_breed;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import primetechnologies.faith_breed.adapters.AllAudioDisplayAdapter;
import primetechnologies.faith_breed.authentication.SignInActivity;
import primetechnologies.faith_breed.data.AudioDetails;
import primetechnologies.faith_breed.downloadmanager.DownloadPage;
import primetechnologies.faith_breed.mediaplayer.ExoPlayer;
import primetechnologies.faith_breed.mediaplayer.MainActivity;
import primetechnologies.faith_breed.mediaplayer.OnlinePlayer;
import primetechnologies.faith_breed.payment.PaymentActivity;
import primetechnologies.faith_breed.utils.NetwotkUtils;

import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static primetechnologies.faith_breed.downloadmanager.DownloadManagerService.PREFS_NAME;
import static primetechnologies.faith_breed.downloadmanager.DownloadManagerService.URILIST;
import static primetechnologies.faith_breed.payment.PaymentActivity.BALANCE_PREF;
import static primetechnologies.faith_breed.payment.PaymentActivity.BALANCE_WALLET;


public class AllAudioList extends AppCompatActivity implements
AllAudioDisplayAdapter.ListItemClickListener, AllAudioDisplayAdapter.MoreMenuClickListener,
NavigationView.OnNavigationItemSelectedListener{

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
    private int position;
    public static final String AMOUNT_TO_ADD = "amount-to-add";
    public static final String AUDNAME = "audio-name";
    public static final String AUDDOWNLINK = "audio-download-link";
    public static final String AUDIMAGELINK = "audio-image-link";
    public static final String AUDARTIST = "audio-artist";
    public static final String AUDSTREAMNAME = "audio-stream-name";
    public static final String AUDSTREAMLINK = "audio-stream-link";
    public static final String AUDSTREAMARTIST = "audio-stream-artist";
    private static final int REQUEST_CODE = 30;
    public static final String NAME_PAYEE = "name-of-payer";
    public static final String EMAIL_PAYEE = "email-of-payer";
    private FirebaseAuth mAuth;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_audio_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        requestAllPermission();
        mProgressDialog = new ProgressDialog(this);
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
//        PaymentActivity.addMoneyToUserBalance("0", this, mAuth);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AllAudioList.this, MainActivity.class);
                startActivity(intent);
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.all_messages) {
            // Handle the camera action
            position = 0;
        } else if (id == R.id.faith_alive) {
            position = 1;
        } else if (id == R.id.series) {
            position = 2;
        } else if (id == R.id.sun_wed) {
            position = 3;
        } else if (id == R.id.short_clips) {
            position = 4;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void loadData() {
        if (NetwotkUtils.isConnected(this)) {
            fetchDataFromFirebase();
        } else {
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
        } else if (id == R.id.wallet) {
            openWalletDialog();
        } else if (id == R.id.add_money) {
            openAmountDialog();
        } else if (id == R.id.home) {
            Intent intent = new Intent(this, AudioCategories.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.signout) {
            signOut();
        }else if (id == R.id.about) {
            Intent intent = new Intent(this, AboutApp.class);
            startActivity(intent);
        }else if (id == R.id.contactus) {
            contactUsDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    private void signOut() {
        mProgressDialog.setMessage("logging you out...");
        mProgressDialog.show();
        AuthUI.getInstance().signOut(this).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Intent intent = new Intent(AllAudioList.this, SignInActivity.class);
                startActivity(intent);
                mProgressDialog.dismiss();
                mProgressDialog.cancel();
                finish();
            }
        });
    }

    private void contactUsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_contact_us, null);
        builder.setView(view);
        final EditText name = view.findViewById(R.id.name);
        EditText email = view.findViewById(R.id.email);
        final EditText message = view.findViewById(R.id.message);
        Button licenses = view.findViewById(R.id.send);
        licenses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sendIntent = new Intent(Intent.ACTION_SENDTO);
                sendIntent.setType("text/plain");
                sendIntent.setData(Uri.parse("mailto:harjacober@gmail.com"));
//                sendIntent.setClassName("com.google.android.gm", "com.google.android.gm.ComposeActivityGmail");
                sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Mr "+name.getText().toString());
                sendIntent.putExtra(Intent.EXTRA_TEXT, message.getText().toString());
                sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    startActivity(Intent.createChooser(sendIntent, "Send email using..."));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(AllAudioList.this, "No email clients installed.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
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
        if (user != null) {
            final String uId = user.getUid();
            DatabaseReference rootReference = FirebaseDatabase.getInstance()
                    .getReference().child("users").child(uId);
            rootReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String balance = (String) dataSnapshot.child("balance").getValue();
                    SharedPreferences preferences = AllAudioList.this
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
        final EditText editText = view.findViewById(R.id.amount_edit_text);
        Button proceed = view.findViewById(R.id.proceed);
        builder.setView(view);
        final AlertDialog dialog = builder.create();
        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String amountHere = editText.getText().toString();
                if (inputIsValid(amountHere)) {
                    String email = mAuth.getCurrentUser().getEmail();
                    String name = mAuth.getCurrentUser().getDisplayName();
                    Intent intent = new Intent(AllAudioList.this, PaymentActivity.class);
                    intent.putExtra(AMOUNT_TO_ADD, amountHere);
                    intent.putExtra(NAME_PAYEE, name);
                    intent.putExtra(EMAIL_PAYEE, email);
                    startActivity(intent);
                    dialog.dismiss();
                }else {
                    Toast.makeText(AllAudioList.this, "Invalid amount",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialog.show();
    }

    private boolean inputIsValid(String amountHere) {
        try {
            Double aDouble = Double.valueOf(amountHere);
            if (aDouble >= 100) {
                return true;
            } else {
                return false;
            }
        }catch (Exception e){
            return false;
        }
    }

    @Override
    public void onListItemClick(String downloadLink, String imageLink,
                                String audioName, String audioArtist) {
        streamAudio(audioName, downloadLink, audioArtist);

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
                    streamAudio(mName, mDownloadLink, mArtist);
                    return true;
                case R.id.download:
                    if (NetwotkUtils.isConnected(AllAudioList.this)) {
                        if (!downloadedBefore(mName)) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(AllAudioList.this);
                            builder.setMessage("100.00NGN will be deducted from your wallet.\n" +
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
                        }else{
                            Toast.makeText(AllAudioList.this,
                                    "Message downloaded before", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(AllAudioList.this, MainActivity.class);
                            startActivity(intent);
                        }
                    } else {
                        Toast.makeText(AllAudioList.this, "No Internet Connection",
                                Toast.LENGTH_LONG).show();
                    }

            }
            return false;
        }
    }

    private void isWalletFunded() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String uId = user.getUid();
            DatabaseReference rootReference = FirebaseDatabase.getInstance()
                    .getReference().child("users").child(uId);
            rootReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String balance = (String) dataSnapshot.child("balance").getValue();
                    Integer balanceInt = Integer.valueOf(balance);
                    if (balanceInt >= 100) {
                        downloadAudio(mDownloadLink, mImageLink,
                                mName, mArtist);
                        String deductedAmount = "-100";
                        PaymentActivity.addMoneyToUserBalance(deductedAmount,
                                AllAudioList.this, mAuth);
                    } else {
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

    private boolean downloadedBefore(String audioName) {
        String pathName = Environment.getExternalStorageDirectory()+"/Android/data/primetechnologies.faith_breed/files/"
                +Environment.getExternalStorageDirectory()
                .toString()+"/.FaithBreed/.Audio/"+audioName+".mp3";
        File file = new File(pathName);
        if (file.exists()){
            return true;
        }
        return false;
    }

    void fetchDataFromFirebase(){
        mLoadIndicator.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.INVISIBLE);
        mNoConnectionView.setVisibility(View.INVISIBLE);

        DatabaseReference rootReference = null;
        /*Intent intentFromCategory = getIntent();
        int position = intentFromCategory.getIntExtra(AudioCategories.POSITION, 0);*/
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
            }else if (position == 4){
                rootReference = FirebaseDatabase.getInstance()
                        .getReference().child("audio").child("AUDIO CLIPS");
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

    public void streamAudio(String audioName, String audioLink, String audioArtist){
        Intent intent = new Intent(this, ExoPlayer.class);
        intent.putExtra(AUDSTREAMNAME, audioName);
        intent.putExtra(AUDSTREAMLINK, audioLink);
        intent.putExtra(AUDSTREAMARTIST, audioArtist);
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
/*
     class SignOutTask extends AsyncTask<Void, Void, Void>{
         @Override
         protected void onPreExecute() {
             super.onPreExecute();
             mProgressDialog.setMessage("logging you out...");
             mProgressDialog.show();
         }

         @Override
         protected Void doInBackground(Void... voids) {
             mAuth.signOut();

             return null;
         }

         @Override
         protected void onPostExecute(Void aVoid) {
             if (mAuth.getCurrentUser() == null){
                 Intent intent = new Intent(AllAudioList.this, SignInActivity.class);
                 startActivity(intent);
                 mProgressDialog.dismiss();
                 mProgressDialog.cancel();
                 finish();
             }
             super.onPostExecute(aVoid);
         }
     }*/

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mProgressDialog.cancel();
    }
}
