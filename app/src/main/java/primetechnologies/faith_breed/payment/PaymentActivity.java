package primetechnologies.faith_breed.payment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.flutterwave.raveandroid.RaveConstants;
import com.flutterwave.raveandroid.RavePayActivity;
import com.flutterwave.raveandroid.RavePayManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.UUID;

import primetechnologies.faith_breed.AllAudioList;
import primetechnologies.faith_breed.R;

public class PaymentActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String uId;
    public static final String BALANCE_PREF = "balance-sharedpref";
    public static final String BALANCE_WALLET = "wallet-balance";
    private RelativeLayout relativeLayout;
    private ImageView imageView;
    private TextView textView;
    private String amount;
    String name ;
    String email;
    RelativeLayout rel;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        mAuth = FirebaseAuth.getInstance();

        rel = findViewById(R.id.relLayout);
        progressDialog = new ProgressDialog(this);
        Intent intent = getIntent();
        if (intent.hasExtra(AllAudioList.AMOUNT_TO_ADD)){ 
            amount = intent.getStringExtra(AllAudioList.AMOUNT_TO_ADD);
            name = intent.getStringExtra(AllAudioList.NAME_PAYEE);
            email = intent.getStringExtra(AllAudioList.EMAIL_PAYEE);
            retrieveSecretKey(this);
        }

        android.support.v7.app.ActionBar actiobar=getSupportActionBar();
        if (actiobar!=null){
            actiobar.setDisplayHomeAsUpEnabled(true);
        imageView = findViewById(R.id.image_view);
    }
        textView = findViewById(R.id.text_view1);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RaveConstants.RAVE_REQUEST_CODE && data != null) {
            String message = data.getStringExtra("response");
            rel.setVisibility(View.VISIBLE);
            if (resultCode == RavePayActivity.RESULT_SUCCESS) {
                addMoneyToUserBalance(amount,
                        PaymentActivity.this, mAuth);
                imageView.setImageResource(R.drawable.successss);
                textView.setText("Payment Success");
            }
            else if (resultCode == RavePayActivity.RESULT_ERROR) {
                imageView.setImageResource(R.drawable.failed);
                textView.setText("Payment Failed");

            }
            else if (resultCode == RavePayActivity.RESULT_CANCELLED) {
                imageView.setImageResource(R.drawable.cancelled);
                textView.setText("Payment Cancelled");
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId() ;
        if (id==android.R.id.home){
            NavUtils.navigateUpFromSameTask(this);
        }
        return super.onOptionsItemSelected(item);
    }

    public static void addMoneyToUserBalance(final String amount, final Context context,
                                             FirebaseAuth mAuth) {
        FirebaseUser user = mAuth.getCurrentUser();
        final DatabaseReference mDatabase = FirebaseDatabase.getInstance()
                .getReference().child("users");
        if ( user != null){
            final String id = user.getUid();
            DatabaseReference rootReference = FirebaseDatabase.getInstance()
                    .getReference().child("users").child(id);
            rootReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String balance = (String) dataSnapshot.child("balance").getValue();
                    SharedPreferences preferences =context.getSharedPreferences(BALANCE_PREF, MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(BALANCE_WALLET, balance);
                    editor.commit();
                    if (balance == null){
                        balance = "0";
                    }
                    Integer prevBalInt = Integer.valueOf(balance);
                    Integer amountAdded = Integer.valueOf(amount);
                    HashMap<String, String> dataMap = new HashMap<>();
                    dataMap.put("balance", String.valueOf(prevBalInt + amountAdded));
                    mDatabase.child(id).setValue(dataMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if (databaseError!=null){
                                Toast.makeText(context,
                                        "Balance will be added later", Toast.LENGTH_SHORT).show();
                            }else{
                                //Retrieve balance from server and save in s.preference
                                retrieveCurrBalance(id, context);
                            }
                        }
                    });

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    public static void retrieveCurrBalance(String uId, final Context context) {
        DatabaseReference rootReference = FirebaseDatabase.getInstance()
                .getReference().child("users").child(uId);
        rootReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String balance = (String) dataSnapshot.child("balance").getValue();
                SharedPreferences preferences =context.getSharedPreferences(BALANCE_PREF, MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(BALANCE_WALLET, balance);
                editor.commit();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void processPayment(Activity activity, double amount,
                               String country, String currency,
                               String email, String fName,
                               String lName, String narration,
                               String txRef, String secretKey){
        new RavePayManager(activity).setAmount(amount)
                .setCountry(country)
                .setCurrency(currency)
                .setEmail(email)
                .setfName(fName)
                .setlName(lName)
                .setNarration(narration)
                .setPublicKey("FLWPUBK-e6ec740b90b2687085db8af55b574c14-X")
                .setSecretKey(secretKey)
                .setTxRef(txRef)
                .acceptAccountPayments(true)
                .acceptCardPayments(true)
                .allowSaveCardFeature(true)
                .withTheme(R.style.DefaultTheme)
                .initialize();
    }

    public void OnGoToHomePageClicked(View view) {
        Intent intent = new Intent(this, AllAudioList.class);
        startActivity(intent);
        finish();
    }

    public void retrieveSecretKey(final Context context) {
        DatabaseReference rootReference = FirebaseDatabase.getInstance()
                .getReference();
        rel.setVisibility(View.INVISIBLE);
        progressDialog.setMessage("loading...");
        progressDialog.show();
        rootReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String mySecretKey = (String) dataSnapshot.child("secretKey").getValue();
                processPayment((Activity) context, Double.valueOf(amount), "NG",
                        "NGN",email, name,
                        "Mr", "Message Payment",
                        generateReference(), mySecretKey);
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private String generateReference() {
        return UUID.randomUUID().toString();
    }
}
