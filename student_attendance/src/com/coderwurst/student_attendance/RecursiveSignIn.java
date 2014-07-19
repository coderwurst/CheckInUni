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
 * ACTIVITY TO ALLOW LECTURER TO SCAN IN A STUDENT ID AND MODULE QR-CODE SEPERATELY IN ORDER TO CHECK A STUDENT IN
 * ************************
 */

public class RecursiveSignIn extends Activity implements View.OnClickListener

{

    private Button btnGetStudentID;         // button to initiate scanning procedure to store student ID
    private Button btnGetMod;               // button to initiate scanning to store QR-Code information
    private Button btnSignIn;               // button to send information on to server

    private TextView formatTxt, contentTxt;     // text view to inform tester of data captured at this stage
    private int scanID = 0;                     // int to store the type of scan

    // boolean values to ensure the lecturer scans both ID and class codes before attempting to check student in
    private boolean scannedID = false;
    private boolean scannedModule = false;

    // private Strings initiated to null so as information reset upon calling Activity
    private String studentNo = null;
    private String moduleInfo = null;
    private String classInfo = null;

    // url to create new product
    private static String url_sign_in = "http://192.168.1.119/xampp/student_attendance/sign_in.php";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";

    // progress dialog to inform user
    private ProgressDialog pDialog;
    private String dialogText = "success";

    // creates the JSONParser object
    JSONParser jsonParser = new JSONParser();

    // linkedList to store multiple students for batch processing
    private LinkedList <String> studentBatch = new LinkedList<String>();
    private int count;

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
        formatTxt = (TextView) findViewById(R.id.scan_format);
        contentTxt = (TextView) findViewById(R.id.scan_content);

        btnGetStudentID.setOnClickListener(this);
        btnGetMod.setOnClickListener(this);
        btnSignIn.setOnClickListener(this);

    } // onCreate

    @Override
    public void onClick (View view)
    {
        if(view.getId()==R.id.lec_scan_id)
        {
            IntentIntegrator scanIntegrator = new IntentIntegrator(this);
            scanIntegrator.initiateScan();
            scanID = 1;

        } else if (view.getId()==R.id.lec_scan_mod){

            // calls scanner to register new details in system
            IntentIntegrator scanIntegrator = new IntentIntegrator(this);
            scanIntegrator.initiateScan();
            scanID = 2;

        }else {

            if (scannedID == true && scannedModule == true)     // verifies that all info necessary is present
            {

                // for (count = 0; count < studentBatch.size() ; count++)
                // {

                    new LecturerSignStudentIn().execute();          // code to submit details to the database


                // } // for


            } else {

                Toast incompleteData = Toast.makeText(getApplicationContext(),
                        "Please ensure the Student ID and Module Code have both been scanned...", Toast.LENGTH_LONG);
                incompleteData.show();

            } // if - else

        }// if - else-if - else
    }// onClick



    // Returns scanning results for further computation
    public void onActivityResult(int requestCode, int resultCode, Intent intent)
    {

        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);

        // code to process information
        if (scanningResult != null && resultCode == RESULT_OK)                     // to determine if the scan was successful
        {

            String scanContent = scanningResult.getContents();
            String scanFormat = scanningResult.getFormatName();
            formatTxt.setText("FORMAT: " + scanFormat);
            contentTxt.setText("CONTENT: " + scanContent);

            Toast toast = Toast.makeText(getApplicationContext(),
                    "FORMAT: " + scanFormat + "\nCONTENT: " + scanContent, Toast.LENGTH_LONG);
            toast.show();

            /**
             * the following if-else block is implemented at this stage as it is
             * important to determine if the user has scanned in the right type
             * of data for the function he or she has chosen
             */

            if (scanID == 2 && scanFormat.equals("QR_CODE"))                    // "QR_CODE" is a valid QR-Code format
            {
                // store scanned information as the module information
                String scannedQRInfo = scanContent;

                /** extract the necessary information out of this string to be used using special chars {} for module and []
                 * for class type */
                moduleInfo = scannedQRInfo.substring(scannedQRInfo.indexOf("{") + 1, scannedQRInfo.indexOf("}"));

                classInfo = scannedQRInfo.substring(scannedQRInfo.indexOf("[") + 1, scannedQRInfo.indexOf("]"));

                scannedModule = true;


            } else if (scanID == 2 && !scanFormat.equals("QR_CODE"))            // in the event the user does not scan a QR-Code
            {

                Toast QRIncorrectFormat = Toast.makeText(getApplicationContext(),
                        "Format incorrect, please try again..." + scanContent, Toast.LENGTH_LONG);
                QRIncorrectFormat.show();

            } else if (scanID == 1 && scanFormat.equals("CODE_128"))            // "CODE_128" is a valid ID format
            {

                // code to perform device beep to confirm successful scan
                try {
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

                studentBatch.add(studentNo);

                scannedID = true;               // must at least be set to true once to work

                // shows details of last user scanned
                String contents = intent.getStringExtra("SCAN_RESULT");
                Toast.makeText(this, contents , Toast.LENGTH_SHORT).show();

                Log.d("batch status","id scanned"); // allows programmer to follow progress for testing

                // restarts activity for scanning qr code
                IntentIntegrator repeatScan = new IntentIntegrator(this);
                repeatScan.addExtra("studentNo", 0);
                repeatScan.initiateScan();

            }else if (scanID == 1 && !scanFormat.equals("CODE_128"))            // to determine if scan is not in correct format
            {

                Toast IDIncorrectFormat = Toast.makeText(getApplicationContext(),
                        "Valid User ID not scanned, please try again..." + scanContent, Toast.LENGTH_LONG);
                IDIncorrectFormat.show();

            } else {        // NOT NEEDED

                // if no data is returned, the scanner is closed

                // Handle cancel
                Log.d("batch status","scan finished");

                finish();

            } // series of else - if statements
        } else {

            // Handle cancel
            Log.d("batch status","scan finished");

            Toast toast = Toast.makeText(getApplicationContext(),
                    "No scan data received!", Toast.LENGTH_SHORT);
            toast.show();
        }// if-else to confirm scan data has been received


    }// onActivityResult



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

                // parameters to be passed into PHP script on server side
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("student_id", student_id));
                params.add(new BasicNameValuePair("module_id", module_id));
                params.add(new BasicNameValuePair("type", type));

                // getting JSON Object
                // NB url accepts POST method
                json = jsonParser.makeHttpRequest(url_sign_in,
                        "POST", params);

                try
                {
                    Thread.sleep(1500);
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }

                // check log cat for response
                Log.d("Create Response", json.toString());

                // check for success tag

            } // for loop for batch progressing

                try
                {
                    int success = json.getInt(TAG_SUCCESS);

                    if (success == 1)
                    {

                        // returns user to home screen
                        Intent signInSuccess = new Intent(getApplicationContext(), MainScreenActivity.class);
                        startActivity(signInSuccess);

                        // finish this activity
                        finish();

                    } else
                    {

                        // failed to sign-in
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

        }// onPostExecute

    }// signIntoClass


} // RecursiveSignIn
