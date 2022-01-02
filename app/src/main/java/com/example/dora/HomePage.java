package com.example.dora;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HomePage extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    BottomNavigationView bottomNavigationView;
    GoogleMap googleMap;
    MarkerOptions origin, destination;
    int i = 0;
    private static final String GOOGLE_API_KEY = "AIzaSyCHhKekfTMkG1yYlnOjSV6o9RHO8s2z-UE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.home);

    }

    HomeFragment homeFragment = new HomeFragment();
    NewTripFragment newTripFragment = new NewTripFragment();
    ObjectsFragment objectsFragment = new ObjectsFragment();
    ProfileFragment profileFragment = new ProfileFragment();

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.home:
                getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, homeFragment).commit();
                return true;

            case R.id.newtrip:
                SupportMapFragment mapFragment = SupportMapFragment.newInstance();
                getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.flFragment, mapFragment)
                        .commit();
                mapFragment.getMapAsync(this);
                return true;

            case R.id.objects:
                getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, objectsFragment).commit();
                return true;

            case R.id.profile:
                getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, profileFragment).commit();
                return true;
        }
        return false;
    }

    @Override
    public void onMapReady(GoogleMap gMap) {
        gMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                googleMap = gMap;
                if (i == 0) {
                    googleMap.clear();
                    origin = new MarkerOptions().position(point);
                    googleMap.addMarker(origin);
                    i++;
                } else if (i == 1) {
                    destination = new MarkerOptions().position(point);
                    googleMap.addMarker(destination);
                    String url = getDirectionsUrl(origin.getPosition(), destination.getPosition());
                    DownloadTask downloadTask = new DownloadTask();
                    downloadTask.execute(url);
                    Location startPoint=new Location("locationA");
                    startPoint.setLatitude(origin.getPosition().latitude);
                    startPoint.setLongitude(origin.getPosition().longitude);

                    Location endPoint=new Location("locationB");
                    endPoint.setLatitude(destination.getPosition().latitude);
                    endPoint.setLongitude(destination.getPosition().longitude);

                    double distance=startPoint.distanceTo(endPoint);
                    String type = "";
                    StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
                    googlePlacesUrl.append("location=" + (origin.getPosition().latitude+destination.getPosition().latitude)/2 + "," + (origin.getPosition().longitude+destination.getPosition().longitude)/2);
                    googlePlacesUrl.append("&radius=" + distance/2);
                    googlePlacesUrl.append("&types=" + type);
                    googlePlacesUrl.append("&sensor=true");
                    googlePlacesUrl.append("&key=" + GOOGLE_API_KEY);

                    GooglePlacesReadTask googlePlacesReadTask = new GooglePlacesReadTask();
                    Object[] toPass = new Object[2];
                    toPass[0] = googleMap;
                    toPass[1] = googlePlacesUrl.toString();
                    googlePlacesReadTask.execute(toPass);

                    i = 0;
                }
            }
        });


    }

    private class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);
        }
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        String mode = "mode=driving";
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + "AIzaSyCHhKekfTMkG1yYlnOjSV6o9RHO8s2z-UE";
        return url;
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DataParser parser = new DataParser();

                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList points = new ArrayList();
            PolylineOptions lineOptions = new PolylineOptions();
            for (int i = 0; i < result.size(); i++) {

                List<HashMap<String, String>> path = result.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                lineOptions.addAll(points);
                lineOptions.width(12);
                lineOptions.color(Color.RED);
                lineOptions.geodesic(true);

            }
            if (points.size() != 0)
                googleMap.addPolyline(lineOptions);
        }
    }
}
