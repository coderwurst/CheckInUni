package com.coderwurst.student_attendance;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
 * Date: 14/07/2014
 * Time: 08:36
 * Version: V4.0
 * USER INTERFACE FOR STAFF MEMBERS
 * ************************
 */
public class LecturerUI extends Activity implements View.OnClickListener

{
    // buttons to provide lecturer functions
    private Button btnManSignin;
    private Button btnAutoSignin;
    private Button btnGetQR;

    // retrieves shared preferences to be changed
    public static final String USER_ID = "User ID File";

    private TextView formatTxt, contentTxt;     // text view to inform tester of data captured at this stage
    private int scanID = 0;                     // int to store type of scan


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lecturer_ui);

        // Buttons
        btnManSignin = (Button) findViewById(R.id.lec_man_signin);      // button to return all students
        btnAutoSignin = (Button) findViewById(R.id.lec_auto_signin);      // button to initiate scan
        btnGetQR = (Button) findViewById(R.id.getQRCode);

        // TextViews for hold format and content info for testing purposes
        formatTxt = (TextView) findViewById(R.id.scan_format);
        contentTxt = (TextView) findViewById(R.id.scan_content);

        btnManSignin.setOnClickListener(this);
        btnAutoSignin.setOnClickListener(this);
        btnGetQR.setOnClickListener(this);

    } // onCreate

    @Override
    public void onClick (View view)
    {
        if(view.getId()==R.id.lec_man_signin)
        {
            // opens up manual sign in activity

        } else if (view.getId()==R.id.lec_auto_signin) {

            // opens up scanner & sets scanID = 2;

        }else {

            // calls scanner to register new details in system
            IntentIntegrator scanIntegrator = new IntentIntegrator(this);
            scanIntegrator.initiateScan();
            scanID = 1;

        }// if - else - else
    }// onClick


    public void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);

        if (scanningResult != null)
        {

            // toast for unit testing to show tester scan contents
            String scanContent = scanningResult.getContents();
            String scanFormat = scanningResult.getFormatName();
            formatTxt.setText("FORMAT: " + scanFormat);
            contentTxt.setText("CONTENT: " + scanContent);

            Toast toast = Toast.makeText(getApplicationContext(),
                    "FORMAT: " + scanFormat + "\nCONTENT: " + scanContent, Toast.LENGTH_LONG);
            toast.show();

                if (scanID == 2) // && formatTxt == "QR_CODE" to be called if auto sign-in required
                {
                    // launching SignIn Activity
                    Intent i = new Intent(getApplicationContext(), SignIn.class);

                    // takes the scanned info and packs it into a bundle before sending it to the SignIn class
                    String scannedInfo = scanContent;
                    i.putExtra("Info", scannedInfo);
                    startActivity(i);

                    // closing this screen
                    finish();

                } else {            // && formatTxt == "Code_128"   called to change user ID (testing purposes only)

                    // launching Registration Activity
                    Intent i = new Intent(getApplicationContext(), InitialReg.class);


                    // takes the scanned info and packs it into a bundle before sending it to the Registration class
                    String scannedInfo = scanContent;
                    i.putExtra("Info", scannedInfo);
                    startActivity(i);

                    // closing this screen
                    finish();

                }// if-else to determine if scan was to sign in or register

        } else {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "No scan data received!", Toast.LENGTH_SHORT);
            toast.show();
        }// if-else to confirm scan data has been received

    }// onActivityResult


} // LectureUI
