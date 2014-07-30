package com.coderwurst.student_attendance;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * ***********************
 * Created by IntelliJ IDEA
 * User: andrew
 * Date: 14/07/2014
 * Time: 08:36
 * Version: V7.0
 * SPRINT 6 - USER INTERFACE FOR STAFF MEMBERS TO OFFER AUTO, MANUAL SIGN-IN FUNCTIONS & QR-CODE RETRIEVAL
 * ************************
 */
public class LecturerUI extends Activity implements View.OnClickListener

{
    // buttons to provide lecturer functions
    private Button btnManSignin;
    private Button btnAutoSignin;
    private Button btnGetQR;
    private Button btnReset;

    // retrieves shared preferences to be changed
    public static final String USER_ID = "User ID File";

    private TextView formatTxt, contentTxt;     // text view to inform tester of data captured at this stage
    private int scanID = 0;                     // int to store type of scan

    // components for checking internet connection
    WifiManager wifi;                           // wifi manager
    private String url_test_connection = "http://172.17.10.237/xampp/student_attendance/test_connection.php";     // can be changed to server address
    protected static boolean serverAvailable;          // boolean to be used in addStudentManually and RecursiveSignIn to determine if internet connection is available

    // JSON Node names
    private static final String TAG_SUCCESS = "success";

    // JSON parser class
    JSONParser jsonParser = new JSONParser();

