package com.example.android.asymmetricfingerprintdialog;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.OvershootInterpolator;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.arlib.floatingsearchview.FloatingSearchView;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.LocationListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;

public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        GoogleMap.OnCameraMoveStartedListener,
        GoogleMap.OnCameraMoveListener {
    private static final String TAG = "MapsActivity";

    GoogleMap mGoogleMap;
    SupportMapFragment mapFrag;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    Marker mTargetMarker;
    FloatingActionMenu menu;
    FloatingActionMenu menu3;
    FloatingActionButton offerGoodsBtn;
    FloatingActionButton offerServicesBtn;
    FloatingActionButton compassBtn;
    FloatingActionButton profileBtn;
    FloatingActionButton pinjamBtn;
    FloatingSearchView mSearchView;

    FloatingActionButton searchMoneyBtn;
    FloatingActionButton searchServicesBtn;
    FloatingActionButton searchGoodsBtn;

    String myAvatar = "cyclops";
    String myTarget = "goal";
    String myTitle = "me";
    String userId = "";

    double latInitial = -6.174668;
    double lngInitial = 106.827126;
    float zoomInitial = 15.0f;
    double latCurr = latInitial;
    double lngCurr = lngInitial;

    boolean isTracked = true;
    List<Marker> markerList = new ArrayList<>();

    String type;
    JSONObject obj = null;
    JSONObject objProfile = null;
    String searchKey;

    ProgressDialog progressDialog;

    private class ProfileTask extends AsyncTask<JSONObject, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(JSONObject... params) {
            try {
                URL url = new URL("http://182.16.165.81:8080/main/getProfile");
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

                objProfile = new JSONObject(jsonString);
                urlConnection.disconnect();
                return Boolean.TRUE;
            } catch (Exception e) {
                e.printStackTrace();
                return Boolean.FALSE;
            }
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Boolean result) {
            progressDialog.dismiss();
            if (result) {
                // success
                try {
                    Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                    intent.putExtra("userId", objProfile.get("userId").toString());
                    intent.putExtra("email", objProfile.get("email").toString());
                    intent.putExtra("mobile", objProfile.get("mobile").toString());
                    intent.putExtra("exp", objProfile.get("exp").toString());
                    intent.putExtra("title", objProfile.get("title").toString());
                    intent.putExtra("avatar", objProfile.get("avatar").toString());
                    intent.putExtra("nextExp", objProfile.get("nextExp").toString());
                    startActivity(intent);
                    finish();
                    overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                // failed
                Toast.makeText(getBaseContext(), "Gagal memuat profil",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private class SearchTask extends AsyncTask<JSONObject, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(JSONObject... params) {
            try {
                URL url = new URL("http://182.16.165.81:8080/main/getOfferByLatLng");
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

                JSONArray json = new JSONArray(jsonString);
                urlConnection.disconnect();
                return true;
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
            } else {
                // failed
            }
        }
    }

    private class GetProfileTask extends AsyncTask<JSONObject, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(JSONObject... params) {
            try {
                URL url = new URL("http://182.16.165.81:8080/main/getProfile");
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
                myAvatar = json.getString("avatar");
                myTitle = json.getString("title");
                return true;
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
            } else {
                // failed
            }
        }
    }

    private void createCustomAnimation() {
        AnimatorSet set = new AnimatorSet();

        ObjectAnimator scaleOutX = ObjectAnimator.ofFloat(menu.getMenuIconView(), "scaleX", 1.0f, 0.2f);
        ObjectAnimator scaleOutY = ObjectAnimator.ofFloat(menu.getMenuIconView(), "scaleY", 1.0f, 0.2f);

        ObjectAnimator scaleInX = ObjectAnimator.ofFloat(menu.getMenuIconView(), "scaleX", 0.2f, 1.0f);
        ObjectAnimator scaleInY = ObjectAnimator.ofFloat(menu.getMenuIconView(), "scaleY", 0.2f, 1.0f);

        scaleOutX.setDuration(50);
        scaleOutY.setDuration(50);

        scaleInX.setDuration(150);
        scaleInY.setDuration(150);

        scaleInX.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                menu.getMenuIconView().setImageResource(menu.isOpened()
                        ? R.drawable.megaphone : R.drawable.closed);
            }
        });

        set.play(scaleOutX).with(scaleOutY);
        set.play(scaleInX).with(scaleInY).after(scaleOutX);
        set.setInterpolator(new OvershootInterpolator(2));

        menu.setIconToggleAnimatorSet(set);
    }

    protected void updateMyMarker(LatLng latLng) {
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }
        int avatarId = getResources().getIdentifier(myAvatar, "drawable", getPackageName());
        //Place current location marker
        BitmapDescriptor meIcon = BitmapDescriptorFactory.fromResource(avatarId);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title(myTitle);
        markerOptions.icon(meIcon);
        mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);
    }

    protected void updateMyTargetMarker(LatLng latLng) {
        if (mTargetMarker != null) {
            mTargetMarker.remove();
        }
        int avatarId = getResources().getIdentifier(myTarget, "drawable", getPackageName());
        //Place current location marker
        BitmapDescriptor meIcon = BitmapDescriptorFactory.fromResource(avatarId);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("target");
        markerOptions.icon(meIcon);
        mTargetMarker = mGoogleMap.addMarker(markerOptions);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFrag.getMapAsync(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            userId = extras.getString("USER_ID");
        }

        menu = (FloatingActionMenu) findViewById(R.id.menu);
        menu.setClosedOnTouchOutside(true);
        createCustomAnimation();

        menu3 = (FloatingActionMenu) findViewById(R.id.menu3);
        menu3.setClosedOnTouchOutside(true);

        offerGoodsBtn = (FloatingActionButton) findViewById(R.id.button1);
        final Context context = this;
        offerGoodsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                offerGoodsBtn.setLabelColors(ContextCompat.getColor(context, R.color.primary),
//                        ContextCompat.getColor(context, R.color.warning_color),
//                        ContextCompat.getColor(context, R.color.transparent));
//                offerGoodsBtn.setLabelTextColor(ContextCompat.getColor(context, R.color.black));
                Intent intent = new Intent(getApplicationContext(), OfferGoodActivity.class);
                intent.putExtra("LAT_CURR", latCurr + "");
                intent.putExtra("LNG_CURR", lngCurr + "");
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });

        offerServicesBtn = (FloatingActionButton) findViewById(R.id.button2);
        offerServicesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), OfferServiceActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });

        searchMoneyBtn = (FloatingActionButton) findViewById(R.id.button12);
        searchMoneyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menu3.getMenuIconView().setImageResource(R.drawable.goblin);
                menu3.close(true);
                type = "uang";
                if (obj != null) {
                    try {
                        obj.put("type", type);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                mSearchView.setVisibility(View.INVISIBLE);
                pinjamBtn.setVisibility(View.VISIBLE);
            }
        });

        searchServicesBtn = (FloatingActionButton) findViewById(R.id.button22);
        searchServicesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menu3.getMenuIconView().setImageResource(R.drawable.robot);
                menu3.close(true);
                mSearchView.setVisibility(View.VISIBLE);
                pinjamBtn.setVisibility(View.INVISIBLE);
                type = "jasa";
                obj = new JSONObject();
                try {
                    obj.put("type", type);
                    obj.put("lat", latCurr + "");
                    obj.put("lng", lngCurr + "");
                    obj.put("radius", "1");
                    obj.put("searchKey", searchKey);
                    new SearchTask().execute(obj);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        searchGoodsBtn = (FloatingActionButton) findViewById(R.id.button32);
        searchGoodsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menu3.getMenuIconView().setImageResource(R.drawable.witch);
                menu3.close(true);
                mSearchView.setVisibility(View.VISIBLE);
                pinjamBtn.setVisibility(View.INVISIBLE);
                type = "barang";
                obj = new JSONObject();
                try {
                    obj.put("type", type);
                    obj.put("lat", latCurr + "");
                    obj.put("lng", lngCurr + "");
                    obj.put("radius", "1");
                    obj.put("searchKey", searchKey);
                    new SearchTask().execute(obj);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        pinjamBtn = (FloatingActionButton) findViewById(R.id.pinjam);
        pinjamBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), PinjamActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });

        mSearchView = (FloatingSearchView) findViewById(R.id.floating_search_view);
        mSearchView.setOnQueryChangeListener(new FloatingSearchView.OnQueryChangeListener() {
            @Override
            public void onSearchTextChanged(String oldQuery, final String newQuery) {
                searchKey = newQuery;
                if (newQuery.length() >= 3) {
                    obj = new JSONObject();
                    try {
                        obj.put("type", type);
                        obj.put("searchKey", newQuery);
                        obj.put("lat", latCurr + "");
                        obj.put("lng", lngCurr + "");
                        obj.put("radius", "1");

                        new SearchTask().execute(obj);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        });

        final Handler h = new Handler();
        h.postDelayed(new Runnable() {
            private long time = 0;

            @Override
            public void run() {
                // do stuff then
                if (obj != null) {
                    new SearchTask().execute(obj);
                }
                // can call h again after work!
                time += 1000;
                Log.d("TimerExample", "Going for... " + time);
                h.postDelayed(this, 1000);
            }
        }, 1000); // 1 second delay (takes millis)

        compassBtn = (FloatingActionButton) findViewById(R.id.compass);
        compassBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                updateMyMarker(latLng);

                //move map camera
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                isTracked = true;
                compassBtn.setEnabled(false);
                if (mTargetMarker != null) {
                    mTargetMarker.remove();
                }
            }
        });

        // initial isTracked is true
        if (isTracked) compassBtn.setEnabled(false);
        JSONObject obj2 = new JSONObject();
        try {
            obj2.put("userId", userId);
            new GetProfileTask().execute(obj2);
        } catch (Exception e) {

        }

        profileBtn = (FloatingActionButton) findViewById(R.id.profile);
        profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog = new ProgressDialog(MapsActivity.this,
                        R.style.AppTheme_Dark_Dialog);
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage("Please wait...");
                progressDialog.show();
                try {
                    JSONObject obj3 = new JSONObject();
                    obj3.put("userId", userId);
                    new ProfileTask().execute(obj3);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();

        //stop location updates when Activity is no longer active
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mGoogleMap.setOnCameraMoveStartedListener(this);
        mGoogleMap.setOnCameraMoveListener(this);

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                buildGoogleApiClient();
                mGoogleMap.setMyLocationEnabled(false);
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latInitial, lngInitial), zoomInitial));
            } else {
                //Request Location Permission
                checkLocationPermission();
            }
        } else {
            buildGoogleApiClient();
            mGoogleMap.setMyLocationEnabled(false);
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latInitial, lngInitial), zoomInitial));
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        updateMyMarker(latLng);
        if (isTracked) {
            //move map camera on when tracked
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

        }
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MapsActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mGoogleMap.setMyLocationEnabled(false);
                        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latInitial, lngInitial), zoomInitial));
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public void onCameraMove() {
        //isTracked = false;
        LatLng target = mGoogleMap.getCameraPosition().target;
        latCurr = target.latitude;
        lngCurr = target.longitude;
        if (!isTracked) {
            updateMyTargetMarker(target);
        }
        //compassBtn.setEnabled(true);
    }

    @Override
    public void onCameraMoveStarted(int reason) {
        if (reason == REASON_GESTURE) {
            isTracked = false;
            compassBtn.setEnabled(true);
        }
    }
}
