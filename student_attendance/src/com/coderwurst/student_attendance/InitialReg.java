package com.coderwurst.student_attendance;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.util.Log;
import android.widget.Toast;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * ***********************
 * Created by IntelliJ IDEA
 * User: andrew
 * Date: 08/07/2014
 * Time: 12:10
 * Version: V2.0
 * SPRINT 5 - CLASS TO ALLOW STUDENT OR LECTURER TO SIGN INTO DEVICE, DETAILS TO BE SAVED
 * ************************
 */
public class InitialReg extends Activity

{
    private String scannedID = "";      // string to store scanned ID data

    // booleans to identify from the user ID which type of user and therefore what functionality the app can offer
    private boolean studentUser = false;
    private boolean staffUser = false;

    // further booleans used in user authentication
    private boolean deviceOK = true;

    // opens the sharedPref file to allow user id to be stored
    public static final String PREFERENCES_FILE = "User ID File";
    static SharedPreferences userDetails;
    // EditText userID;
    // EditText userType;

    // progress dialog to inform user
    private ProgressDialog pDialog;
    private String dialogText = "success";

    // creates the JSONParser object
    JSONParser jsonParser = new JSONParser();

    // server IP address
    private static String serverAddress = MainScreenActivity.serverIP;

    // url to authenticate user - separate PHP scripts for student and staff IDs
    private static String url_student_auth = "http://" + serverAddress + "/xampp/student_attendance/auth_student.php";
    private static String url_staff_auth = "http://" + serverAddress + "/xampp/student_attendance/auth_staff.php";
    private static String url_device_auth = "http://" + serverAddress + "/xampp/student_attendance/auth_device.php";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";

    // code to retrieve device specific details
    private String deviceID;

