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
 * Version: V6.0
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
        setContentView(R.layout.lecturer_ui);                           // opens up corresponding XML file

        // Buttons
        btnManSignin = (Button) findViewById(R.id.lec_man_signin);          // to enter student & class details manually
        btnAutoSignin = (Button) findViewById(R.id.lec_auto_signin);        // to scan student ID & class QR-Code
        btnGetQR = (Button) findViewById(R.id.getQRCode);                   // to retrieve a particular QR-Code

        // TextViews for hold format and content info for testing purposes
        formatTxt = (TextView) findViewById(R.id.scan_format);
        contentTxt = (TextView) findViewById(R.id.scan_content);

        // set onClick listeners for all 3 buttons
        btnManSignin.setOnClickListener(this);
        btnAutoSignin.setOnClickListener(this);
        btnGetQR.setOnClickListener(this);

    } // onCreate

    @Override
    public void onClick (View view)
    {
        if(view.getId()==R.id.lec_man_signin)
        {
            // opens up manual sign in activity with text input fields
            Intent openManSignin = new Intent(getApplicationContext(), AddStudentMan.class);
            startActivity(openManSignin);

            // closing this screen
            finish();

        } else if (view.getId()==R.id.lec_auto_signin) {

            // opens up recursive sign in activity
            Intent openAutoSignin = new Intent(getApplicationContext(), RecursiveSignIn.class);
            startActivity(openAutoSignin);

            // closing this screen
            finish();

        }else {

            // temp test code
            IntentIntegrator scanIntegrator = new IntentIntegrator(this);
            scanIntegrator.initiateScan();
            scanID = 1;
            // to be replaced with code to open up recall QR-Code activity

        }// if - else - else
    }// onClick


    public void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        // takes the scanned in data & prepares for use within this method
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);

        if (scanningResult != null)             // as long as something has been scanned
        {
            // toast for unit testing to show tester scan contents
            String scanContent = scanningResult.getContents();
            String scanFormat = scanningResult.getFormatName();
            formatTxt.setText("FORMAT: " + scanFormat);
            contentTxt.setText("CONTENT: " + scanContent);

            Toast toast = Toast.makeText(getApplicationContext(),
                    "FORMAT: " + scanFormat + "\nCONTENT: " + scanContent, Toast.LENGTH_LONG);
            toast.show();

                // allows the user to re-register

                    // launching Registration Activity
                    Intent i = new Intent(getApplicationContext(), InitialReg.class);


                    // takes the scanned info and packs it into a bundle before sending it to the Registration class
                    String scannedInfo = scanContent;
                    i.putExtra("Info", scannedInfo);
                    startActivity(i);

                    // closing this screen
                    finish();

        } else {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "No scan data received!", Toast.LENGTH_SHORT);
            toast.show();
        }// if-else to confirm scan data has been received

    }// onActivityResult


} // LectureUI
