package com.example.app_stage2;

import android.app.Activity;
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
 * Date: 26/06/2014
 * Time: 15:04
 * Version: V1.0
 * ADD CLASS INFORMATION HERE
 * ************************
 */


public class SignIn extends Activity
{

    // String to hold captured data
    String scanInfo = "";

	// Progress Dialog
	private ProgressDialog pDialog;

	JSONParser jsonParser = new JSONParser();
	EditText inputStudentID;
	EditText inputModuleID;
	EditText inputType;

	// url to create new product
	private static String url_sign_in = "http://172.17.45.96/xampp/student_attendance/sign_in.php";

	// JSON Node names
	private static final String TAG_SUCCESS = "success";

	@Override
	public void onCreate(Bundle savedInstanceState)
    {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sign_in);

        //unpack the data scanned into the app
        Bundle bundle = getIntent().getExtras();
        scanInfo = bundle.getString("Info");

		// Edit Text
		inputStudentID = (EditText) findViewById(R.id.student_id);
		inputModuleID = (EditText) findViewById(R.id.module_id);
        inputModuleID.setText(scanInfo);
		inputType = (EditText) findViewById(R.id.type);

		// Create button
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
	 * Background Async Task to Create new product
	 * */
	class SignIntoClass extends AsyncTask<String, String, String>
    {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(SignIn.this);
			pDialog.setMessage("Signing in...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}

		/**
		 * Registering the Student as Present
		 * */
		protected String doInBackground(String... args)
        {


			String student_id = inputStudentID.getText().toString();
			String module_id = inputModuleID.getText().toString();
			String type = inputType.getText().toString();

			// Building Parameters
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("student_id", student_id));
			params.add(new BasicNameValuePair("module_id", module_id));
			params.add(new BasicNameValuePair("type", type));

			// getting JSON Object
			// Note that create product url accepts POST method
			JSONObject json = jsonParser.makeHttpRequest(url_sign_in,
					"POST", params);
			
			// check log cat fro response
			Log.d("Create Response", json.toString());

			// check for success tag
			try {
				int success = json.getInt(TAG_SUCCESS);

				if (success == 1) {
					// successfully created product
					Intent i = new Intent(getApplicationContext(), AllProductsActivity.class);
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
		}

		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		protected void onPostExecute(String file_url) {
			// dismiss the dialog once done
			pDialog.dismiss();
		}

	}// createNewProduct
}//