    private String serverResponse = "";

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lecturer_ui);                           // opens up corresponding XML file

        // Buttons
        btnManSignin = (Button) findViewById(R.id.lec_man_signin);          // to enter student & class details manually
        btnAutoSignin = (Button) findViewById(R.id.lec_auto_signin);        // to scan student ID & class QR-Code
        btnGetQR = (Button) findViewById(R.id.getQRCode);                   // to retrieve a particular QR-Code

        btnReset = (Button) findViewById(R.id.reset_user);                  // testing purpose button to reset user

        // TextViews for hold format and content info for testing purposes
        formatTxt = (TextView) findViewById(R.id.scan_format);
        contentTxt = (TextView) findViewById(R.id.scan_content);

        // set onClick listeners for all 3 buttons
        btnManSignin.setOnClickListener(this);
        btnAutoSignin.setOnClickListener(this);
        btnGetQR.setOnClickListener(this);

        btnReset.setOnClickListener(this);

        checkWifi();

    } // onCreate


    public void checkWifi()
    {
        // check to see if wifi is enabled, and if not, activate
        wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        if (wifi.isWifiEnabled() == false)
        {
            Toast wifiToast = Toast.makeText(getApplicationContext(),
                    "activating wifi on device", Toast.LENGTH_LONG);
            wifiToast.show();
            wifi.setWifiEnabled(true);
            Log.d("lecturer ui", "wifi activated");
        } // if

        if(wifi.isWifiEnabled())
        {
            // check to see if connection to University Server is available
            new TestConnection().execute();
        }

    } // checkWifi


    @Override
    public void onClick (View view)
    {
        if (serverAvailable)
        {
            if (view.getId() == R.id.lec_man_signin)
            {
                // logcat tag to view app progress
                Log.d("lecturer ui", "manual check in");

                // opens up manual sign in activity with text input fields
                Intent openManSignin = new Intent(getApplicationContext(), AddStudentMan.class);
                startActivity(openManSignin);

            } else if (view.getId() == R.id.lec_auto_signin)
            {

                // logcat tag to view app progress
                Log.d("lecturer ui", "auto check in");

                // opens up recursive sign in activity
                Intent openAutoSignin = new Intent(getApplicationContext(), RecursiveSignIn.class);
                startActivity(openAutoSignin);

            } else if (view.getId() == R.id.getQRCode)
            {

                // logcat tag to view app progress
                Log.d("lecturer ui", "retrieve QR");

                // code to retrieve QR-Image from database
                Intent openViewAllModules = new Intent(getApplicationContext(), ViewAllModules.class);
                startActivity(openViewAllModules);

            } else
            {
                // logcat tag to view app progress
                Log.d("lecturer ui", "reset user");

                // temp test code to reset user ID
                IntentIntegrator scanIntegrator = new IntentIntegrator(this);
                scanIntegrator.initiateScan();
                scanID = 1;
                // to be replaced with code to open up recall QR-Code activity

            }// if - else - else
        // connection with database currently not available
        } else
        {
            if (view.getId() == R.id.lec_man_signin)
            {
                Toast toast = Toast.makeText(getApplicationContext(),
                        "offline mode, data will be stored on device, please upload ASAP", Toast.LENGTH_LONG);
                toast.show();

                // logcat tag to view app progress
                Log.e("lecturer ui", "manual check in - server not available");

                // opens up manual sign in activity with text input fields
                Intent openManSignin = new Intent(getApplicationContext(), AddStudentMan.class);
                startActivity(openManSignin);

            } else if (view.getId() == R.id.lec_auto_signin)
            {

                Toast toast = Toast.makeText(getApplicationContext(),
                        "offline mode, data will be stored on device, please upload ASAP", Toast.LENGTH_LONG);
                toast.show();

                // logcat tag to view app progress
                Log.e("lecturer ui", "auto check in - server not available");

                // opens up recursive sign in activity
                Intent openAutoSignin = new Intent(getApplicationContext(), RecursiveSignIn.class);
                startActivity(openAutoSignin);

            } else if (view.getId() == R.id.getQRCode)
            {

                Toast toast = Toast.makeText(getApplicationContext(),
                        "QR-Codes cannot be retrieved at this time", Toast.LENGTH_LONG);
                toast.show();

                // logcat tag to view app progress
                Log.e("lecturer ui", "retrieve QR - server not available");

            } // if - else to determine user choice

        } // if - else to determine operation if internet is or isn't available

    }// onClick


    public void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        // takes the scanned in data & prepares for use within this method
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);

        if (scanningResult != null)             // as long as something has been scanned
        {
            // toast for unit testing to show tester scan contents
            String scanContent = scanningResult.getContents();
            String scanFormat = scanningResult.getFormatName();
            formatTxt.setText("FORMAT: " + scanFormat);
            contentTxt.setText("CONTENT: " + scanContent);

            /* Toast toast = Toast.makeText(getApplicationContext(),
                    "FORMAT: " + scanFormat + "\nCONTENT: " + scanContent, Toast.LENGTH_LONG);
            toast.show(); */

            Log.d("lecturer ui", "user wishes to register as another user");

                    // allows the user to re-register

                    // launching Registration Activity
                    Intent i = new Intent(getApplicationContext(), InitialReg.class);


                    // takes the scanned info and packs it into a bundle before sending it to the Registration class
                    String scannedInfo = scanContent;
                    i.putExtra("Info", scannedInfo);
                    startActivity(i);

                    // closing this screen
                    finish();

        } else {

            Log.e("lecturer ui", "scan failed");

            Toast toast = Toast.makeText(getApplicationContext(),
                    "no scan data received!", Toast.LENGTH_SHORT);
            toast.show();
        }// if-else to confirm scan data has been received

    }// onActivityResult

    /**
     * Background Async Task to establish if a connection to the server is available
     * */

     class TestConnection extends AsyncTask<String, String, String>
    {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        } // onPreExecute

        /**
         * registering the student as present
         * */
        protected String doInBackground(String... args)
        {

            // parameters to be passed into PHP script on server side
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            // no parameters to be added

            // getting JSON Object
            // NB url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(url_test_connection,
                    "POST", params);

            // check log cat for response
            // check log cat for response
            Log.d("lecturer ui", "database response; " + json.toString());

            // check for success tag
            try {
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {

                    serverResponse = "connection available";
                    serverAvailable = true;
                    // details have been stored and the student is checked in
                    Log.d("lecturer ui", "connection established");

                } else {

                    serverResponse = "connection not available, offline mode activated";
                    serverAvailable = false;
                    // failed to sign-in, PHP has returned an error
                    Log.e("lecturer ui", "connection with server cannot be established");

                } // if - else
            } catch (JSONException e) {
                e.printStackTrace();
            } // try - catch
            return null;
        }// doInBackground


        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {

            Toast toast = Toast.makeText(getApplicationContext(),
                    serverResponse, Toast.LENGTH_LONG);
            toast.show();

        } // onPostExecute
    } // SignInStudent



    /* this if to come after determined if internet connection is established
    returns the stored information for previously unsuccessful checkins
            userDetails = getSharedPreferences(USER_ID, 0);
            String prevSavedCheckIn = userDetails.getString("prev_checkin", null);

            if (prevSavedCheckIn != null)
            {
                Log.d("student ui", "previous checkin info found: " + prevSavedCheckIn);

                sendPrevCheckinDetails(prevSavedCheckIn);           // takes the previously saved info, passes into method

            } // if */



    // method called when student wifi is not available, to be sent to database upon next successful sign in

    /*
    private void storeCheckIn ()     // scanned details need to be stored and sent to database later
    {
        // Toast contents used to follow data flow through app, confirm input
        String scanContent = scanningResult.getContents();
        String scanFormat = scanningResult.getFormatName();
        // formatTxt.setText("FORMAT: " + scanFormat);
        // contentTxt.setText("CONTENT: " + scanContent);

        Log.d("student ui", "scan content: " + scanContent);

        if (scanID == 2 && scanFormat.equals("QR_CODE"))            // "QR_CODE" is only valid QR-Code format
        {

            Log.d("student ui", "device to store data, to be sent to database later");

            // system time-stamp will be sent
            new java.util.Date();
            String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
            Log.d("student ui", "current system time: " + timeStamp);

            userDetails = getSharedPreferences(USER_ID, 0);                     // get the userDetails file for editing
            SharedPreferences.Editor editor = userDetails.edit();               // edit the userID to the shared preference file
            editor.remove("prev_checkin");                                      // removes any previously stored checkin information
            editor.putString("prev_checkin", scanContent);                      // adds info to shared preferences
            editor.commit();                                                    // commits the changes

            // editor.putLong("student_ui", "time millis: " + timeStamp);


            String savedInfo = userDetails.getString("prev_checkin", "default");     // Logs the data for Testing purposes
            Log.d("student ui", "saved info" + savedInfo);



        } else if (scanID == 2 && !scanFormat.equals("QR_CODE"))    // in the event the user does not scan a QR
        {

            Log.e("student ui", "student has scanned wrong type of code");

            // informs user that the code recently scanned is not of correct type
            Toast QRIncorrectFormat = Toast.makeText(getApplicationContext(),
                    "format incorrect, please try again..." + scanContent, Toast.LENGTH_LONG);
            QRIncorrectFormat.show();

        } // if else to determine that the student has scanned in the correct data

    } // storeCheckIn
    */

    /*

    /**
     * The following method is to check if there are any previously existing files containing check in data that can
     * be sent to the database now there is internet connection available


    private void sendPrevCheckinDetails (String pAllInfo)
    {

        String allInfo = pAllInfo;

        Log.d("student ui", "Data to be sent to Database: " + allInfo);

        // launching SignIn Activity
        Intent openSignIn = new Intent(getApplicationContext(), SignIn.class);    // creates a new intent

        // takes the previously scanned info and packs it into a bundle before sending it to the SignIn class
        String scannedInfo = allInfo;                        // creates a copy of the info passed into method
        openSignIn.putExtra("Info", scannedInfo);            // puts the scanned info into a bundle
        startActivity(openSignIn);                           // starts the sign in activity

        // after which shared preference to be removed
        SharedPreferences.Editor editor = userDetails.edit();               // edit the userID to the shared preference file
        editor.remove("prev_checkin");                                      // adds info to shared preferences
        editor.commit();                                                    // commits the changes

        // closing this screen
        finish();

    } // sendPrevCheckinDetails

    */

} // LectureUI
