package com.example.negativeion.ui.pager;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.negativeion.R;
import com.example.negativeion.activity.SignInActivity;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class AboutFragment extends Fragment {

    private GoogleSignInClient mGoogleSignInClient;
    private static ImageView mUserImage;
    private TextView mUserName;
    public AboutFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_about, container, false);
        mUserImage = view.findViewById(R.id.userImage);
        mUserName = view.findViewById(R.id.userName);

        view.findViewById(R.id.sign_out_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SignInActivity.class);
                intent.putExtra("Sign out", true);
                startActivity(intent);
            }
        });


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        Bundle bundle=getArguments();
        updateUI(bundle);
    }

    private void updateUI(@Nullable  Bundle bundle)
    {
        String photoUri = bundle.getString("User photoUrl");
        String userName = bundle.getString("User name");
        String userID = bundle.getString("User ID");
        mUserName.setText("歡迎 " + userName + " !");
        startImageTask(photoUri);
    }
    public static void startImageTask(String photoUri)
    {
        //建立一個AsyncTask執行緒進行圖片讀取動作，並帶入圖片連結網址路徑
        new AsyncTask<String, Void, Bitmap>()
        {
            @Override
            protected Bitmap doInBackground(String... params)
            {
                String url = params[0];
                return getBitmapFromURL(url);
            }

            @Override
            protected void onPostExecute(Bitmap result)
            {
                mUserImage.setImageBitmap(result);
                super.onPostExecute(result);
            }
        }.execute(photoUri);

    }
    public static Bitmap getBitmapFromURL(String photoUri)
    {
        try
        {
            URL url = new URL(photoUri);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.connect();
            InputStream isCover = conn.getInputStream();
            Bitmap bmpCover = BitmapFactory.decodeStream(isCover);
            isCover.close();
            return bmpCover;
        }
        catch (Exception e)
        {
            return null;
        }
    }

}
