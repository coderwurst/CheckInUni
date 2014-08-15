package com.coderwurst.student_attendance;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


/**
 * ***********************
 * Created by IntelliJ IDEA
 * User: andrew
 * Date: 16/07/2014
 * Time: 12:37
 * Version: V7.0
 * SPRINT 7 - ACTIVITY TO ALLOW LECTURER TO SCAN IN A STUDENT ID AND MODULE QR-CODE IN ORDER TO CHECK A STUDENT IN
 * ************************
 */

public class RecursiveSignIn extends Activity implements View.OnClickListener

{

    private Button btnGetStudentID;         // button to initiate scanning procedure to store student ID
    private Button btnGetMod;               // button to initiate scanning to store QR-Code information
    private Button btnSignIn;               // button to send information on to server

    // private TextView formatTxt, contentTxt;     // text view to inform tester of data captured at this stage TESTING ONLY
    private int scanID = 0;                     // int to store the type of scan

    // boolean values to ensure the lecturer scans both ID and class codes before attempting to check student in
    private boolean scannedID = false;
    private boolean scannedModule = false;

    // private Strings initiated to null so as information reset upon calling Activity
    private String studentNo = null;
    private String moduleInfo = null;
    private String moduleName = null;
    private String classInfo = null;

    // server IP address
    private static String serverAddress = MainScreenActivity.serverIP;

    // url to create new product
    private static String url_sign_in = "http://" + serverAddress + "/xampp/student_attendance/sign_in.php";
    private static String url_return_forename = "http://" + serverAddress + "/xampp/student_attendance/return_forename.php";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    // progress dialog to inform user
    private ProgressDialog pDialog;
    private String dialogText = "success";

    // creates the JSONParser object
    JSONParser jsonParser = new JSONParser();

    // linkedList to store multiple students for batch processing
    private LinkedList <String> studentBatch = new LinkedList<String>();
    private int count;

