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
import android.content.Intent;
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

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_student_man);

        // logcat tag to view app progress
        Log.d("add student manual", "manual ui open");

        // button to send details to server
		btnSignIn = (Button) findViewById(R.id.add_student_man);

		// confirm details button click event
		btnSignIn.setOnClickListener(new View.OnClickListener() {


			@Override
			public void onClick(View arg0) {

                sStudentID = inputStudentID.getText().toString();
                sModuleID = inputModuleID.getText().toString();
                sClassType = dropdown.getSelectedItem().toString();

                if (sStudentID.isEmpty() || sModuleID.isEmpty() || sClassType.isEmpty())
                {
                    Toast errorToast = Toast.makeText(getApplicationContext(),
                            "please check that all details have been entered and try again", Toast.LENGTH_LONG);

                    errorToast.show();

                } else
                {

                    // starting background task to update product
                    new SignStudentIn().execute();

                } // if - else to prevent an incomplete record being stored on the database
			} // onClick
		}); // onClickListener

        // edit text in input boxes for comparison before being sent
        inputStudentID = (EditText) findViewById(R.id.student_id);
        inputStudentID.setHint("eg. B000000");
        inputModuleID = (EditText) findViewById(R.id.module_id);
        inputModuleID.setHint("eg. ABC000");
        //inputType = (EditText) findViewById(R.id.type);
        //inputType.setHint("lecture or tutorial");
        // loads options into type list
        // dropdown.setAdapter(adapter);
        //set the default according to value
        // dropdown.setSelection(0);

        populateDeviceTypeSpinner();

    } // onCreate



    private void populateDeviceTypeSpinner()
    {

        // dropdown menu components
        dropdown = (Spinner)findViewById(R.id.typeSpinner);

        ArrayAdapter<CharSequence> deviceTypeArrayAdapter = ArrayAdapter.createFromResource(this, R.array.classType, android.R.layout.simple_spinner_item);

        deviceTypeArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        dropdown.setAdapter(deviceTypeArrayAdapter);

        //set the default according to value
        dropdown.setSelection(0);
    } // populateDeviceSpinner



	/**
    * Background Async Task to Add a student manually
    * */
    class SignStudentIn extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            pDialog = new ProgressDialog(AddStudentMan.this);
            pDialog.setMessage("submitting Details...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();

        } // onPreExecute

        /**
         * registering the student as present
         * */
        protected String doInBackground(String... args)
        {

            // Data captured from input fields
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
            // check log cat for response
            Log.d("add student manual", "database response; " + json.toString());

            // check for success tag
            try {
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {

                    // details have been stored and the student is checked in
                    Log.d("add student manual", "check in success");

                    // returns user to home screen
                    Intent signInSuccess = new Intent(getApplicationContext(), MainScreenActivity.class);
                    startActivity(signInSuccess);

                    // finish this activity
                    finish();

                } else {

                    // failed to sign-in, PHP has returned an error
                    Log.e("add student manual", "php error occurred");

                    // error message needed for when sign in is not successful
                    dialogText = "an error has occurred, please try again...";

                    // returns user to home screen
                    Intent signInError = new Intent(getApplicationContext(), MainScreenActivity.class);
                    startActivity(signInError);

                    // finish this activity
                    finish();

                } // if - else
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }// doInBackground


        /**
         * After completing background task Dismiss the progress dialog
         * **/
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
