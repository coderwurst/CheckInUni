package com.coderwurst.student_attendance;

import java.util.ArrayList;
import java.util.List;

import android.widget.*;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;


/**
 * ***********************
 * Created by IntelliJ IDEA
 * User: andrew
 * Date: 17/07/2014
 * Time: 15:08
 * Version: V7.0
 * SPRINT 7 - TO ALLOW LECTURER TO MANUALLY ENTER STUDENT DETAILS AND SEND TO DATABASE
 * ************************
 */

public class AddStudentMan extends Activity implements View.OnClickListener
{

    // fields to store data
    private EditText inputStudentID;
    private EditText inputModuleID;

    // strings to hold entered information
    private String sStudentID;
    private String sModuleID;
    private String sClassType;
    private boolean sLecture = false;
    private boolean sTutorial= false;

    // button to sent student info to database
	private Button btnSignIn;

	// progress Dialog
	private ProgressDialog pDialog;
    private String dialogText = "success";

	// JSON parser class
	private JSONParser jsonParser = new JSONParser();

	// server IP address
    private static String serverAddress = MainScreenActivity.serverIP;

	// single product url
	private static final String url_man_signin = "http://" + serverAddress + "/xampp/student_attendance/sign_in.php";

	// JSON Node names
	private static final String TAG_SUCCESS = "success";

    // dropdown menu components
    private Button btnTutorial;
    private Button btnLecture;

    // data previously stored from Choose QR class
    protected static String addModuleID = null;
    protected static String addClassType = null;

    // tags for log statements
    private static final String TAG = "add manual";


    @Override
	public void onCreate(Bundle savedInstanceState)
    {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_student_man);

        Log.d(TAG, "mod ID: " + addModuleID);

        // logcat tag to view app progress
        Log.d(TAG, "manual ui open");

        // button to send details to server
		btnSignIn = (Button) findViewById(R.id.add_student_man);

        // button to determine if class is a lecture or tutorial
        btnLecture = (Button)findViewById(R.id.chooseLecture);
        btnTutorial = (Button)findViewById(R.id.chooseTutorial);

        // confirm details button click event
        btnSignIn.setOnClickListener(this);
        btnLecture.setOnClickListener(this);
        btnTutorial.setOnClickListener(this);

        // text fields to be updated by user, until then an example input is shown
        inputStudentID = (EditText) findViewById(R.id.student_id);
        inputStudentID.setHint("eg. B000000");
        inputModuleID = (EditText) findViewById(R.id.module_id);

        if (addModuleID == null)
        {
            inputModuleID.setHint("eg. ABC000");

        } else      // if previous module has been stored, entered automatically into text field
        {
            inputModuleID.setText(addModuleID);
        } // if - else

