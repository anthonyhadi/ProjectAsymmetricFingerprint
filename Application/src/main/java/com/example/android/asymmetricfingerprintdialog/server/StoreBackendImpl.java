/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.example.android.asymmetricfingerprintdialog.server;


import android.util.Base64;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A fake backend implementation of {@link StoreBackend}.
 */
public class StoreBackendImpl implements StoreBackend {

    private final Map<String, PublicKey> mPublicKeys = new HashMap<>();
    private final Set<Transaction> mReceivedTransactions = new HashSet<>();

    @Override
    public boolean verify(Transaction transaction, byte[] transactionSignature) {
        try {
            if (mReceivedTransactions.contains(transaction)) {
                // It verifies the equality of the transaction including the client nonce
                // So attackers can't do replay attacks.
                return false;
            }
            mReceivedTransactions.add(transaction);
            PublicKey publicKey = mPublicKeys.get(transaction.getUserId());
            Signature verificationFunction = Signature.getInstance("SHA256withECDSA");
            verificationFunction.initVerify(publicKey);
            verificationFunction.update(transaction.toByteArray());
            if (verificationFunction.verify(transactionSignature)) {
                // Transaction is verified with the public key associated with the user
                // Do some post purchase processing in the server
                return true;
            }
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            // In a real world, better to send some error message to the user
        }
        return false;
    }

    @Override
    public boolean verify(Transaction transaction, String password) {
        // As this is just a sample, we always assume that the password is right.
        return true;
    }

    @Override
    public boolean enroll(final String userId, final String password, final PublicKey publicKey, final String pin) {
        if (publicKey != null) {
            Thread thread = new Thread() {
                public void run() {
                    try {
                        JSONObject obj = new JSONObject();
                        obj.put("userId", userId);
                        obj.put("password", password);
                        obj.put("pin", pin);

                        byte[] publicKeyByte = publicKey.getEncoded();
                        String base64Encoded = Base64.encodeToString(publicKeyByte, Base64.DEFAULT);
                        obj.put("publicKey", base64Encoded);

                        URL url = new URL("http://192.168.110.154:6969/hackathon/enroll");
                        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                        urlConnection.setDoOutput(true);
                        urlConnection.setChunkedStreamingMode(0);
                        urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                        urlConnection.setRequestProperty("Accept", "application/json");

                        OutputStreamWriter wr = new OutputStreamWriter(urlConnection.getOutputStream());
                        wr.write(obj.toString());
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
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            thread.start();
//            mPublicKeys.put(userId, publicKey);
        }
        // We just ignore the provided password here, but in real life, it is registered to the
        // backend.
        return true;
    }

    @Override
    public boolean generatePin(final String userId, final String password, final String email, final String mobile) {
        Thread thread = new Thread() {
            public void run() {
                try {
                    JSONObject obj = new JSONObject();
                    obj.put("userId", userId);
                    obj.put("password", password);
                    obj.put("email", email);
                    obj.put("mobile", mobile);

                    URL url = new URL("http://192.168.110.154:6969/hackathon/generatePin");
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setDoOutput(true);
                    urlConnection.setChunkedStreamingMode(0);
                    urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    urlConnection.setRequestProperty("Accept", "application/json");

                    OutputStreamWriter wr = new OutputStreamWriter(urlConnection.getOutputStream());
                    wr.write(obj.toString());
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
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
        return true;
    }
}
