package com.coderwurst.student_attendance;  // Sprint 4 - Sign into Database

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.OnClickListener;

// import Zxing Files for Barcode Scanner
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * ***********************
 * Created by IntelliJ IDEA
 * User: andrew
 * Date: 21/07/2014
 * Time: 11:52
 * Version: V8.0
 * SPRINT 4 - MAIN SCREEN TO ALLOW INITIAL REGISTRATION ON DEVICE AND UPON NEXT START UP DETERMINE APP INTERFACE
 * ************************
 */


public class MainScreenActivity extends Activity implements OnClickListener

{
	private Button btnReg;                      // button to register a new user
    private TextView formatTxt, contentTxt;     // text view to inform tester of data captured at this stage
    private int savedID = 0;                     // int to store type of scan
    private int scanID = 0;

    // opens the sharedPref file to allow user id to be stored
    public static final String USER_ID = "User ID File";
    static SharedPreferences userDetails;

    // serverIP available across whole app, to increase efficiency & prevent user error when entering the address
    protected static String serverIP = "172.17.12.16";

    protected static boolean serverAvailable;            // to determine if the server is available
    private String serverResponse;

    private Context context = this;             // context to be used to check server connectivity



    @Override
    protected void onStart()
    {
        super.onStart();

        new TestConnection().execute();

    } // onResume


    /**
     * Main screen activity called after splash screen to determine if user details have been previously stored,
     * and if so what type of user to open up corresponding UI. If no user details have been previously stored,
     * then the app opens to allow a user to register on the device
     **/

   	@Override
	public void onCreate(Bundle savedInstanceState)
    {
		super.onCreate(savedInstanceState);

        // determine if device has access to server
        new TestConnection().execute();

        // returns the stored sharedPrefs for the app and stores the user type
        userDetails = getSharedPreferences(USER_ID, 0);
        savedID = userDetails.getInt("user_Type", 0);

        /**
         * if to determine if there is any outstanding check in info from a previous session
         * to be sent to the database before continuing
         **/

        if (savedID == 2)               // savedPreferences for student, then the app will open to student UI
        {
            Log.d("main screen", "user type; student");   // log in java console which type of user is registered with device

            // code to open up student UI
            Intent openStudentUI = new Intent(getApplicationContext(), StudentUI.class);
            startActivity(openStudentUI);

            // closing this screen
            finish();


        } else if (savedID == 1)        // otherwise the app will start up straight to lecturer UI
        {

            Log.d("main screen", "user type; lecturer");   // log in java console which type of user is registered with device

            // code to open up staff UI
            Intent openLecturerUI = new Intent(getApplicationContext(), LecturerUI.class);
            startActivity(openLecturerUI);

            // closing this screen
            finish();

        } else                           // no savedPreferences then the app will allow user to register
        {
            setContentView(R.layout.main_screen);

            Log.d("main screen", "user type not found");   // log in java console which type of user is registered with device

            // button to register details
            btnReg = (Button) findViewById(R.id.reg_button);

            // textViews for hold format and content info for testing purposes
            //formatTxt = (TextView) findViewById(R.id.scan_format);
            //contentTxt = (TextView) findViewById(R.id.scan_content);

            // set up onClick listener to take user to scanning functionality
            btnReg.setOnClickListener(this);

        } // if - else

    }// OnCreate


    // onClick method to determine which classes are called dependant on which button is clicked
    @Override
    public void onClick (View view)
    {
        // student or lecturer wishes to register for first time use
        if(view.getId()==R.id.reg_button)       // register device
        {
                IntentIntegrator scanIntegrator = new IntentIntegrator(this);
                scanIntegrator.initiateScan();
                scanID = 1;
        }// if

    }// onClick


    /**
     * takes the contents of the code scanned and sends this information to the registration
     * class, handles erroronous scanning
     */
    public void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);

        if (scanningResult != null && resultCode == RESULT_OK)
        {

            // strings to store scanned data
            String scanContent = scanningResult.getContents();
            String scanFormat = scanningResult.getFormatName();
            // formatTxt.setText("FORMAT: " + scanFormat);
            // contentTxt.setText("CONTENT: " + scanContent);

            Log.d("main screen", "new user" + scanContent);   // log in java console which type of user is registering with device

            // launching Registration Activity
            Intent register = new Intent(getApplicationContext(), InitialReg.class);

             // takes the scanned info and packs it into a bundle before sending it to the Registration class
            String scannedInfo = scanContent;
            register.putExtra("Info", scannedInfo);
            startActivity(register);

            // closing this screen
            finish();


        } else {

            Log.e("main screen", "cancelled registration");   // log in java console to show an error has occurred

            // inform user of incompatible scan
            Toast toast = Toast.makeText(getApplicationContext(),
                    "No scan data received!", Toast.LENGTH_SHORT);
            toast.show();

        }// if-else to confirm scan data has been received

    }// onActivityResult

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
                    serverAvailable = true;
                    serverResponse = "connection available";

                    // details have been stored and the student is checked in
                    Log.d("main screen", "Server connection established");

                } catch (IOException e) {

                    // otherwise an exception will be thrown as the server is not available
                    serverResponse = "connection not available, offline mode activated";
                    serverAvailable = false;
                    // failed to sign-in, PHP has returned an error
                    Log.e("main screen", "Server connection unavailable: " + e.getMessage());
                } // try - catch
            } else {
                // else no internet connection is available
                serverResponse = "connection not available, offline mode activated";
                serverAvailable = false;
                Log.e("main screen", "Internet Connection Unavailable");
            } // if - else

            return null;
        }// doInBackground



        /**
         * In the event that the server is not available, and the user is either a student or registering for the first
         * time, the app will inform the user of the problem before closing. A lecturer should still have access to the
         * app, as they have the offline capability of saving check-in details to the device
         **/

        protected void onPostExecute(String file_url)
        {

            if (serverAvailable != true && savedID == 0)
            {
                // inform user of incompatible scan
                Toast connectionError = Toast.makeText(getApplicationContext(),
                        "server not available at this time", Toast.LENGTH_LONG);
                connectionError.show();

                finish();

            } else if (serverAvailable != true && savedID == 2)
            {
                // inform user of incompatible scan
                Toast connectionError = Toast.makeText(getApplicationContext(),
                        "server not available at this time", Toast.LENGTH_LONG);
                connectionError.show();

                finish();


            }// if the server is not available

        } // onPostExecute
    } // TestConnection


}// MainScreenActivity