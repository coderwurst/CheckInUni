package com.coderwurst.student_attendance;

import android.app.Activity;
import android.content.*;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * ***********************
 * Created by IntelliJ IDEA
 * User: andrew
 * Date: 21/07/2014
 * Time: 11:54
 * Version: V8.0
 * SPRINT 6 - USER INTERFACE FOR STUDENTS TO ALLOW USER TO 'CHECK IN' TO A CLASS
 * ************************
 */


public class StudentUI extends Activity implements View.OnClickListener

{
    private Button btnScan;                     // button to initiate sign-in process
    private Button btnResetUsr;                 // button to reset sharedPref for testing purposes

    IntentResult scanningResult = null;         // intent result to store scanned information

    // private TextView formatTxt, contentTxt;     // text view for data captured at this stage TESTING ONLY
    private int scanID = 0;                     // int to store type of scan

    WifiManager wifi;                           // wifi manager
    private boolean firstNetwork = false;         // boolean to determine if student is on campus
    private boolean secondNetwork = false;
    private boolean thirdNetwork = false;
    private boolean fourthNetwork = false;


    private int wifiInRange = 0;

    private boolean onCampus = false;           // boolean to determine if 2 Uni wifi SSIDs are in range

    // opens the sharedPref file to allow any previously saved checkins to be used
    // public static final String USER_ID = "User ID File";
    // static SharedPreferences userDetails;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.student_ui);

        // Buttons
        btnScan = (Button) findViewById(R.id.scan_button);          // button to return all students
        btnResetUsr = (Button) findViewById(R.id.test_button);      // button to initiate scan

        // TextViews for hold format and content info FOR TESTING PURPOSES ONLY
        // formatTxt = (TextView) findViewById(R.id.scan_format);
        // contentTxt = (TextView) findViewById(R.id.scan_content);

