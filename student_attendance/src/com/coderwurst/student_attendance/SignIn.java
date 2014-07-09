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

    public static final String USER_ID = "User ID File";

    static SharedPreferences userDetails;

    // Strings to hold data elements
    String allInfo = "";
    String moduleInfo = "";
    String userID = "";
    String classInfo = "";

	// progress dialog to inform user
	private ProgressDialog pDialog;

	JSONParser jsonParser = new JSONParser();
	EditText inputStudentID;
	EditText inputModuleID;
	EditText inputType;

	// url to create new product
	private static String url_sign_in = "http://172.17.50.167/xampp/student_attendance/sign_in.php";

	// JSON Node names
	private static final String TAG_SUCCESS = "success";

	@Override
	public void onCreate(Bundle savedInstanceState)
    {
        // retrieves the shared preferences file containing the user ID
        userDetails = getSharedPreferences(USER_ID, 0);
        userID = userDetails.getString("user_ID", "default");
        Log.d(userID, "middle ID value");          // logcat tag to view contents of string at this stage (testing purposes only)


        /* code if the user ID is not in the database to inform them of an error or
           not saved to take the user to the register screen */

        // opens up sign-in confirmation screen
        super.onCreate(savedInstanceState);
		setContentView(R.layout.sign_in);

        // unpack the data scanned into the app
        Bundle bundle = getIntent().getExtras();
        allInfo = bundle.getString("Info");

        /** extract the necessary information out of this string to be used using special chars {} for module and []
        * for class type */
        moduleInfo = allInfo.substring(allInfo.indexOf("{") + 1, allInfo.indexOf("}"));
        classInfo = allInfo.substring(allInfo.indexOf("[") + 1, allInfo.indexOf("]"));

        Log.d(userID, "end ID value");          // logcat tag to view contents of string at this stage (testing purposes only)


		// edit text in input boxes for comparison before being sent
		inputStudentID = (EditText) findViewById(R.id.student_id);
        inputStudentID.setText(userID);
		inputModuleID = (EditText) findViewById(R.id.module_id);
        inputModuleID.setText(moduleInfo);
		inputType = (EditText) findViewById(R.id.type);
        inputType.setText(classInfo);

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

			String student_id = inputStudentID.getText().toString();
			String module_id = inputModuleID.getText().toString();
			String type = inputType.getText().toString();

			// parameters to be passed into PHP script on server side
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("student_id", student_id));
			params.add(new BasicNameValuePair("module_id", module_id));
			params.add(new BasicNameValuePair("type", type));

			// getting JSON Object
			// NB url accepts POST method
			JSONObject json = jsonParser.makeHttpRequest(url_sign_in,
					"POST", params);

			// check log cat for response
			Log.d("Create Response", json.toString());

			// check for success tag
			try {
				int success = json.getInt(TAG_SUCCESS);

				if (success == 1) {

                    // successfully created product
					Intent i = new Intent(getApplicationContext(), MainScreenActivity.class);
					startActivity(i);

					// closing this screen
					finish();
				} else {
					// failed to create product
				}
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
            pDialog.setMessage("success!");
			// dismiss the dialog once done
			pDialog.dismiss();
		}// onPostExecute

	}// createNewProduct
}// SignIn
