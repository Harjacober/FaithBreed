package primetechnologies.faith_breed.authentication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageException;

import java.util.HashMap;

import primetechnologies.faith_breed.AudioCategories;
import primetechnologies.faith_breed.R;

public class SignInActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    private static final int RC_SIGN_IN = 200;
    private static final String TAG = SignInActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);
        mAuth = FirebaseAuth.getInstance();
        if (isUSerLoggedIn()){
            Toast.makeText(this, "login successful", Toast.LENGTH_SHORT).show();
            launchCorrespondingActivity();
            finish();
        }else {
            launchFirebaseUi();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "login success", Toast.LENGTH_SHORT).show();
                launchCorrespondingActivity();
            }
            if (resultCode == RESULT_CANCELED){
                Toast.makeText(this, "login failed", Toast.LENGTH_SHORT).show();
                launchFirebaseUi();
            }
        }
        Toast.makeText(this, "unknown error", Toast.LENGTH_SHORT).show();
        launchFirebaseUi();
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
        startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder()
                .setTosUrl("")
                .setIsSmartLockEnabled(false)
                .build(), RC_SIGN_IN);
    }
}
