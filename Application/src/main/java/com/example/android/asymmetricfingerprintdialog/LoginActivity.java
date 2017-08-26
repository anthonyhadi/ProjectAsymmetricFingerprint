package com.example.android.asymmetricfingerprintdialog;

import android.app.KeyguardManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.fingerprint.FingerprintManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.asymmetricfingerprintdialog.server.Transaction;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;
    private static final int SUCCESS_SIGNIN = 1;

    private static final String DIALOG_FRAGMENT_TAG = "myFragment";
    /** Alias for our key in the Android Key Store */
    public static final String KEY_NAME = "my_key";

    @Bind(R.id.input_email) EditText _emailText;
    @Bind(R.id.input_password) EditText _passwordText;
    @Bind(R.id.btn_login) Button _loginButton;
    @Bind(R.id.btn_login_finger) Button _loginFingerButton;
    @Bind(R.id.link_signup) TextView _signupLink;

    @Inject KeyguardManager mKeyguardManager;
    @Inject FingerprintAuthenticationDialogFragment mFragment;
    @Inject Signature mSignature;
    @Inject SharedPreferences mSharedPreferences;
    @Inject KeyStore mKeyStore;
    @Inject KeyPairGenerator mKeyPairGenerator;
    @Inject FingerprintManager mFingerprintManager;

    ProgressDialog progressDialog;
    FingerprintManager.CryptoObject mCryptoObject;
    Transaction transaction;
    String userId;

    byte[] sigBytes;

    private class VerifyTask extends AsyncTask<JSONObject, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(JSONObject... params) {
            try {
                URL url = new URL("http://182.16.165.81:8080/main/verify");
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
                onLoginSuccess();
                progressDialog.dismiss();
            } else {
                // failed
                onLoginFailed();
                progressDialog.dismiss();
            }
        }
    }

    private class VerifyFingerprintTask extends AsyncTask<JSONObject, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(JSONObject... params) {
            try {
                URL url = new URL("http://182.16.165.81:8080/main/verify");
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
                if (json.get("publicKey") != null) {
                    byte[] publicKeyByte = Base64.decode(json.get("publicKey").toString(), Base64.DEFAULT);
                    PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(publicKeyByte));
                    Signature verificationFunction = Signature.getInstance("SHA256withECDSA");
                    verificationFunction.initVerify(publicKey);
                    verificationFunction.update(transaction.toByteArray());
                    if (verificationFunction.verify(sigBytes)) {
                        // Transaction is verified with the public key associated with the user
                        // Do some post purchase processing in the server
                        return Boolean.TRUE;
                    } else {
                        return Boolean.FALSE;
                    }
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
                progressDialog.dismiss();
            } else {
                // failed
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((InjectedApplication) getApplication()).inject(this);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        SharedPreferences prefs = this.getSharedPreferences("com.bluecamel.app", Context.MODE_PRIVATE);

        String userid_key = "com.bluecamel.app.userid";

        // use a default value using new Date()
        userId = prefs.getString(userid_key, "demo");

        _emailText.setText(userId);

        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });

//        if (!mKeyguardManager.isKeyguardSecure()) {
//            // Show a message that the user hasn't set up a fingerprint or lock screen.
//            Toast.makeText(this,
//                    "Secure lock screen hasn't set up.\n"
//                            + "Go to 'Settings -> Security -> Fingerprint' to set up a fingerprint",
//                    Toast.LENGTH_LONG).show();
//            purchaseButton.setEnabled(false);
//            return;
//        }
//        //noinspection ResourceType
//        if (!mFingerprintManager.hasEnrolledFingerprints()) {
//            purchaseButton.setEnabled(false);
//            // This happens when no fingerprints are registered.
//            Toast.makeText(this,
//                    "Go to 'Settings -> Security -> Fingerprint' and register at least one fingerprint",
//                    Toast.LENGTH_LONG).show();
//            return;
//        }
        createKeyPair();

        _loginFingerButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