        setClassType();

    } // onCreate

    /**
     * the onCLick method in this class controls the appearance for the lecture and tutorial choice buttons
     * as well as a check to ensure all information has been entered before sending to the database.
     * It was decided during Phase 3 of testing to remove a Spinner object for the choice between lecture
     * and tutorial - as with only 2 options a drop down list was not required.
     */

    @Override
    public void onClick(View view)
    {
        if(view.getId() == R.id.chooseLecture)
        {
            btnLecture.setBackgroundResource(R.drawable.lecture_small_enabled);
            btnTutorial.setBackgroundResource(R.drawable.tutorial_small);

            sLecture = true;
            sTutorial = false;

            Log.d(TAG, "user select: lecture");

        } else if (view.getId() == R.id.chooseTutorial)
        {
            btnTutorial.setBackgroundResource(R.drawable.tutorial_small_enabled);
            btnLecture.setBackgroundResource(R.drawable.lecture_small);

            sTutorial = true;
            sLecture = false;

            Log.d(TAG, "user select: tutorial");

        } else if (view.getId() == R.id.add_student_man)
        {

            // retrieves current text entered into each field to be used to check if all data has been entered
            sStudentID = inputStudentID.getText().toString();
            sModuleID = inputModuleID.getText().toString();

            if (sLecture)
            {
                sClassType = "lecture";
            } else if (sTutorial)
            {
                sClassType = "tutorial";
            } else
            {
                sClassType = null;
            } // if - else - if - else

            if (sStudentID.isEmpty() || sModuleID.isEmpty() || sClassType.isEmpty())
            {
                // informs the user that at least one piece of information has not yet been entered
                Toast errorToast = Toast.makeText(getApplicationContext(),
                        "please check that all details have been entered and try again", Toast.LENGTH_LONG);

                errorToast.show();

            } else
            {
                // starting background task check student in
                new SignStudentIn().execute();

            } // if - else to prevent an incomplete record being stored on the database
        } // if - else - if for choosing onClick functions

    } // onClick


    /**
     * This method is used to create a drop down menu (spinner) to hold the values for the types
     * of class available to the user. The types are coded in the String resource file of the app,
     * and can be changed at a later date. Currently there are the options for 'lecture' and
     * 'tutorial', but this could be extended later to include; 'lad', 'seminar' or others
     **/

    private void setClassType()
    {
        // if - else block to select previously selected class type info form ChooseQR.java (if any)
        if(addClassType == "lecture")
        {
            btnLecture.setBackgroundResource(R.drawable.lecture_small_enabled);
            btnTutorial.setBackgroundResource(R.drawable.tutorial_small);

            sLecture = true;
            sTutorial = false;

            Log.d(TAG, "saved class type: lecture");

        } else if (addClassType == "tutorial")
        {
            btnTutorial.setBackgroundResource(R.drawable.tutorial_small_enabled);
            btnLecture.setBackgroundResource(R.drawable.lecture_small);

            sTutorial = true;
            sLecture = false;

            Log.d(TAG, "saved class type: tutorial");

        } else
        {
            // if no previously saved selection, both booleans set to false
            sLecture = false;
            sTutorial = false;

            Log.d(TAG, "no saved class type");
        } // if - else - if - else

    } // populateDeviceSpinner



	/**
    * Background Async Task to Add a student manually
    **/

     class SignStudentIn extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog to inform
         * user that the app is processing information.
         **/

         @Override
        protected void onPreExecute()
        {
            // instantiates the process, whilst showing the user a process dialog box
            super.onPreExecute();
            pDialog = new ProgressDialog(AddStudentMan.this);
            pDialog.setMessage("submitting student details...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();

        } // onPreExecute

        /**
         * This doInBackground method completes the work involved in contacting the
         * server to register the student details as entered by the lecturer
         **/
        protected String doInBackground(String... args)
        {

            // data captured from input fields stored in strings
            String student_id = inputStudentID.getText().toString();
            String module_id = inputModuleID.getText().toString();
            String type;

            if(sLecture)
            {
                type = "lecture";
            } else
            {
                type = "tutorial";
            } // if -else


            // logcat tag to view app progress
            Log.d(TAG, "details to be sent: " + student_id + "," + module_id + "," + type);

            // parameters to be passed into PHP script on server side
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("student_id", student_id));
            params.add(new BasicNameValuePair("module_id", module_id));
            params.add(new BasicNameValuePair("type", type));

            // getting JSON Object
            // NB url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(url_man_signin,
                    "POST", params);

            // check log cat for response
            Log.d(TAG, "database response; " + json.toString());

            // check for success tag if data has been successfully added to database
            try {
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {

                    // details have been stored and the student is checked in
                    Log.d(TAG, "check in success");

                    // finish this activity, returns user to home screen
                    finish();

                } else {

                    // failed to sign-in, PHP has returned an error
                    Log.e(TAG, "php error occurred");

                    // error message needed for when sign in is not successful
                    dialogText = "an error has occurred, please try again later...";

                    // finish this activity, returns user to home screen
                    finish();

                } // if - else
            } catch (JSONException e) {
                e.printStackTrace();
            } // try - catch

            return null;
        }// doInBackground


        /**
         * After completing background task the progress dialog can be dismissed, and the user
         * is informed using a toast message if the process has or has not been successful
         **/

        protected void onPostExecute(String file_url) {

            // dialog to inform user sign in result
            pDialog.setMessage(dialogText);
            pDialog.dismiss();

            Toast toast = Toast.makeText(getApplicationContext(),
                    "check in: " + dialogText, Toast.LENGTH_LONG);
            toast.show();

        } // onPostExecute
    } // SignInStudent


} // AddStudentMan
