package com.coderwurst.student_attendance;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

/**
 * ***********************
 * Created by IntelliJ IDEA
 * User: andrew
 * Date: 21/07/2014
 * Time: 11:54
 * Version: V8.0
 * SPRINT 6 - USER INTERFACE FOR STUDENTS TO ALLOW USER TO 'CHECK IN' TO A CLASS
 * ************************
 */
public class StudentUI extends Activity implements View.OnClickListener

{

    private Button btnScan;                     // button to initiate sign-in process
    private Button btnResetUsr;                 // button to reset sharedPref for testing purposes

    // retrieves shared preferences to be used in determining student ID
    public static final String USER_ID = "User ID File";

    private TextView formatTxt, contentTxt;     // text view to inform tester of data captured at this stage
    private int scanID = 0;                     // int to store type of scan

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.student_ui);

        // Buttons
        btnScan = (Button) findViewById(R.id.scan_button);          // button to return all students
        btnResetUsr = (Button) findViewById(R.id.test_button);      // button to initiate scan

        // TextViews for hold format and content info FOR TESTING PURPOSES ONLY
        formatTxt = (TextView) findViewById(R.id.scan_format);
        contentTxt = (TextView) findViewById(R.id.scan_content);

        // sets onCLick listeners for both buttons
        btnScan.setOnClickListener(this);
        btnResetUsr.setOnClickListener(this);       // button to allow user type to be reset (testing purposes only)

    } // onCreate

    @Override
    public void onClick (View view)
    {
        if(view.getId()==R.id.scan_button)      // determines what the user wishes to do, amends scanID accordingly
        {
            Log.d("student ui", "student wishes to check into a class");
            // to be used when scanning in QR-Code to register attendance
            IntentIntegrator scanIntegrator = new IntentIntegrator(this);
            scanIntegrator.initiateScan();      // opens Zxing scanner
            scanID = 2;

        } else {

            Log.d("student ui", "user wishes to register as another user");
            // calls scanner to register new details in system TO BE REMOVED FOR FINAL VERSION
            IntentIntegrator scanIntegrator = new IntentIntegrator(this);
            scanIntegrator.initiateScan();      // opens Zxing scanner
            scanID = 1;

        }// if - else
    }// onClick


    // returns scanning results for futher computation
    public void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);

        if (scanningResult != null)                     // to determine if the scan was successful
        {

            // Toast contents used to follow data flow through app, confirm input
            String scanContent = scanningResult.getContents();
            String scanFormat = scanningResult.getFormatName();
            formatTxt.setText("FORMAT: " + scanFormat);
            contentTxt.setText("CONTENT: " + scanContent);

            Log.d("student ui", "scan content: " + scanContent);

            /*Toast toast = Toast.makeText(getApplicationContext(),
                    "FORMAT: " + scanFormat + "\nCONTENT: " + scanContent, Toast.LENGTH_LONG);
            toast.show(); */

            /**
             * the following if-else block is implemented at this stage as it is
             * important to determine if the user has scanned in the right type
             * of data for the function he or she has chosen
             */

                if (scanID == 2 && scanFormat.equals("QR_CODE"))            // "QR_CODE" is only valid QR-Code format
                {

                    Log.d("student ui", "student wishes to check into a class");

                    // launching SignIn Activity
                    Intent openSignIn = new Intent(getApplicationContext(), SignIn.class);

                    // takes the scanned info and packs it into a bundle before sending it to the SignIn class
                    String scannedInfo = scanContent;
                    openSignIn.putExtra("Info", scannedInfo);
                    startActivity(openSignIn);

                    // closing this screen
                    finish();

                } else if (scanID == 2 && !scanFormat.equals("QR_CODE"))    // in the event the user does not scan a QR
                {

                    Log.e("student ui", "student has scanned wrong type of code");

                    // informs user that the code recently scanned is not of correct type
                    Toast QRIncorrectFormat = Toast.makeText(getApplicationContext(),
                            "format incorrect, please try again..." + scanContent, Toast.LENGTH_LONG);
                    QRIncorrectFormat.show();

                } else if (scanID == 1 && scanFormat.equals("CODE_128"))         // FOR TESTING PURPOSES ONLY
                {

                    Log.d("student ui", "user wishes to register as another user");

                    // launching Registration Activity
                    Intent openReg = new Intent(getApplicationContext(), InitialReg.class);

                    // takes the scanned info and packs it into a bundle before sending it to the Registration class
                    String scannedInfo = scanContent;
                    openReg.putExtra("Info", scannedInfo);
                    startActivity(openReg);

                    // closing this screen
                    finish();

                }else if (scanID == 1 && !scanFormat.equals("CODE_128"))          // scan is incorrect format
                {

                    Log.e("student ui", "student has scanned wrong type of code");   // log in java console to show error

                    // user informed of error
                    Toast IDIncorrectFormat = Toast.makeText(getApplicationContext(),
                            "valid User ID not scanned, please try again...", Toast.LENGTH_LONG);
                    IDIncorrectFormat.show();

                } // series of else - if statements
        } else {

            Log.e("student ui", "check in failed");   // log in java console to show an error has occurred

            // inform user of incompatible scan
            Toast toast = Toast.makeText(getApplicationContext(),
                    "no scan data received!", Toast.LENGTH_SHORT);
            toast.show();
        }// if-else to confirm scan data has been received

    }// onActivityResult


} // StudentUI
