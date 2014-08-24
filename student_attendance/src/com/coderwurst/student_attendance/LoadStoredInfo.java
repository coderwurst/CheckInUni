package com.coderwurst.student_attendance;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;



/**
 * ***********************
 * Created by IntelliJ IDEA
 * User: andrew
 * Date: 16/07/2014
 * Time: 12:37
 * Version: V7.0
 * SPRINT 10 - ACTIVITY TO SEARCH AND LOAD ANY PREVIOUSLY SAVED CHECK-IN(S) BEFORE SENDING TO DATABASE
 * ************************
 */

public class LoadStoredInfo extends Activity implements View.OnClickListener

{


    // private Strings initiated to null so as information reset upon calling Activity
    private String moduleInfo = null;
    private String classInfo = null;
    private String filename = null;

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
    private LinkedList <String> studentBatch = new LinkedList<String>();
    private int count;

    // list view and title to show previously stored information on screen
    private ListView lv;
    private TextView title;
    private TextView runningTotal;


    // buttons to be used to send the saved information, or delete file
    private Button btnConfirm;
    private Button btnDelete;

    // tags for log statements
    private static final String TAG = "load";

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

        // the stored file is located on device memory
        checkForStoredData();

    } // onCreate


    public void onClick (View view)
    {
        if (view.getId() == R.id.lec_confirm)     // && scannedModule == true
        {
            // logcat tag to view app progress
            Log.d(TAG, "user wishes to load previous file");

            new LecturerSignStudentIn().execute();          // code to submit details to the database

        } else
        {

            deleteFile(filename);                       // removes the file stored in internal memory
            Log.d(TAG, filename + " deleted");       // logcat tag to view app progress

            finish();
        }// if - else

    } // onClick


    /**
     * checkForStoredData method searches the device memory for the stored file and calls
     * the loadStoredData method
     */

    private void checkForStoredData()
    {

        File file = this.getFilesDir();          // returns storage location

        Log.d(TAG, file.toString());

        ArrayList<String> names = new ArrayList<String>(Arrays.asList(file.list()));
        String filename = names.get(names.size() - 1);;

        Log.d(TAG, names.size() + " stored files: " + names);

        if (names.size() >= 1 && filename != "scanfile.txt")                  // currently always a scanfile.txt also stored in this directory - INVESTIGATE
        {
            Log.d(TAG, "file to be read: " + filename);

            loadStoredData(filename);

        } // if

    } // checkForStoredData


    /**
     * loadStoredData method takes the filename from the checkForStoredData method,
     * uses a file input stream to read the data into the activity, establishes
     * the different components of the saved file (module name, class type & student
     * numbers) before calling the loadList method to present this information
     * on screen to the user
     */

    private void loadStoredData (String pFilename)
    {

        filename = pFilename;
        // loading the data back into the app
        String dataToRead = "<";               // string to store characters being read
        int charRead;                           // int to store the number of characters being read
        char[] inputBuffer = new char[100];     // buffer to store characters being read from file
        FileInputStream inputStream;

        try
        {
            inputStream = openFileInput(pFilename);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            inputStreamReader.read();

            while ((charRead = inputStreamReader.read(inputBuffer)) > 0)
            {
                String readString = String.copyValueOf(inputBuffer, 0,
                        charRead);

                dataToRead += readString;

                inputBuffer = new char[100];
            } // while

            inputStream.close();

        } catch (Exception e)
        {

            e.printStackTrace();
        } // try - catch

        Log.d(TAG, "read data: " + dataToRead);

        // success message
        Toast.makeText(getBaseContext(), "file loaded successfully", Toast.LENGTH_SHORT).show();

        // method to break down code and send to server
        /** extract the necessary information out of this string to be used using special chars {} for module and []
         * for class type. Values taken from index position +1 as indexes begin at position 0 */
        moduleInfo = dataToRead.substring(dataToRead.indexOf("<") + 1, dataToRead.indexOf(">"));
        Log.d("load", "module code: " + moduleInfo);
        title.setText(moduleInfo);

        classInfo = dataToRead.substring(dataToRead.indexOf("{") + 1, dataToRead.indexOf("}"));
        Log.d(TAG, "class type: " + classInfo);

        String allIDs = dataToRead.substring(dataToRead.indexOf("[") + 1, dataToRead.indexOf("]"));
        Log.d(TAG, "all ids: " + allIDs);

        String thisID; // to store the contents of the current id being removed from the list

        do
        {
            thisID = allIDs.substring(allIDs.indexOf("£") + 1, allIDs.indexOf("$"));

            studentBatch.add(thisID);               // id added to the LinkedList for further processing

            String readID = "£" + thisID + "$";     // re-inserts the StudentID identifiers

            Log.d(TAG, "read ID: " + readID);

            allIDs = allIDs.replace(readID, "");    // removes the ID last added to studentBatch from the remaining list

            Log.d(TAG, "remaining: " + allIDs);

            // NB this code to replace the last read ID with "" IE also eliminates duplicates


        } while (!allIDs.isEmpty());                // do - while loop to continue until no text remains

        // updates the total number of students in the saved file
        runningTotal.setText("Total: " + studentBatch.size() + " students");

        loadList();


    } // loadStoredData

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
            pDialog = new ProgressDialog(LoadStoredInfo.this);
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
                String module_id = moduleInfo;
                String type = classInfo;

                // logcat to view progress of app
                Log.d(TAG, "info sent to database; " + student_id + "," +  module_id + "," + type);

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
                Log.d("load", "database response" + json.toString());

            } // for loop for batch progressing multiple students

            try
            {
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1)
                {
                    // check log cat for response
                    Log.d(TAG, "database response; php success");

                    deleteFile(filename);

                } else
                {
                    // failed to sign-in
                    Log.e(TAG, "database response; php error");

                    // error message needed for when sign in is not successful
                    dialogText = "an error has occurred, please try again...";

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

            // informs the user of a success or an error
            Toast toast = Toast.makeText(getApplicationContext(),
                    "check in: " + dialogText, Toast.LENGTH_LONG);
            toast.show();

            // finish this activity, returning user to home screen
            finish();

        }// onPostExecute

    }// LecturerSignIntoClass

} // LoadStoredInfo