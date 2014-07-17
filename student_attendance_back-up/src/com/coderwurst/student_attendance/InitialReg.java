package com.coderwurst.student_attendance;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
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
 * Date: 08/07/2014
 * Time: 12:10
 * Version: V2.0
 * CLASS TO ALLOW STUDENT OR LECTURER TO SIGN INTO DEVICE, DETAILS TO BE SAVED
 * ************************
 */
public class InitialReg extends Activity

{
    String scannedID = "";      // string to store scanned ID data

    // booleans to identify from the user ID which type of user and therefore what functionality the app can offer
    boolean studentUser = false;
    boolean staffUser = false;

    // opens the sharedPref file to allow user id to be stored
    public static final String PREFERENCES_FILE = "User ID File";
    static SharedPreferences userDetails;
    EditText userID;
    EditText userType;



    // progress dialog to inform user
    private ProgressDialog pDialog;

    // creates the JSONParser object
    JSONParser jsonParser = new JSONParser();

    // url to authenticate user
    private static String url_auth = "http://192.168.1.104/xampp/student_attendance/auth_user.php";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";



    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.initial_reg);

        // unpack the data scanned into the activity
        Bundle bundle = getIntent().getExtras();
        scannedID = bundle.getString("Info");

        // user Authentication Check
        Log.d(scannedID, "ID Auth");          // logcat tag to view contents of string at this stage (testing purposes only)


        // assign the app student or staff user privileges
        if(scannedID.charAt(0) == 'E' || scannedID.charAt(0) == 'e')
        {

            staffUser = true;       // boolean stored in shared preferences, automatically loaded on next start up

            userType = (EditText) findViewById(R.id.user_type);
            userType.setText("staff");

            // code to authenticate staff ID with Database

        } else if (scannedID.charAt(0) == 'B' || scannedID.charAt(0) == 'b')
        {

            studentUser = true;     // boolean stored in shared preferences, automatically loaded on next start up

            userType = (EditText) findViewById(R.id.user_type);
            userType.setText("student");

            // code to authenticate student ID with Database

        } // else - if



        userID = (EditText) findViewById(R.id.user_id);
        userID.setText(scannedID);


        // button to confirm input and send to database
        Button btnSubmitID = (Button) findViewById(R.id.confirm_reg_details);

        // button click event
        btnSubmitID.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view)
            {

                new AuthenticateUser().execute();

                /*
                userDetails = getSharedPreferences(PREFERENCES_FILE, 0); // create the shared preferences package

                SharedPreferences.Editor editor = userDetails.edit();           // edit the userID to the shared preference file
                editor.putString("user_ID", userID.getText().toString());

                if (staffUser == true)
                {
                    editor.putInt("user_Type",1);                                // stored in the preferences a staff user

                } else if (studentUser == true)
                {
                    editor.putInt("user_Type",2);                                // stored in the preferences a student user

                } else
                {
                    editor.putInt("user_Type", 0);                              // for testing purposes, allows Type to be reset

                } // if - else

                editor.commit();                                                // save changes

                Toast toast = Toast.makeText(getApplicationContext(),
                        "user stored: " + userDetails.getString("user_ID", "default") + "type: " +
                                userDetails.getInt("user_Type", 0), Toast.LENGTH_LONG);
                toast.show();

                Log.d(userID.getText().toString(), "initial ID value");          // logcat tag to view contents of string

                */

            }// onClick
        });

    }// OnCreate

    /**
     * Background Async Task to send information to database
     */
    class AuthenticateUser extends AsyncTask<String, String, String>
    {

        /**
         * shows user a progress dialog box
         * */
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            pDialog = new ProgressDialog(InitialReg.this);
            pDialog.setMessage("authenticating...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        } // onPreExecute



/**
 * authenticating ID details that have been scanned into device
 * */
    protected String doInBackground(String... args)
    {

        String user_id = scannedID;

        // parameters to be passed into PHP script on server side
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("user_id", user_id));

        // getting JSON Object
        // NB url accepts POST method
        JSONObject json = jsonParser.makeHttpRequest(url_auth,
                "POST", params);

        // check log cat for response
        Log.d("Create Response", json.toString());

        // check for success tag
        try {
            int success = json.getInt(TAG_SUCCESS);

            if (success == 1) {

                // successfully authenticated user
                Intent openMainScreen = new Intent(getApplicationContext(), MainScreenActivity.class);
                startActivity(openMainScreen);

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
        pDialog.setMessage("complete");
        // dismiss the dialog once done
        pDialog.dismiss();
    }// onPostExecute

}// signIntoClass



} // InitialReg