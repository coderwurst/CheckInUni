package com.coderwurst.student_attendance;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


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

    // opens the sharedPref file to allow user id to be stored
    public static final String PREFERENCES_FILE = "User ID File";
    static SharedPreferences userDetails;
    EditText userID;

    // need to think about identifying staff & student ids to allow for different interfaces

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.initial_reg);

        // unpack the data scanned into the activity
        Bundle bundle = getIntent().getExtras();
        scannedID = bundle.getString("Info");

        userID = (EditText) findViewById(R.id.user_id);
        userID.setText(scannedID);

        // button to confirm input and send to database
        Button btnSubmitID = (Button) findViewById(R.id.confirm_reg_details);

        // button click event
        btnSubmitID.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view)
            {
                // creating new product in background thread
                // new SignIntoClass().execute();


                userDetails = getSharedPreferences(PREFERENCES_FILE, 0); // create the shared preferences package

                SharedPreferences.Editor editor = userDetails.edit();           // edit the userID to the shared preference file
                editor.putString("user_ID", userID.getText().toString());
                editor.commit();                                                // save changes

                Toast toast = Toast.makeText(getApplicationContext(),
                        "user stored: " + userDetails.getString("user_ID", "default") , Toast.LENGTH_LONG);
                toast.show();

                Log.d(userID.getText().toString(), "initial ID value");          // logcat tag to view contents of string


            }// onClick
        });



    }// OnCreate

} // InitialReg
