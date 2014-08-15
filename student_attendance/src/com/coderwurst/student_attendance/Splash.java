package com.coderwurst.student_attendance;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

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
    WifiManager wifi;                                   // wifi manager

    // called when the activity is first created
    @Override
    public void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);

        // show splash screen
        setContentView(R.layout.splash);                // opens up the splash XML file
        Log.d("splash", "starting");

        // begin checking network information
        new CheckWifi().execute();

 } // onCreate


    class CheckWifi extends AsyncTask<Void, Void, Integer>{

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
