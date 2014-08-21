package com.coderwurst.student_attendance;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * ***********************
 * Created by IntelliJ IDEA
 * User: andrew
 * Date: 18/08/2014
 * Time: 08:47
 * Version: V1.0
 * ADD CLASS INFORMATION HERE
 * ************************
 */

public class ReviewInfo extends Activity implements View.OnClickListener

{
        // private Strings initiated to null so as information reset upon calling Activity
        private String moduleID = null;
        private String classType = null;

        // server IP address (stored in MainScreenActivity
        private static String serverAddress = MainScreenActivity.serverIP;

        // url to access PHP script on server to add a student check-in
        private static String url_sign_in = "http://" + serverAddress + "/xampp/student_attendance/sign_in.php";

        // JSON Node names
        private static final String TAG_SUCCESS = "success";

        // progress dialog to inform user
        private ProgressDialog pDialog;
        private String dialogText = "success";

        // creates the JSONParser object
        JSONParser jsonParser = new JSONParser();

        // linkedList to store multiple students for batch processing
        private ArrayList<String> studentBatch = new ArrayList<String>();
        private int count;

        // list view and title to show previously stored information on screen
        private ListView lv;
        private TextView title;
        private TextView runningTotal;

        // buttons to be used to send the saved information, or delete file
        private Button btnConfirm;
        private Button btnDelete;

        // tags used when passing the information into the ReviewInfo class
        private static final String TAG_STUDENTLIST = "studentList";
        private static final String TAG_MODID = "moduleId";
        private static final String TAG_CLASSTYPE = "classType";

        // tags for log statements
        private static final String TAG = "review info";

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.load_info);

            // buttons
            btnConfirm = (Button) findViewById(R.id.lec_confirm);
            btnDelete = (Button) findViewById(R.id.lec_delete);

            // onClick listeners
            btnConfirm.setOnClickListener(this);
            btnDelete.setOnClickListener(this);

            // text view to show module info for saved file being presented
            title = (TextView) findViewById(R.id.file_id);

            // text view to show the total number of students scanned
            runningTotal = (TextView) findViewById(R.id.total);

            // fill ListView with last stored data
            lv = (ListView) findViewById(R.id.checkin_list);

            // getting module details from intent
            Intent reviewInput = getIntent();

            // getting info from the intent packaged in the RecursiveSignIn class
            moduleID = reviewInput.getStringExtra(TAG_MODID);
            classType = reviewInput.getStringExtra(TAG_CLASSTYPE);

            // for correct functionality with passing info via an Intent, an ArrayList has to be implemented
            studentBatch = (ArrayList<String>)reviewInput.getSerializableExtra(TAG_STUDENTLIST);

            int initialSize = studentBatch.size();

            // LinkedHashSets can be used to remove duplicate values whilst preserving the order of the Array List
            LinkedHashSet removeDuplicates = new LinkedHashSet();

            // details are passed to the LinkedHastSet, whilst studentBatch is cleaned, before re-inserting the values
            removeDuplicates.addAll(studentBatch);

            studentBatch.clear();

            // duplicates have been removed
            studentBatch.addAll(removeDuplicates);

            int finalSize = studentBatch.size();

            int removedIDs = initialSize - finalSize;

            // logcat tag to view app progress
            Log.d("TAG", "module id: " + moduleID + ", " + classType);
            Log.d("TAG", "scanned total: " + studentBatch.size());

            // change the text views to that of the imported details
            title.setText(moduleID + ", " + classType);
            runningTotal.setText("Total: " + finalSize + " students, " + removedIDs +
                    " duplicates have been removed");

            // load the info into the list
            loadList();

        } // onCreate


        public void onClick (View view)
        {
            if (view.getId() == R.id.lec_confirm)     // && scannedModule == true
            {
                // logcat tag to view app progress
                Log.d("TAG", "user wishes to load previous file");

                new LecturerSignStudentIn().execute();          // code to submit details to the database

            } else      // user wishes to delete scanned info
            {
                deleteFile();

                finish();
            }// if - else

        } // onClick

    private void deleteFile ()
    {
        // resets all variables to null before exiting screen
        moduleID = null;
        classType = null;
        studentBatch = null;
        Log.d("TAG", "details removed");       // logcat tag to view app progress
    } // deleteFile


    /**
         * loadList used to take the contents of the file imported in the loadStoredData
         * method and present them on screen using an arrayadapter
         */

        private void loadList ()
        {

            // uses an array adapter to load the information stored into the listview
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                    this,
                    android.R.layout.simple_list_item_1,
                    studentBatch);

            // listView formatted using format_lv.xml
            lv.setAdapter(arrayAdapter);
        } // loadList



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
                pDialog = new ProgressDialog(ReviewInfo.this);
                pDialog.setMessage("sending info...");
                pDialog.setIndeterminate(false);
                pDialog.setCancelable(true);
                pDialog.show();

            } // onPreExecute

            /**
             * This doInBackground method completes the work involved in contacting the
             * server to register each student individually
             **/

            protected String doInBackground(String... args)
            {

                JSONObject json = null;

                // for loop to cycle through the stored ids and send each one to database
                for (count = 0; count < studentBatch.size(); count++)
                {
                    // Strings to hold check-in information
                    String student_id = studentBatch.get(count).toString();
                    String module_id = moduleID;
                    String type = classType;

                    // logcat to view progress of app
                    Log.d("TAG", "info sent to database; " + student_id + "," +  module_id + "," + type);

                    // parameters to be passed into PHP script on server side
                    List<NameValuePair> params = new ArrayList<NameValuePair>();
                    params.add(new BasicNameValuePair("student_id", student_id));
                    params.add(new BasicNameValuePair("module_id", module_id));
                    params.add(new BasicNameValuePair("type", type));

                    // getting JSON Object
                    json = jsonParser.makeHttpRequest(url_sign_in, "POST", params);

                    /** try - catch implements a delay as during development it was found that greater
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
                    Log.d("TAG", "database response" + json.toString());

                } // for loop for batch progressing multiple students

                try
                {
                    int success = json.getInt(TAG_SUCCESS);

                    if (success == 1)
                    {
                        // check log cat for response
                        Log.d("TAG", "database response; php success");

                        deleteFile();

                    } else
                    {
                        // failed to sign-in
                        Log.e("TAG", "database response; php error");

                        // error message needed for when sign in is not successful
                        dialogText = "an error has occurred, please try again later...";

                    } // if - else
                } catch (JSONException e)
                {
                    e.printStackTrace();
                } // try - catch


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

                Toast result = Toast.makeText(getApplicationContext(),
                        "check-in: " + dialogText, Toast.LENGTH_LONG);
                result.show();


                // finish this activity, returning user to home screen
                finish();

            }// onPostExecute

        }// LecturerSignIntoClass

} // ReviewInfo
