package com.example.negativeion.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.negativeion.Attribute;
import com.example.negativeion.R;
import com.example.negativeion.model.GoogleInfoModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SignInActivity extends AppCompatActivity  implements
        View.OnClickListener{

    private String idToken;
    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9001;
    private int signInStatusCode = GoogleSignInStatusCodes.SUCCESS;

    private GoogleSignInClient mGoogleSignInClient;
    private static ImageView mImageView;
    private ProgressDialog progressDialog;

    private Runnable getProfileRunnable;

    private GoogleInfoModel topicGoogleUserData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        mImageView = findViewById(R.id.imageView);
        mImageView.setImageResource(R.drawable.member);

        /** Google sign in*/
        // Doesn't need to set the dimensions of the sign-in button.
        // Use the sign-in button of the other developers.
        //SignInButton signInButton = findViewById(R.id.sign_in_button);
        //signInButton.setSize(SignInButton.SIZE_STANDARD);

        // [START configure_signin]
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .build();
        // [END configure_signin]

        // [START build_client]
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        // [END build_client]
        findViewById(R.id.sign_in_button).setOnClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();

        if(getIntent().getBooleanExtra("Sign out", false)){
            signOut();
            getIntent().putExtra("Sign out", false);
        }
        else {
            // [START on_start_sign_in]
            // Check for existing Google Sign In account, if the user is already signed in
            // the GoogleSignInAccount will be non-null.
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
            updateUI(account);
        }
        // [END on_start_sign_in]
        initRunnable();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
        //finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //android.os.Process.killProcess(android.os.Process.myPid());
    }

    // [START onActivityResult]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }
    // [END onActivityResult]

    // [START handleSignInResult]
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            idToken = account.getIdToken();
            Log.w(TAG,"idToken=" + idToken);
            new Thread(getProfileRunnable).start();
            // Signed in successfully, show authenticated UI.
            updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            signInStatusCode = e.getStatusCode();
            updateUI(null);
        }
    }
    // [END handleSignInResult]

    // [START signIn]
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    // [END signIn]

    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        updateUI(null);
                    }
                });
    }

    private void updateUI(@Nullable GoogleSignInAccount account) {
        if (account != null) {
            progressDialog = new ProgressDialog(SignInActivity.this);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage(this.getString(R.string.signing_in));
            progressDialog.show();

            Intent intent = new Intent(SignInActivity.this, MainActivity.class);
            intent.putExtra(Attribute.USER_PHOTOURL, account.getPhotoUrl().toString())
                    .putExtra(Attribute.USER_NAME, account.getDisplayName())
                    .putExtra(Attribute.USER_ID, account.getId());

            new Handler().postDelayed(() -> {
                progressDialog.dismiss();
                startActivity(intent);
            }, 500);
        } else {
            mImageView.setImageResource(R.drawable.member);
            if(signInStatusCode == GoogleSignInStatusCodes.NETWORK_ERROR)
                Toast.makeText(this, "請連上網路", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
        }
    }

    private void initRunnable() {
        getProfileRunnable = new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient.Builder()
                        .build();

                Request request = new Request.Builder()
                        .url("https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken )
                        .method("GET", null)
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    Gson gson = new Gson();
                    topicGoogleUserData = gson.fromJson(response.body().string() //此格式形同JsonArray的主體
                            , new TypeToken<GoogleInfoModel>(){ }.getType());
                    Log.w(TAG, topicGoogleUserData.getUserId());
                    //Log.w(TAG, topicGoogleUserData.getUserId());
                } catch (Exception e){Log.w(TAG,"Error:"+e.getMessage());}
            }
        };
    }
}