//                findViewById(R.id.confirmation_message).setVisibility(View.GONE);
//                findViewById(R.id.encrypted_message).setVisibility(View.GONE);

                // Set up the crypto object for later. The object will be authenticated by use
                // of the fingerprint.
                if (initSignature()) {

                    // Show the fingerprint dialog. The user has the option to use the fingerprint with
                    // crypto, or you can fall back to using a server-side verified password.
                    mFragment.setUserId(userId);
                    mFragment.setCryptoObject(new FingerprintManager.CryptoObject(mSignature));
                    boolean useFingerprintPreference = mSharedPreferences
                            .getBoolean(getString(R.string.use_fingerprint_to_authenticate_key),
                                    true);
                    if (useFingerprintPreference) {
                        mFragment.setStage(
                                FingerprintAuthenticationDialogFragment.Stage.FINGERPRINT);
                    } else {
                        mFragment.setStage(
                                FingerprintAuthenticationDialogFragment.Stage.PASSWORD);
                    }
                    mFragment.show(getFragmentManager(), DIALOG_FRAGMENT_TAG);
                } else {
                    // This happens if the lock screen has been disabled or or a fingerprint got
                    // enrolled. Thus show the dialog to authenticate with their password first
                    // and ask the user if they want to authenticate with fingerprints in the
                    // future
                    mFragment.setStage(
                            FingerprintAuthenticationDialogFragment.Stage.NEW_FINGERPRINT_ENROLLED);
                    mFragment.show(getFragmentManager(), DIALOG_FRAGMENT_TAG);
                }
                //loginFinger();
            }
        });

        _signupLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });
    }

    /**
     * Initialize the {@link Signature} instance with the created key in the
     * {@link #createKeyPair()} method.
     *
     * @return {@code true} if initialization is successful, {@code false} if the lock screen has
     * been disabled or reset after the key was generated, or if a fingerprint got enrolled after
     * the key was generated.
     */
    private boolean initSignature() {
        try {
            mKeyStore.load(null);
            PrivateKey key = (PrivateKey) mKeyStore.getKey(KEY_NAME, null);
            mSignature.initSign(key);
            return true;
        } catch (KeyPermanentlyInvalidatedException e) {
            return false;
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException | IOException
                | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }
    }

    /**
     * Generates an asymmetric key pair in the Android Keystore. Every use of the private key must
     * be authorized by the user authenticating with fingerprint. Public key use is unrestricted.
     */
    public void createKeyPair() {
        // The enrolling flow for fingerprint. This is where you ask the user to set up fingerprint
        // for your flow. Use of keys is necessary if you need to know if the set of
        // enrolled fingerprints has changed.
        try {
            // Set the alias of the entry in Android KeyStore where the key will appear
            // and the constrains (purposes) in the constructor of the Builder
            mKeyPairGenerator.initialize(
                    new KeyGenParameterSpec.Builder(KEY_NAME,
                            KeyProperties.PURPOSE_SIGN)
                            .setDigests(KeyProperties.DIGEST_SHA256)
                            .setAlgorithmParameterSpec(new ECGenParameterSpec("secp256r1"))
                            // Require the user to authenticate with a fingerprint to authorize
                            // every use of the private key
                            .setUserAuthenticationRequired(true)
                            .build());
            mKeyPairGenerator.generateKeyPair();
        } catch (InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        }
    }

    public void login() {
        Log.d(TAG, "Login");

        if (!validate()) {
            onLoginFailed();
            return;
        }

        _loginButton.setEnabled(false);

        progressDialog = new ProgressDialog(LoginActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        // TODO: Implement your own authentication logic here.
        JSONObject obj = new JSONObject();
        try {
            obj.put("userId", email);
            obj.put("password", password);
            new VerifyTask().execute(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loginFinger() {
        Log.d(TAG, "LoginFinger");

        if (!validateFinger()) {
            onLoginFailed();
            return;
        }

        _loginButton.setEnabled(false);

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        // TODO: open fingerprint dialog and call /verify on server.
        JSONObject obj = new JSONObject();
        try {
            Signature signature = mCryptoObject.getSignature();
            sigBytes = signature.sign();
            transaction = new Transaction(email, 1, new SecureRandom().nextLong());
            obj.put("userId", email);
            obj.put("password", "");
            new VerifyFingerprintTask().execute(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {

                // TODO: Implement successful signup logic here
                // By default we just finish the Activity and log them in automatically
                this.finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        // Disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void onLoginSuccess() {
        userId = _emailText.getText().toString();
        _loginButton.setEnabled(true);
        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
        intent.putExtra("USER_ID", userId);
        startActivityForResult(intent, SUCCESS_SIGNIN);
        finish();
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();

        _loginButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (email.isEmpty()) {
            _emailText.setError("username cannot be empty");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }

    public boolean validateFinger() {
        boolean valid = true;

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (email.isEmpty()) {
            _emailText.setError("username cannot be empty");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        return valid;
    }
}