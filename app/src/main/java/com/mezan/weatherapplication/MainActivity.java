package com.mezan.weatherapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import java.sql.Timestamp;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity {

    public  static final int RequestPermissionCode  = 1 ;
    ConstraintLayout mainRoot;

    Context context;
    Intent intent1 ;

    LocationManager locationManager ;
    boolean GpsStatus = false ;
    Criteria criteria ;
    String Holder;
    FusedLocationProviderClient fusedLocationClient;

    TextView tempView,pressureView,windView,weatherView,cityView,LatView,LonView;
    EditText editCity;
    Button btnSearch;


    final String APIKEY = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainRoot = findViewById(R.id.mainroot);

        tempView = findViewById(R.id.temp);
        pressureView = findViewById(R.id.pressure);
        windView = findViewById(R.id.wind);
        cityView = findViewById(R.id.city);
        LatView = findViewById(R.id.sunrise);
        LonView = findViewById(R.id.sunset);
        weatherView = findViewById(R.id.weather);

        editCity = findViewById(R.id.editCity);
        btnSearch = findViewById(R.id.btnSearch);

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    //https://api.openweathermap.org/data/2.5/weather?q=Rangpur&appid=123da5ffd72f20432e253545b522f074
                    String cityname = editCity.getText().toString();
                    if(isInternetConnection()) {
                        new MyTask().execute("https://api.openweathermap.org/data/2.5/weather?q=" + cityname + "&appid=" + APIKEY).get();
                    }else {
                        Snackbar snackbar = Snackbar
                                .make(mainRoot, "No Internet Connection!", Snackbar.LENGTH_LONG);
                        snackbar.show();
                    }
                }catch (Exception e){
                    Snackbar snackbar = Snackbar
                            .make(mainRoot, "Input City", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            }
        });


        EnableRuntimePermission();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        criteria = new Criteria();

        Holder = locationManager.getBestProvider(criteria, false);

        context = getApplicationContext();

        CheckGpsStatus();

    }


    public void CheckGpsStatus(){

        locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);

        GpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

    }

    public void EnableRuntimePermission(){
        Toast.makeText(MainActivity.this,"For Automatic Weather, We Need to Access GPS", Toast.LENGTH_LONG).show();
        ActivityCompat.requestPermissions(MainActivity.this,new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION}, RequestPermissionCode);
    }

    @Override
    public void onRequestPermissionsResult(int RC, String per[], int[] PResult) {

        switch (RC) {

            case RequestPermissionCode:

                if (PResult.length > 0 && PResult[0] == PackageManager.PERMISSION_GRANTED) {

                    FindLatLon();
                    Toast.makeText(MainActivity.this,"GPS Permission Granted", Toast.LENGTH_LONG).show();


                } else {

                    EnableRuntimePermission();
                    Toast.makeText(MainActivity.this,"GPS Permission Rejected", Toast.LENGTH_LONG).show();

                }
                break;
        }
    }

    private void FindLatLon(){
        CheckGpsStatus();


        if(GpsStatus) {
            if (Holder != null) {
                if (ActivityCompat.checkSelfPermission(
                        MainActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                       ) {
                    EnableRuntimePermission();
                }

                /*This line is main work in location and must include gradle of google play services and also coarse location and internet connection must be enable*/

                fusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location loc) {
                                if(loc != null){
                                    Log.d("GPS Lat", String.valueOf(loc.getLatitude()));
                                    Log.d("GPS Lon",String.valueOf(loc.getLongitude()));
                                    String  lat = String.valueOf(loc.getLatitude());
                                    String lon = String.valueOf(loc.getLongitude());

                                    //Do What you want?
                                    if(isInternetConnection()){
                                        try {
                                            new MyTask().execute("http://api.openweathermap.org/data/2.5/weather?lat="+lat+"&lon="+lon+"&appid="+APIKEY).get();
                                        } catch (ExecutionException e) {
                                            Snackbar snackbar = Snackbar
                                                    .make(mainRoot, "Something Went Wrong!", Snackbar.LENGTH_LONG);
                                            snackbar.show();
                                            e.printStackTrace();
                                        } catch (InterruptedException e) {
                                            Snackbar snackbar = Snackbar
                                                    .make(mainRoot, "Something Went Wrong!", Snackbar.LENGTH_LONG);
                                            snackbar.show();
                                            e.printStackTrace();
                                        }
                                    }else {
                                        Snackbar snackbar = Snackbar
                                                .make(mainRoot, "No Internet Connection!", Snackbar.LENGTH_LONG);
                                        snackbar.show();

                                    }



                                }else {
                                    Snackbar snackbar = Snackbar
                                            .make(mainRoot, "Try Again Later!", Snackbar.LENGTH_LONG);
                                    snackbar.show();

                                    Log.d("GPS Location","Location not found!");


                                }
                            }
                        });
                /*Location(Latitude,Longitude)*/
            }
        }else {

            intent1 = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent1);


            finish();

        }
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mainmenu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

       if(item.getItemId() == R.id.gps){

           intent1 = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
           startActivity(intent1);

           return true;

       }
       return false;
    }

    private class MyTask extends AsyncTask<String,String,String> {


        @Override
        protected void onPostExecute(String result) {
            // Res.setText(result);
            ParseJSON(result);
            Log.d("Weather JSON",result);
            Snackbar snackbar = Snackbar
                    .make(mainRoot, "Data Reading Completed!", Snackbar.LENGTH_LONG);
            snackbar.show();

        }

        @Override
        protected String doInBackground(String... strings) {

            StringBuilder result=new StringBuilder();
            HttpURLConnection urlConnection=null;
            try{
                URL url=new URL(strings[0]);
                urlConnection=(HttpURLConnection)url.openConnection();
                InputStream in=new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader reader=new BufferedReader(new InputStreamReader(in));
                String line;
                while((line=reader.readLine())!=null){

                    result.append(line);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                urlConnection.disconnect();
            }

            return result.toString();
        }
    }
    public boolean isInternetConnection() {

        ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);


        if (conMgr.getActiveNetworkInfo() != null
                && conMgr.getActiveNetworkInfo().isAvailable()
                && conMgr.getActiveNetworkInfo().isConnected()) {

            return true;

        } else {
            return false;
        }


    }


    private void ParseJSON(String json){
        try {
            JSONObject object = new JSONObject(json);
            JSONObject cordObj = object.getJSONObject("coord");
            String lat = cordObj.getString("lat");
            String lon = cordObj.getString("lon");

            JSONArray weatherArr = object.getJSONArray("weather");
            JSONObject weatherObj = weatherArr.getJSONObject(0);
            String WeatherInfo = weatherObj.getString("description");

            // Clouds Rain Clear


            String mainWeather = weatherObj.getString("main");
            if(mainWeather.equals("Clouds")){
                mainRoot.setBackgroundResource(R.drawable.cloudy);
            }
            if (mainWeather.equals("Rain")){
                mainRoot.setBackgroundResource(R.drawable.rain);
            }
            if (mainWeather.equals("Clear")){
                mainRoot.setBackgroundResource(R.drawable.clear);
            }

            JSONObject mainObj = object.getJSONObject("main");
            double temp = mainObj.getDouble("temp");
            String tempCelcious = KelvinToCelcius(temp);

            double pressure = mainObj.getDouble("pressure");
            String atm = PascalToAtm(pressure);

            JSONObject windObj = object.getJSONObject("wind");
            double windspeed = windObj.getDouble("speed");
            String windKMH = WindSpeedKmH(windspeed);



            String CityName = object.getString("name");
            JSONObject sysObj = object.getJSONObject("sys");
            String country = sysObj.getString("country");

            String location = Normalization(CityName,country);

            Log.d("ParseJSON - Weather",WeatherInfo);
            Log.d("ParseJSON - temp","Temp:"+temp);
            Log.d("ParseJSON - pressure","pressure:"+pressure);
            Log.d("ParseJSON - winspeed","winspeed:"+windspeed);
            Log.d("ParseJSON - lat","lat:"+lat);
            Log.d("ParseJSON - lon","lon:"+lon);
            Log.d("ParseJSON - name","Name:"+CityName);
            tempView.setText(tempCelcious);
            pressureView.setText(atm);
            windView.setText(windKMH);
            weatherView.setText(WeatherInfo);
            cityView.setText(location);
            LatView.setText(lat);
            LonView.setText(lon);



        } catch (JSONException e) {
            Snackbar snackbar = Snackbar
                    .make(mainRoot, "Something Went Wrong!", Snackbar.LENGTH_LONG);
            snackbar.show();
            e.printStackTrace();
        }
    }
    private String KelvinToCelcius(double kelvin){
        double x = round(kelvin);
        return x +" K";
    }
    private String WindSpeedKmH(double speed){
        return speed+" Km/h";
    }
    private String PascalToAtm(double pascal){
        double x = round(pascal/1000);
        return (x)+" atm";
    }
    private String Normalization(String city,String country){
        return "Your Current Location : "+city+","+country;
    }
    private double round(double val){
        double rounded = (double) Math.round(val * 100) / 100;
        return rounded;
    }

}
