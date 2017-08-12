package com.example.android.asymmetricfingerprintdialog;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.alimuzaffar.lib.pin.PinEntryEditText;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

public class VerifyActivity extends AppCompatActivity {
    private static final String TAG = "VerifyActivity";

    @Inject SharedPreferences mSharedPreferences;

    @Bind(R.id.title_pin) TextView _pinTitle;
    @Bind(R.id.title_request) TextView _requestTitle;
    @Bind(R.id.txt_pin_entry) PinEntryEditText _pinEntry;
    @Bind(R.id.link_login) TextView _loginLink;
    String userId;
    String password;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);
        ButterKnife.bind(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            userId = extras.getString("USER_ID");
            password = extras.getString("PASSWORD");
            _requestTitle.setText("request-id : " + userId);
        }

        _loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });

        _pinEntry.setOnPinEnteredListener(new PinEntryEditText.OnPinEnteredListener() {
            @Override
            public void onPinEntered(CharSequence str) {
                final ProgressDialog progressDialog = new ProgressDialog(VerifyActivity.this,
                        R.style.AppTheme_Dark_Dialog);
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage("Verifying PIN...");
                progressDialog.show();
                // TODO: call service /enroll {userId, password, publicKey, pin}

                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                // On complete call either onLoginSuccess or onLoginFailed
                                onSignupSuccess();
                                // onLoginFailed();
                                progressDialog.dismiss();
                            }
                        }, 1000);

                _pinEntry.setText(null);
            }
        });
    }

    public void onSignupSuccess() {
        SharedPreferences prefs = this.getSharedPreferences("com.bluecamel.app", Context.MODE_PRIVATE);
        String userid_key = "com.bluecamel.app.userid";
        prefs.edit().putString(userid_key, userId).apply();

        setResult(RESULT_OK, null);
        _pinEntry.setText(null);
        // Finish the registration screen and return to the Login activity
        Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    public void onSignupFailed() {
        _pinEntry.setText(null);
        Toast.makeText(getBaseContext(), "Verify PIN failed", Toast.LENGTH_LONG).show();
    }
}