    // String to store student number to be used in retrieving student forename
    private String scannedStuNo;
    private int scanCount = 1;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recursive_signin);

        // Buttons
        btnGetStudentID = (Button) findViewById(R.id.lec_scan_id);
        btnGetMod = (Button) findViewById(R.id.lec_scan_mod);
        btnSignIn = (Button) findViewById(R.id.add_student_auto);

        // TextViews for hold format and content info for testing purposes
        // formatTxt = (TextView) findViewById(R.id.scan_format);
        // contentTxt = (TextView) findViewById(R.id.scan_content);

        btnGetStudentID.setOnClickListener(this);
        btnGetMod.setOnClickListener(this);
        btnSignIn.setOnClickListener(this);

    } // onCreate

    /**
     * if - else block in onClick method to ensure that the lecturer enters all the information necessary
     */

    @Override
    public void onClick (View view)
    {
        // outer if statement to determine (from UI Activity) if a server connection is available
        if(LecturerUI.serverAvailable)
        {
            if (view.getId() == R.id.lec_scan_id)     // && scannedModule == true
            {
                // logcat tag to view app progress
                Log.d("recursive", "user wants to scan student id");

                IntentIntegrator scanIntegrator = new IntentIntegrator(this);
                scanIntegrator.initiateScan();
                scanID = 1;

            } else if (view.getId() == R.id.lec_scan_mod)
            {

                // logcat to view app progress
                Log.d("recursive", "user wants to scan module code");

                // calls scanner to register new details in system
                IntentIntegrator scanIntegrator = new IntentIntegrator(this);
                scanIntegrator.initiateScan();
                scanID = 2;


            } else
            {

                if (scannedID == true && scannedModule == true)     // verifies that all info necessary is present
                {
                    // logcat to view app progress
                    Log.d("recursive", "scanned details to be sent to database");

                    new LecturerSignStudentIn().execute();          // code to submit details to the database

                } else
                {

                    // logcat to view app progress
                    Log.e("recursive", "necessary details have not been successfully scanned");

                    Toast incompleteData = Toast.makeText(getApplicationContext(),
                            "please ensure the Student ID and Module Code have both been scanned...", Toast.LENGTH_LONG);
                    incompleteData.show();

                } // if - else

            }// if - else - if - else


            /**
             * internet connection not available
             */


        } else
        {
            if (view.getId() == R.id.lec_scan_id)     // && scannedModule == true
            {
                // logcat tag to view app progress
                Log.d("recursive offline", "user wants to scan student id");

                IntentIntegrator scanIntegrator = new IntentIntegrator(this);
                scanIntegrator.initiateScan();
                scanID = 1;

            } else if (view.getId() == R.id.lec_scan_mod)
            {

                // logcat to view app progress
                Log.d("recursive offline", "user wants to scan module code");

                // calls scanner to register new details in system
                IntentIntegrator scanIntegrator = new IntentIntegrator(this);
                scanIntegrator.initiateScan();
                scanID = 2;

            } else
            {

                if (scannedID == true && scannedModule == true)     // verifies that all info necessary is present
                {

                    Toast noConnection = Toast.makeText(getApplicationContext(),
                            "server connection not available, storing information", Toast.LENGTH_LONG);
                    noConnection.show();

                    // logcat to view app progress
                    Log.d("recursive offline", "scanned details to be stored to device");

                    // call method to store scanned details to device
                    storeScannedInfo();         // calls method to store scanned details to device

                } else
                {

                    // logcat to view app progress
                    Log.e("recursive offline", "necessary details have not been successfully scanned");

                    Toast incompleteData = Toast.makeText(getApplicationContext(),
                            "please ensure the Student ID and Module Code have both been scanned...", Toast.LENGTH_LONG);
                    incompleteData.show();
                } // if - else

            }// if - else - if - else to determine button click

           } // if - else to determine functionality depending on internet connectivity
    }// onClick



    // Returns scanning results for further computation
    public void onActivityResult(int requestCode, int resultCode, Intent intent)
    {

        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);

        // code to process information
        if (scanningResult != null && resultCode == RESULT_OK)              // to determine if the scan was successful
        {

            String scanContent = scanningResult.getContents();
            String scanFormat = scanningResult.getFormatName();
            //formatTxt.setText("FORMAT: " + scanFormat);                   // Testing Purposes only
            //contentTxt.setText("CONTENT: " + scanContent);

            // logcat to view app progress
            Log.d("recursive", "scanned details; " + scanContent);

             /**
             * the following if-else block is implemented at this stage as it is
             * important to determine if the user has scanned in the right type
             * of data for the function he or she has chosen
             */

            if (scanID == 2 && scanFormat.equals("QR_CODE"))                    // "QR_CODE" is a valid QR-Code format
            {

                // store scanned information as the module information
                String scannedQRInfo = scanContent;

                // logcat to view app progress
                Log.d("recursive", "scanned module details");

                /** extract the necessary information out of this string to be used using special chars {} for module and []
                 * for class type */
                moduleInfo = scannedQRInfo.substring(scannedQRInfo.indexOf("{") + 1, scannedQRInfo.indexOf("}"));
                int findModName = moduleInfo.indexOf('-');                  // searches the scanned data for module title tag
                moduleName = moduleInfo.substring(findModName + 2);         // determines the start of the module name
                classInfo = scannedQRInfo.substring(scannedQRInfo.indexOf("[") + 1, scannedQRInfo.indexOf("]"));

                // toast to notify user of error in scanning of information
                Toast QRCorrectFormat = Toast.makeText(getApplicationContext(),
                        moduleName + ", " + classInfo, Toast.LENGTH_LONG);

                QRCorrectFormat.show();

                // logcat to view app progress
                Log.d("recursive", "module info; " + moduleInfo);
                Log.d("recursive", "module type; " + classInfo);

                scannedModule = true;       // boolean to ensure necessary information has been included before processing


            } else if (scanID == 2 && !scanFormat.equals("QR_CODE"))            // in the event the user does not scan a QR-Code
            {

                // logcat to view app progress
                Log.e("recursive", "incorrect data for module code scanned" + scanFormat);

                // toast to notify user of error in scanning of information
                Toast QRIncorrectFormat = Toast.makeText(getApplicationContext(),
                        "format incorrect, please try again...(" + scanFormat + ")", Toast.LENGTH_LONG);

                QRIncorrectFormat.show();

            } else if (scanID == 1 && scanFormat.equals("CODE_128"))            // "CODE_128" is a valid ID format
            {

                // logcat to view app progress
                Log.d("recursive", "scanned id details");

                // code to perform device beep to confirm successful scan
                try
                {

                    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);

                    r.play();

                } catch (Exception e)
                {
                    e.printStackTrace();
                }

                // stores scanned ID as the student number

                String scannedIDInfo = scanContent;

                studentNo = scannedIDInfo;      // CHANGE TO STORE THE SCANNED ID AS AN ADDITION TO A LINKED LIST

                /* linear search to eliminate multiple IDs being scanned

                int index = 0, comparison = 1;                            // index int for search position, comparison to count number of times linear search performed
                boolean exit = false;                                     // boolean to show if value has been found

                if (studentBatch != null)
                {
                    while (studentNo != studentBatch.get(index) && !exit)                  // ???while loop in order to search linearly for the value - why INDEX used here???
                    {

                    }//while

                }//linearSearch                                // end of Linear Search Code
                */

                studentBatch.add(studentNo);

                scannedStuNo = scannedIDInfo;

                scannedID = true;               // must at least be set to true once to work

                // shows details of last user scanned & keeps a count of number of student ids
                String contents = intent.getStringExtra("SCAN_RESULT");

                if(LecturerUI.serverAvailable)
                {
                    // if internet is available, return forename
                    Log.d("recursive","batch id; " + contents);         // allows programmer to follow progress for testing
                    new returnForename().execute();          // runs background task to retrieve student forename

                } else{


                    Toast confirmScan = Toast.makeText(getApplicationContext(),
                            "scan " + scanCount, Toast.LENGTH_SHORT);

                    confirmScan.show();

                    scanCount++;

                } // toast to notify user of error in scanning of information


                // restarts activity for scanning qr code
                IntentIntegrator repeatScan = new IntentIntegrator(this);
                repeatScan.addExtra("studentNo", 0);
                repeatScan.initiateScan();

            }else if (scanID == 1 && !scanFormat.equals("CODE_128"))            // to determine if scan is not in correct format
            {

                // logcat to view app progress
                Log.e("recursive", "incorrect data for student id scanned" + scanFormat);

                Toast IDIncorrectFormat = Toast.makeText(getApplicationContext(),
                        "format incorrect, please try again...(" + scanFormat + ")", Toast.LENGTH_LONG);
                IDIncorrectFormat.show();

            } else {

                // if no data is returned, the scanner is closed
                // the following code lead to unpredictability during testing by RD - investigate other methods of
                // dealing with data that has been scanned incorrectly
                // handle cancel REMOVE
                // Log.d("recursive","scan finished"); REMOVE
                // finish(); REMOVE

            } // series of else - if statements
        } else {

            // handle cancel
            Log.d("recursive","batch status; complete, returning to check in screen");

        }// if-else to confirm scan data has been received

    }// onActivityResult



    /**
     * method called if no internet connection available to store scanned information onto device
     */

    public void storeScannedInfo()
    {
        String filename = moduleInfo + ".txt";                          // files identified by module code
        String header1 = "<" + moduleInfo + ">";                        // < & > identifiers used to locate module info
        String header2 = "{" + classInfo + "}";                         // { & } used to locate class type

        String currentID;
        String allIds = "[";                                            // adds identifier to signalise the start of IDs

        for (count = 0; count < studentBatch.size(); count++)           // for loop to store each student ID scanned
        {
            currentID = studentBatch.get(count).toString();             // student numbers identified using £ & $
            allIds = allIds + "£" + currentID + "$";

        } // for loop to add each student number to stored file

        allIds = allIds + "]";                                          // adds identifier to signalise the end of IDs

        Log.d("recursive offline", "store student IDs: " + allIds);           // shows all ids to be stored

        String toBeSent = header1 + header2 + allIds;

        Log.d("recursive offline", "contents: " + toBeSent);                // show the file as it is being written

        Log.d("recursive offline", "filename: " + filename);                // show filename assigned

        FileOutputStream outputStream;

        try {
            outputStream = openFileOutput(filename, 0);
            outputStream.write(toBeSent.getBytes());
            outputStream.flush();
            outputStream.close();

        } catch (Exception e) {

            e.printStackTrace();
        } // try - catch

        // success message
        Toast.makeText(getBaseContext(), "file saved successfully",Toast.LENGTH_SHORT).show();

        // returns user to home screen
        Intent sendUserHome = new Intent(getApplicationContext(), MainScreenActivity.class);
        startActivity(sendUserHome);

        // finish this activity
        finish();

    } // StoreScannedInfo


    /**
     * Background Async Task to send information to database
     */
    class LecturerSignStudentIn extends AsyncTask<String, String, String>
    {

        /**
         * shows user a progress dialog box
         * */
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            pDialog = new ProgressDialog(RecursiveSignIn.this);
            pDialog.setMessage("sending info...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        } // onPreExecute

        /**
         * registering the student as present
         * */
        protected String doInBackground(String... args)
        {

            JSONObject json = null;

            for (count = 0; count < studentBatch.size(); count++)
            {
                String student_id = studentBatch.get(count).toString();
                String module_id = moduleInfo;
                String type = classInfo;

                // logcat to view progress of app
                Log.d("recursive", "info sent to database; " + student_id + "," +  module_id + "," + type);

                // parameters to be passed into PHP script on server side
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("student_id", student_id));
                params.add(new BasicNameValuePair("module_id", module_id));
                params.add(new BasicNameValuePair("type", type));

                // getting JSON Object
                json = jsonParser.makeHttpRequest(url_sign_in, "POST", params);

                try
                {
                    Thread.sleep(1500);
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }

                // check log cat for response
                Log.d("recursive", "database response" + json.toString());

            } // for loop for batch progressing multiple students

                try
                {
                    int success = json.getInt(TAG_SUCCESS);

                    if (success == 1)
                    {
                        // check log cat for response
                        Log.d("recursive", "database response; php success");

                        // returns user to home screen
                        Intent signInSuccess = new Intent(getApplicationContext(), MainScreenActivity.class);
                        startActivity(signInSuccess);

                        // finish this activity
                        finish();

                    } else
                    {
                        // failed to sign-in
                        Log.e("recursive", "database response; php error");

                        // error message needed for when sign in is not successful
                        dialogText = "an error has occurred, please try again...";

                        // returns user to home screen
                        Intent signInError = new Intent(getApplicationContext(), MainScreenActivity.class);
                        startActivity(signInError);

                        // finish this activity
                        finish();

                    } // if - else
                } catch (JSONException e)
                {
                    e.printStackTrace();
                }


                return null;
        }// doInBackground

        /**
         * after completing background task dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url)
        {
            // dialog to inform user sign in result
            pDialog.setMessage(dialogText);

            // dismiss the dialog once done
            pDialog.dismiss();

            Toast toast = Toast.makeText(getApplicationContext(),
                    "check in: " + dialogText, Toast.LENGTH_LONG);
            toast.show();

            finish();                   // closes this activity after data has been sent, returns user to home UI


        }// onPostExecute

    }// LecturerSignIntoClass





    /**
     * Background Async Task to retrieve student forename
     */
    class returnForename extends AsyncTask<String, String, String>
    {

        String forename = null;

        /**
         * shows user a progress dialog box
         * */
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

        } // onPreExecute

        /**
         * sending the student ID to database to return student name
         * */
        protected String doInBackground(String... args)
        {

                JSONObject jsonForename = null;

                // logcat to view progress of app
                Log.d("recursive", "info sent to database; " + scannedStuNo);

                // parameters to be passed into PHP script on server side
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("student_id", scannedStuNo));


                // getting JSON Object
                // NB url accepts POST method
                jsonForename = jsonParser.makeHttpRequest(url_return_forename,
                        "POST", params);

                // check log cat for response
                Log.d("recursive", "database response" + jsonForename.toString());

            try
            {
                int success = jsonForename.getInt(TAG_SUCCESS);
                forename = jsonForename.getString(TAG_MESSAGE);

                if (success == 1)
                {
                    // check log cat for response
                    Log.d("recursive", "database response; " + forename);

                } else
                {
                    // failed to find name
                    Log.e("recursive", "database response; php error");

                    // error message needed for when sign in is not successful
                    dialogText = "an error has occurred, please try again...";

                    // returns user to home screen
                    Intent signInError = new Intent(getApplicationContext(), MainScreenActivity.class);
                    startActivity(signInError);

                    // finish this activity
                    finish();

                } // if - else
            } catch (JSONException e)
            {
                e.printStackTrace();
            }

            return null;
        }// doInBackground


        /**
         * after completing background task dismiss the progress dialog and confirm the student name to the lecturer
         * **/
        protected void onPostExecute(String file_url)
        {

            Toast studentName = Toast.makeText(getApplicationContext(),
                    "scan "+ scanCount + ", " + forename, Toast.LENGTH_SHORT);
            studentName.show();

            scanCount++;

        }// onPostExecute

    }// signIntoClass

} // RecursiveSignIn