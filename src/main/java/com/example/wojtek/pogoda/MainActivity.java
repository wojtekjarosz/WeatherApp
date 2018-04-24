package com.example.wojtek.pogoda;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity {
    private ArrayList permissionsToRequest;
    private ArrayList permissionsRejected = new ArrayList();
    private ArrayList permissions = new ArrayList();

    private final static int ALL_PERMISSIONS_RESULT = 101;
    LocationTrack locationTrack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        permissions.add(ACCESS_FINE_LOCATION);
        permissions.add(ACCESS_COARSE_LOCATION);

        permissionsToRequest = findUnAskedPermissions(permissions);
        //get the permissions we have asked for before but are not granted..
        //we will store this in a global list to access later.


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {


            if (permissionsToRequest.size() > 0)
                requestPermissions((String[]) permissionsToRequest.toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
        }


        Button btn = (Button) findViewById(R.id.btn);


        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                locationTrack = new LocationTrack(MainActivity.this);


                if (locationTrack.canGetLocation()) {


                    double longitude = locationTrack.getLongitude();
                    double latitude = locationTrack.getLatitude();

                    Toast.makeText(getApplicationContext(), "Longitude:" + Double.toString(longitude) + "\nLatitude:" + Double.toString(latitude), Toast.LENGTH_SHORT).show();
                } else {

                    locationTrack.showSettingsAlert();
                }

            }
        });

    }

    public void aktualizujButtonOnClick(View v) {
        locationTrack = new LocationTrack(MainActivity.this);


        if (locationTrack.canGetLocation()) {


            double longitude = locationTrack.getLongitude();
            double latitude = locationTrack.getLatitude();

            Toast.makeText(getApplicationContext(), "Longitude:" + Double.toString(longitude) + "\nLatitude:" + Double.toString(latitude), Toast.LENGTH_SHORT).show();

        Map<String, String> jsonParams = new HashMap<>();
        jsonParams.put("key1","value1");
        jsonParams.put("key2","value2");
        JSONObject postData = new JSONObject(jsonParams);
        RequestQueue queue = Volley.newRequestQueue(this);
        String longitudeString = String.valueOf(longitude);
        String latitudeString = String.valueOf(latitude);
        //String longitudeString = "-77.03";
        //String latitudeString = "-12.04";
        String url = "http://api.openweathermap.org/data/2.5/weather?lat="+latitudeString+ "&lon="+longitudeString+"&appid=55250fd8edab9a589dab362cd5ee461e&units=metric";

        //String url = "http://api.openweathermap.org/data/2.5/weather?q=Krakow,pl&appid=55250fd8edab9a589dab362cd5ee461e&units=metric";
        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, url, postData,
                new Response.Listener <JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //response zawiera odpowiedź w postaci obiektu JSON
                        TextView nameView = (TextView) findViewById(R.id.nameView);
                        TextView tempView = (TextView) findViewById(R.id.tempView);
                        TextView pressureView = (TextView) findViewById(R.id.pressureView);
                        TextView windSpeedView = (TextView) findViewById(R.id.windSpeedView);
                        TextView coordView = (TextView) findViewById(R.id.coordView);
                        TextView helloView = (TextView) findViewById(R.id.hello);

                        try {
                            //city & country
                            String city = response.getString("name");
                            JSONObject sysObj = response.getJSONObject("sys");
                            //String country = sysObj.getString("country");
                            nameView.setText(city );//+ ", "+ country);
                            //temp
                            JSONObject mainObj = response.getJSONObject("main");
                            String temp = mainObj.getString("temp");
                            tempView.setText("Temperatura: " + temp + " °C");
                            //pressure
                            String pressure = mainObj.getString("pressure");
                            pressureView.setText("Ciśnienie: " + pressure + " hPa" );
                            //wind speed
                            JSONObject windObj = response.getJSONObject("wind");
                            String windspeed = windObj.getString("speed");
                            windSpeedView.setText("Prędkość wiatru: " + windspeed + " mps");

                            //longitude &latitude
                            JSONObject coordObj = response.getJSONObject("coord");
                            String lon = coordObj.getString("lon");
                            String lat = coordObj.getString("lat");
                            String lonSymbol="", latSymbol="";
                            if(Float.parseFloat(lon)>0)  lonSymbol = " E";
                            if(Float.parseFloat(lon)<0)  lonSymbol = " W";
                            if(Float.parseFloat(lat)>0)  latSymbol = " N";
                            if(Float.parseFloat(lat)<0)  latSymbol = " S";
                            coordView.setText("Położenie: " + lon + lonSymbol+", "+lat + latSymbol);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Błąd!
                    }
                }) {
        };
        queue.add(postRequest);
        } else {

            locationTrack.showSettingsAlert();
        }
    }

    private ArrayList findUnAskedPermissions(ArrayList wanted) {
        ArrayList result = new ArrayList();

        for (Object perm : wanted) {
            if (!hasPermission((String)perm)) {
                result.add(perm);
            }
        }

        return result;
    }

    private boolean hasPermission(String permission) {
        if (canMakeSmores()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
            }
        }
        return true;
    }

    private boolean canMakeSmores() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }


    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {

            case ALL_PERMISSIONS_RESULT:
                for (Object perms : permissionsToRequest) {
                    if (!hasPermission((String)perms)) {
                        permissionsRejected.add(perms);
                    }
                }

                if (permissionsRejected.size() > 0) {


                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale((String)permissionsRejected.get(0))) {
                            showMessageOKCancel("These permissions are mandatory for the application. Please allow access.",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermissions((String[]) permissionsRejected.toArray(new String[permissionsRejected.size()]), ALL_PERMISSIONS_RESULT);
                                            }
                                        }
                                    });
                            return;
                        }
                    }

                }

                break;
        }

    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationTrack.stopListener();
    }







}
