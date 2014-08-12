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
 * SPRINT 7 - CLASS TO ACCESS ALL MODULES IN DATABASE AND LOAD INTO LIST
 * ************************
 */
public class ViewAllModules extends ListActivity
{

	// Progress Dialog
	private ProgressDialog pDialog;

	// Creating JSON Parser object
	JSONParser jParser = new JSONParser();

	// array list to store the modules as they are being returned from database
    ArrayList<HashMap<String, String>> moduleList;

    // server IP address
    private static String serverAddress = MainScreenActivity.serverIP;

	// url to get all modules list
    private static String url_all_modules = "http://" + serverAddress + "/xampp/student_attendance/get_all_modules.php";
    private static String url_location = "http://" + serverAddress + "/module_codes/";

	// JSON Node names used in sending and receiving the JSON object
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

		// loading all modules in background thread
		new LoadAllModules().execute();

		// get listview from xml file
		ListView lv = getListView();

		// on click, user will be taken to chosen module QR-Codes
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				// getting values from selected ListItem
				String pid = ((TextView) view.findViewById(R.id.pid)).getText()
                        .toString();
                String lectureFile = ((TextView) view.findViewById(R.id.lecture_url)).getText()
                        .toString();
                String tutorialFile = ((TextView) view.findViewById(R.id.tutorial_url)).getText()
                        .toString();

                // log app progress for testing purposes
                Log.d("view modules", "stored address; " + pid + "," + lectureFile + tutorialFile);

                // concatenates server address with location on server (returned from database)
                String lectureUrl = url_location + lectureFile;
                String tutorialUrl = url_location + tutorialFile;

                // log app progress for testing purposes
                Log.d("view modules", "actual address; " + lectureUrl + "," + tutorialUrl);

				// Starting new intent
				Intent viewQR = new Intent(getApplicationContext(),	ChooseQR.class);

				// sending pid to next activity
				viewQR.putExtra(TAG_ID, pid);
                viewQR.putExtra(TAG_LECTURE, lectureUrl);
                viewQR.putExtra(TAG_TUTORIAL, tutorialUrl);
				
				// starting new activity and expecting some response back
				startActivityForResult(viewQR, 100);
			} // onItemClick
		}); // onItemClickListener

	}// onCreate

	// Response from Edit Product Activity
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
		super.onActivityResult(requestCode, resultCode, data);

        /**
         * the activity was started with the identifier 100, the program waits for the result of this activity (ie the
         * ChooseQR.java to be returned to this screen, after which the intent is executed; preventing unused
         * app windows from remaining active and open in the background
         **/

		if (resultCode == 100)              // if result code 100
        {
			Intent intent = getIntent();    // gets the current intent (ViewAllModules.java)
			finish();                       // finishes ChooseQR.java
			startActivity(intent);          // restarts ViewAllModules
		} // if

	}// onActivityResult

	/**
	 * background async task to load all modules as stored in database
	 * */
	class LoadAllModules extends AsyncTask<String, String, String>
    {

		/**
		 * before starting background thread, show Progress Dialog
		 * this provides the user with some information whilst the app
         * is performing background tasks. The purpose of this dialog
         * is to prevent the user from thinking that the app has crashed
         * or become unresponsive
         * */
		@Override
		protected void onPreExecute()
        {
			super.onPreExecute();
			pDialog = new ProgressDialog(ViewAllModules.this);
			pDialog.setMessage("Loading modules. Please wait...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		} // onPreExecute

		/**
		 * once the user has been infromed, the process begins to get all modules from the server url,
         * during which time the dialog box remains open
		 * */
		protected String doInBackground(String... args)
        {
			// build Parameters
			List<NameValuePair> params = new ArrayList<NameValuePair>();

			// getting JSON string from URL
			JSONObject json = jParser.makeHttpRequest(url_all_modules, "GET", params);

			
			// log checked for JSON response
			Log.d("view modules", "all modules; " + json.toString());

			try {
				// Checking for SUCCESS TAG
				int success = json.getInt(TAG_SUCCESS);

				if (success == 1)               // modules found
                {
					// get array of modules
					modules = json.getJSONArray(TAG_MODULES);

					// looping through all returned modules
					for (int i = 0; i < modules.length(); i++)
                    {
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
				} else {                // no modules found

					// no modules have been returned
                    // ?????????????????????????????????? ERROR HANDLING ????????????????????????????????????

				} // if - else
			} catch (JSONException e) {
				e.printStackTrace();
			} // try - catch

			return null;
		}// doInBackground

		/**
		 * after completing background task onPostExecute dismisses the progress dialog and assigns the respective
         * information to the matching values in the module list. At this stage, all details have been retrieved from
         * the database, however are not all viewable by the user. Upon clicking a module in the list, the data for
         * the location of the QR images are passed over into the Choose QR class - meaning that the same PHP script
         * does not need to be run twice in both classes to retrieve the storage location of the QR images
		 * */

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

	}// LoadAllModules
}// ViewAllModules