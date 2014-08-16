package com.coderwurst.student_attendance;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * ***********************
 * Created by IntelliJ IDEA
 * User: andrew
 * Date: 19/07/2014
 * Time: 11:34
 * Version: V7.0
 * SPRINT 7 - LECTURER TO BE ABLE TO RECALL A QR-CODE BASED ON MODULE NAME AND CLASS TYPE
 * ************************
 */
public class ChooseQR extends Activity implements View.OnClickListener

{
    private String moduleID, lectureUrl, tutorialUrl;       // used to store information sent over from ViewAllModules

    // strings assiciated with JSON object, used when sending and receiving data from database
    private static final String TAG_ID = "id";
    private static final String TAG_LECTURE = "lectureUrl";
    private static final String TAG_TUTORIAL = "tutorialUrl";

    // buttons to allow user to select between lecture or tutorial QR-Code
    private Button btnLectureQR;
    private Button btnTutorialQR;
    private Button btnSaveDetails;

    private TextView qrID;                                  // informs user of current choice of QR-Code

    // webview to hold image stored on server, accessed with url provided from ViewAllModules
    private WebView webview = null;

    // string to store selected info to be used as defaults for other lecturer functionality
    protected static String moduleCode = null;
    protected static String classType = null;

    /**
     * Included in this onCreate method, is a small piece of code on line 56 to prevent the device from
     * going to sleep on the lecturer whilst presenting the QR-Code information for students to check
     * themselves into class
     */

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_qr);

        // called to prevent the device from going to sleep in this view
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // getting module details from intent
        Intent moduleInfo = getIntent();

        // getting module from the view all modules intent
        moduleID = moduleInfo.getStringExtra(TAG_ID);
        lectureUrl = moduleInfo.getStringExtra(TAG_LECTURE);
        tutorialUrl = moduleInfo.getStringExtra(TAG_TUTORIAL);

        // change the page title to that of the module number selected
        TextView title = (TextView) findViewById(R.id.module_number);
        title.setText(moduleID);

        // logcat staement to follow app process on console
        Log.d("choose qr ", "qr address; " + lectureUrl + "," + tutorialUrl);

        // load the default QR-Code data into the webview (lecture)
        webview = (WebView)this.findViewById(R.id.webView);

        // recieves the current settings for webview object, used to fill the available space as much as possible
        WebSettings settings = webview.getSettings();
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);

        // load the url, formatted using html code
        webview.loadData("<html><head><style type='text/css'>body{margin:auto auto;text-align:center;}" +
                "img{width:90%;} </style></head><body><img src='" + lectureUrl + "'/></body></html>" ,"text/html",  "UTF-8");

        // set textview default contents to lecture
        qrID = (TextView)findViewById(R.id.qr_id);
        qrID.setText("lecture");

        // button objects assigned to xml components
        btnLectureQR = (Button) findViewById(R.id.lec_lecqr);       // to retrieve QR code for lecture
        btnTutorialQR = (Button) findViewById(R.id.lec_tutqr);      // to retrieve QR code for tutorial
        btnSaveDetails= (Button) findViewById(R.id.lec_saveDetails);// to allow user to save current details

        // show lecture as active
        btnLectureQR.setBackgroundResource(R.drawable.lecture_enabled);

        // set on click listeners for both buttons
        btnLectureQR.setOnClickListener(this);
        btnTutorialQR.setOnClickListener(this);
        btnSaveDetails.setOnClickListener(this);

    } // onCreate

    /**
     * onCLick method updated further after usability testing to change the appearance of the
     * buttons on screen depending on which one has been selected by the user; in order to
     * make it easier to determine which QR-Code is being shown.
     **/


    @Override
    public void onClick (View view)
    {
        if(view.getId()==R.id.lec_tutqr)
        {
            ChooseQR.this.webview.loadData("<html><head><style type='text/css'>body{margin:auto auto;text-align:center;} img{width:90%;} </style></head><body><img src='" + tutorialUrl + "'/></body></html>" ,"text/html",  "UTF-8");
            // set textview default contents to lecture
            qrID.setText("tutorial");
            btnTutorialQR.setBackgroundResource(R.drawable.tutorial_enabled);
            btnLectureQR.setBackgroundResource(R.drawable.lecture);


        } else if (view.getId()==R.id.lec_lecqr){

            ChooseQR.this.webview.loadData("<html><head><style type='text/css'>body{margin:auto auto;text-align:center;} img{width:90%;} </style></head><body><img src='" + lectureUrl + "'/></body></html>" ,"text/html",  "UTF-8");
            // set textview default contents to lecture
            qrID.setText("lecture");
            btnLectureQR.setBackgroundResource(R.drawable.lecture_enabled);
            btnTutorialQR.setBackgroundResource(R.drawable.tutorial);


        } else {

            moduleCode = moduleID;                  // stores selected module id in protected variable
            classType = qrID.getText().toString();  // stores selected class type in protected variable

            Toast confirmation = Toast.makeText(getApplicationContext(),
                    "module: " + moduleCode + ", class: " + classType, Toast.LENGTH_LONG);
            confirmation.show();

        }// if else for button click

    }// onClick

} // ChooseQR
