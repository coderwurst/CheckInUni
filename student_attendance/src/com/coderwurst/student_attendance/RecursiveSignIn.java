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
import android.widget.TextView;
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
    private TextView savedMod;

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

    // url addresses to check a student into a class, and return the student forename upon scanning
    private static String url_sign_in = "http://" + serverAddress + "/xampp/student_attendance/sign_in.php";
    private static String url_return_forename = "http://" + serverAddress + "/xampp/student_attendance/return_forename.php";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    // progress dialog to inform user
    private ProgressDialog pDialog;
    private String dialogText = "success";

    // creates the JSONParser object
    private JSONParser jsonParser = new JSONParser();

    // linkedList to store multiple students for batch processing
    private LinkedList <String> studentBatch = new LinkedList<String>();
    private int count;

    // String to store student number to be used in retrieving student forename
    private String scannedStuNo;
    private int scanCount = 1;

    // data previously stored from Choose QR class
    protected static String recModuleID = null;
    protected static String recClassType = null;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recursive_signin);

        // buttons to access recursive functionality
        btnGetStudentID = (Button) findViewById(R.id.lec_scan_id);
        btnGetMod = (Button) findViewById(R.id.lec_scan_mod);
        btnSignIn = (Button) findViewById(R.id.add_student_auto);

        // TextViews for hold format and content info for testing purposes
        // formatTxt = (TextView) findViewById(R.id.scan_format);
        // contentTxt = (TextView) findViewById(R.id.scan_content);

        // textview to hold current selected class info
        savedMod= (TextView)findViewById(R.id.rec_savedModule);

        // onClickListeners for all 3 buttons
        btnGetStudentID.setOnClickListener(this);
        btnGetMod.setOnClickListener(this);
        btnSignIn.setOnClickListener(this);

        /**
         * in the event that the lecturer has not previously selected a module code and class type from the find QR
         * screen, they will be prompted to scan a module code upon opening this activity. Otherwise the previously
         * selected class details will be presented to them on screen
         **/

        if (recModuleID == null)
        {
            // show scan QR button
            savedMod.setText("scan QR Code");

        } else      // if previous module has been stored, entered automatically into text field
        {
            savedMod.setText(recModuleID + ", " + recClassType);
            moduleInfo = recModuleID;
            classInfo = recClassType;
            scannedModule = true;
            // make scan QR button invisible, instead set a textview to hold value
        } // if - else

    } // onCreate

    /**
     * if - else block in onClick method to ensure that the lecturer enters all the information necessary before
     * attempting to send the information to the database
     */

    @Override
    public void onClick (View view)
    {
        // outer if-statement to determine (from UI Activity) if a server connection is available
        if(MainScreenActivity.serverAvailable)
        {
            if (view.getId() == R.id.lec_scan_id)
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

                /**
                 * internet connection available, checks are made to ensure that the user has scanned
                 * both a student ID and a module ID before sending the information to the database
                 */

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
             * internet connection not available, the user can still scan a module ID and student ID(s)
             * however upon pressing the check-in button the method is called to store the data on the
             * device as opposed to sending it to the server
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
                    // toast to inform user that the info will be scanned to the device
                    Toast noConnection = Toast.makeText(getApplicationContext(),
                            "server connection not available, storing information", Toast.LENGTH_LONG);
                    noConnection.show();

                    // logcat to view app progress
                    Log.d("recursive offline", "scanned details to be stored to device");

                    // call method to store scanned details to device
                    storeScannedInfo();         // calls method to store scanned details to device

                } else      // the necessary details have not been entered
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


    /**
     * onactivityResult allows the user to scan multiple students, and keeps a count of the number of users scanned
     * as well as presenting the name of student on screen after a scan (when Internet available). The user must exit
     * this mode by pressing back on the device hardware
     */

    // Returns scanning results for further computation
    public void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        // scanning result
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

                savedMod.setText(moduleInfo + ", " + classInfo);


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

                Log.d("recursive", scannedIDInfo);

                studentNo = scannedIDInfo;      // CHANGE TO STORE THE SCANNED ID AS AN ADDITION TO A LINKED LIST

                /* linear search to eliminate multiple IDs being scanned

                int index = 0, comparison = 1;                            // index int for search position, comparison to count number of times linear search performed
                boolean exit = false;                                     // boolean to show if value has been found

                if (studentBatch != null)
                {
                    while (studentNo != studentBatch.get(index) && !exit)                  // ???while loop in order to search linearly for the value - why INDEX used here???
                    {

                    }//while

                }//linearSearch                             // end of Linear Search Code
                */

                studentBatch.add(studentNo);                // adds the student number to a linkedList

                scannedStuNo = scannedIDInfo;

                scannedID = true;               // must at least be set to true once to work

                // shows details of last user scanned & keeps a count of number of student ids
                String contents = intent.getStringExtra("SCAN_RESULT");

                // if internet is available, return forename
                if(MainScreenActivity.serverAvailable)
                {
                    Log.d("recursive","batch id; " + contents);  // allows programmer to follow progress for testing
                    new returnForename().execute();             // runs background task to retrieve student forename

                } else {

                    // if a server connection cannot be established, the scan simply returns a count
                    Toast confirmScan = Toast.makeText(getApplicationContext(),
                            "scan " + scanCount, Toast.LENGTH_SHORT);

                    confirmScan.show();
                    scanCount++;        // increments value for next scan

                } // toast to notify user of error in scanning of information


                // restarts activity for scanning qr code
                IntentIntegrator repeatScan = new IntentIntegrator(this);
                repeatScan.addExtra("studentNo", 0);
                repeatScan.initiateScan();

            }else if (scanID == 1 && !scanFormat.equals("CODE_128"))    // to determine if scan is not in correct format
            {

                // logcat to view app progress
                Log.e("recursive", "incorrect data for student id scanned" + scanFormat);

                // informs user of an errornous scan
                Toast IDIncorrectFormat = Toast.makeText(getApplicationContext(),
                        "format incorrect, please try again...(" + scanFormat + ")", Toast.LENGTH_LONG);

                IDIncorrectFormat.show();

            } else {

                Log.e("recirsive", "scan incomplete");

                // in the event that the image has not been successfully scanned, informs user to try again
                Toast errorScan = Toast.makeText(getApplicationContext(),
                        "scan incomplete, please try again", Toast.LENGTH_SHORT);

                errorScan.show();

            } // series of else - if statements
        } else {

            // handle cancel
            Log.d("recursive","batch status; complete, returning to check in screen");

        }// if-else to confirm scan data has been received

    }// onActivityResult



    /**
     * method called if no internet connection available to store scanned information onto device. Info
     * stored in local device memory assigned to app, and formatted to allow for effective recall of
     * information upon reading the file back into the app before sending to database
     */

    public void storeScannedInfo()
    {
        // strings to concatenate information for formatting
        String filename = moduleInfo + ".txt";                          // files identified by module code
        String header1 = "<" + moduleInfo + ">";                        // < & > identifiers used to locate module info
        String header2 = "{" + classInfo + "}";                         // { & } used to locate class type

        String currentID;                                               // string to hold the current ID
        String allIds = "[";                                            // adds identifier to signalise the start of IDs

        for (count = 0; count < studentBatch.size(); count++)           // for loop to store each student ID scanned
        {
            currentID = studentBatch.get(count).toString();             // student numbers identified using £ & $
            allIds = allIds + "£" + currentID + "$";

        } // for loop to add each student number to stored file

        allIds = allIds + "]";                                          // adds identifier to signalise the end of IDs

        Log.d("recursive offline", "store student IDs: " + allIds);     // shows all ids to be stored

        String toBeSent = header1 + header2 + allIds;                   // string to store all information concatentated

        Log.d("recursive offline", "contents: " + toBeSent);            // show the file as it is being written

        Log.d("recursive offline", "filename: " + filename);            // show filename assigned

        FileOutputStream outputStream;                                  // new fileOutputStream to save file to device

        try {
            outputStream = openFileOutput(filename, 0);                 // opens a new stream
            outputStream.write(toBeSent.getBytes());                    // converts information to be sent to byte format
            outputStream.flush();                                       // flush to remove any lingering contents
            outputStream.close();                                       // closes stream to finish the process

        } catch (Exception e) {

            e.printStackTrace();
        } // try - catch

        // success message
        Toast.makeText(getBaseContext(), "file saved successfully",Toast.LENGTH_SHORT).show();

        // finish this activity
        finish();

    } // StoreScannedInfo


    /**
     * Background Async Task to send information to database
     */
    class LecturerSignStudentIn extends AsyncTask<String, String, String>
    {

        /**
         * Before starting background thread Show Progress Dialog to inform
         * user that the app is processing information.
         **/

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
         * This doInBackground method completes the work involved in contacting the
         * server to register the student and module details input by lecturer
         **/

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

                /**
                 *  try - catch implements a delay as during development it was found that greater
                 *  numbers of students being processed lead to app crashing after first 3 pieces
                 *  of student information sent to database
                 **/

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

                        // finish this activity
                        finish();

                    } else
                    {
                        // failed to sign-in
                        Log.e("recursive", "database response; php error");

                        // error message needed for when sign in is not successful
                        dialogText = "an error has occurred, please try again...";

                        // finish this activity
                        finish();

                    } // if - else
                } catch (JSONException e)
                {
                    e.printStackTrace();
                } // try  -catch

                return null;
        }// doInBackground

        /**
         * After completing background task the progress dialog can be dismissed, and the user
         * is informed using a toast message if the process has or has not been successful
         **/

        protected void onPostExecute(String file_url)
        {
            // dialog to inform user sign in result
            pDialog.setMessage(dialogText);

            // dismiss the dialog once done
            pDialog.dismiss();

            // informs the user of a success or an error
            Toast toast = Toast.makeText(getApplicationContext(),
                    "check in: " + dialogText, Toast.LENGTH_LONG);
            toast.show();

            finish();                   // closes this activity after data has been sent, returns user to home UI


        }// onPostExecute

    }// LecturerSignIntoClass


     /**
     * Background Async Task to retrieve student forename from database each time a lecturer scans
     * in a student ID
     **/

    class returnForename extends AsyncTask<String, String, String>
    {

        String forename = null;

        /**
         * Before starting background thread there is no need to use a dialog box;
         * as the operation is processed so quickly there is no notable change in performance
         **/

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

        } // onPreExecute

        /**
         * Background task to send the student ID to database to return student name
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

                    // returns user to home screen removed 16.08.14
                    //Intent signInError = new Intent(getApplicationContext(), MainScreenActivity.class);
                    //startActivity(signInError);

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
         * after completing background task
         * confirm the returned student name to the lecturer
         * **/
        protected void onPostExecute(String file_url)
        {
            // toast to return the scan number and student forename
            Toast studentName = Toast.makeText(getApplicationContext(),
                    "scan "+ scanCount + ", " + forename, Toast.LENGTH_SHORT);
            studentName.show();

            scanCount++;            // increments the count for the next student

        }// onPostExecute

    }// signIntoClass

} // RecursiveSignIn