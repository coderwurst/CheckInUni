package com.coderwurst.student_attendance;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * ***********************
 * Created by IntelliJ IDEA
 * User: andrew
 * Date: 25/06/2014
 * Time: 10:17
 * Version: V2.0
 * SPRINT 4 - SPLASH SCREEN ON STARTUP
 * ************************
 */
public class Splash extends Activity

{
    // private final int SPLASH_DISPLAY_LENGTH = 5000;     // int to determine the length of time the splash screen appears

    WifiManager wifi;                           // wifi manager

    // called when the activity is first created
    @Override
    public void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);

        // show splash screen
        setContentView(R.layout.splash);                // opens up the splash XML file
        Log.d("splash", "starting");

        // begin checking network information
        new LoadData().execute();

 } // onCreate


    class LoadData extends AsyncTask<Void, Void, Integer>{

        @Override
         protected Integer doInBackground(Void... params) {

            // check to see if wifi is enabled, and if not, activate
            wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);

            // causes APP crash - fix
            if (wifi.isWifiEnabled() == false)
            {

                wifi.setWifiEnabled(true);
                Log.d("wifi", "wifi activated");

            } // if


            long timeIn = System.nanoTime();
            int state;

            while (wifi.isWifiEnabled() == false)
            {
                state = wifi.getWifiState();
                Log.d("wifi", "current state: " + state);
                // wait
            } // while

            state = wifi.getWifiState();
            Log.d("wifi", "final state: " + state);


            // time stamp
            long timeOut = System.nanoTime();

            long duration = timeOut - timeIn;

            double seconds = (double)duration / 1000000000.0;

            Log.d("wifi", "time taken: " + seconds);
            // at this point wifi is activated

            return 1;
        } // doInBackground

        protected void onPostExecute(Integer result)
        {
            Intent mainIntent = new Intent(Splash.this,MainScreenActivity.class);   // creates an Intent
            Splash.this.startActivity(mainIntent);                                  // runs new activity
            Splash.this.finish();                                                   // closes splash activity
            Log.d("splash", "leaving");
        } // onPostExecute

    } // LoadData

} // Splash


/*


        // handler to start the MainScreenActivity and close this Splash-Screen after specified time (1000 sec)

        new Handler().postDelayed(new Runnable(){
            @Override
            public void run()
            {

                checkWifi();

                Intent mainIntent = new Intent(Splash.this,MainScreenActivity.class);   // creates an Intent
                Splash.this.startActivity(mainIntent);                                  // runs new activity
                Splash.this.finish();                                                   // closes splash activity
                Log.d("splash", "leaving");

            }
        }, SPLASH_DISPLAY_LENGTH);





        private void checkWifi ()
    {

        // check to see if wifi is enabled, and if not, activate
        wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        // causes APP crash - fix
        if (wifi.isWifiEnabled() == false)
        {
            // Toast wifiToast = Toast.makeText(getApplicationContext(),
            //        "wifi is currently disabled...activating", Toast.LENGTH_LONG);
            // wifiToast.show();
            wifi.setWifiEnabled(true);
            Log.d("wifi", "wifi activated");

        } // if


        long timeIn = System.nanoTime();
        int state = -1;

        while (wifi.isWifiEnabled() == false)
        {
            state = wifi.getWifiState();
            Log.d("wifi", "current state: " + state);
            // wait
        } // while

        state = wifi.getWifiState();
        Log.d("wifi", "final state: " + state);


        // time stamp
        long timeOut = System.nanoTime();

        long duration = timeOut - timeIn;

        double seconds = (double)duration / 1000000000.0;

        Log.d("wifi", "time taken: " + seconds);

    } // check wifi




Intent mainIntent = new Intent(Splash.this,MainScreenActivity.class);   // creates an Intent
        Splash.this.startActivity(mainIntent);                                  // runs new activity
        Splash.this.finish();                                                   // closes splash activity
        Log.d("splash", "leaving");




 */