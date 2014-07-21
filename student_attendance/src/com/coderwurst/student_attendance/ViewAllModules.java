package com.coderwurst.student_attendance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.widget.*;
import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;


/**
 * ***********************
 * Created by IntelliJ IDEA
 * User: andrew
 * Date: 26/06/2014
 * Time: 14:57
 * Version: V1.0
 * ADD CLASS INFORMATION HERE
 * ************************
 */
public class ViewAllModules extends ListActivity
{

	// Progress Dialog
	private ProgressDialog pDialog;

	// Creating JSON Parser object
	JSONParser jParser = new JSONParser();

	ArrayList<HashMap<String, String>> moduleList;

	// url to get all modules list
    private static String url_all_modules = "http://172.17.1.113/xampp/student_attendance/get_all_modules.php";
    private static String url_location = "http://172.17.1.113/module_codes/";

	// JSON Node names
	private static final String TAG_SUCCESS = "success";
	private static final String TAG_MODULES = "modules";
	private static final String TAG_ID = "id";
	private static final String TAG_NAME = "name";
    private static final String TAG_LECTURE = "lectureUrl";
    private static final String TAG_TUTORIAL = "tutorialUrl";

	// modules JSONArray
	JSONArray modules = null;

	@Override
	public void onCreate(Bundle savedInstanceState)
    {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_all_modules);

		// Hashmap for ListView
		moduleList = new ArrayList<HashMap<String, String>>();

		// Loading products in Background Thread
		new LoadAllModules().execute();

		// Get listview
		ListView lv = getListView();

		// on seleting single product
		// launching Edit Product Screen
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

                // code to return image to App and display on screen

				// getting values from selected ListItem
				String pid = ((TextView) view.findViewById(R.id.pid)).getText()
                        .toString();
                String lectureFile = ((TextView) view.findViewById(R.id.lecture_url)).getText()
                        .toString();
                String tutorialFile = ((TextView) view.findViewById(R.id.tutorial_url)).getText()
                        .toString();

                Log.d("view modules", "stored address; " + pid + "," + lectureFile + tutorialFile);

                String lectureUrl = url_location + lectureFile;
                String tutorialUrl = url_location + tutorialFile;

                Log.d("view modules", "actual address; " + lectureUrl + "," + tutorialUrl);

				// Starting new intent
				Intent in = new Intent(getApplicationContext(),	ChooseQR.class);

				// sending pid to next activity
				in.putExtra(TAG_ID, pid);
                in.putExtra(TAG_LECTURE, lectureUrl);
                in.putExtra(TAG_TUTORIAL, tutorialUrl);
				
				// starting new activity and expecting some response back
				startActivityForResult(in, 100);
			}
		});

	}// onCreate

	// Response from Edit Product Activity
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
		super.onActivityResult(requestCode, resultCode, data);
		// if result code 100

		if (resultCode == 100) {
			// if result code 100 is received 
			// means user edited/deleted product
			// reload this screen again
			Intent intent = getIntent();
			finish();
			startActivity(intent);
		}

	}// onActivityResult

	/**
	 * Background Async Task to Load all product by making HTTP Request
	 * */
	class LoadAllModules extends AsyncTask<String, String, String>
    {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(ViewAllModules.this);
			pDialog.setMessage("Loading modules. Please wait...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}

		/**
		 * getting All modules from url
		 * */
		protected String doInBackground(String... args) {
			// Building Parameters
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			// getting JSON string from URL
			JSONObject json = jParser.makeHttpRequest(url_all_modules, "GET", params);

			
			// Check your log cat for JSON reponse
			Log.d("view modules", "all modules; " + json.toString());

			try {
				// Checking for SUCCESS TAG
				int success = json.getInt(TAG_SUCCESS);

				if (success == 1) {
					// modules found
					// Getting Array of Products
					modules = json.getJSONArray(TAG_MODULES);

					// looping through All Products
					for (int i = 0; i < modules.length(); i++) {
						JSONObject c = modules.getJSONObject(i);

						// Storing each json item in variable
						String id = c.getString(TAG_ID);
                        String lecture = c.getString(TAG_LECTURE);
                        String tutorial = c.getString(TAG_TUTORIAL);
                        String name = c.getString(TAG_NAME);


						// creating new HashMap
						HashMap<String, String> map = new HashMap<String, String>();

						// adding each child node to HashMap key => value
						map.put(TAG_ID, id);
						map.put(TAG_NAME, name);
                        map.put(TAG_LECTURE, lecture);
                        map.put(TAG_TUTORIAL, tutorial);

						// adding HashList to ArrayList
						moduleList.add(map);
					}
				} else {
					// no modules found
					// Launch Add New product Activity
					Intent i = new Intent(getApplicationContext(),
							SignIn.class);
					// Closing all previous activities
					i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(i);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

			return null;
		}// doInBackground

		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		protected void onPostExecute(String file_url)
        {
			// dismiss the dialog after getting all modules
			pDialog.dismiss();
			// updating UI from Background Thread
			runOnUiThread(new Runnable() {
				public void run() {
					/**
					 * Updating parsed JSON data into ListView
					 * */
					ListAdapter adapter = new SimpleAdapter(
							ViewAllModules.this, moduleList,
							R.layout.view_qr, new String[] { TAG_ID,
									TAG_NAME, TAG_LECTURE, TAG_TUTORIAL},
							new int[] { R.id.pid, R.id.name, R.id.lecture_url, R.id.tutorial_url});

					// updating listview
					setListAdapter(adapter);
				}
			});

		}// onPostExecute

	}// LoadAllProducts
}// AllProductsActivity