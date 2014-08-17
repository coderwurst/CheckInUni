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

public class AddStudentMan extends Activity
{

    // fields to store data
    private EditText inputStudentID;
    private EditText inputModuleID;
    // private EditText inputType;

    // strings to hold entered information
    private String sStudentID;
    private String sModuleID;
    private String sClassType;

    // button to sent student info to database
	private Button btnSignIn;

	// progress Dialog
	private ProgressDialog pDialog;
    private String dialogText = "success";

	// JSON parser class
	JSONParser jsonParser = new JSONParser();

	// server IP address
    private static String serverAddress = MainScreenActivity.serverIP;

	// single product url
	private static final String url_man_signin = "http://" + serverAddress + "/xampp/student_attendance/sign_in.php";


	// JSON Node names
	private static final String TAG_SUCCESS = "success";

    // dropdown menu components
    private Spinner dropdown;

    // data previously stored from Choose QR class
    protected static String addModuleID = null;
    protected static String addClassType = null;


    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_student_man);

        Log.d("add student manually", "mod ID: " + addModuleID);


        // logcat tag to view app progress
        Log.d("add student manual", "manual ui open");

        // button to send details to server
		btnSignIn = (Button) findViewById(R.id.add_student_man);

		// confirm details button click event
		btnSignIn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {

                // retrieves current text entered into each field to be used to check if all data has been entered
                sStudentID = inputStudentID.getText().toString();
                sModuleID = inputModuleID.getText().toString();
                sClassType = dropdown.getSelectedItem().toString();

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
			} // onClick
		}); // onClickListener

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

        populateDeviceTypeSpinner();        // call to load the dropdown menu with programmed options

        //inputType = (EditText) findViewById(R.id.type);
        //inputType.setHint("lecture or tutorial");
        // loads options into type list
        // dropdown.setAdapter(adapter);
        //set the default according to value
        // dropdown.setSelection(0);

    } // onCreate

    /**
     * This method is used to create a drop down menu (spinner) to hold the values for the types
     * of class available to the user. The types are coded in the String resource file of the app,
     * and can be changed at a later date. Currently there are the options for 'lecture' and
     * 'tutorial', but this could be extended later to include; 'lad', 'seminar' or others
     **/

    private void populateDeviceTypeSpinner()
    {
        // assigns the spinner object to the element in the xml file
        dropdown = (Spinner)findViewById(R.id.typeSpinner);

        // finds the array storing the information in the values resource file, Strings
        ArrayAdapter<CharSequence> deviceTypeArrayAdapter = ArrayAdapter.createFromResource(this, R.array.classType, android.R.layout.simple_spinner_item);

        // sets the contents of the previously found array to an array adapter
        deviceTypeArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // assigns the contents of the array to the dropdown object
        dropdown.setAdapter(deviceTypeArrayAdapter);

        // if - else block to select previously selected class type info form ChooseQR.java (if any)
        if(addClassType == "lecture")
        {
            dropdown.setSelection(0);
        } else if (addClassType == "tutorial")
        {
            dropdown.setSelection(1);
        } else

        // if no previously saved selection, set the default according to array index (currently lecture)
        dropdown.setSelection(0);

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
            String type = dropdown.getSelectedItem().toString();

            // logcat tag to view app progress
            Log.d("add student manual", "details to be sent: " + student_id + "," + module_id + "," + type);

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
            Log.d("add student manual", "database response; " + json.toString());

            // check for success tag if data has been successfully added to database
            try {
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {

                    // details have been stored and the student is checked in
                    Log.d("add student manual", "check in success");

                    // finish this activity, returns user to home screen
                    finish();

                } else {

                    // failed to sign-in, PHP has returned an error
                    Log.e("add student manual", "php error occurred");

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