    /**
     * This onCreate method unpacks data scanned into the app in the MainScreenActivity class, determines the user
     * type and saved this data in the app's shared preferences accordingly. Calls authenticate user background task
     * to determine if the ID scanned into the device is that of a registerd student or lecturer on the server. The
     * method also stamps the device ID number, which is unique on each android device, to be sent and stored on the
     * database if the user is a student. This is one of several functions in the app to prevent students from
     * manipulating the check-in system.
     */

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.initial_reg);

        // unpack the data scanned sent from MainScreenActivity class
        Bundle bundle = getIntent().getExtras();
        scannedID = bundle.getString("Info");

        // retrieves the unique Android ID value for the device being used to register
        deviceID = Secure.getString(this.getContentResolver(), Secure.ANDROID_ID);

        // user Authentication Check with server to ensure that the user is registered to use app
        Log.d("initial reg", "ID Auth " + scannedID);          // logcat tag to view string contents
        Log.d("initial reg", "Device ID " + deviceID);

        // assign the app student or staff user privileges
        if(scannedID.charAt(0) == 'E' || scannedID.charAt(0) == 'e')
        {
            Log.d("initial reg", "user is a staff member");          // logcat tag to view string contents (testing purposes only)

            staffUser = true;       // boolean stored in shared preferences, automatically loaded on next start up

            // userType = (EditText) findViewById(R.id.user_type);
            // userType.setText("staff");

            // code to authenticate staff ID with Database

        } else if (scannedID.charAt(0) == 'B' || scannedID.charAt(0) == 'b')
        {

            Log.d("initial reg", "user is a student");          // logcat tag to view string contents (testing purposes only)

            studentUser = true;     // boolean stored in shared preferences, automatically loaded on next start up

            // userType = (EditText) findViewById(R.id.user_type);
            // userType.setText("student");

            // code to authenticate student ID with Database found in PHP scripts

        } else {        // scanned data is neither a staff or student number


            Log.e("initial reg", "scanned data incorrect");          // logcat tag to view string contents (testing purposes only)

            // in the event that neither an B or a E id has been scanned....
            Toast errorScan = Toast.makeText(getApplicationContext(),
                    "scanned data not in correct format...", Toast.LENGTH_LONG);
            errorScan.show();

            Intent openMainScreen = new Intent(getApplicationContext(), MainScreenActivity.class);
            startActivity(openMainScreen);

            // closing this screen
            finish();


        } // if - else - if

        // text set to scanned information for confirmation (testing purposes only)
        // userID = (EditText) findViewById(R.id.user_id);
        // userID.setText(scannedID);


        /* button to confirm input and send to database
        Button btnSubmitID = (Button) findViewById(R.id.confirm_reg_details);

        // button click event
        btnSubmitID.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view)
            {

                new AuthenticateUser().execute();
                Log.d("initial reg", "user wishes to register on device");          // logcat tag to view string contents (testing purposes only)


                // previous position of sharedPreferences code
            }// onClick
        }); */

        // the users' details can be sent to the database for authentication
        new AuthenticateUser().execute();
        Log.d("initial reg", "user wishes to register on device");          // logcat tag to view string contents (testing purposes only)

    }// OnCreate

    /**
     * Background Async Task to send scanned information to database and perform a number of authentication checks. In
     * the event of the user being a student, both the device ID and Student ID are authenticated. This level of
     * authentication was decided to be not necessary for staff members, however if the security threat was determined
     * to be high enough the same process could be used to authenticate staff members and their devices - should, for
     * example, a staff member have his or her ID card stoled.
     */

    class AuthenticateUser extends AsyncTask<String, String, String>
    {

        /**
         * Before starting background thread Show Progress Dialog to inform
         * user that the app is processing information.
         **/

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            pDialog = new ProgressDialog(InitialReg.this);
            pDialog.setMessage("authenticating...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();

        } // onPreExecute



        /**
         * doInBackground makes several calls to the server,
         * authenticating ID details that have been scanned into device
         * */

         protected String doInBackground(String... args)
        {

            String user_id = scannedID;         // string to store the scanned information
            String device_id = deviceID;
            JSONObject jsonUser = null;             // declare the JSON object to auth user details
            JSONObject jsonDevice = null;           // declare the JSON object to auth device details

            // parameters to be passed into PHP script on server side
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("user_id", user_id));         // user ID


            // NB both parameters cannot be passed to the same PHP script as this leads to Mysql Injection
            // parameters to be passed into PHP script on server side
            List<NameValuePair> deviceParams = new ArrayList<NameValuePair>();
            deviceParams.add(new BasicNameValuePair("user_id", user_id));         // user ID
            deviceParams.add(new BasicNameValuePair("device_id", device_id));     // unique device number


            /**
             * Following if-else block determines which PHP calls to make, determined by the type of ID that is scanned
             * in; only student users will be device checked
             */

            if (studentUser == true)
            {
                Log.d("initial reg", "student user being authenticated");          // logcat tag to view string contents (testing purposes only)

                jsonUser = jsonParser.makeHttpRequest(url_student_auth, "POST", params);

                Log.d("initial reg", "device ID being authenticated");
                jsonDevice = jsonParser.makeHttpRequest(url_device_auth, "POST", deviceParams);


            } else if (staffUser == true)
            {
                Log.d("initial reg", "staff user being authenticated");          // logcat tag to view string contents (testing purposes only)


                jsonUser = jsonParser.makeHttpRequest(url_staff_auth, "POST", params);

            } else
            {

                Log.e("initial reg", "scanned details are not that of student or staff");          // logcat tag to view string contents (testing purposes only)


                // in the event that neither an B or a E id has been scanned....
                Toast errorScan = Toast.makeText(getApplicationContext(),
                        "scanned data not in correct format...", Toast.LENGTH_LONG);
                errorScan.show();

                Intent openMainScreen = new Intent(getApplicationContext(), MainScreenActivity.class);
                startActivity(openMainScreen);

                // closing this screen
                finish();

            } // if - else - else to determine if user should be checked with staff or student parameters



            /**
             * The following if-else block determines if the Log cat should be checked for a second response from the
             * Database (ie only when student user has been determined)
             */
            // check log cat for response (if user in database)
            Log.d("initial reg", " user ID response" + jsonUser.toString());

            // check log cat for response (if user is student and device id ok)
            if(studentUser == true)
            {
                Log.d("initial reg", " device ID response" + jsonDevice.toString());
                try
                {
                    int devicecheck = jsonDevice.getInt(TAG_SUCCESS);

                    if (devicecheck == 1)       // device registration has been successful
                    {
                        deviceOK = true;
                    } else                      // user is registered on another device
                    {
                        deviceOK = false;
                    } // if else to determine is device

                } catch (JSONException e)
                {
                    e.printStackTrace();
                } // try catch for device ID check

            } // check for device ID if user is student

            /**
             * Third if - else block used to determine which procedure the app should follow, dependant on a number
             * of situations resulting from the checks found in code above
             */

            if (staffUser == true || studentUser == true && deviceOK == true)
            {
                // check for success tag as php script will have been run
                try
                {
                    int success = jsonUser.getInt(TAG_SUCCESS);

                    if (success == 1)
                    {
                        Log.d("initial reg", "user successfully authenticated");          // logcat tag to view string contents (testing purposes only)


                        // successfully authenticated user must be saved to sharedPreferences
                        userDetails = getSharedPreferences(PREFERENCES_FILE, 0); // create the shared preferences package

                        SharedPreferences.Editor editor = userDetails.edit();           // edit the userID to the shared preference file
                        editor.putString("user_ID", user_id);                           // ******userID.getText().toString()******

                        if (staffUser == true)
                        {
                            Log.d("initial reg", "user type set to staff");             // logcat tag to view string contents (testing purposes only)

                            editor.putInt("user_Type", 1);                                // stored in the preferences a staff user

                        } else if (studentUser == true)
                        {
                            Log.d("initial reg", "user type set to student");           // logcat tag to view string contents (testing purposes only)

                            editor.putInt("user_Type", 2);                                // stored in the preferences a student user

                        } else
                        {
                            Log.d("initial reg", "user type not recognised");           // logcat tag to view string contents (testing purposes only)

                            editor.putInt("user_Type", 0);                              // for testing purposes, allows Type to be reset

                        } // if - else

                        editor.commit();                                                // save changes

                        Log.d("initial reg", "ID value; " + user_id);          // logcat tag to view contents of string

                        // once user has been saved, return to main screen - to determine which UI to be called
                        Intent openMainScreen = new Intent(getApplicationContext(), MainScreenActivity.class);
                        startActivity(openMainScreen);

                        // closing this screen
                        finish();
                    } else                                                              // the database has returned an error
                    {

                        Log.e("initial reg", "scanned details are incorrect");          // tag to view error

                        dialogText = "oops! an error has occurred, please try again...";// inform user that an error has occurred

                        // failed to authenticate user, returned to main screen to have option to register again
                        Intent openMainScreen = new Intent(getApplicationContext(), MainScreenActivity.class);
                        startActivity(openMainScreen);

                        // closing this screen
                        finish();

                    } // if - else in the event a valid ID has been scanned but doesn't match any database records



                } catch (JSONException e)
                {
                    e.printStackTrace();
                } // try - catch to confirm JSON success after running of PHP script


            } else if (studentUser == true && deviceOK == false)
            {
                Log.e("initial reg", "student already registered on another device");          // tag to view error

                dialogText = "oops! an error has occurred, please try again...";                // inform user that they are already registered

                // failed to authenticate user, returned to main screen to have option to register again
                Intent openMainScreen = new Intent(getApplicationContext(), MainScreenActivity.class);
                startActivity(openMainScreen);

                // closing this screen
                finish();

            } // if else block to authenticate user details

            return null;
        }// doInBackground

        /**
         * After completing background task the progress dialog can be dismissed, and the user
         * is informed using a toast message if the process has not been successful that they
         * must contact the administration office. Otherwise they will be taken to the home screen
         **/
        protected void onPostExecute(String file_url)
        {

            if (!deviceOK)
            {
                Toast deviceError = Toast.makeText(getApplicationContext(),
                        "user already registered on another device, please contact your administrator...", Toast.LENGTH_LONG);
                deviceError.show();
            } // if to inform user that the device is already registered

            pDialog.setMessage(dialogText);
            // dismiss the dialog once done
            pDialog.dismiss();
        }// onPostExecute

    }// authenticateUser

} // InitialReg
