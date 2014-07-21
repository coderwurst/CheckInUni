package com.coderwurst.student_attendance;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

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

    private String pid, lectureUrl, tutorialUrl;


    private static final String TAG_ID = "id";

    // private static final String TAG_NAME = "name";
    private static final String TAG_LECTURE = "lectureUrl";
    private static final String TAG_TUTORIAL = "tutorialUrl";

    private Button btnLectureQR;
    private Button btnTutorialQR;

    private TextView qrID;

    private WebView webview = null;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_qr);

        // getting product details from intent
        Intent i = getIntent();

        // getting product id (pid) from intent
        pid = i.getStringExtra(TAG_ID);
        lectureUrl = i.getStringExtra(TAG_LECTURE);
        tutorialUrl = i.getStringExtra(TAG_TUTORIAL);

        // change the page title to that of the module number selected
        TextView title = (TextView) findViewById(R.id.module_number);
        title.setText(pid);

        Log.d("choose qr ", "qr address; " + lectureUrl + "," + tutorialUrl);

        // load the default QR-Code data into the webview
        webview = (WebView)this.findViewById(R.id.webView);

        WebSettings settings = webview.getSettings();
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        // load the url, formatted using html
        webview.loadData("<html><head><style type='text/css'>body{margin:auto auto;text-align:center;} img{width:100%;} </style></head><body><img src='" + lectureUrl + "'/></body></html>" ,"text/html",  "UTF-8");

        // set textview default contents to lecture
        qrID = (TextView)findViewById(R.id.qr_id);
        qrID.setText("lecture");

        // Buttons
        btnLectureQR = (Button) findViewById(R.id.lec_lecqr);       // to retrieve QR code for lecture
        btnTutorialQR = (Button) findViewById(R.id.lec_tutqr);      // to retrieve QR code for tutorial

        btnLectureQR.setOnClickListener(this);
        btnTutorialQR.setOnClickListener(this);

    } // onCreate

    @Override
    public void onClick (View view)
    {
        if(view.getId()==R.id.lec_tutqr)
        {
            ChooseQR.this.webview.loadData("<html><head><style type='text/css'>body{margin:auto auto;text-align:center;} img{width:100%;} </style></head><body><img src='" + tutorialUrl + "'/></body></html>" ,"text/html",  "UTF-8");
            // set textview default contents to lecture
            qrID.setText("tutorial");

        } else {

            ChooseQR.this.webview.loadData("<html><head><style type='text/css'>body{margin:auto auto;text-align:center;} img{width:100%;} </style></head><body><img src='" + lectureUrl + "'/></body></html>" ,"text/html",  "UTF-8");
            // set textview default contents to lecture
            qrID.setText("lecture");

        } // if else for button click

    }// onClick

} // ChooseQR
