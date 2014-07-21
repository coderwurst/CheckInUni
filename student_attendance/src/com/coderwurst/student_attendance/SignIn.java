package com.coderwurst.student_attendance;

import android.app.Activity;
import android.content.SharedPreferences;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
 * Date: 07/07/2014
 * Time: 15:04
 * Version: V1.0
 * SPRINT 4 - TO TAKE CAPTURED DATA FROM QR-CODE AND SEND TO DATABASE
 * ************************
 */


public class SignIn extends Activity
{

    public static final String USER_ID = "User ID File";        // string to hold the user id information

    static SharedPreferences userDetails;                       // sharedPreferences file for user ID

    // Strings to hold data elements
    String allInfo = "";
    String moduleInfo = "";
    String userID = "";
    String classInfo = "";

	// progress dialog to inform user of events
	private ProgressDialog pDialog;
    private String dialogText = "success";

    // creates the JSONParser object
	JSONParser jsonParser = new JSONParser();

    // fields to store data - for testing purposes only
	EditText inputStudentID;
	EditText inputModuleID;
	EditText inputType;

	// url to create new product, NB I.P. address not static and needs to be changed depending on IP address of server
	private static String url_sign_in = "http://172.17.1.113/xampp/student_attendance/sign_in.php";

	// JSON Node names
	private static final String TAG_SUCCESS = "success";

	@Override
	public void onCreate(Bundle savedInstanceState)
    {
        // retrieves the shared preferences file containing the user ID
        userDetails = getSharedPreferences(USER_ID, 0);
        userID = userDetails.getString("user_ID", "default");

        // opens up sign-in confirmation screen
        super.onCreate(savedInstanceState);
		setContentView(R.layout.sign_in);

        // unpack the data scanned into the app
        Bundle bundle = getIntent().getExtras();
        allInfo = bundle.getString("Info");

        // logcat tag to view contents of string at this stage (testing purposes only)
        Log.d("check in", "user ID value; " + userID);
        Log.d("check in", "module ID value; " + allInfo);

        /** extract the necessary information out of this string to be used using special chars {} for module and []
        * for class type. Values taken from index position +1 as indexes begin at position 0 */
        moduleInfo = allInfo.substring(allInfo.indexOf("{") + 1, allInfo.indexOf("}"));
        classInfo = allInfo.substring(allInfo.indexOf("[") + 1, allInfo.indexOf("]"));

        // edit text in input boxes for comparison before being sent    FOR TESTING PURPOSES ONLY, TO BE REMOVED
		inputStudentID = (EditText) findViewById(R.id.student_id);
        inputStudentID.setText(userID);
		inputModuleID = (EditText) findViewById(R.id.module_id);
        inputModuleID.setText(moduleInfo);
		inputType = (EditText) findViewById(R.id.type);
        inputType.setText(classInfo);

        // logcat tag to view contents of string at this stage (testing purposes only)
        Log.d("check in", "module ID info; " + moduleInfo);
        Log.d("check in", "class type; " + classInfo);

		// button to confirm input and send to database
		Button btnCreateProduct = (Button) findViewById(R.id.btnCreateProduct);

		// button click event
		btnCreateProduct.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view)
            {
				// creating new product in background thread
				new SignIntoClass().execute();
			}// onClick
		});

	}// onCreate


	/**
	 * Background Async Task to send information to database
	 */
	class SignIntoClass extends AsyncTask<String, String, String>
    {

		/**
		 * shows user a progress dialog box
		 * */
		@Override
		protected void onPreExecute()
        {
			super.onPreExecute();
			pDialog = new ProgressDialog(SignIn.this);
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

			// Strings to take contents of TextViews, to be changed when implementing automatic transition
            String student_id = inputStudentID.getText().toString();
			String module_id = inputModuleID.getText().toString();
			String type = inputType.getText().toString();

			// parameters to be passed into PHP script on server side
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("student_id", student_id));
			params.add(new BasicNameValuePair("module_id", module_id));
			params.add(new BasicNameValuePair("type", type));

			// getting JSON Object NB url accepts POST method
			JSONObject json = jsonParser.makeHttpRequest(url_sign_in,
					"POST", params);

			// check log cat for response
			Log.d("check in", "database response; " + json.toString());

			// check for success tag
			try {
				int success = json.getInt(TAG_SUCCESS);

				if (success == 1) {             // if to determine if the PHP script has returned a success message (1)

                    // details have been stored and the student is checked in
                    Log.d("check in", "check in success");

                    // returns user to home screen
					Intent signInSuccess = new Intent(getApplicationContext(), MainScreenActivity.class);
					startActivity(signInSuccess);

                    // finish this activity
                    finish();

				} else {

					// failed to sign-in, PHP has returned an error
                    Log.e("check in", "php error occurred");

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


}// SignIn
