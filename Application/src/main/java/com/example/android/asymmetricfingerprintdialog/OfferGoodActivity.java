package com.example.android.asymmetricfingerprintdialog;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

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

public class OfferGoodActivity extends AppCompatActivity {

    AppCompatButton submitButton;
    String latCurr;
    String lngCurr;
    ProgressDialog progressDialog;

    private class OfferTask extends AsyncTask<JSONObject, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(JSONObject... params) {
            try {
                URL url = new URL("http://182.16.165.81:8080/main/offer");
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
                progressDialog.dismiss();
                Toast.makeText(getBaseContext(), "Submit sukses",
                        Toast.LENGTH_LONG).show();

                Intent intent = new Intent(getApplicationContext(),MapsActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            } else {
                // failed
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offer_good);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            Double latCurrDouble = new Double(extras.getString("LAT_CURR"));
            latCurr = latCurrDouble.toString();
            Double lngCurrDouble = new Double(extras.getString("LNG_CURR"));
            lngCurr = lngCurrDouble.toString();
        }

        Spinner spinner = (Spinner) findViewById(R.id.category);
// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.category_array, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        submitButton = (AppCompatButton) findViewById(R.id.btn_submit);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog = new ProgressDialog(OfferGoodActivity.this,
                        R.style.AppTheme_Dark_Dialog);
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage("Submiting...");
                progressDialog.show();

                Spinner categorySpinner = (Spinner) findViewById(R.id.category);
                String category = categorySpinner.getSelectedItem().toString();

                EditText nameEditText = (EditText) findViewById(R.id.editText3);
                String name = nameEditText.getText().toString();

                EditText tagsEditText = (EditText) findViewById(R.id.editText4);
                String tags = tagsEditText.getText().toString();

                EditText costEditText = (EditText) findViewById(R.id.cost);
                String cost = costEditText.getText().toString();

                EditText descEditText = (EditText) findViewById(R.id.description);
                String desc = descEditText.getText().toString();

                JSONObject obj = new JSONObject();
                try {
                    obj.put("type", "barang");
                    obj.put("category", category);
                    obj.put("name", name);
                    obj.put("tag", tags);
                    obj.put("cost", cost);
                    obj.put("desc", desc);
                    obj.put("lat", latCurr);
                    obj.put("lng", lngCurr);

                    new OfferTask().execute(obj);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(this, MapsActivity.class);
        this.finish();
        startActivity(i);
    }
}
