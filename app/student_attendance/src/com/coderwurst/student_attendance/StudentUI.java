package com.coderwurst.student_attendance;

import android.app.Activity;
import android.content.*;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
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
    private Button btnScan;                         // button to initiate sign-in process

    IntentResult scanningResult = null;             // intent result to store scanned information

    private int scanID = 0;                         // int to store type of scan

    private WifiManager wifi;                       // wifi manager
    private boolean firstNetwork = false;           // boolean to determine if student is on campus
    private boolean secondNetwork = false;
    private boolean thirdNetwork = false;
    private boolean fourthNetwork = false;


    private int wifiInRange = 0;                    // to store the numbers of wifi SSIDs in range

    private boolean onCampus = false;               // boolean to determine if 2 Uni wifi SSIDs are in range

    // to determine if the server is available
    private static boolean serverAvailable = MainScreenActivity.serverAvailable;
    private String serverResponse;

    private Context context = this;             // context to be used to check server connectivity

    // tags for log statements
    private static final String TAG = "student ui";

    @Override
    public void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.student_ui);

        // determine if device has access to server
        new TestConnection().execute();

        // buttons
        btnScan = (Button) findViewById(R.id.scan_button);          // button to return all students

        // sets onCLick listeners for both buttons
        btnScan.setOnClickListener(this);

        // method call to check if uni networks are in range
        checkNet();

    } // onCreate


    @Override
    public void onClick (View view)
    {
        if(view.getId()==R.id.scan_button)      // user wishes to check-in to class
        {
            Log.d(TAG, "student wishes to check into a class");
            // to be used when scanning in QR-Code to register attendance
            IntentIntegrator scanIntegrator = new IntentIntegrator(this);
            scanIntegrator.initiateScan();      // opens Zxing scanner
            scanID = 2;
        } // if

    }// onClick


    // returns scanning results for further computation
    public void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        // stores the information scanned
        scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);

        // to determine if the scan was successful
        if(scanningResult != null && resultCode == RESULT_OK)
        {
            Log.d(TAG, "scanned info ok");

            // only if the student has been determined to be on cmapus can the check-in process be completed
            if (onCampus)
            {

                Log.d(TAG, "student is on campus");

                // Toast contents used to follow data flow through app, confirm input
                String scanContent = scanningResult.getContents();
                String scanFormat = scanningResult.getFormatName();
                // formatTxt.setText("FORMAT: " + scanFormat);
                // contentTxt.setText("CONTENT: " + scanContent);

                Log.d(TAG, "scan content: " + scanContent);

                 /**
                 * the following if-else block is implemented at this stage as it is
                 * important to determine if the user has scanned in the right type
                 * of data for the function he or she has chosen
                 */

                if (scanID == 2 && scanFormat.equals("QR_CODE"))            // "QR_CODE" is only valid QR-Code format
                {

                    Log.d(TAG, "student to check in");

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

                    Log.e(TAG, "student has scanned wrong type of code");

                    // informs user that the code recently scanned is not of correct type
                    Toast QRIncorrectFormat = Toast.makeText(getApplicationContext(),
                            "format incorrect, please try again..." + scanContent, Toast.LENGTH_LONG);
                    QRIncorrectFormat.show();

                } else if (scanID == 1 && scanFormat.equals("CODE_128"))         // to ensure scanned image is correct
                {

                    Log.d(TAG, "user wishes to register as another user");

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

                    Log.e(TAG, "student has scanned wrong type of code");   // log in java console to show error

                    // user informed of error
                    Toast IDIncorrectFormat = Toast.makeText(getApplicationContext(),
                            "valid User ID not scanned, please try again...", Toast.LENGTH_LONG);
                    IDIncorrectFormat.show();

                } // series of else - if statements
            } else          // student not on campus and data to be stored
            {
                Log.d(TAG, "student not on campus");
                // inform user of incompatible scan
                Toast toast = Toast.makeText(getApplicationContext(),
                        "error 101; please contact class lecturer", Toast.LENGTH_LONG);
                toast.show();

            }// if-else to confirm scan data has been received
        } else {

            Log.e(TAG, "check in failed");   // log in java console to show an error has occurred

            // inform user of incompatible scan
            Toast toast = Toast.makeText(getApplicationContext(),
                    "no scan data received!", Toast.LENGTH_SHORT);
            toast.show();

        }// if else to determine if student is on campus
    }// onActivityResult


    /**
     * the following method uses wifi manager to perform a number of checks in order to determine if there
     * are 2 recognised university of ulster networks in range to determine if the student is on campus
     */

    private boolean checkNet ()
    {

        // check to see if wifi is enabled, and if not, activate
        wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);    // get the current wifi networks in range

        long timeIn = System.nanoTime();
        List<ScanResult> results = null;            // list to store the scan results
        int count = 0;                              // count to store the number of networks in range

        while(results == null)                      // to cater for the delay in the Wifi transmitter getting results
        {
            results = wifi.getScanResults();        // to get a list of current wifi networks
            count ++;
        } // while

        // time stamp
        long timeOut = System.nanoTime();           // used to calculate the time taken to return the wifi results

        long duration = timeOut - timeIn;           // time taken calculated in nano seconds

        double seconds = (double)duration / 1000000000.0;   // computes the time into seconds

        Log.d("wifi", "scan time taken: " + seconds + " (" +count + ")");   // informs tester of time taken

        /**
         * Multiple wifi routers in range of the device could disrupt the counting process, if for example
         * there are 5 routers in range, each of the network SSIDs searched for below would be being transmitted
         * five times; resulting in an OK scan even if there were no others present. To prevent this, booleans
         * where used to set one true for a present network, and then these trues are counted to determine if
         * the network is in range
         **/


        for (ScanResult mScanResult : results)                  // for loop to be preformed for number of results
        {
            Log.d("wifi", "scanning results");
            if (mScanResult.SSID.toString().equals("wifi one"))  // check to see if eduroam network is in range
            {        // eduroam
                firstNetwork = true;
            } else if (mScanResult.SSID.toString().equals("wifi two"))   // check to see if Student network is in range
            {        // Student
                secondNetwork = true;
            } else if (mScanResult.SSID.toString().equals("wifi three"))     // check to see if eng_j network is in range
            {        // eng_j

                thirdNetwork = true;
            } else if (mScanResult.SSID.toString().equals("wifi four"))     // check to see if Staff network is in range
            {         // Staff

                fourthNetwork = true;
            } // if else block to determine how many networks are in range of device

            firstNetwork = true;            // TODO once real wifi ssids added in if - else block above, can remove this line

            Log.d("wifi check", "networks in range: " + mScanResult.SSID.toString());

        } // for

        // series of if statements to determine how many networks are in range

        if (firstNetwork)       // if the first network is present the count is incremented by 1
        {
            wifiInRange ++;
        } // if
        if (secondNetwork)      // repeated for all other networks
        {
            wifiInRange ++;
        } // if
        if (thirdNetwork)
        {
            wifiInRange ++;
        } // if
        if (fourthNetwork)
        {
            wifiInRange ++;
        } // if

        Log.d("wifi check", "networks in range: " + wifiInRange);       // outpus in console to show number of networks in range


        if (wifiInRange >= 2)   // only if at least 2 of these networks present,device on Campus (for test can be set to 5)

        {
            onCampus = true;
            Log.d("wifi check", "student on campus: " + onCampus);

            return true;
        } else

            onCampus = false;
        Log.d("wifi check", "student not on campus, offline mode initiated");

        return false;

    } // checkWifi

    /**
     * Background Async Task to call a PHP script on the
     * server machine in order to establish if a connection
     * to the server is available
     * */

    class TestConnection extends AsyncTask<String, String, String>
    {

        /**
         * Before starting background thread Show Progress Dialog to inform
         * user that the app is processing information. As this process was
         * completed automatically, in less than half a second and in the
         * background, it was not necessary to show the user a dialog box
         **/

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        } // onPreExecute

        /**
         * send request to server to return success confirmation - code completed with reference to
         * http://www.desousa.com.pt/blog/2012/01/testing-server-reachability-on-android, first Accessed 17.08.14s
         * */

        protected String doInBackground(String... args)
        {

            // use connectivity manager and network info to establish if the device has Internet access
            final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            final NetworkInfo netInfo = connMgr.getActiveNetworkInfo();
            // boolean serverOnline = false;                       // assume that the server is not yet available

            if (netInfo != null && netInfo.isConnected())       // first check that there is Internet Connectivity on device
            {
                // then try and make a connection to the server
                try {
                    URL url = new URL("http://" + MainScreenActivity.serverIP);                 // server IP address
                    HttpURLConnection testServer = (HttpURLConnection) url.openConnection();    // open up connection

                    // sets the HttpURLConnection defaults for testing a connection
                    testServer.setRequestProperty("User-Agent", "Android Application");
                    testServer.setRequestProperty("Connection", "close");

                    // the connection timeout period is 5000 milliseconds (3 seconds)
                    testServer.setConnectTimeout(5000);
                    // connects to server
                    testServer.connect();

                    // the connection will then return a value, which is stored as the boolean for true if server available
                    serverAvailable = (testServer.getResponseCode() == 200);
                    serverResponse = "connection available";

                    // details have been stored and the student is checked in
                    Log.d(TAG, "Server connection established");

                } catch (IOException e) {

                    // otherwise an exception will be thrown as the server is not available
                    serverResponse = "connection not available, offline mode activated";
                    serverAvailable = false;
                    // failed to sign-in, PHP has returned an error
                    Log.e(TAG, "Server connection unavailable" + e.getMessage());
                }
            } else {
                // else no internet connection is available
                serverResponse = "connection not available, offline mode activated";
                serverAvailable = false;
                Log.e(TAG, "Internet Connection Unavailable");
            } // if - else

            return null;
        }// doInBackground



        /**
         * After completing background task, the textview showing server connection was to be
         * updated accordingly. In the event there are files stored on the device waiting to
         * be sent, the corresponding menu option is activated
         **/

        protected void onPostExecute(String file_url)
        {

            if (!serverAvailable)
            {
                finish();       // exit the app
            } // if the server is not available

        } // onPostExecute
    } // TestConnection


} // StudentUI