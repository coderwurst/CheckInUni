package com.coderwurst.student_attendance;  // Sprint 4 - Sign into Database

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.OnClickListener;

// import Zxing Files for Barcode Scanner
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;


/**
 * ***********************
 * Created by IntelliJ IDEA
 * User: andrew
 * Date: 21/07/2014
 * Time: 11:52
 * Version: V8.0
 * SPRINT 4 - MAIN SCREEN TO ALLOW INITIAL REGISTRATION ON DEVICE AND UPON NEXT START UP DETERMINE APP INTERFACE
 * ************************
 */


public class MainScreenActivity extends Activity implements OnClickListener

{
	private Button btnReg;                      // button to register a user
    private TextView formatTxt, contentTxt;     // text view to inform tester of data captured at this stage
    private int scanID = 0;                     // int to store type of scan

    // opens the sharedPref file to allow user id to be stored
    public static final String USER_ID = "User ID File";
    static SharedPreferences userDetails;

    protected static String serverIP = "192.168.1.100";



    /* format to be used at a later stage, to determine what data is to be. For example, if a register uses recursive
     * mode to read a Barcode, the app should send the student ID as well as the module ID and class type. If the
     * scan returns a QR-Code format, the student ID should be retrieved from the shared preferences */
	
	@Override
	public void onCreate(Bundle savedInstanceState)
    {
		super.onCreate(savedInstanceState);

        // returns the stored sharedPrefs for the app and stores the user type
        userDetails = getSharedPreferences(USER_ID, 0);
        int savedID = userDetails.getInt("user_Type", 0);

        Log.d("wifi", "check if wifi is on");

        /** if to determine if there is any outstanding check in info from a previous session
         * to be sent to the database before continuing
         */

        if (savedID == 2)        // if there are no savedPreferences then the app will allow user to register
        {

            /*Toast toast2 = Toast.makeText(getApplicationContext(),
                    "User is a student member: " + savedID, Toast.LENGTH_LONG);
            toast2.show(); */

            Log.d("main screen", "user type; student");   // log in java console which type of user is registered with device

            // code to open up student UI
            Intent openStudentUI = new Intent(getApplicationContext(), StudentUI.class);
            startActivity(openStudentUI);

            // closing this screen
            finish();


        } else if (savedID == 1){                    // otherwise the app will start up straight to lecturer UI


            /* Toast toast1 = Toast.makeText(getApplicationContext(),
                    "User is a staff member: " + savedID, Toast.LENGTH_LONG);
            toast1.show(); */

            Log.d("main screen", "user type; lecturer");   // log in java console which type of user is registered with device

            // code to open up staff UI
            Intent openLecturerUI = new Intent(getApplicationContext(), LecturerUI.class);
            startActivity(openLecturerUI);

            // closing this screen
            finish();

        } else {

            setContentView(R.layout.main_screen);

            Log.d("main screen", "user type not found");   // log in java console which type of user is registered with device

            // button to register details
            btnReg = (Button) findViewById(R.id.reg_button);

            // textViews for hold format and content info for testing purposes
            formatTxt = (TextView) findViewById(R.id.scan_format);
            contentTxt = (TextView) findViewById(R.id.scan_content);

            // set up onClick listeners for both buttons
            btnReg.setOnClickListener(this);

        } // if - else

    }// OnCreate


            // onClick method to determine which classes are called dependant on which button is clicked
            @Override
            public void onClick (View view)
            {
                // student or lecturer wishes to register for first time use
                if(view.getId()==R.id.reg_button)       // register device
                {
                    IntentIntegrator scanIntegrator = new IntentIntegrator(this);
                    scanIntegrator.initiateScan();
                    scanID = 1;
                }// if

            }// onClick


    // Returns scanning results to the main screen - testing purposes TO BE REMOVED FOR FINAL
    public void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);

        if (scanningResult != null && resultCode == RESULT_OK)
        {

            String scanContent = scanningResult.getContents();
            String scanFormat = scanningResult.getFormatName();
            formatTxt.setText("FORMAT: " + scanFormat);
            contentTxt.setText("CONTENT: " + scanContent);

            /* Toast toast = Toast.makeText(getApplicationContext(),
                    "FORMAT: " + scanFormat + "\nCONTENT: " + scanContent, Toast.LENGTH_LONG);
            toast.show(); */

            Log.d("main screen", "new user" + scanContent);   // log in java console which type of user is registering with device

            // launching Registration Activity
            Intent register = new Intent(getApplicationContext(), InitialReg.class);


            // takes the scanned info and packs it into a bundle before sending it to the Registration class
            String scannedInfo = scanContent;
            register.putExtra("Info", scannedInfo);
            startActivity(register);

            // closing this screen
            finish();


        } else {

            Log.e("main screen", "cancelled registration");   // log in java console to show an error has occurred

            // inform user of incompatible scan
            Toast toast = Toast.makeText(getApplicationContext(),
                    "No scan data received!", Toast.LENGTH_SHORT);
            toast.show();

        }// if-else to confirm scan data has been received

    }// onActivityResult

}// MainScreenActivity