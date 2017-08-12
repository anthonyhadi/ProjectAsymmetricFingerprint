package com.example.android.asymmetricfingerprintdialog;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.alimuzaffar.lib.pin.PinEntryEditText;
import com.example.android.asymmetricfingerprintdialog.server.StoreBackend;
import com.example.android.asymmetricfingerprintdialog.server.StoreBackendImpl;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

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

    @Inject
    StoreBackend mStoreBackend;
    String userId;
    String password;
    ProgressDialog progressDialog;

    private class EnrollTask extends AsyncTask<JSONObject, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(JSONObject... params) {
            try {
                URL url = new URL("http://192.168.110.154:6969/hackathon/enroll");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setChunkedStreamingMode(0);
                urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                urlConnection.setRequestProperty("Accept", "application/json");

                OutputStreamWriter wr = new OutputStreamWriter(urlConnection.getOutputStream());
                wr.write(params[0].toString());
                wr.close();

                InputStream stream = urlConnection.getInputStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                    Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)

                }

                String jsonString = buffer.toString();

                JSONObject json = new JSONObject(jsonString);
                urlConnection.disconnect();
                if (Integer.parseInt(json.get("status").toString()) == 200) {
                    return Boolean.TRUE;
                } else {
                    return Boolean.FALSE;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return Boolean.FALSE;
            }
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Boolean result) {
            if (result) {
                // success
                onSignupSuccess();
                // onLoginFailed();
                progressDialog.dismiss();
            } else {
                // failed
            }
        }
    }

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
                progressDialog = new ProgressDialog(VerifyActivity.this,
                        R.style.AppTheme_Dark_Dialog);
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage("Verifying PIN...");
                progressDialog.show();
                // TODO: call service /enroll {userId, password, publicKey, pin}

                JSONObject obj = new JSONObject();
                try {
                    obj.put("userId", userId);
                    obj.put("password", password);
                    obj.put("pin", str);

                    KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
                    keyStore.load(null);
                    PublicKey publicKey = keyStore.getCertificate(MainActivity.KEY_NAME).getPublicKey();
                    KeyFactory factory = KeyFactory.getInstance(publicKey.getAlgorithm());
                    X509EncodedKeySpec spec = new X509EncodedKeySpec(publicKey.getEncoded());
                    PublicKey verificationKey = factory.generatePublic(spec);
                    String algorithm = verificationKey.getAlgorithm();
                    StoreBackendImpl.verificationKey = verificationKey;
                    byte[] publicKeyByte = verificationKey.getEncoded();
                    String base64Encoded = Base64.encodeToString(publicKeyByte, Base64.DEFAULT);
                    obj.put("publicKey", base64Encoded);
                    new EnrollTask().execute(obj);
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
