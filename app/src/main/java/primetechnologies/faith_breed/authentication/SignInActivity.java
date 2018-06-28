package primetechnologies.faith_breed.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;

import primetechnologies.faith_breed.AudioCategories;
import primetechnologies.faith_breed.R;
import primetechnologies.faith_breed.payment.PaymentActivity;
import primetechnologies.faith_breed.utils.NetwotkUtils;

public class SignInActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    private static final int RC_SIGN_IN = 200;
    private static final String TAG = SignInActivity.class.getSimpleName();
    TextView splashText;
    TextView splashText2;
    LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        getSupportActionBar().hide();
        splashText = findViewById(R.id.splash_text);
        splashText2 = findViewById(R.id.splash_text2);
        linearLayout = findViewById(R.id.linear_layout);
        showAnimation();
        mAuth = FirebaseAuth.getInstance();

        splashText2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (splashText2.getText() == "RETRY"){
                    if (NetwotkUtils.isConnected(SignInActivity.this)){
                        launchFirebaseUi();
                    }
                }
            }
        });
    }

    private void showAnimation() {
        final ImageView splashImage = findViewById(R.id.splash_image);
        final Animation animation_1 = AnimationUtils.loadAnimation(getBaseContext(), R.anim.rotate);
        Animation animation_2 = AnimationUtils.loadAnimation(getBaseContext(), R.anim.antirotate);
        final Animation animation_3 = AnimationUtils.loadAnimation(getBaseContext(), R.anim.abc_fade_out);

        splashImage.startAnimation(animation_2);
        animation_2.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                splashImage.startAnimation(animation_1);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        animation_1.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                splashImage.startAnimation(animation_3);
                linearLayout.setVisibility(View.INVISIBLE);
                if (isUSerLoggedIn()){
                    launchCorrespondingActivity();
                    finish();
                }else {
                    if (NetwotkUtils.isConnected(SignInActivity.this)) {
                        launchFirebaseUi();
                    }else{
                        linearLayout.setVisibility(View.VISIBLE);
                        splashImage.setImageResource(R.drawable.ic_error_outline_black_24dp);
                        splashText.setText(R.string.no_internet_connection);
                        splashText2.setText("RETRY");
                    }
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "login success", Toast.LENGTH_SHORT).show();
                PaymentActivity.addMoneyToUserBalance("0", this, mAuth);
                launchCorrespondingActivity();
            }
            if (resultCode == RESULT_CANCELED){
                Toast.makeText(this, "login failed", Toast.LENGTH_SHORT).show();
                launchFirebaseUi();
            }
        }else{
            Toast.makeText(this, "unknown error", Toast.LENGTH_SHORT).show();
            launchFirebaseUi();
        }
    }

    private void launchCorrespondingActivity() {
        Intent intent = new Intent(this, AudioCategories.class);
        startActivity(intent);
        finish();
    }

    private boolean isUSerLoggedIn(){
        if (mAuth.getCurrentUser() != null){
            return true;
        }
        return false;
    }

    public  void launchFirebaseUi(){
        startActivityForResult(AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setTosUrl("")
                .setTheme(R.style.LoginTheme)
                .setIsSmartLockEnabled(true)
                .build(), RC_SIGN_IN);
    }
}
