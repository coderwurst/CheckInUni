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
    private WifiManager wifi;                                   // wifi manager object

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


    /**
     * Inner class to perform a check to determine if a Wifi transmitter has been deactivated, and if
     * so it is turned on again before loading up the student or lecturer Ui screen. In both of these
     * activities wifi is needed to establish if a student is on campus, or in the case of the
     * lecturer if the device is able to connect to the server. As this processing can sometimes take
     * a long amount of time (up to 9 seconds) it was decided to complete the task within the splash
     * activity to create a better user experience once the app has been loaded
     */

    class CheckWifi extends AsyncTask<Void, Void, Integer>
    {

        /**
         * This doInBackground method completes the work involved in contacting the
         * server to register the student and module details input by lecturer
         **/

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


            /**
             * calculations made to determine the average time taken to activate the Wifi transmitter
             * during development, including outputting the state of the adapter to allow the
             * developer to determine when the next process can be called
             */

            long timeIn = System.nanoTime();
            int state;

            while (wifi.isWifiEnabled() == false)       // turns wifi on if currently off
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

            // at this point wifi is activated
            Log.d("wifi", "time taken: " + seconds);

            return 1;
        } // doInBackground


        /**
         * upon completion the splash activity is ended and the user is taken to the main
         * activity screen
         */

        protected void onPostExecute(Integer result)
        {
            Intent mainIntent = new Intent(Splash.this,MainScreenActivity.class);   // creates an Intent
            Splash.this.startActivity(mainIntent);                                  // runs new activity
            Splash.this.finish();                                                   // closes splash activity
            Log.d("splash", "leaving");
        } // onPostExecute
    } // LoadData

} // Splash
