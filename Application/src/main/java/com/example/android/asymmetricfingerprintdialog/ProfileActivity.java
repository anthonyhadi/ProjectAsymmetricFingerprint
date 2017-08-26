package com.example.android.asymmetricfingerprintdialog;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Emerio on 8/26/2017.
 */

public class ProfileActivity extends AppCompatActivity {

    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile2);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            userId = extras.getString("userId");
            String email = extras.getString("email");
            String mobile = extras.getString("mobile");
            Integer exp = Integer.parseInt(extras.getString("exp"));
            String title = extras.getString("title");
            String avatar = extras.getString("avatar");
            Integer nextExp = Integer.parseInt(extras.getString("nextExp"));

            EditText userIdTextView = (EditText) findViewById(R.id.input_nama);
            userIdTextView.setText(userId);

            EditText emailTextView = (EditText) findViewById(R.id.input_email);
            emailTextView.setText(email);

            EditText mobileTextView = (EditText) findViewById(R.id.input_mobile);
            mobileTextView.setText(mobile);

            EditText titleTextView = (EditText) findViewById(R.id.input_title);
            titleTextView.setText(title);

            ProgressBar expProgressBar = (ProgressBar) findViewById(R.id.progressBar_exp);
            expProgressBar.setMax(nextExp);
            expProgressBar.setProgress(exp);

            int avatarId = getResources().getIdentifier(avatar, "drawable", getPackageName());
            ImageView avatarImageView = (ImageView) findViewById(R.id.imageView_avatar);
            avatarImageView.setImageResource(avatarId);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(this, MapsActivity.class);
        this.finish();
        startActivity(i);
    }
}
