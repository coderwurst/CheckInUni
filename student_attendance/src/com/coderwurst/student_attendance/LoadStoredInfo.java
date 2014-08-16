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

    // server IP address
    private static String serverAddress = MainScreenActivity.serverIP;

    // url to create new product
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

    Button btnConfirm;
    Button btnDelete;

    private ListView lv;
    private TextView title;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.load_info);

        // Buttons
        btnConfirm = (Button) findViewById(R.id.lec_confirm);
        btnDelete = (Button) findViewById(R.id.lec_delete);

        // onClick listeners
        btnConfirm.setOnClickListener(this);
        btnDelete.setOnClickListener(this);

        title = (TextView) findViewById(R.id.file_id);

        // fill ListView with last stored data
        lv = (ListView) findViewById(R.id.checkin_list);
        // loadList();

        checkForStoredData();

    } // onCreate


    public void onClick (View view)
    {
        if (view.getId() == R.id.lec_confirm)     // && scannedModule == true
        {
            // logcat tag to view app progress
            Log.d("load", "user wishes to load previous file");

            new LecturerSignStudentIn().execute();          // code to submit details to the database

        } else
        {

            deleteFile(filename);                       // removes the file stored in internal memory
            Log.d("load", filename + " deleted");       // logcat tag to view app progress

            finish();
        }// if - else

    } // onClick

    private void loadList ()
    {

        // uses an array adapter to load the information stored into the listview
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                studentBatch);

        lv.setAdapter(arrayAdapter);
    } // loadList


        private void checkForStoredData()
    {

        File file = this.getFilesDir();          // returns storage location

        Log.d("load", file.toString());

        ArrayList<String> names = new ArrayList<String>(Arrays.asList(file.list()));
        String filename = names.get(names.size() - 1);;

        Log.d("load", names.size() + " stored files: " + names);

        if (names.size() >= 1 && filename != "scanfile.txt")                  // currently always a scanfile.txt also stored in this directory - INVESTIGATE
        {
            Log.d("load", "file to be read: " + filename);

            loadStoredData(filename);

        } // if

    } // checkForStoredData


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
            }

            inputStream.close();

        } catch (Exception e)
        {

            e.printStackTrace();
        } // try - catch

        Log.d("load", "read data: " + dataToRead);

        // success message
        Toast.makeText(getBaseContext(), "file loaded successfully", Toast.LENGTH_SHORT).show();

        // method to break down code and send to server
        /** extract the necessary information out of this string to be used using special chars {} for module and []
         * for class type. Values taken from index position +1 as indexes begin at position 0 */
        moduleInfo = dataToRead.substring(dataToRead.indexOf("<") + 1, dataToRead.indexOf(">"));
        Log.d("load", "module code: " + moduleInfo);
        title.setText(moduleInfo);

        classInfo = dataToRead.substring(dataToRead.indexOf("{") + 1, dataToRead.indexOf("}"));
        Log.d("load", "class type: " + classInfo);

        String allIDs = dataToRead.substring(dataToRead.indexOf("[") + 1, dataToRead.indexOf("]"));
        Log.d("load", "all ids: " + allIDs);

        String thisID; // to store the contents of the current id being removed from the list

        do
        {
            thisID = allIDs.substring(allIDs.indexOf("£") + 1, allIDs.indexOf("$"));

            studentBatch.add(thisID);               // id added to the LinkedList for further processing

            String readID = "£" + thisID + "$";     // re-inserts the StudentID identifiers

            Log.d("load", "read ID: " + readID);

            allIDs = allIDs.replace(readID, "");    // removes the ID last added to studentBatch from the remaining list

            Log.d("load", "remaining: " + allIDs);

            // NB this code to replace the last read ID with "" IE also eliminates duplicates


        } while (!allIDs.isEmpty());                // do - while loop to continue until no text remains

        loadList();


    } // loadStoredData



    /**
     * Background Async Task to send information to database
     */
    class LecturerSignStudentIn extends AsyncTask<String, String, String>
    {

        /**
         * shows user a progress dialog box
         * */
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
         * registering the student as present
         * */
        protected String doInBackground(String... args)
        {

            JSONObject json = null;

            for (count = 0; count < studentBatch.size(); count++)
            {
                String student_id = studentBatch.get(count).toString();
                String module_id = moduleInfo;
                String type = classInfo;

                // logcat to view progress of app
                Log.d("load", "info sent to database; " + student_id + "," +  module_id + "," + type);

                // parameters to be passed into PHP script on server side
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("student_id", student_id));
                params.add(new BasicNameValuePair("module_id", module_id));
                params.add(new BasicNameValuePair("type", type));

                // getting JSON Object
                json = jsonParser.makeHttpRequest(url_sign_in, "POST", params);

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
                    Log.d("load", "database response; php success");

                    deleteFile(filename);

                    // returns user to home screen
                    // Intent signInSuccess = new Intent(getApplicationContext(), LecturerUI.class);
                    // startActivity(signInSuccess);


                } else
                {
                    // failed to sign-in
                    Log.e("load", "database response; php error");

                    // error message needed for when sign in is not successful
                    dialogText = "an error has occurred, please try again...";

                    // returns user to home screen
                    // Intent signInError = new Intent(getApplicationContext(), LecturerUI.class);
                    // startActivity(signInError);

                    // finish this activity
                    // finish();

                } // if - else
            } catch (JSONException e)
            {
                e.printStackTrace();
            } // try - catch


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

            // finish this activity
            finish();

        }// onPostExecute

    }// LecturerSignIntoClass

} // LoadStoredInfo