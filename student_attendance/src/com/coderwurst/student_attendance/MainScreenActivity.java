package com.coderwurst.student_attendance;  // Sprint 4 - Sign into Database


/**
 * ***********************
 * Created by IntelliJ IDEA
 * User: andrew
 * Date: 04/07/2014
 * Time: 10:00
 * Version: V2.0
 * SPRINT 4 - MAIN SCREEN TO ALLOW SIGN-IN AND TEST CONNECTION TO DATABASE
 * ************************
 */

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.OnClickListener;

// import Zxing Files for Barcode Scanner
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;




public class MainScreenActivity extends Activity implements OnClickListener

{
	private Button btnTest;                     // button for testing purposes - to check if connected to database server
	private Button btnScan;                     // button to initiate sign-in process
    private Button btnReg;                      // button to register a user
    private boolean registered = false;         // set to true after initial registration as ID will be saved
    private TextView formatTxt, contentTxt;     // text view to inform tester of data captured at this stage
    private int scanID = 0;                     // int to store type of scan

    /* format to be used at a later stage, to determine what data is to be. For example, if a lecturer uses recursive
     * mode to read a Barcode, the app should send the student ID as well as the module ID and class type. If the
     * scan returns a QR-Code format, the student ID should be retrieved from the shared preferences */
	
	@Override
	public void onCreate(Bundle savedInstanceState)
    {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_screen);
		
		// Buttons
		btnTest = (Button) findViewById(R.id.test_button);      // button to return all students
		btnScan = (Button) findViewById(R.id.scan_button);      // button to initiate scan
        btnReg = (Button) findViewById(R.id.reg_button);

        // TextViews for hold format and content info for testing purposes
        formatTxt = (TextView)findViewById(R.id.scan_format);
        contentTxt = (TextView)findViewById(R.id.scan_content);

        // set up onClick listeners for both buttons
        btnTest.setOnClickListener(this);
        btnScan.setOnClickListener(this);
        btnReg.setOnClickListener(this);

    }// OnCreate


    // onClick method to determine which classes are called dependant on which button is clicked
            @Override
            public void onClick (View view)
            {
                if(view.getId()==R.id.test_button)     // test server connection
                {
                    // Launching Test Activity
                    Intent i = new Intent(getApplicationContext(), AllProductsActivity.class);
                    startActivity(i);
                }// if

                // student wishes to sign into class
                if(view.getId()==R.id.scan_button)     // sign-in
                {
                    IntentIntegrator scanIntegrator = new IntentIntegrator(this);
                    scanIntegrator.initiateScan();
                    scanID = 2;
                }// if

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

        if (scanningResult != null)
        {

            String scanContent = scanningResult.getContents();
            String scanFormat = scanningResult.getFormatName();
            formatTxt.setText("FORMAT: " + scanFormat);
            contentTxt.setText("CONTENT: " + scanContent);

            Toast toast = Toast.makeText(getApplicationContext(),
                    "FORMAT: " + scanFormat + "\nCONTENT: " + scanContent, Toast.LENGTH_LONG);
            toast.show();

                    if (scanID == 2) // && formatTxt == "QR_CODE"
                    {
                        // launching SignIn Activity
                        Intent i = new Intent(getApplicationContext(), SignIn.class);
                        // takes the scanned info and packs it into a bundle before sending it to the SignIn class
                        String scannedInfo = scanContent;
                        i.putExtra("Info", scannedInfo);
                        startActivity(i);
                    } else {            // && formatTxt == "Code_128"
                        // launching SignIn Activity
                        Intent i = new Intent(getApplicationContext(), InitialReg.class);
                    // takes the scanned info and packs it into a bundle before sending it to the SignIn class *********
                        String scannedInfo = scanContent;
                        i.putExtra("Info", scannedInfo);
                    startActivity(i);
                    }// if-else to determine if scan was to sign in or register

        } else {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "No scan data received!", Toast.LENGTH_SHORT);
            toast.show();
        }// if-else to confirm scan data has been received

    }// onActivityResult

}// MainScreenActivity