        // sets onCLick listeners for both buttons
        btnScan.setOnClickListener(this);
        btnResetUsr.setOnClickListener(this);       // button to allow user type to be reset (testing purposes only)

    } // onCreate


     /**
     * the following method uses wifimanager to perform a number of checks in order to determine if the student is
     * firstly connected through wifi to the internet, and secondly if there are 2 recognised university of ulster
     * networks in range to determine if the student is on campus
     */

    private boolean checkWifi ()
    {

        // check to see if wifi is enabled, and if not, activate
        wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        if (wifi.isWifiEnabled() == false)
        {
            Toast wifiToast = Toast.makeText(getApplicationContext(),
                    "wifi is currently disabled...activating", Toast.LENGTH_LONG);
            wifiToast.show();
            wifi.setWifiEnabled(true);
            Log.d("student ui", "wifi activated");

        } // if

        // list to store the scan results
        List<ScanResult> results = wifi.getScanResults();      // to get a list of current wifi networks

        for (ScanResult mScanResult : results)                  // for loop to be preformed for number of results
        {
            if (mScanResult.SSID.toString().equals("eduroam"))  // check to see if eduroam network is in range
            {
                firstNetwork = true;
                // wifiInRange ++;
            } else if (mScanResult.SSID.toString().equals("Student"))   // check to see if Student network is in range Student
            {
                secondNetwork = true;
                //wifiInRange ++;
            } else if (mScanResult.SSID.toString().equals("eng_j")){        // eng_j

                thirdNetwork = true;
                //wifiInRange ++;

            }else if (mScanResult.SSID.toString().equals("Staff")){         // Staff

                fourthNetwork = true;

            } // if else block to determine how many networks are in range of device



        Log.d("wifi check", "networks in range: " + mScanResult.SSID.toString());

        } // for


        // series of if statements to determine how many networks are in range

        if (firstNetwork)
        {
            wifiInRange ++;
        }
        if (secondNetwork)
        {
            wifiInRange ++;
        }
        if (thirdNetwork)
        {
            wifiInRange ++;
        }
        if (fourthNetwork)
        {
            wifiInRange ++;
        }

        Log.d("wifi check", "networks in range: " + wifiInRange);


        if (wifiInRange >= 5)                   // only if at least 2 of these networks present is device on Campus

        {
            onCampus = true;
            Log.d("wifi check", "student on campus: " + onCampus);

            return true;
        } else

            onCampus = false;
            Log.d("wifi check", "student not on campus, offline mode initiated");

        return false;

    } // checkWifi


    @Override
    public void onClick (View view)
    {
        if(view.getId()==R.id.scan_button)      // determines what the user wishes to do, amends scanID accordingly
        {
            Log.d("student ui", "student wishes to check into a class");
            // to be used when scanning in QR-Code to register attendance
            IntentIntegrator scanIntegrator = new IntentIntegrator(this);
            scanIntegrator.initiateScan();      // opens Zxing scanner
            scanID = 2;

        } else {

            Log.d("student ui", "user wishes to register as another user");
            // calls scanner to register new details in system TO BE REMOVED FOR FINAL VERSION
            IntentIntegrator scanIntegrator = new IntentIntegrator(this);
            scanIntegrator.initiateScan();      // opens Zxing scanner
            scanID = 1;

        }// if - else
    }// onClick


    // returns scanning results for futher computation
    public void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        checkWifi();                                // method called to check if wifi connection is available

        scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);

        if(scanningResult != null && resultCode == RESULT_OK)
        {
            Log.d("student ui", "scan ok!");


            if (onCampus)                     // to determine if the scan was successful
            {

                Log.d("student ui", "student is on campus");

                // Toast contents used to follow data flow through app, confirm input
                String scanContent = scanningResult.getContents();
                String scanFormat = scanningResult.getFormatName();
                // formatTxt.setText("FORMAT: " + scanFormat);
                // contentTxt.setText("CONTENT: " + scanContent);

                Log.d("student ui", "scan content: " + scanContent);

                 /**
                 * the following if-else block is implemented at this stage as it is
                 * important to determine if the user has scanned in the right type
                 * of data for the function he or she has chosen
                 */

                if (scanID == 2 && scanFormat.equals("QR_CODE"))            // "QR_CODE" is only valid QR-Code format
                {

                    Log.d("student ui", "student to check in");

                    // launching SignIn Activity
                    Intent openSignIn = new Intent(getApplicationContext(), SignIn.class);

                    // takes the scanned info and packs it into a bundle before sending it to the SignIn class
                    String scannedInfo = scanContent;
                    openSignIn.putExtra("Info", scannedInfo);
                    startActivity(openSignIn);

                    // closing this screen
                    finish();

                } else if (scanID == 2 && !scanFormat.equals("QR_CODE"))    // in the event the user does not scan a QR
                {

                    Log.e("student ui", "student has scanned wrong type of code");

                    // informs user that the code recently scanned is not of correct type
                    Toast QRIncorrectFormat = Toast.makeText(getApplicationContext(),
                            "format incorrect, please try again..." + scanContent, Toast.LENGTH_LONG);
                    QRIncorrectFormat.show();

                } else if (scanID == 1 && scanFormat.equals("CODE_128"))         // FOR TESTING PURPOSES ONLY
                {

                    Log.d("student ui", "user wishes to register as another user");

                    // launching Registration Activity
                    Intent openReg = new Intent(getApplicationContext(), InitialReg.class);

                    // takes the scanned info and packs it into a bundle before sending it to the Registration class
                    String scannedInfo = scanContent;
                    openReg.putExtra("Info", scannedInfo);
                    startActivity(openReg);

                    // closing this screen
                    finish();

                } else if (scanID == 1 && !scanFormat.equals("CODE_128"))          // scan is incorrect format
                {

                    Log.e("student ui", "student has scanned wrong type of code");   // log in java console to show error

                    // user informed of error
                    Toast IDIncorrectFormat = Toast.makeText(getApplicationContext(),
                            "valid User ID not scanned, please try again...", Toast.LENGTH_LONG);
                    IDIncorrectFormat.show();

                } // series of else - if statements
            } else          // student not on campus and data to be stored
            {
                Log.d("student ui", "student not on campus");
                // inform user of incompatible scan
                Toast toast = Toast.makeText(getApplicationContext(),
                        "error 101; please contact class lecturer", Toast.LENGTH_LONG);
                toast.show();

                // storeCheckIn();

            }// if-else to confirm scan data has been received
        } else {

            Log.e("student ui", "check in failed");   // log in java console to show an error has occurred

            // inform user of incompatible scan
            Toast toast = Toast.makeText(getApplicationContext(),
                    "no scan data received!", Toast.LENGTH_SHORT);
            toast.show();

            // finish();

        }// if else to determine if student is on campus
    }// onActivityResult

} // StudentUI
